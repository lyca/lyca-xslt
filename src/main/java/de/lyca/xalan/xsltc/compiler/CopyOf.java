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

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class CopyOf extends Instruction {
  private Expression _select;

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("CopyOf");
    indent(indent + IndentIncrement);
    Util.println("select " + _select.toString());
  }

  @Override
  public void parseContents(Parser parser) {
    _select = parser.parseExpression(this, "select", null);
    // make sure required attribute(s) have been set
    if (_select.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "select");
      return;
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type tselect = _select.typeCheck(stable);
    if (tselect instanceof NodeType || tselect instanceof NodeSetType || tselect instanceof ReferenceType
            || tselect instanceof ResultTreeType) {
      // falls through
    } else {
      _select = new CastExpr(_select, Type.String);
    }
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    final Type tselect = _select.getType();
//    final String CPY1_SIG = "(" + NODE_ITERATOR_SIG + TRANSLET_OUTPUT_SIG + ")V";
//    final int cpy1 = cpg.addInterfaceMethodref(DOM_INTF, "copy", CPY1_SIG);
//
//    final String CPY2_SIG = "(" + NODE_SIG + TRANSLET_OUTPUT_SIG + ")V";
//    final int cpy2 = cpg.addInterfaceMethodref(DOM_INTF, "copy", CPY2_SIG);
//
//    final String getDoc_SIG = "()" + NODE_SIG;
//    final int getDoc = cpg.addInterfaceMethodref(DOM_INTF, "getDocument", getDoc_SIG);

    if (tselect instanceof NodeSetType) {
      JInvocation copy = ctx.currentBlock().invoke(ctx.currentDom(), "copy");
      JInvocation iterator = _select.compile(ctx).invoke("setStartNode").arg(ctx.currentNode());
      copy.arg(iterator).arg(ctx.param(TRANSLET_OUTPUT_PNAME));
      
//      il.append(method.loadDOM());
      
      // push NodeIterator
//      _select.translate(definedClass, method);
//      _select.startIterator(definedClass, method);

      // call copy from the DOM 'library'
//      il.append(method.loadHandler());
//      il.append(new INVOKEINTERFACE(cpy1, 3));
    } else if (tselect instanceof NodeType) {
      JInvocation copy = ctx.currentBlock().invoke(ctx.currentDom(), "copy");
      JExpression dom = _select.compile(ctx);
      copy.arg(dom).arg(ctx.param(TRANSLET_OUTPUT_PNAME));

//      il.append(method.loadDOM());
//      _select.translate(ctx);
//      il.append(method.loadHandler());
//      il.append(new INVOKEINTERFACE(cpy2, 3));
    } else if (tselect instanceof ResultTreeType) {
      JExpression dom = _select.compile(ctx);
      JInvocation document = dom.invoke("getDocument");
      JInvocation copy = ctx.currentBlock().invoke(dom, "copy");
      copy.arg(document).arg(ctx.param(TRANSLET_OUTPUT_PNAME));

//      _select.translate(ctx);
      // We want the whole tree, so we start with the root node
//      il.append(DUP); // need a pointer to the DOM ;
//      il.append(new INVOKEINTERFACE(getDoc, 1)); // ICONST_0);
//      il.append(method.loadHandler());
//      il.append(new INVOKEINTERFACE(cpy2, 3));
    } else if (tselect instanceof ReferenceType) {
      _select.translate(ctx);
//      il.append(method.loadHandler());
//      il.append(method.loadCurrentNode());
//      il.append(method.loadDOM());
//      final int copy = cpg.addMethodref(BASIS_LIBRARY_CLASS, "copy", "(" + OBJECT_SIG + TRANSLET_OUTPUT_SIG + NODE_SIG
//              + DOM_INTF_SIG + ")V");
//      il.append(new INVOKESTATIC(copy));
    } else {
      ctx.currentBlock().invoke(CHARACTERSW).arg(_select.compile(ctx)).arg(ctx.param(TRANSLET_OUTPUT_PNAME));
//      il.append(definedClass.loadTranslet());
//      _select.translate(ctx);
//      il.append(method.loadHandler());
//      il.append(new INVOKEVIRTUAL(cpg.addMethodref(TRANSLET_CLASS, CHARACTERSW, CHARACTERSW_SIG)));
    }

  }
}
