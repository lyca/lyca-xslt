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

import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.RealType;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class FormatNumberCall extends FunctionCall {
  private Expression _value;
  private Expression _format;
  private Expression _name;
  private QName _resolvedQName = null;

  public FormatNumberCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
    if (argumentCount() < 2)
      return;
    _value = argument(0);
    _format = argument(1);
    _name = argumentCount() == 3 ? argument(2) : null;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (argumentCount() < 2) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(err);
    }

    // Inform stylesheet to instantiate a DecimalFormat object
    getStylesheet().numberFormattingUsed();

    final Type tvalue = _value.typeCheck(stable);
    if (tvalue instanceof RealType == false) {
      _value = new CastExpr(_value, Type.Real);
    }
    final Type tformat = _format.typeCheck(stable);
    if (tformat instanceof StringType == false) {
      _format = new CastExpr(_format, Type.String);
    }
    if (argumentCount() == 3) {
      final Type tname = _name.typeCheck(stable);

      if (_name instanceof LiteralExpr) {
        final LiteralExpr literal = (LiteralExpr) _name;
        _resolvedQName = getParser().getQNameIgnoreDefaultNs(literal.getValue());
      } else if (tname instanceof StringType == false) {
        _name = new CastExpr(_name, Type.String);
      }
    }
    return _type = Type.String;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression value = _value.toJExpression(ctx);
    JExpression format = _format.toJExpression(ctx);
    JExpression name;
    if (_name == null) {
      name = lit("");
    } else if (_resolvedQName != null) {
      name = lit(_resolvedQName.toString());
    } else {
      name = _name.toJExpression(ctx);
    }
    return ctx.ref(BasisLibrary.class).staticInvoke("formatNumber").arg(value).arg(format)
        .arg(invoke("getDecimalFormat").arg(name));
  }

}
