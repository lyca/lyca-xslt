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

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.Constants.STRING_CLASS;
import static de.lyca.xalan.xsltc.compiler.Constants.STRING_TO_REAL;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.Constants;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public class StringType extends Type {
  protected StringType() {
  }

  @Override
  public String toString() {
    return "string";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public JType toJCType() {
    return JCM._ref(String.class);
  }

  /**
   * Compiles a string expression into an object of internal type
   * <code>type</code>. The compilation to int is undefined since strings are
   * always converted to reals in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Compiles a string expression into a boolean expression.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return expr.invoke("length").gt(lit(0));
  }

  /**
   * Compiles a string expression into a real expression by calling
   * stringToReal() from the basis library.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke(STRING_TO_REAL).arg(expr);
  }

  /**
   * Expects a string expression and returns a boxed string. As strings are
   * already boxed so the compilation just returns the given expression.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return expr;
  }

  /**
   * Compiles an internal string expression into an external (Java) string.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateFrom
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    // Is String <: clazz? I.e. clazz in { String, Object }
    if (clazz.isAssignableFrom(java.lang.String.class)) {
      return expr;
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
  }

  /**
   * Translates an external (primitive) Java type into a string.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateFrom
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (String.class.equals(clazz)) {
      JVar var = ctx.currentBlock().decl(ctx.ref(String.class), ctx.nextVar(), expr);
      // same internal representation, convert null to ""
      return cond(var.eq(_null()), JExpr.lit(""), var);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
  }

  /**
   * Returns the class name of an internal type's external representation.
   */
  @Override
  public String getClassName() {
    return STRING_CLASS;
  }

}
