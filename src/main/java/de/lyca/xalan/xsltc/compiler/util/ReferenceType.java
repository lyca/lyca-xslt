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

import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xml.dtm.DTMAxisIterator.RESET;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 */
public final class ReferenceType extends Type {
  protected ReferenceType() {
  }

  @Override
  public String toString() {
    return "reference";
  }

  @Override
  public boolean identicalTo(Type other) {
    return this == other;
  }

  @Override
  public JType toJCType() {
    return JCM._ref(Object.class);
  }

  /**
   * Translates a reference to an object of internal type <code>type</code>. The
   * translation to int is undefined since references are always converted to
   * reals in arithmetic expressions.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    if (type == Type.String) {
      return compileTo(ctx, expr, (StringType) type);
    } else if (type == Type.Real) {
      return compileTo(ctx, expr, (RealType) type);
    } else if (type == Type.Boolean) {
      return compileTo(ctx, expr, (BooleanType) type);
    } else if (type == Type.NodeSet) {
      return compileTo(ctx, expr, (NodeSetType) type);
    } else if (type == Type.Node) {
      return compileTo(ctx, expr, (NodeType) type);
    } else if (type == Type.ResultTree) {
      return compileTo(ctx, expr, (ResultTreeType) type);
    } else if (type == Type.Object) {
      return compileTo(ctx, expr, (ObjectType) type);
    } else if (type == Type.Reference) {
      return expr;
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().internalErr(type), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Translates reference into object of internal type <code>type</code>.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    JExpression currentNode = ctx.currentNode();
    // If no current, conversion is a top-level
    if (currentNode == null) {
      currentNode = lit(DTM.ROOT_NODE);
    }
    return ctx.ref(BasisLibrary.class).staticInvoke("stringF").arg(expr).arg(currentNode).arg(ctx.currentDom());
  }

  /**
   * Translates a reference into an object of internal type <code>type</code>.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("numberF").arg(expr).arg(ctx.currentDom());
  }

  /**
   * Translates a reference to an object of internal type <code>type</code>.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("booleanF").arg(expr);
  }

  /**
   * Casts a reference into a NodeIterator.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeSetType type) {
    // Reset this iterator
    return ctx.ref(BasisLibrary.class).staticInvoke("referenceToNodeSet").arg(expr).invoke(RESET);
  }

  /**
   * Casts a reference into a Node.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeType type) {
    return Type.NodeSet.compileTo(ctx, compileTo(ctx, expr, Type.NodeSet), type);
  }

  /**
   * Casts a reference into a ResultTree.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ResultTreeType type) {
    return ctx.ref(BasisLibrary.class).staticInvoke("referenceToResultTree").arg(expr);
  }

  /**
   * Subsume reference into ObjectType.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param type
   *          TODO
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, ObjectType type) {
    return expr;
  }

  /**
   * Translates a reference into the Java type denoted by <code>clazz</code>.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param clazz
   *          TODO
   * @return TODO
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    // FIXME
    // final ConstantPoolGen cpg = classGen.getConstantPool();
    // final InstructionList il = methodGen.getInstructionList();
    //
    // final int referenceToLong = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToLong", "(" + OBJECT_SIG + ")J");
    // final int referenceToDouble = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToDouble", "(" + OBJECT_SIG + ")D");
    // final int referenceToBoolean = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToBoolean", "(" + OBJECT_SIG + ")Z");
    //
    // if (clazz.getName().equals("java.lang.Object")) {
    // il.append(NOP);
    // } else if (clazz == Double.TYPE) {
    // il.append(new INVOKESTATIC(referenceToDouble));
    // } else if (clazz.getName().equals("java.lang.Double")) {
    // il.append(new INVOKESTATIC(referenceToDouble));
    // Type.Real.translateTo(classGen, methodGen, Type.Reference);
    // } else if (clazz == Float.TYPE) {
    // il.append(new INVOKESTATIC(referenceToDouble));
    // il.append(D2F);
    // } else if (clazz.getName().equals("java.lang.String")) {
    // final int index = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToString", "(" + OBJECT_SIG + DOM_INTF_SIG
    // + ")" + "Ljava/lang/String;");
    // il.append(methodGen.loadDOM());
    // il.append(new INVOKESTATIC(index));
    // } else if (clazz.getName().equals("org.w3c.dom.Node")) {
    // final int index = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToNode", "(" + OBJECT_SIG + DOM_INTF_SIG + ")"
    // + "Lorg/w3c/dom/Node;");
    // il.append(methodGen.loadDOM());
    // il.append(new INVOKESTATIC(index));
    // } else if (clazz.getName().equals("org.w3c.dom.NodeList")) {
    // final int index = cpg.addMethodref(BASIS_LIBRARY_CLASS,
    // "referenceToNodeList", "(" + OBJECT_SIG + DOM_INTF_SIG
    // + ")" + "Lorg/w3c/dom/NodeList;");
    // il.append(methodGen.loadDOM());
    // il.append(new INVOKESTATIC(index));
    // } else if (clazz.getName().equals("de.lyca.xalan.xsltc.DOM")) {
    // translateTo(classGen, methodGen, Type.ResultTree);
    // } else if (clazz == Long.TYPE) {
    // il.append(new INVOKESTATIC(referenceToLong));
    // } else if (clazz == Integer.TYPE) {
    // il.append(new INVOKESTATIC(referenceToLong));
    // il.append(L2I);
    // } else if (clazz == Short.TYPE) {
    // il.append(new INVOKESTATIC(referenceToLong));
    // il.append(L2I);
    // il.append(I2S);
    // } else if (clazz == Byte.TYPE) {
    // il.append(new INVOKESTATIC(referenceToLong));
    // il.append(L2I);
    // il.append(I2B);
    // } else if (clazz == Character.TYPE) {
    // il.append(new INVOKESTATIC(referenceToLong));
    // il.append(L2I);
    // il.append(I2C);
    // } else if (clazz == java.lang.Boolean.TYPE) {
    // il.append(new INVOKESTATIC(referenceToBoolean));
    // } else if (clazz.getName().equals("java.lang.Boolean")) {
    // il.append(new INVOKESTATIC(referenceToBoolean));
    // Type.Boolean.translateTo(classGen, methodGen, Type.Reference);
    // } else {
    // final ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
    // toString(), clazz.getName());
    // classGen.getParser().reportError(Constants.FATAL, err);
    // }
    return expr;
  }

  /**
   * Translates an external Java type into a reference. Only conversion allowed
   * is from java.lang.Object.
   * 
   * @param ctx
   *          TODO
   * @param expr
   *          TODO
   * @param clazz
   *          TODO
   * @return TODO
   */
  @Override
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    if (!clazz.getName().equals("java.lang.Object")) {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz.getName()), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
    }
    return expr;
  }

}
