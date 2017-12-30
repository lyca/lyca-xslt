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
package de.lyca.xalan.xsltc.compiler.util;

import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.Constants;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class VoidType extends Type {
  protected VoidType() {
  }

  @Override
  public String toString() {
    return "void";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public JType toJCType() {
    return JCM.VOID; // should never be called
  }

  /**
   * Translates a void into an object of internal type <code>type</code>. This
   * translation is needed when calling external functions that return void.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Translates a void into a string by pushing the empty string ''.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return JExpr.lit("");
  }

  /**
   * Translates an external (primitive) Java type into a void. Only an external
   * "void" can be converted to this class.
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (!clazz.getName().equals("void")) {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
    }
    return expr;
  }

}
