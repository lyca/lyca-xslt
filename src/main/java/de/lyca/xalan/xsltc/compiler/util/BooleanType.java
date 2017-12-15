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
import static com.sun.codemodel.JOp.cond;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.DATA_CONVERSION_ERR;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class BooleanType extends Type {
  protected BooleanType() {
  }

  @Override
  public String toString() {
    return "boolean";
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
    return JCM.BOOLEAN;
  }

  /**
   * Compiles a real expression into an expression of internal type
   * <code>type</code>. The compilation to int is undefined since booleans are
   * always converted to reals in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Takes a boolean expression and returns a string ecpression. If the value of
   * the boolean expression is zero, then the string 'false' is returned.
   * Otherwise, the string 'true'.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    JVar var = ctx.currentBlock().decl(ctx.owner().BOOLEAN, ctx.nextVar(), expr);
    return cond(var, lit("true"), lit("false"));
  }

  /**
   * Takes a boolean expression and returns a real expression. The value "true"
   * is converted to 1.0 and the value "false" to 0.0.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    JVar var = ctx.currentBlock().decl(ctx.owner().BOOLEAN, ctx.nextVar(), expr);
    return cond(var, lit(1.0), lit(0.0));
  }

  /**
   * Takes a boolean expression and returns a boxed boolean expression. Boxed
   * booleans are represented by an instance of <code>java.lang.Boolean</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Boolean.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Compiles an internal boolean into an external (Java) boolean.
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == java.lang.Boolean.TYPE) {
      return expr;
    }
    // Is Boolean <: clazz? I.e. clazz in { Boolean, Object }
    else if (clazz.isAssignableFrom(java.lang.Boolean.class)) {
      return compileTo(ctx, expr, Type.Reference);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Compiles an external (Java) boolean into internal boolean.
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == java.lang.Boolean.TYPE) {
      return expr;
    }
    // Is Boolean <: clazz? I.e. clazz in { Boolean, Object }
    else if (clazz.isAssignableFrom(java.lang.Boolean.class)) {
      return cast(ctx.ref(Boolean.class), expr);
    } else {
      final ErrorMsg err = new ErrorMsg(DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

}
