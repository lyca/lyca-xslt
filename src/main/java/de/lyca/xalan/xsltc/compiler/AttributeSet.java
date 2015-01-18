/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

package de.lyca.xalan.xsltc.compiler;

import static com.sun.codemodel.JExpr.invoke;

import java.util.List;
import java.util.ListIterator;

import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class AttributeSet extends TopLevelElement {

  // This prefix is used for the method name of attribute set methods
  private static final String AttributeSetPrefix = "$as$";

  // Element contents
  private QName _name;
  private UseAttributeSets _useSets;
  private AttributeSet _mergeSet;
  private String _method;
  private boolean _ignore = false;

  /**
   * Returns the QName of this attribute set
   */
  public QName getName() {
    return _name;
  }

  /**
   * Returns the method name of this attribute set. This method name is
   * generated by the compiler (XSLTC)
   */
  public String getMethodName() {
    return _method;
  }

  /**
   * Call this method to prevent a method for being compiled for this set. This
   * is used in case several <xsl:attribute-set...> elements constitute a single
   * set (with one name). The last element will merge itself with any previous
   * set(s) with the same name and disable the other set(s).
   */
  public void ignore() {
    _ignore = true;
  }

  /**
   * Parse the contents of this attribute set. Recognised attributes are "name"
   * (required) and "use-attribute-sets" (optional).
   */
  @Override
  public void parseContents(Parser parser) {

    // Get this attribute set's name
    final String name = getAttribute("name");

    if (!XML11Char.isXML11ValidQName(name)) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name, this);
      parser.reportError(Constants.ERROR, err);
    }
    _name = parser.getQNameIgnoreDefaultNs(name);
    if (_name == null || _name.equals(EMPTYSTRING)) {
      final ErrorMsg msg = new ErrorMsg(ErrorMsg.UNNAMED_ATTRIBSET_ERR, this);
      parser.reportError(Constants.ERROR, msg);
    }

    // Get any included attribute sets (similar to inheritance...)
    final String useSets = getAttribute("use-attribute-sets");
    if (useSets.length() > 0) {
      if (!Util.isValidQNames(useSets)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, useSets, this);
        parser.reportError(Constants.ERROR, err);
      }
      _useSets = new UseAttributeSets(useSets, parser);
    }

    // Parse the contents of this node. All child elements must be
    // <xsl:attribute> elements. Other elements cause an error.
    final List<SyntaxTreeNode> contents = getContents();
    for (final SyntaxTreeNode child : contents) {
      if (child instanceof XslAttribute) {
        parser.getSymbolTable().setCurrentNode(child);
        child.parseContents(parser);
      } else if (child instanceof Text) {
        // ignore
      } else {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_CHILD_ERR, this);
        parser.reportError(Constants.ERROR, msg);
      }
    }

    // Point the symbol table back at us...
    parser.getSymbolTable().setCurrentNode(this);
  }

  /**
   * Type check the contents of this element
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {

    if (_ignore)
      return Type.Void;

    // _mergeSet Point to any previous definition of this attribute set
    _mergeSet = stable.addAttributeSet(this);

    _method = AttributeSetPrefix + getXSLTC().nextAttributeSetSerial();

    if (_useSets != null) {
      _useSets.typeCheck(stable);
    }
    typeCheckContents(stable);
    return Type.Void;
  }

  @Override
  public JStatement compile(CompilerContext ctx) {
    return invoke(_method).arg(ctx.currentDom()).arg(ctx.param(ITERATOR_PNAME)).arg(ctx.currentHandler());
  }
  
  /**
   * Compile a method that outputs the attributes in this set
   */
  @Override
  public void translate(CompilerContext ctx) {
// FIXME

    if (_ignore)
      return;

    JMethod method = ctx.method(JMod.PRIVATE, void.class, _method)._throws(TransletException.class);
    ctx.param(DOM.class, DOCUMENT_PNAME);
    ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar handler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);
    ctx.pushBlock(method.body());
    ctx.pushHandler(handler);
    // Create a new method generator for an attribute set method
//    methodGen = new AttributeSetMethodGenerator(_method, classGen);

    // Generate a reference to previous attribute-set definitions with the
    // same name first. Those later in the stylesheet take precedence.
    if (_mergeSet != null) {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final InstructionList il = methodGen.getInstructionList();
      final String methodName = _mergeSet.getMethodName();

      ctx.currentBlock().add(_mergeSet.compile(ctx));
//      il.append(classGen.loadTranslet());
//      il.append(methodGen.loadDOM());
//      il.append(methodGen.loadIterator());
//      il.append(methodGen.loadHandler());
//      final int method = cpg.addMethodref(classGen.getClassName(), methodName, ATTR_SET_SIG);
//      il.append(new INVOKESPECIAL(method));
    }

    // Translate other used attribute sets first, as local attributes
    // take precedence (last attributes overrides first)
    if (_useSets != null) {
      _useSets.translate(ctx);
    }

    // Translate all local attributes
    for (SyntaxTreeNode element : getContents()) {
      if (element instanceof XslAttribute) {
        element.translate(ctx);
      }
    }
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(RETURN);

//    classGen.addMethod(methodGen);
    ctx.popHandler();
    ctx.popBlock();
    ctx.popMethodContext();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("attribute-set: ");
    // Translate all local attributes
    final ListIterator<SyntaxTreeNode> attributes = elements();
    while (attributes.hasNext()) {
      final XslAttribute attribute = (XslAttribute) attributes.next();
      sb.append(attribute);
    }
    return sb.toString();
  }
}
