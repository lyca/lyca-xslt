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

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.minus;
import static com.sun.codemodel.JOp.ne;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.Constants;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class IntType extends NumberType {
  protected IntType() {
  }

  @Override
  public String toString() {
    return "int";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public JType toJCType() {
    return JCM.INT;
  }

  /**
   * @see de.lyca.xalan.xsltc.compiler.util.Type#distanceTo
   */
  @Override
  public int distanceTo(Type type) {
    if (type == this)
      return 0;
    else if (type == Type.Real)
      return 1;
    else
      return Integer.MAX_VALUE;
  }

  /**
   * Compiles an integer expression into an expression of internal type
   * <code>type</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Expects an integer expression and returns a real expression.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return cast(type.toJCType(), expr);
  }

  /**
   * Expects an integer expression and returns its string expression by calling
   * <code>Integer.toString(int i)</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return ctx.ref(Integer.class).staticInvoke("toString").arg(expr);
  }

  /**
   * Expects an integer expression and returns a 0 if its value is 0 and a 1
   * otherwise.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return ne(expr, lit(0)).cand(ne(expr, minus(lit(0))));
  }

  /**
   * Expects an integer expression and returns a boxed integer expression. Boxed
   * integers are represented by an instance of <code>java.lang.Integer</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Integer.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Compiles an integer expression into the Java expression denoted by
   * <code>clazz</code>. Expects an integer expression and returns a number
   * expression of the appropriate type after coercion.
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == Character.TYPE) {
      return cast(ctx.owner().CHAR, expr);
    } else if (clazz == Byte.TYPE) {
      return cast(ctx.owner().BYTE, expr);
    } else if (clazz == Short.TYPE) {
      return cast(ctx.owner().SHORT, expr);
    } else if (clazz == Integer.TYPE) {
      return expr;
    } else if (clazz == Long.TYPE) {
      return cast(ctx.owner().LONG, expr);
    } else if (clazz == Float.TYPE) {
      return cast(ctx.owner().FLOAT, expr);
    } else if (clazz == Double.TYPE) {
      return cast(ctx.owner().DOUBLE, expr);
    }
    // Is Double <: clazz? I.e. clazz in { Double, Number, Object }
    else if (clazz.isAssignableFrom(java.lang.Double.class)) {
      return cast(ctx.ref(Double.class), expr);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
  }

}
