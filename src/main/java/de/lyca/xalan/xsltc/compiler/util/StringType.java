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

import static com.sun.codemodel.JExpr.lit;

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.Instruction;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.FlowList;
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
  public String toSignature() {
    return "Ljava/lang/String;";
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
   * Translates a string into an object of internal type <code>type</code>. The
   * translation to int is undefined since strings are always converted to reals
   * in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Type type) {
//    FIXME
//    if (type == Type.Boolean) {
//      translateTo(classGen, methodGen, (BooleanType) type);
//    } else if (type == Type.Real) {
//      translateTo(classGen, methodGen, (RealType) type);
//    } else if (type == Type.Reference) {
//      translateTo(classGen, methodGen, (ReferenceType) type);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return null;
    }
  }
  
  /**
   * Translates a string into a synthesized boolean.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, BooleanType type) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    final FlowList falsel = translateToDesynthesized(classGen, methodGen, type);
//    il.append(ICONST_1);
//    final BranchHandle truec = il.append(new GOTO(null));
//    falsel.backPatch(il.append(ICONST_0));
//    truec.setTarget(il.append(NOP));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return expr.invoke("length").gt(lit(0));
  }

  /**
   * Translates a string into a real by calling stringToReal() from the basis
   * library.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, RealType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, STRING_TO_REAL, STRING_TO_REAL_SIG)));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke(STRING_TO_REAL).arg(expr);
  }

  /**
   * Translates a string into a non-synthesized boolean. It does not push a 0 or
   * a 1 but instead returns branchhandle list to be appended to the false list.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateToDesynthesized
   */
  @Override
  public FlowList translateToDesynthesized(JDefinedClass definedClass, JMethod method, BooleanType type) {
    return null;
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_CLASS, "length", "()I")));
//    return new FlowList(il.append(new IFEQ(null)));
  }

  /**
   * Expects a string on the stack and pushes a boxed string. Strings are
   * already boxed so the translation is just a NOP.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return expr;
  }

  /**
   * Translates a internal string into an external (Java) string.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateFrom
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    // Is String <: clazz? I.e. clazz in { String, Object }
//    if (clazz.isAssignableFrom(java.lang.String.class)) {
//      methodGen.getInstructionList().append(NOP);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Translates an external (primitive) Java type into a string.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateFrom
   */
  @Override
  public void translateFrom(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    if (clazz.getName().equals("java.lang.String")) {
//      // same internal representation, convert null to ""
//      il.append(DUP);
//      final BranchHandle ifNonNull = il.append(new IFNONNULL(null));
//      il.append(POP);
//      il.append(new PUSH(cpg, ""));
//      ifNonNull.setTarget(il.append(NOP));
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Translates an object of this type to its boxed representation.
   */
  @Override
  public void translateBox(JDefinedClass definedClass, JMethod method) {
//    FIXME
//    translateTo(classGen, methodGen, Type.Reference);
  }

  /**
   * Translates an object of this type to its unboxed representation.
   */
  @Override
  public void translateUnBox(JDefinedClass definedClass, JMethod method) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  /**
   * Returns the class name of an internal type's external representation.
   */
  @Override
  public String getClassName() {
    return STRING_CLASS;
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
