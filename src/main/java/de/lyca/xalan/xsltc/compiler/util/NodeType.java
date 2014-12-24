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

import static com.sun.codemodel.JExpr._new;

import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.Instruction;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.FlowList;
import de.lyca.xalan.xsltc.compiler.NodeTest;
import de.lyca.xalan.xsltc.dom.SingletonIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class NodeType extends Type {
  private final int _type;

  protected NodeType() {
    this(NodeTest.ANODE);
  }

  protected NodeType(int type) {
    _type = type;
  }

  public int getType() {
    return _type;
  }

  @Override
  public String toString() {
    return "node-type";
  }

  @Override
  public boolean identicalTo(Type other) {
    return other instanceof NodeType;
  }

  @Override
  public int hashCode() {
    return _type;
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
   * Translates a node into an object of internal type <code>type</code>. The
   * translation to int is undefined since nodes are always converted to reals
   * in arithmetic expressions.
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
    } else if (type == Type.NodeSet) {
      translateTo(definedClass, method, (NodeSetType) type);
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
    } else if (type == Type.NodeSet) {
      return compileTo(ctx, expr, (NodeSetType) type);
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
   * Expects a node on the stack and pushes its string value.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    switch (_type) {
//      case NodeTest.ROOT:
//      case NodeTest.ELEMENT:
//        il.append(methodGen.loadDOM());
//        il.append(SWAP); // dom ref must be below node index
//        int index = cpg.addInterfaceMethodref(DOM_INTF, GET_ELEMENT_VALUE, GET_ELEMENT_VALUE_SIG);
//        il.append(new INVOKEINTERFACE(index, 2));
//        break;
//
//      case NodeTest.ANODE:
//      case NodeTest.COMMENT:
//      case NodeTest.ATTRIBUTE:
//      case NodeTest.PI:
//        il.append(methodGen.loadDOM());
//        il.append(SWAP); // dom ref must be below node index
//        index = cpg.addInterfaceMethodref(DOM_INTF, GET_NODE_VALUE, GET_NODE_VALUE_SIG);
//        il.append(new INVOKEINTERFACE(index, 2));
//        break;
//
//      default:
//        final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
//        classGen.getParser().reportError(Constants.FATAL, err);
//        break;
//    }
  }
  
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    switch (_type) {
    case NodeTest.ROOT:
    case NodeTest.ELEMENT:
      return ctx.currentDom().invoke(GET_ELEMENT_VALUE).arg(expr);
    case NodeTest.ANODE:
    case NodeTest.COMMENT:
    case NodeTest.ATTRIBUTE:
    case NodeTest.PI:
      return ctx.currentDom().invoke(GET_NODE_VALUE).arg(expr);
    default:
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return null;
    }
  }

  /**
   * Translates a node into a synthesized boolean. If the expression is "@attr",
   * then "true" is pushed iff "attr" is an attribute of the current node. If
   * the expression is ".", the result is always "true".
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

  /**
   * Expects a node on the stack and pushes a real. First the node is converted
   * to string, and from string to real.
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
   * Expects a node on the stack and pushes a singleton node-set. Singleton
   * iterators are already started after construction.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, NodeSetType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // Create a new instance of SingletonIterator
//    il.append(new NEW(cpg.addClass(SINGLETON_ITERATOR)));
//    il.append(DUP_X1);
//    il.append(SWAP);
//    final int init = cpg.addMethodref(SINGLETON_ITERATOR, "<init>", "(" + NODE_SIG + ")V");
//    il.append(new INVOKESPECIAL(init));
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeSetType type) {
    return _new(ctx.ref(SingletonIterator.class)).arg(expr);
  }

  /**
   * Subsume Node into ObjectType.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ObjectType type) {
//    FIXME
//    methodGen.getInstructionList().append(NOP);
  }

  /**
   * Translates a node into a non-synthesized boolean. It does not push a 0 or a
   * 1 but instead returns branchhandle list to be appended to the false list.
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
   * Expects a node on the stack and pushes a boxed node. Boxed nodes are
   * represented by an instance of <code>de.lyca.xalan.xsltc.dom.Node</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new NEW(cpg.addClass(RUNTIME_NODE_CLASS)));
//    il.append(DUP_X1);
//    il.append(SWAP);
//    il.append(new PUSH(cpg, _type));
//    il.append(new INVOKESPECIAL(cpg.addMethodref(RUNTIME_NODE_CLASS, "<init>", "(II)V")));
  }

  /**
   * Translates a node into the Java type denoted by <code>clazz</code>. Expects
   * a node on the stack and pushes an object of the appropriate type after
   * coercion.
   */
  @Override
  public void translateTo(JDefinedClass definedClass, JMethod method, Class<?> clazz) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    final String className = clazz.getName();
//    if (className.equals("java.lang.String")) {
//      translateTo(classGen, methodGen, Type.String);
//      return;
//    }
//
//    il.append(methodGen.loadDOM());
//    il.append(SWAP); // dom ref must be below node index
//
//    if (className.equals("org.w3c.dom.Node") || className.equals("java.lang.Object")) {
//      final int index = cpg.addInterfaceMethodref(DOM_INTF, MAKE_NODE, MAKE_NODE_SIG);
//      il.append(new INVOKEINTERFACE(index, 2));
//    } else if (className.equals("org.w3c.dom.NodeList")) {
//      final int index = cpg.addInterfaceMethodref(DOM_INTF, MAKE_NODE_LIST, MAKE_NODE_LIST_SIG);
//      il.append(new INVOKEINTERFACE(index, 2));
//    } else {
//      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), className);
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
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    il.append(new CHECKCAST(cpg.addClass(RUNTIME_NODE_CLASS)));
//    il.append(new GETFIELD(cpg.addFieldref(RUNTIME_NODE_CLASS, NODE_FIELD, NODE_FIELD_SIG)));
  }

  /**
   * Returns the class name of an internal type's external representation.
   */
  @Override
  public String getClassName() {
    return RUNTIME_NODE_CLASS;
  }

  @Override
  public Instruction LOAD(int slot) {
    return new ILOAD(slot);
  }

  @Override
  public Instruction STORE(int slot) {
    return new ISTORE(slot);
  }
}
