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
import static com.sun.codemodel.JExpr.lit;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
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
    _value = argument(0);
    _format = argument(1);
    _name = argumentCount() == 3 ? argument(2) : null;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {

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
  public JExpression compile(CompilerContext ctx) {
    JExpression value = _value.compile(ctx);
    JExpression format = _format.compile(ctx);
    JExpression name;
    if (_name == null) {
      name = lit(EMPTYSTRING);
    } else if (_resolvedQName != null) {
      name = lit(_resolvedQName.toString());
    } else {
      name = _name.compile(ctx);
    }
    return ctx.ref(BasisLibrary.class).staticInvoke("formatNumber").arg(value).arg(format)
        .arg(invoke("getDecimalFormat").arg(name));
  }

  @Override
  public void translate(CompilerContext ctx) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    _value.translate(classGen, methodGen);
//    _format.translate(classGen, methodGen);
//
//    final int fn3arg = cpg.addMethodref(BASIS_LIBRARY_CLASS, "formatNumber", "(DLjava/lang/String;"
//            + "Ljava/text/DecimalFormat;)" + "Ljava/lang/String;");
//    final int get = cpg.addMethodref(TRANSLET_CLASS, "getDecimalFormat", "(Ljava/lang/String;)"
//            + "Ljava/text/DecimalFormat;");
//
//    il.append(classGen.loadTranslet());
//    if (_name == null) {
//      il.append(new PUSH(cpg, EMPTYSTRING));
//    } else if (_resolvedQName != null) {
//      il.append(new PUSH(cpg, _resolvedQName.toString()));
//    } else {
//      _name.translate(classGen, methodGen);
//    }
//    il.append(new INVOKEVIRTUAL(get));
//    il.append(new INVOKESTATIC(fn3arg));
  }
}
