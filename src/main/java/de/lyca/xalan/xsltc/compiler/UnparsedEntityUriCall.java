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

import static de.lyca.xalan.xsltc.DOM.GET_UNPARSED_ENTITY_URI;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class UnparsedEntityUriCall extends FunctionCall {
  private Expression _entity;

  public UnparsedEntityUriCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
    _entity = argument();
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (argumentCount() != 1) {
      // TODO better error reporting
      final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(err);
    }

    final Type entity = _entity.typeCheck(stable);
    if (entity instanceof StringType == false) {
      _entity = new CastExpr(_entity, Type.String);
    }
    return _type = Type.String;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    return ctx.currentDom().invoke(GET_UNPARSED_ENTITY_URI).arg(_entity.toJExpression(ctx));
  }

}
