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
import static com.sun.codemodel.JOp.ne;
import static com.sun.codemodel.JOp.not;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.Constants;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class RealType extends NumberType {
  protected RealType() {
  }

  @Override
  public String toString() {
    return "real";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public JType toJCType() {
    return JCM.DOUBLE;
  }

  /**
   * @see de.lyca.xalan.xsltc.compiler.util.Type#distanceTo
   */
  @Override
  public int distanceTo(Type type) {
    if (type == this)
      return 0;
    else if (type == Type.Int)
      return 1;
    else
      return Integer.MAX_VALUE;
  }

  /**
   * Compiles a real expression into an expression of internal type
   * <code>type</code>. The translation to int is undefined since reals are
   * never converted to ints.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else if (type == Type.Int) {
      return compileTo(ctx, expr, (IntType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return null;
    }
  }

  /**
   * Expects a real on the stack and pushes its string value by calling
   * <code>Double.toString(double d)</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("realToString").arg(expr);
  }

  /**
   * Expects a real on the stack and pushes a 0 if that number is 0.0 and a 1
   * otherwise.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return ne(expr, lit(0.0)).cand(ne(expr, lit(-0.0)))
        .cand(not(ctx.ref(Double.class).staticInvoke("isNaN").arg(expr)));
  }

  /**
   * Expects a real on the stack and pushes a truncated integer value
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, IntType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("realToInt").arg(expr);
  }

  /**
   * Expects a double on the stack and pushes a boxed double. Boxed double are
   * represented by an instance of <code>java.lang.Double</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Double.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Translates a real into the Java type denoted by <code>clazz</code>. Expects
   * a real on the stack and pushes a number of the appropriate type after
   * coercion.
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
      return cast(ctx.owner().INT, expr);
    } else if (clazz == Long.TYPE) {
      return cast(ctx.owner().LONG, expr);
    } else if (clazz == Float.TYPE) {
      return cast(ctx.owner().FLOAT, expr);
    } else if (clazz == Double.TYPE) {
      return expr;
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

  /**
   * Translates an external (primitive) Java type into a real. Expects a java
   * object on the stack and pushes a real (i.e., a double).
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == Character.TYPE || clazz == Byte.TYPE || clazz == Short.TYPE || clazz == Integer.TYPE) {
      return cast(ctx.owner().DOUBLE, expr);
    } else if (clazz == Long.TYPE) {
      return cast(ctx.owner().DOUBLE, expr);
    } else if (clazz == Float.TYPE) {
      return cast(ctx.owner().DOUBLE, expr);
    } else if (clazz == Double.TYPE) {
      return expr;
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
  }

}
