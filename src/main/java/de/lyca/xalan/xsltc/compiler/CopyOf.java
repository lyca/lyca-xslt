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
package de.lyca.xalan.xsltc.compiler;

import static de.lyca.xalan.xsltc.DOM.COPY;
import static de.lyca.xalan.xsltc.DOM.GET_DOCUMENT;
import static de.lyca.xalan.xsltc.compiler.Constants.CHARACTERSW;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class CopyOf extends Instruction {
  private Expression _select;

  @Override
  public void parseContents(Parser parser) {
    _select = parser.parseExpression(this, "select", null);
    // make sure required attribute(s) have been set
    if (_select.isDummy()) {
      reportError(this, parser, Messages.get().requiredAttrErr("select"));
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
    final Type type = _select.getType();
    JExpression select = _select.toJExpression(ctx);
    if (type instanceof NodeSetType) {
      JInvocation copy = ctx.currentBlock().invoke(ctx.currentDom(), COPY);
      JInvocation iterator = select.invoke(SET_START_NODE).arg(ctx.currentNode());
      copy.arg(iterator).arg(ctx.currentHandler());
    } else if (type instanceof NodeType) {
      JInvocation copy = ctx.currentBlock().invoke(ctx.currentDom(), COPY);
      JExpression dom = select;
      copy.arg(dom).arg(ctx.currentHandler());
    } else if (type instanceof ResultTreeType) {
      JExpression dom = select;
      // We want the whole tree, so we start with the root node
      JInvocation document = dom.invoke(GET_DOCUMENT);
      JInvocation copy = ctx.currentBlock().invoke(dom, COPY);
      copy.arg(document).arg(ctx.currentHandler());
    } else if (type instanceof ReferenceType) {
      ctx.currentBlock().add(ctx.ref(BasisLibrary.class).staticInvoke(COPY).arg(select).arg(ctx.currentHandler())
          .arg(ctx.currentNode()).arg(ctx.currentDom()));
    } else {
      ctx.currentBlock().invoke(CHARACTERSW).arg(select).arg(ctx.currentHandler());
    }
  }

}
