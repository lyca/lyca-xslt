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

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Morten Jorgensen
 */
final class LangCall extends FunctionCall {
  private Expression _lang;
  private Type _langType;

  /**
   * Get the parameters passed to function: lang(string)
   */
  public LangCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
    _lang = argument(0);
  }

  /**
     *
     */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _langType = _lang.typeCheck(stable);
    if (!(_langType instanceof StringType)) {
      _lang = new CastExpr(_lang, Type.String);
    }
    return Type.Boolean;
  }

  /**
     *
     */
  @Override
  public Type getType() {
    return Type.Boolean;
  }

  /**
   * This method is called when the constructor is compiled in
   * Stylesheet.compileConstructor() and not as the syntax tree is traversed.
   */
  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression lang = _lang.toJExpression(ctx);
    // TODO
    // if (classGen instanceof FilterGenerator) {
    // il.append(new ILOAD(1));
    // } else {
    // il.append(methodGen.loadContextNode());
    // }
    return ctx.ref(BasisLibrary.class).staticInvoke("testLanguage").arg(lang).arg(ctx.currentDom())
        .arg(ctx.currentNode());
  }

}
