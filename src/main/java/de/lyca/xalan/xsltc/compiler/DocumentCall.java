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

import static com.sun.codemodel.JExpr._this;
import static de.lyca.xalan.xsltc.compiler.Constants.DOM_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_PNAME;

import java.util.List;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.LoadDocument;

/**
 * @author Jacek Ambroziak
 * @author Morten Jorgensen
 */
final class DocumentCall extends FunctionCall {

  private Expression _arg1 = null;
  private Expression _arg2 = null;
  private Type _arg1Type;

  /**
   * Default function call constructor
   */
  public DocumentCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  /**
   * Type checks the arguments passed to the document() function. The first
   * argument can be any type (we must cast it to a string) and contains the URI
   * of the document
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // At least one argument - two at most
    final int ac = argumentCount();
    if (ac < 1 || ac > 2) {
      final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(msg);
    }
    if (getStylesheet() == null) {
      final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(msg);
    }

    // Parse the first argument
    _arg1 = argument(0);

    if (_arg1 == null) {// should not happened
      final ErrorMsg msg = new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, this);
      throw new TypeCheckError(msg);
    }

    _arg1Type = _arg1.typeCheck(stable);
    if (_arg1Type != Type.NodeSet && _arg1Type != Type.String) {
      _arg1 = new CastExpr(_arg1, Type.String);
    }

    // Parse the second argument
    if (ac == 2) {
      _arg2 = argument(1);

      if (_arg2 == null) {// should not happened
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, this);
        throw new TypeCheckError(msg);
      }

      final Type arg2Type = _arg2.typeCheck(stable);

      if (arg2Type.identicalTo(Type.Node)) {
        _arg2 = new CastExpr(_arg2, Type.NodeSet);
      } else if (arg2Type.identicalTo(Type.NodeSet)) {
        // falls through
      } else {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, this);
        throw new TypeCheckError(msg);
      }
    }

    return _type = Type.NodeSet;
  }

  /**
   * Translates the document() function call to a call to LoadDocument()'s
   * static method document().
   */
  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    final int ac = argumentCount();

    JExpression translet;
    JExpression document;
    if (ctx.isInnerClass()) {
      translet = ctx.param(TRANSLET_PNAME);
      document = ((JExpression) JExpr.cast(ctx.clazz().outer(), translet)).ref(DOM_FIELD);
    } else {
      translet = _this();
      document = ctx.field(DOM_FIELD);
    }

    // The URI can be either a node-set or something else cast to a string
    JExpression arg1 = _arg1.toJExpression(ctx);
    if (_arg1Type == Type.NodeSet) {
      arg1 = _arg1.startIterator(ctx, arg1);
    }

    if (ac == 2) {
      // _arg2 == null was tested in typeCheck()
      JExpression arg2 = _arg2.startIterator(ctx, _arg2.toJExpression(ctx));
      return ctx.ref(LoadDocument.class).staticInvoke("documentF").arg(arg1).arg(arg2)
          .arg(getStylesheet().getSystemId()).arg(translet).arg(document);
    }

    return ctx.ref(LoadDocument.class).staticInvoke("documentF").arg(arg1).arg(getStylesheet().getSystemId())
        .arg(translet).arg(document);
  }

}
