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

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.ObjectFactory;

/**
 * @author Todd Miller
 * @author Santiago Pericas-Geertsen
 */
public final class ObjectType extends Type {

  private String _javaClassName = "java.lang.Object";
  private Class<?> _clazz = java.lang.Object.class;

  /**
   * Used to represent a Java Class type such is required to support non-static
   * java functions.
   * 
   * @param javaClassName
   *          name of the class such as 'com.foo.Processor'
   */
  protected ObjectType(String javaClassName) {
    _javaClassName = javaClassName;

    try {
      _clazz = ObjectFactory.findProviderClass(javaClassName, ObjectFactory.findClassLoader(), true);
    } catch (final ClassNotFoundException e) {
      _clazz = null;
    }
  }

  protected ObjectType(Class<?> clazz) {
    _clazz = clazz;
    _javaClassName = clazz.getName();
  }

  /**
   * Must return the same value for all ObjectType instances. This is needed in
   * CastExpr to ensure the mapping table is used correctly.
   */
  @Override
  public int hashCode() {
    return java.lang.Object.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ObjectType;
  }

  @Override
  public String getClassName() {
    return _javaClassName;
  }

  public String getJavaClassName() {
    return _javaClassName;
  }

  public Class<?> getJavaClass() {
    return _clazz;
  }

  @Override
  public String toString() {
    return _javaClassName;
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public JType toJCType() {
    return JCM._ref(_clazz);
  }

  /**
   * Compiles an expression into an expression of internal type
   * <code>type</code>. This translation is needed when calling external
   * functions that return void.
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
   * Expects an integer on the stack and pushes its string value by calling
   * <code>Integer.toString(int i)</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    JVar var = ctx.currentBlock().decl(ctx.ref(Object.class), ctx.nextVar(), expr);
    return cond(var.eq(_null()), lit(""), var.invoke("toString"));
  }

  /**
   * Translates an object of this type to the external (Java) type denoted by
   * <code>clazz</code>. This method is used to translate parameters when
   * external functions are called.
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (!clazz.isAssignableFrom(_clazz)) {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
    }
    return expr;
  }

  /**
   * Translates an external Java type into an Object type
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    return expr;
  }

}
