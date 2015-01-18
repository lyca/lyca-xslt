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

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.minus;
import static com.sun.codemodel.JOp.ne;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.IFGE;
import org.apache.bcel.generic.IFGT;
import org.apache.bcel.generic.IFLE;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.IF_ICMPGT;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

import de.lyca.xalan.xsltc.compiler.FlowList;

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
  public String toSignature() {
    return "I";
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
   * Translates an integer into an object of internal type <code>type</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, final Type type) {
    if (type == Type.Real) {
      translateTo(definedClass, method, (RealType) type);
    } else if (type == Type.String) {
      translateTo(definedClass, method, (StringType) type);
    } else if (type == Type.Boolean) {
      translateTo(definedClass, method, (BooleanType) type);
    } else if (type == Type.Reference) {
      translateTo(definedClass, method, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      // FIXME classGen.getParser().reportError(Constants.FATAL, err);
    }
  }

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
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return null;
    }
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return cast(type.toJCType(), expr);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return ctx.ref(Integer.class).staticInvoke("toString").arg(expr);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return ne(expr, lit(0)).cand(ne(expr, minus(lit(0))));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Integer.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Expects an integer on the stack and pushes a real.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, RealType type) {
//    FIXME
//    methodGen.getInstructionList().append(I2D);
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
//    il.append(new INVOKESTATIC(cpg.addMethodref(INTEGER_CLASS, "toString", "(I)" + STRING_SIG)));
  }

  /**
   * Expects an integer on the stack and pushes a 0 if its value is 0 and a 1
   * otherwise.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, BooleanType type) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    final BranchHandle falsec = il.append(new IFEQ(null));
//    il.append(ICONST_1);
//    final BranchHandle truec = il.append(new GOTO(null));
//    falsec.setTarget(il.append(ICONST_0));
//    truec.setTarget(il.append(NOP));
  }

  /**
   * Expects an integer on the stack and translates it to a non-synthesized
   * boolean. It does not push a 0 or a 1 but instead returns branchhandle list
   * to be appended to the false list.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateToDesynthesized
   */
  @Override
  public FlowList translateToDesynthesized(JDefinedClass definedClass, JMethod method, BooleanType type) {
    return null;
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    return new FlowList(il.append(new IFEQ(null)));
  }

  /**
   * Expects an integer on the stack and pushes a boxed integer. Boxed integers
   * are represented by an instance of <code>java.lang.Integer</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new NEW(cpg.addClass(INTEGER_CLASS)));
//    il.append(DUP_X1);
//    il.append(SWAP);
//    il.append(new INVOKESPECIAL(cpg.addMethodref(INTEGER_CLASS, "<init>", "(I)V")));
  }

  /**
   * Translates an integer into the Java type denoted by <code>clazz</code>.
   * Expects an integer on the stack and pushes a number of the appropriate type
   * after coercion.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    if (clazz == Character.TYPE) {
//      il.append(I2C);
//    } else if (clazz == Byte.TYPE) {
//      il.append(I2B);
//    } else if (clazz == Short.TYPE) {
//      il.append(I2S);
//    } else if (clazz == Integer.TYPE) {
//      il.append(NOP);
//    } else if (clazz == Long.TYPE) {
//      il.append(I2L);
//    } else if (clazz == Float.TYPE) {
//      il.append(I2F);
//    } else if (clazz == Double.TYPE) {
//      il.append(I2D);
//    }
//    // Is Double <: clazz? I.e. clazz in { Double, Number, Object }
//    else if (clazz.isAssignableFrom(java.lang.Double.class)) {
//      il.append(I2D);
//      Type.Real.translateTo(classGen, methodGen, Type.Reference);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

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
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
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
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new CHECKCAST(cpg.addClass(INTEGER_CLASS)));
//    final int index = cpg.addMethodref(INTEGER_CLASS, INT_VALUE, INT_VALUE_SIG);
//    il.append(new INVOKEVIRTUAL(index));
  }

  @Override
  public Instruction ADD() {
    return InstructionConstants.IADD;
  }

  @Override
  public Instruction SUB() {
    return InstructionConstants.ISUB;
  }

  @Override
  public Instruction MUL() {
    return InstructionConstants.IMUL;
  }

  @Override
  public Instruction DIV() {
    return InstructionConstants.IDIV;
  }

  @Override
  public Instruction REM() {
    return InstructionConstants.IREM;
  }

  @Override
  public Instruction NEG() {
    return InstructionConstants.INEG;
  }

  @Override
  public Instruction LOAD(int slot) {
    return new ILOAD(slot);
  }

  @Override
  public Instruction STORE(int slot) {
    return new ISTORE(slot);
  }

  @Override
  public BranchInstruction GT(boolean tozero) {
    return tozero ? (BranchInstruction) new IFGT(null) : (BranchInstruction) new IF_ICMPGT(null);
  }

  @Override
  public BranchInstruction GE(boolean tozero) {
    return tozero ? (BranchInstruction) new IFGE(null) : (BranchInstruction) new IF_ICMPGE(null);
  }

  @Override
  public BranchInstruction LT(boolean tozero) {
    return tozero ? (BranchInstruction) new IFLT(null) : (BranchInstruction) new IF_ICMPLT(null);
  }

  @Override
  public BranchInstruction LE(boolean tozero) {
    return tozero ? (BranchInstruction) new IFLE(null) : (BranchInstruction) new IF_ICMPLE(null);
  }
}
