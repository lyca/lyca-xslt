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

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.PUSH;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class XslElement extends Instruction {

  private String _prefix;
  private boolean _ignore = false;
  private boolean _isLiteralName = true;
  private AttributeValueTemplate _name;
  private AttributeValueTemplate _namespace;

  /**
   * Displays the contents of the element
   */
  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("Element " + _name);
    displayContents(indent + IndentIncrement);
  }

  /**
   * This method is now deprecated. The new implemation of this class never
   * declares the default NS.
   */
  public boolean declaresDefaultNS() {
    return false;
  }

  @Override
  public void parseContents(Parser parser) {
    final SymbolTable stable = parser.getSymbolTable();

    // Handle the 'name' attribute
    String name = getAttribute("name");
    if (name == EMPTYSTRING) {
      final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_ELEM_NAME_ERR, name, this);
      parser.reportError(WARNING, msg);
      parseChildren(parser);
      _ignore = true; // Ignore the element if the QName is invalid
      return;
    }

    // Get namespace attribute
    String namespace = getAttribute("namespace");

    // Optimize compilation when name is known at compile time
    _isLiteralName = Util.isLiteral(name);
    if (_isLiteralName) {
      if (!XML11Char.isXML11ValidQName(name)) {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_ELEM_NAME_ERR, name, this);
        parser.reportError(WARNING, msg);
        parseChildren(parser);
        _ignore = true; // Ignore the element if the QName is invalid
        return;
      }

      final QName qname = parser.getQNameSafe(name);
      String prefix = qname.getPrefix();
      final String local = qname.getLocalPart();

      if (prefix == null) {
        prefix = EMPTYSTRING;
      }

      if (!hasAttribute("namespace")) {
        namespace = lookupNamespace(prefix);
        if (namespace == null) {
          final ErrorMsg err = new ErrorMsg(ErrorMsg.NAMESPACE_UNDEF_ERR, prefix, this);
          parser.reportError(WARNING, err);
          parseChildren(parser);
          _ignore = true; // Ignore the element if prefix is undeclared
          return;
        }
        _prefix = prefix;
        _namespace = new AttributeValueTemplate(namespace, parser, this);
      } else {
        if (prefix == EMPTYSTRING) {
          if (Util.isLiteral(namespace)) {
            prefix = lookupPrefix(namespace);
            if (prefix == null) {
              prefix = stable.generateNamespacePrefix();
            }
          }

          // Prepend prefix to local name
          final StringBuilder newName = new StringBuilder(prefix);
          if (prefix != EMPTYSTRING) {
            newName.append(':');
          }
          name = newName.append(local).toString();
        }
        _prefix = prefix;
        _namespace = new AttributeValueTemplate(namespace, parser, this);
      }
    } else {
      // name attribute contains variable parts. If there is no namespace
      // attribute, the generated code needs to be prepared to look up
      // any prefix in the stylesheet at run-time.
      _namespace = namespace == EMPTYSTRING ? null : new AttributeValueTemplate(namespace, parser, this);
    }

    _name = new AttributeValueTemplate(name, parser, this);

    final String useSets = getAttribute("use-attribute-sets");
    if (useSets.length() > 0) {
      if (!Util.isValidQNames(useSets)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, useSets, this);
        parser.reportError(Constants.ERROR, err);
      }
      setFirstElement(new UseAttributeSets(useSets, parser));
    }

    parseChildren(parser);
  }

  /**
   * Run type check on element name & contents
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (!_ignore) {
      _name.typeCheck(stable);
      if (_namespace != null) {
        _namespace.typeCheck(stable);
      }
    }
    typeCheckContents(stable);
    return Type.Void;
  }

  /**
   * This method is called when the name of the element is known at compile
   * time. In this case, there is no need to inspect the element name at runtime
   * to determine if a prefix exists, needs to be generated, etc.
   */
  public void translateLiteral(ClassGenerator classGen, MethodGenerator methodGen) {
    final ConstantPoolGen cpg = classGen.getConstantPool();
    final InstructionList il = methodGen.getInstructionList();

    if (!_ignore) {
      il.append(methodGen.loadHandler());
      _name.translate(classGen, methodGen);
      il.append(DUP2);
      il.append(methodGen.startElement());

      if (_namespace != null) {
        il.append(methodGen.loadHandler());
        il.append(new PUSH(cpg, _prefix));
        _namespace.translate(classGen, methodGen);
        il.append(methodGen.namespace());
      }
    }

    translateContents(classGen, methodGen);

    if (!_ignore) {
      il.append(methodGen.endElement());
    }
  }

  /**
   * At runtime the compilation of xsl:element results in code that: (i)
   * evaluates the avt for the name, (ii) checks for a prefix in the name (iii)
   * generates a new prefix and create a new qname when necessary (iv) calls
   * startElement() on the handler (v) looks up a uri in the XML when the prefix
   * is not known at compile time (vi) calls namespace() on the handler (vii)
   * evaluates the contents (viii) calls endElement().
   */
  @Override
  public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
    final ConstantPoolGen cpg = classGen.getConstantPool();
    final InstructionList il = methodGen.getInstructionList();

    // Optimize translation if element name is a literal
    if (_isLiteralName) {
      translateLiteral(classGen, methodGen);
      return;
    }

    if (!_ignore) {

      // if the qname is an AVT, then the qname has to be checked at runtime if
      // it is a valid qname
      final LocalVariableGen nameValue = methodGen.addLocalVariable2("nameValue", Util.getJCRefType(STRING_SIG), null);

      // store the name into a variable first so _name.translate only needs to
      // be called once
      _name.translate(classGen, methodGen);
      nameValue.setStart(il.append(new ASTORE(nameValue.getIndex())));
      il.append(new ALOAD(nameValue.getIndex()));

      // call checkQName if the name is an AVT
      final int check = cpg.addMethodref(BASIS_LIBRARY_CLASS, "checkQName", "(" + STRING_SIG + ")V");
      il.append(new INVOKESTATIC(check));

      // Push handler for call to endElement()
      il.append(methodGen.loadHandler());

      // load name value again
      nameValue.setEnd(il.append(new ALOAD(nameValue.getIndex())));

      if (_namespace != null) {
        _namespace.translate(classGen, methodGen);
      } else {
        // If name is an AVT and namespace is not specified, need to
        // look up any prefix in the stylesheet by calling
        // BasisLibrary.lookupStylesheetQNameNamespace(
        // name, stylesheetNode, ancestorsArray,
        // prefixURIsIndexArray, prefixURIPairsArray,
        // !ignoreDefaultNamespace)
        final String transletClassName = getXSLTC().getClassName();
        il.append(DUP);
        il.append(new PUSH(cpg, getNodeIDForStylesheetNSLookup()));
        il.append(new GETSTATIC(cpg.addFieldref(transletClassName, STATIC_NS_ANCESTORS_ARRAY_FIELD,
                NS_ANCESTORS_INDEX_SIG)));
        il.append(new GETSTATIC(cpg.addFieldref(transletClassName, STATIC_PREFIX_URIS_IDX_ARRAY_FIELD,
                PREFIX_URIS_IDX_SIG)));
        il.append(new GETSTATIC(cpg.addFieldref(transletClassName, STATIC_PREFIX_URIS_ARRAY_FIELD,
                PREFIX_URIS_ARRAY_SIG)));
        // Default namespace is significant
        il.append(ICONST_0);
        il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, LOOKUP_STYLESHEET_QNAME_NS_REF,
                LOOKUP_STYLESHEET_QNAME_NS_SIG)));
      }

      // Push additional arguments
      il.append(methodGen.loadHandler());
      il.append(methodGen.loadDOM());
      il.append(methodGen.loadCurrentNode());

      // Invoke BasisLibrary.startXslElemCheckQName()
      il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, "startXslElement", "(" + STRING_SIG + STRING_SIG
              + TRANSLET_OUTPUT_SIG + DOM_INTF_SIG + "I)" + STRING_SIG)));

    }

    translateContents(classGen, methodGen);

    if (!_ignore) {
      il.append(methodGen.endElement());
    }
  }

  /**
   * Override this method to make sure that xsl:attributes are not copied to
   * output if this xsl:element is to be ignored
   */
  @Override
  public void translateContents(ClassGenerator classGen, MethodGenerator methodGen) {
    for (final SyntaxTreeNode item : getContents()) {
      if (_ignore && item instanceof XslAttribute) {
        continue;
      }
      item.translate(classGen, methodGen);
    }
  }

}