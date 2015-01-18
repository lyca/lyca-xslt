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

package de.lyca.xalan.xsltc.compiler.util;

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.Instruction;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

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
  public String toSignature() {
    final StringBuilder result = new StringBuilder("L");
    result.append(_javaClassName.replace('.', '/')).append(';');
    return result.toString();
  }

  @Override
  public JType toJCType() {
    return JCM._ref(_clazz);
  }

  /**
   * Translates a void into an object of internal type <code>type</code>. This
   * translation is needed when calling external functions that return void.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Type type) {
//    FIXME
//    if (type == Type.String) {
//      translateTo(classGen, methodGen, (StringType) type);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Expects an integer on the stack and pushes its string value by calling
   * <code>Integer.toString(int i)</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    il.append(DUP);
//    final BranchHandle ifNull = il.append(new IFNULL(null));
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(_javaClassName, "toString", "()" + STRING_SIG)));
//    final BranchHandle gotobh = il.append(new GOTO(null));
//    ifNull.setTarget(il.append(POP));
//    il.append(new PUSH(cpg, ""));
//    gotobh.setTarget(il.append(NOP));
  }

  /**
   * Translates an object of this type to the external (Java) type denoted by
   * <code>clazz</code>. This method is used to translate parameters when
   * external functions are called.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    if (clazz.isAssignableFrom(_clazz)) {
//      methodGen.getInstructionList().append(NOP);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getClass().toString());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz.isAssignableFrom(_clazz)) {
      return expr;
    }
    return super.compileTo(ctx, expr, clazz);
  }

  /**
   * Translates an external Java type into an Object type
   */
  @Override
  public void translateFrom(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  @Override
  public Instruction LOAD(int slot) {
    return new ALOAD(slot);
  }

  @Override
  public Instruction STORE(int slot) {
    return new ASTORE(slot);
  }
}
