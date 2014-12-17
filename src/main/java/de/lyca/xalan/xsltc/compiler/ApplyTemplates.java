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

import static com.sun.codemodel.JExpr.direct;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ApplyTemplates extends Instruction {
  private Expression _select;
  private Type _type = null;
  private QName _modeName;
  private String _functionName;

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("ApplyTemplates");
    indent(indent + IndentIncrement);
    Util.println("select " + _select.toString());
    if (_modeName != null) {
      indent(indent + IndentIncrement);
      Util.println("mode " + _modeName);
    }
  }

  public boolean hasWithParams() {
    return hasContents();
  }

  @Override
  public void parseContents(Parser parser) {
    final String select = getAttribute("select");
    final String mode = getAttribute("mode");

    if (select.length() > 0) {
      _select = parser.parseExpression(this, "select", null);

    }

    if (mode.length() > 0) {
      if (!XML11Char.isXML11ValidQName(mode)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, mode, this);
        parser.reportError(Constants.ERROR, err);
      }
      _modeName = parser.getQNameIgnoreDefaultNs(mode);
    }

    // instantiate Mode if needed, cache (apply temp) function name
    _functionName = parser.getTopLevelStylesheet().getMode(_modeName).functionName();
    parseChildren(parser);// with-params
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_select != null) {
      _type = _select.typeCheck(stable);
      if (_type instanceof NodeType || _type instanceof ReferenceType) {
        _select = new CastExpr(_select, Type.NodeSet);
        _type = Type.NodeSet;
      }
      if (_type instanceof NodeSetType || _type instanceof ResultTreeType) {
        typeCheckContents(stable); // with-params
        return Type.Void;
      }
      throw new TypeCheckError(this);
    } else {
      typeCheckContents(stable); // with-params
      return Type.Void;
    }
  }

  /**
   * Translate call-template. A parameter frame is pushed only if some template
   * in the stylesheet uses parameters.
   */
  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
    boolean setStartNodeCalled = false;
    final Stylesheet stylesheet = getStylesheet();
    JVar[] params = method.listParams();

//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final int current = methodGen.getLocalIndex("current");

    // check if sorting nodes is required
    final List<Sort> sortObjects = new ArrayList<>();
    for (SyntaxTreeNode child : getContents()) {
      if (child instanceof Sort) {
        sortObjects.add((Sort) child);
      }
    }

    // Push a new parameter frame
    if (stylesheet.hasLocalParams() || hasContents()) {
      body.invoke(PUSH_PARAM_FRAME);
      // translate with-params
      translateContents(definedClass, method, body);
    }
//    il.append(classGen.loadTranslet());

    JInvocation select = null;
    // The 'select' expression is a result-tree
    if (_type != null && _type instanceof ResultTreeType) {
      // <xsl:sort> cannot be applied to a result tree - issue warning
      if (sortObjects.size() > 0) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.RESULT_TREE_SORT_ERR, this);
        getParser().reportError(WARNING, err);
      }
      // Put the result tree (a DOM adapter) on the stack
      _select.translate(definedClass, method, body);
      // Get back the DOM and iterator (not just iterator!!!)
      _type.translateTo(definedClass, method, Type.NodeSet);
    } else {
//      il.append(methodGen.loadDOM());

      // compute node iterator for applyTemplates
      if (sortObjects.size() > 0) {
        Sort.translateSortIterator(definedClass, method, _select, sortObjects);
        body.invoke(params[0] ,SET_START_NODE).arg(direct("current"));
//        final int setStartNode = cpg.addInterfaceMethodref(NODE_ITERATOR, SET_START_NODE, "(I)" + NODE_ITERATOR_SIG);
//        il.append(methodGen.loadCurrentNode());
//        il.append(new INVOKEINTERFACE(setStartNode, 2));
        setStartNodeCalled = true;
      } else {
        if (_select == null) {
//          Mode.compileGetChildren(definedClass, method, current);
        } else {
          select = (JInvocation) _select.compile(definedClass, method);
        }
      }
    }

    if (_select != null && !setStartNodeCalled) {
      select = select.invoke("setStartNode").arg(params[3]);
//      _select.startIterator(definedClass, method);
    }

    // !!! need to instantiate all needed modes
    final String className = getStylesheet().getClassName();
    body.invoke(APPLY_TEMPLATES).arg(params[0]).arg(select == null ? params[1] : select).arg(params[2]);
    
//    il.append(methodGen.loadHandler());
//    final String applyTemplatesSig = classGen.getApplyTemplatesSig();
//    final int applyTemplates = cpg.addMethodref(className, _functionName, applyTemplatesSig);
//    il.append(new INVOKEVIRTUAL(applyTemplates));

    // Pop parameter frame
    if (stylesheet.hasLocalParams() || hasContents()) {
      body.invoke(POP_PARAM_FRAME);
    }
  }
}
