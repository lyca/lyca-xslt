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
import static com.sun.codemodel.JOp.cond;

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.Instruction;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.FlowList;
import de.lyca.xml.dtm.DTMAxisIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class NodeSetType extends Type {
  protected NodeSetType() {
  }

  @Override
  public String toString() {
    return "node-set";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public String toSignature() {
    return NODE_ITERATOR_SIG;
  }

  @Override
  public JType toJCType() {
    return JCM._ref(DTMAxisIterator.class);
  }

  /**
   * Translates a node-set into an object of internal type <code>type</code>.
   * The translation to int is undefined since node-sets are always converted to
   * reals in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Type type) {
    if (type == Type.String) {
      translateTo(definedClass, method, (StringType) type);
    } else if (type == Type.Boolean) {
      translateTo(definedClass, method, (BooleanType) type);
    } else if (type == Type.Real) {
      translateTo(definedClass, method, (RealType) type);
    } else if (type == Type.Node) {
      translateTo(definedClass, method, (NodeType) type);
    } else if (type == Type.Reference) {
      translateTo(definedClass, method, (ReferenceType) type);
    } else if (type == Type.Object) {
      translateTo(definedClass, method, (ObjectType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      // FIXME classGen.getParser().reportError(Constants.FATAL, err);
    }
  }

  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.Node) {
      return compileTo(ctx, expr, (NodeType) type);
    } else if (type == Type.Reference) {
      return compileTo(ctx, expr, (ReferenceType) type);
    } else if (type == Type.Object) {
      return compileTo(ctx, expr, (ObjectType) type);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return null;
    }
  }
  
  /**
   * Translates an external Java Class into an internal type. Expects the Java
   * object on the stack, pushes the internal type
   */
  @Override
  public void translateFrom(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    if (clazz.getName().equals("org.w3c.dom.NodeList")) {
//      // w3c NodeList is on the stack from the external Java function call.
//      // call BasisFunction to consume NodeList and leave Iterator on
//      // the stack.
//      il.append(classGen.loadTranslet()); // push translet onto stack
//      il.append(methodGen.loadDOM()); // push DOM onto stack
//      final int convert = cpg.addMethodref(BASIS_LIBRARY_CLASS, "nodeList2Iterator", "(" + "Lorg/w3c/dom/NodeList;"
//              + TRANSLET_INTF_SIG + DOM_INTF_SIG + ")" + NODE_ITERATOR_SIG);
//      il.append(new INVOKESTATIC(convert));
//    } else if (clazz.getName().equals("org.w3c.dom.Node")) {
//      // w3c Node is on the stack from the external Java function call.
//      // call BasisLibrary.node2Iterator() to consume Node and leave
//      // Iterator on the stack.
//      il.append(classGen.loadTranslet()); // push translet onto stack
//      il.append(methodGen.loadDOM()); // push DOM onto stack
//      final int convert = cpg.addMethodref(BASIS_LIBRARY_CLASS, "node2Iterator", "(" + "Lorg/w3c/dom/Node;"
//              + TRANSLET_INTF_SIG + DOM_INTF_SIG + ")" + NODE_ITERATOR_SIG);
//      il.append(new INVOKESTATIC(convert));
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Translates a node-set into a synthesized boolean. The boolean value of a
   * node-set is "true" if non-empty and "false" otherwise. Notice that the
   * function getFirstNode() is called in translateToDesynthesized().
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
    JInvocation next = expr.invoke(NEXT);
    return next.gte(lit(0));
  }

  /**
   * Translates a node-set into a string. The string value of a node-set is
   * value of its first element.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    getFirstNode(classGen, methodGen);
//    il.append(DUP);
//    final BranchHandle falsec = il.append(new IFLT(null));
//    Type.Node.translateTo(classGen, methodGen, type);
//    final BranchHandle truec = il.append(new GOTO(null));
//    falsec.setTarget(il.append(POP));
//    il.append(new PUSH(classGen.getConstantPool(), ""));
//    truec.setTarget(il.append(NOP));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    JInvocation next = expr.invoke(NEXT);
    return cond(next.gte(lit(0)), Type.Node.compileTo(ctx, next, type), lit(""));
  }

  /**
   * Expects a node-set on the stack and pushes a real. First the node-set is
   * converted to string, and from string to real.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, RealType type) {
//    FIXME
//    translateTo(classGen, methodGen, Type.String);
//    Type.String.translateTo(classGen, methodGen, Type.Real);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return Type.String.compileTo(ctx, compileTo(ctx, expr, Type.String), Type.Real);
  }

  /**
   * Expects a node-set on the stack and pushes a node.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, NodeType type) {
    getFirstNode(definedClass, method);
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeType type) {
//  il.append(new INVOKEINTERFACE(cpg.addInterfaceMethodref(NODE_ITERATOR, NEXT, NEXT_SIG), 1));
    return expr.invoke(NEXT);
  }

  
  /**
   * Subsume node-set into ObjectType.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ObjectType type) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  /**
   * Translates a node-set into a non-synthesized boolean. It does not push a 0
   * or a 1 but instead returns branchhandle list to be appended to the false
   * list.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateToDesynthesized
   */
  @Override
  public FlowList translateToDesynthesized(JDefinedClass definedClass, JMethod method, BooleanType type) {
    return null;
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    getFirstNode(classGen, methodGen);
//    return new FlowList(il.append(new IFLT(null)));
  }

  /**
   * Expects a node-set on the stack and pushes a boxed node-set. Node sets are
   * already boxed so the translation is just a NOP.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  /**
   * Translates a node-set into the Java type denoted by <code>clazz</code>.
   * Expects a node-set on the stack and pushes an object of the appropriate
   * type after coercion.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final String className = clazz.getName();
//
//    il.append(methodGen.loadDOM());
//    il.append(SWAP);
//
//    if (className.equals("org.w3c.dom.Node")) {
//      final int index = cpg.addInterfaceMethodref(DOM_INTF, MAKE_NODE, MAKE_NODE_SIG2);
//      il.append(new INVOKEINTERFACE(index, 2));
//    } else if (className.equals("org.w3c.dom.NodeList") || className.equals("java.lang.Object")) {
//      final int index = cpg.addInterfaceMethodref(DOM_INTF, MAKE_NODE_LIST, MAKE_NODE_LIST_SIG2);
//      il.append(new INVOKEINTERFACE(index, 2));
//    } else if (className.equals("java.lang.String")) {
//      final int next = cpg.addInterfaceMethodref(NODE_ITERATOR, "next", "()I");
//      final int index = cpg.addInterfaceMethodref(DOM_INTF, GET_NODE_VALUE, "(I)" + STRING_SIG);
//
//      // Get next node from the iterator
//      il.append(new INVOKEINTERFACE(next, 1));
//      // Get the node's string value (from the DOM)
//      il.append(new INVOKEINTERFACE(index, 2));
//
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), className);
//      classGen.getParser().reportError(Constants.FATAL, err);
//    }
  }

  /**
   * Some type conversions require gettting the first node from the node-set.
   * This function is defined to avoid code repetition.
   */
  private void getFirstNode(JDefinedClass definedClass, JMethod method) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new INVOKEINTERFACE(cpg.addInterfaceMethodref(NODE_ITERATOR, NEXT, NEXT_SIG), 1));
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
    return NODE_ITERATOR;
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
