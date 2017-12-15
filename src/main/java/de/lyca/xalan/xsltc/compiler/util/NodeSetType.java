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

import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.lyca.xalan.xsltc.DOM.GET_STRING_VALUE_X;
import static de.lyca.xalan.xsltc.DOM.MAKE_NODE;
import static de.lyca.xalan.xsltc.DOM.MAKE_NODE_LIST;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.Constants.NODE_ITERATOR;
import static de.lyca.xml.dtm.DTMAxisIterator.NEXT;

import org.w3c.dom.NodeList;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.runtime.BasisLibrary;
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
  public JType toJCType() {
    return JCM._ref(DTMAxisIterator.class);
  }

  /**
   * Compiles a node-set expression into an expresion of internal type
   * <code>type</code>. The compilation to int is undefined since node-sets are
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
    } else if (type == Type.Node) {
      return compileTo(ctx, expr, (NodeType) type);
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
   * Translates an external Java Class into an internal type. Expects the Java
   * object on the stack, pushes the internal type
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == NodeList.class) {
      return ctx.ref(BasisLibrary.class).staticInvoke("nodeList2Iterator").arg(expr).arg(_this()).arg(ctx.currentDom());
    } else if (clazz == org.w3c.dom.Node.class) {
      return ctx.ref(BasisLibrary.class).staticInvoke("node2Iterator").arg(expr).arg(_this()).arg(ctx.currentDom());
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, toString(), clazz.getName());
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Translates a node-set into a synthesized boolean. The boolean value of a
   * node-set is "true" if non-empty and "false" otherwise. Notice that the
   * function getFirstNode() is called in translateToDesynthesized().
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    JInvocation next = expr.invoke(NEXT);
    return next.gte(lit(0));
  }

  /**
   * Translates a node-set into a string. The string value of a node-set is
   * value of its first element.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    JVar var = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextVar(), expr.invoke(NEXT));
    return cond(var.gte(lit(0)), Type.Node.compileTo(ctx, var, type), lit(""));
  }

  /**
   * Expects a node-set on the stack and pushes a real. First the node-set is
   * converted to string, and from string to real.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return Type.String.compileTo(ctx, compileTo(ctx, expr, Type.String), Type.Real);
  }

  /**
   * Expects a node-set on the stack and pushes a node.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeType type) {
    return expr.invoke(NEXT);
  }

  
  /**
   * Subsume node-set into ObjectType.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ObjectType type) {
    return expr;
  }

  /**
   * Expects a node-set on the stack and pushes a boxed node-set. Node sets are
   * already boxed so the translation is just a NOP.
   * 
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo(CompilerContext, JExpression, Type)
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    return expr;
  }

  /**
   * Translates a node-set into the Java type denoted by <code>clazz</code>.
   * Expects a node-set on the stack and pushes an object of the appropriate
   * type after coercion.
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (clazz == org.w3c.dom.Node.class) {
      return ctx.currentDom().invoke(MAKE_NODE).arg(expr);
    } else if (clazz == NodeList.class || clazz == Object.class) {
      return ctx.currentDom().invoke(MAKE_NODE_LIST).arg(expr);
    } else if (clazz == String.class) {
      return ctx.currentDom().invoke(GET_STRING_VALUE_X).arg(expr.invoke(NEXT));
    }
    return expr;
  }

  /**
   * Returns the class name of an internal type's external representation.
   */
  @Override
  public String getClassName() {
    return NODE_ITERATOR;
  }

}
