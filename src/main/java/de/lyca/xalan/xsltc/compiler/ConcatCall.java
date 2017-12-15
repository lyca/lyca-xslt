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

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

import java.util.List;
import java.util.ListIterator;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ConcatCall extends FunctionCall {
  public ConcatCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    ListIterator<Expression> expressions = getArguments().listIterator();
    while (expressions.hasNext()) {
      Expression exp = expressions.next();
      if (!exp.typeCheck(stable).identicalTo(Type.String)) {
        expressions.set(new CastExpr(exp, Type.String));
      }
    }
    return _type = Type.String;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    final int nArgs = argumentCount();

    switch (nArgs) {
    case 0:
      return lit("");
    case 1:
      return argument().toJExpression(ctx);
    default:
      JExpression stringBuilder = _new(ctx.ref(StringBuilder.class));
      for (int i = 0; i < nArgs; i++) {
        stringBuilder = stringBuilder.invoke("append").arg(argument(i).toJExpression(ctx));
      }
      return stringBuilder.invoke("toString");
    }
  }

}
