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
import static com.sun.codemodel.JOp.ne;
import static com.sun.codemodel.JOp.not;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.DATA_CONVERSION_ERR;

import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;

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
  public String toSignature() {
    return "D";
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
   * Translates a real into an object of internal type <code>type</code>. The
   * translation to int is undefined since reals are never converted to ints.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Type type) {
//    FIXME
//    if (type == Type.String) {
//      translateTo(classGen, methodGen, (StringType) type);
//    } else if (type == Type.Boolean) {
//      translateTo(classGen, methodGen, (BooleanType) type);
//    } else if (type == Type.Reference) {
//      translateTo(classGen, methodGen, (ReferenceType) type);
//    } else if (type == Type.Int) {
//      translateTo(classGen, methodGen, (IntType) type);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

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
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return null;
    }
  }

  /**
   * Expects a real on the stack and pushes its string value by calling
   * <code>Double.toString(double d)</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, "realToString", "(D)" + STRING_SIG)));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("realToString").arg(expr);
  }

  /**
   * Expects a real on the stack and pushes a 0 if that number is 0.0 and a 1
   * otherwise.
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
    return ne(expr, lit(0.0)).cand(ne(expr, lit(-0.0)))
        .cand(not(ctx.ref(Double.class).staticInvoke("isNaN").arg(expr)));
  }

  /**
   * Expects a real on the stack and pushes a truncated integer value
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, IntType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, "realToInt", "(D)I")));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, IntType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("realToInt").arg(expr);
  }

  /**
   * Translates a real into a non-synthesized boolean. It does not push a 0 or a
   * 1 but instead returns branchhandle list to be appended to the false list. A
   * NaN must be converted to "false".
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateToDesynthesized
   */
  @Override
  public FlowList translateToDesynthesized(JDefinedClass definedClass, JMethod method, BooleanType type) {
    return null;
//    FIXME
//    LocalVariableGen local;
//    final FlowList flowlist = new FlowList();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // Store real into a local variable
//    il.append(DUP2);
//    local = methodGen.addLocalVariable("real_to_boolean_tmp", org.apache.bcel.generic.Type.DOUBLE, null, null);
//    local.setStart(il.append(new DSTORE(local.getIndex())));
//
//    // Compare it to 0.0
//    il.append(DCONST_0);
//    il.append(DCMPG);
//    flowlist.add(il.append(new IFEQ(null)));
//
//    // !!! call isNaN
//    // Compare it to itself to see if NaN
//    il.append(new DLOAD(local.getIndex()));
//    local.setEnd(il.append(new DLOAD(local.getIndex())));
//    il.append(DCMPG);
//    flowlist.add(il.append(new IFNE(null))); // NaN != NaN
//    return flowlist;
  }

  /**
   * Expects a double on the stack and pushes a boxed double. Boxed double are
   * represented by an instance of <code>java.lang.Double</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new NEW(cpg.addClass(DOUBLE_CLASS)));
//    il.append(DUP_X2);
//    il.append(DUP_X2);
//    il.append(POP);
//    il.append(new INVOKESPECIAL(cpg.addMethodref(DOUBLE_CLASS, "<init>", "(D)V")));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Double.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Translates a real into the Java type denoted by <code>clazz</code>. Expects
   * a real on the stack and pushes a number of the appropriate type after
   * coercion.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, final Class<?> clazz) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    if (clazz == Character.TYPE) {
//      il.append(D2I);
//      il.append(I2C);
//    } else if (clazz == Byte.TYPE) {
//      il.append(D2I);
//      il.append(I2B);
//    } else if (clazz == Short.TYPE) {
//      il.append(D2I);
//      il.append(I2S);
//    } else if (clazz == Integer.TYPE) {
//      il.append(D2I);
//    } else if (clazz == Long.TYPE) {
//      il.append(D2L);
//    } else if (clazz == Float.TYPE) {
//      il.append(D2F);
//    } else if (clazz == Double.TYPE) {
//      il.append(NOP);
//    }
//    // Is Double <: clazz? I.e. clazz in { Double, Number, Object }
//    else if (clazz.isAssignableFrom(java.lang.Double.class)) {
//      translateTo(classGen, methodGen, Type.Reference);
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
      final ErrorMsg err = new ErrorMsg(DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Translates an external (primitive) Java type into a real. Expects a java
   * object on the stack and pushes a real (i.e., a double).
   */
  @Override
  public void translateFrom(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//
//    if (clazz == Character.TYPE || clazz == Byte.TYPE || clazz == Short.TYPE || clazz == Integer.TYPE) {
//      il.append(I2D);
//    } else if (clazz == Long.TYPE) {
//      il.append(L2D);
//    } else if (clazz == Float.TYPE) {
//      il.append(F2D);
//    } else if (clazz == Double.TYPE) {
//      il.append(NOP);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

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
      final ErrorMsg err = new ErrorMsg(DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(FATAL, err);
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
//    il.append(new CHECKCAST(cpg.addClass(DOUBLE_CLASS)));
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(DOUBLE_CLASS, DOUBLE_VALUE, DOUBLE_VALUE_SIG)));
  }

  @Override
  public Instruction ADD() {
    return InstructionConstants.DADD;
  }

  @Override
  public Instruction SUB() {
    return InstructionConstants.DSUB;
  }

  @Override
  public Instruction MUL() {
    return InstructionConstants.DMUL;
  }

  @Override
  public Instruction DIV() {
    return InstructionConstants.DDIV;
  }

  @Override
  public Instruction REM() {
    return InstructionConstants.DREM;
  }

  @Override
  public Instruction NEG() {
    return InstructionConstants.DNEG;
  }

  @Override
  public Instruction LOAD(int slot) {
    return new DLOAD(slot);
  }

  @Override
  public Instruction STORE(int slot) {
    return new DSTORE(slot);
  }

  @Override
  public Instruction POP() {
    return POP2;
  }

  @Override
  public Instruction CMP(boolean less) {
    return less ? InstructionConstants.DCMPG : InstructionConstants.DCMPL;
  }

  @Override
  public Instruction DUP() {
    return DUP2;
  }
}
