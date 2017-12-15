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
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.DOM.GET_STRING_VALUE_X;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.Constants.GET_ELEMENT_VALUE;
import static de.lyca.xalan.xsltc.compiler.Constants.RUNTIME_NODE_CLASS;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

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
  public JType toJCType() {
    return JCM.INT;
  }

  /**
   * Compiles a node expression into an expression of internal type
   * <code>type</code>. The compilation to int is undefined since nodes are
   * always converted to reals in arithmetic expressions.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
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
      return expr;
    }
  }

  /**
   * Expects a node expression and returns its string value.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext,
   *      JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    switch (_type) {
    case NodeTest.ROOT:
    case NodeTest.ELEMENT:
      return ctx.currentDom().invoke(GET_ELEMENT_VALUE).arg(expr);
    case NodeTest.ANODE:
    case NodeTest.COMMENT:
    case NodeTest.ATTRIBUTE:
    case NodeTest.PI:
      return ctx.currentDom().invoke(GET_STRING_VALUE_X).arg(expr);
    default:
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), type.toString());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Translates a node into a synthesized boolean. If the expression is "@attr",
   * then "true" is pushed iff "attr" is an attribute of the current node. If
   * the expression is ".", the result is always "true".
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return expr.ne(lit(0));
  }

  /**
   * Expects a node on the stack and pushes a real. First the node is converted
   * to string, and from string to real.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return Type.String.compileTo(ctx, compileTo(ctx, expr, Type.String), Type.Real);
  }

  /**
   * Expects a node on the stack and pushes a singleton node-set. Singleton
   * iterators are already started after construction.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeSetType type) {
    // Create a new instance of SingletonIterator
    return _new(ctx.ref(SingletonIterator.class)).arg(expr);
  }

  /**
   * Subsume Node into ObjectType.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ObjectType type) {
    return expr;
  }

  /**
   * Expects a node on the stack and pushes a boxed node. Boxed nodes are
   * represented by an instance of <code>de.lyca.xalan.xsltc.dom.Node</code>.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#translateTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return _new(ctx.ref(de.lyca.xalan.xsltc.runtime.Node.class)).arg(expr).arg(lit(_type));
  }

  /**
   * Translates a node into the Java type denoted by <code>clazz</code>. Expects
   * a node on the stack and pushes an object of the appropriate type after
   * coercion.
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
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
    return expr;
  }

  /**
   * Returns the class name of an internal type's external representation.
   */
  @Override
  public String getClassName() {
    return RUNTIME_NODE_CLASS;
  }

}
