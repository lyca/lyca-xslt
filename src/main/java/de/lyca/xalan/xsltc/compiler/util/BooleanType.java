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
import static com.sun.codemodel.JOp.cond;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.DATA_CONVERSION_ERR;

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

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.Constants;

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
  public String toSignature() {
    return "Z";
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
   * Translates a real into an object of internal type <code>type</code>. The
   * translation to int is undefined since booleans are always converted to
   * reals in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Type type) {
    if (type == Type.String) {
      translateTo(definedClass, method, (StringType) type);
    } else if (type == Type.Real) {
      translateTo(definedClass, method, (RealType) type);
    } else if (type == Type.Reference) {
      translateTo(definedClass, method, (ReferenceType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      // FIXME classGen.getParser().reportError(Constants.FATAL, err);
    }
  }
  
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
      return null;
    }
  }

  /**
   * Expects a boolean on the stack and pushes a string. If the value on the
   * stack is zero, then the string 'false' is pushed. Otherwise, the string
   * 'true' is pushed.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final BranchHandle falsec = il.append(new IFEQ(null));
//    il.append(new PUSH(cpg, "true"));
//    final BranchHandle truec = il.append(new GOTO(null));
//    falsec.setTarget(il.append(new PUSH(cpg, "false")));
//    truec.setTarget(il.append(NOP));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    return cond(expr, lit("true"), lit("false"));
  }

  /**
   * Expects a boolean on the stack and pushes a real. The value "true" is
   * converted to 1.0 and the value "false" to 0.0.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, RealType type) {
//    FIXME
//    methodGen.getInstructionList().append(I2D);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return cond(expr, lit(1.0), lit(0.0));
  }

  /**
   * Expects a boolean on the stack and pushes a boxed boolean. Boxed booleans
   * are represented by an instance of <code>java.lang.Boolean</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new NEW(cpg.addClass(BOOLEAN_CLASS)));
//    il.append(DUP_X1);
//    il.append(SWAP);
//    il.append(new INVOKESPECIAL(cpg.addMethodref(BOOLEAN_CLASS, "<init>", "(Z)V")));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return ctx.ref(Boolean.class).staticInvoke("valueOf").arg(expr);
  }

  /**
   * Translates an internal boolean into an external (Java) boolean.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    if (clazz == java.lang.Boolean.TYPE) {
//      methodGen.getInstructionList().append(NOP);
//    }
//    // Is Boolean <: clazz? I.e. clazz in { Boolean, Object }
//    else if (clazz.isAssignableFrom(java.lang.Boolean.class)) {
//      translateTo(classGen, methodGen, Type.Reference);
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Translates an external (Java) boolean into internal boolean.
   */
  @Override
  public void translateFrom(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
    translateTo(definedClass, method, clazz);
  }

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
      return super.compileFrom(ctx, expr, clazz);
    }
  }

  /**
   * Translates an object of this type to its boxed representation.
   */
  @Override
  public void translateBox(JDefinedClass definedClass, JMethod method) {
    translateTo(definedClass, method, Type.Reference);
  }

  /**
   * Translates an object of this type to its unboxed representation.
   */
  @Override
  public void translateUnBox(JDefinedClass definedClass, JMethod method) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new CHECKCAST(cpg.addClass(BOOLEAN_CLASS)));
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(BOOLEAN_CLASS, BOOLEAN_VALUE, BOOLEAN_VALUE_SIG)));
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
