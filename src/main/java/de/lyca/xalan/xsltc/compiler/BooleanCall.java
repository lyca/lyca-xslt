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

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class BooleanCall extends FunctionCall {

  private Expression _arg = null;

  public BooleanCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
    _arg = argument(0);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _arg.typeCheck(stable);
    return _type = Type.Boolean;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression expr = _arg.toJExpression(ctx);
    final Type targ = _arg.getType();
    if (!targ.identicalTo(Type.Boolean)) {
      expr = _arg.startIterator(ctx, expr);
      expr = targ.compileTo(ctx, expr, Type.Boolean);
    }
    return expr;
  }

}
