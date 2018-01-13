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

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr.direct;
import static com.sun.codemodel.JExpr.invoke;
import static de.lyca.xalan.xsltc.DOM.GET_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.GET_STRING_VALUE;
import static de.lyca.xalan.xsltc.DOM.MAKE_NODE;
import static de.lyca.xalan.xsltc.DOM.MAKE_NODE_LIST;
import static de.lyca.xalan.xsltc.DOM.SETUP_MAPPING;
import static de.lyca.xalan.xsltc.compiler.Constants.DOM_INTF;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.Constants.NAMESPACE_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.NAMES_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.TYPES_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.URIS_INDEX;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.compiler.Constants;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
public final class ResultTreeType extends Type {
  private final String _methodName;

  protected ResultTreeType() {
    _methodName = null;
  }

  public ResultTreeType(String methodName) {
    _methodName = methodName;
  }

  @Override
  public String toString() {
    return "result-tree";
  }

  @Override
  public boolean identicalTo(Type other) {
    return other instanceof ResultTreeType;
  }

  @Override
  public JType toJCType() {
    return JCM._ref(DOM.class);
  }

  public String getMethodName() {
    return _methodName;
  }

  @Override
  public boolean implementedAsMethod() {
    return _methodName != null;
  }

  /**
   * Translates a result tree to object of internal type <code>type</code>. The translation to int is undefined since
   * result trees are always converted to reals in arithmetic expressions.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param type An instance of the type to translate the result tree to
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
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
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
      ctx.xsltc().getParser().reportError(FATAL, err);
      return expr;
    }
  }

  /**
   * Expects an result tree on the stack and pushes a boolean. Translates a result tree to a boolean by first converting
   * it to string.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param type An instance of BooleanType (any)
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, BooleanType type) {
    return TRUE;
  }

  /**
   * Expects an result tree on the stack and pushes a string.
   * 
   * @param definedClass TODO
   * @param method TODO
   * @param type An instance of StringType (any)
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, StringType type) {
    // FIXME
    // final ConstantPoolGen cpg = classGen.getConstantPool();
    // final InstructionList il = methodGen.getInstructionList();
    //
    // if (_methodName == null) {
    // final int index = cpg.addInterfaceMethodref(DOM_INTF, "getStringValue",
    // "()" + STRING_SIG);
    // il.append(new INVOKEINTERFACE(index, 1));
    // } else {
    // final String className = classGen.getClassName();
    //
    // // Push required parameters
    // il.append(classGen.loadTranslet());
    // if (classGen.isExternal()) {
    // il.append(new CHECKCAST(cpg.addClass(className)));
    // }
    // il.append(DUP);
    // il.append(new GETFIELD(cpg.addFieldref(className, "_dom",
    // DOM_INTF_SIG)));
    //
    // // Create a new instance of a StringValueHandler
    // int index = cpg.addMethodref(STRING_VALUE_HANDLER, "<init>", "()V");
    // il.append(new NEW(cpg.addClass(STRING_VALUE_HANDLER)));
    // il.append(DUP);
    // il.append(DUP);
    // il.append(new INVOKESPECIAL(index));
    //
    // // Store new Handler into a local variable
    // final LocalVariableGen handler =
    // methodGen.addLocalVariable("rt_to_string_handler",
    // Util.getJCRefType(STRING_VALUE_HANDLER_SIG), null, null);
    // handler.setStart(il.append(new ASTORE(handler.getIndex())));
    //
    // // Call the method that implements this result tree
    // index = cpg.addMethodref(className, _methodName, "(" + DOM_INTF_SIG +
    // TRANSLET_OUTPUT_SIG + ")V");
    // il.append(new INVOKEVIRTUAL(index));
    //
    // // Restore new handler and call getValue()
    // handler.setEnd(il.append(new ALOAD(handler.getIndex())));
    // index = cpg.addMethodref(STRING_VALUE_HANDLER, "getValue", "()" +
    // STRING_SIG);
    // il.append(new INVOKEVIRTUAL(index));
    // }
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, StringType type) {
    if (_methodName == null) {
      return expr.invoke(GET_STRING_VALUE);
    } else {
      // FIXME
      // final String className = classGen.getClassName();
      //
      // // Push required parameters
      // il.append(classGen.loadTranslet());
      // if (classGen.isExternal()) {
      // il.append(new CHECKCAST(cpg.addClass(className)));
      // }
      // il.append(DUP);
      // il.append(new GETFIELD(cpg.addFieldref(className, "_dom",
      // DOM_INTF_SIG)));
      //
      // // Create a new instance of a StringValueHandler
      // int index = cpg.addMethodref(STRING_VALUE_HANDLER, "<init>", "()V");
      // il.append(new NEW(cpg.addClass(STRING_VALUE_HANDLER)));
      // il.append(DUP);
      // il.append(DUP);
      // il.append(new INVOKESPECIAL(index));
      //
      // // Store new Handler into a local variable
      // final LocalVariableGen handler =
      // methodGen.addLocalVariable("rt_to_string_handler",
      // Util.getJCRefType(STRING_VALUE_HANDLER_SIG), null, null);
      // handler.setStart(il.append(new ASTORE(handler.getIndex())));
      //
      // // Call the method that implements this result tree
      // index = cpg.addMethodref(className, _methodName, "(" + DOM_INTF_SIG +
      // TRANSLET_OUTPUT_SIG + ")V");
      // il.append(new INVOKEVIRTUAL(index));
      //
      // // Restore new handler and call getValue()
      // handler.setEnd(il.append(new ALOAD(handler.getIndex())));
      // index = cpg.addMethodref(STRING_VALUE_HANDLER, "getValue", "()" +
      // STRING_SIG);
      // il.append(new INVOKEVIRTUAL(index));
      return null;
    }
  }

  /**
   * Expects an result tree on the stack and pushes a real. Translates a result tree into a real by first converting it
   * to string.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param type An instance of RealType (any)
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, RealType type) {
    return Type.String.compileTo(ctx, compileTo(ctx, expr, Type.String), Type.Real);
  }

  /**
   * Expects a result tree on the stack and pushes a boxed result tree. Result trees are already boxed so the
   * translation is just a NOP.
   * 
   * @param definedClass TODO
   * @param method TODO
   * @param type An instance of ReferenceType (any)
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ReferenceType type) {
    // FIXME
    // final ConstantPoolGen cpg = classGen.getConstantPool();
    // final InstructionList il = methodGen.getInstructionList();
    //
    // if (_methodName == null) {
    // il.append(NOP);
    // } else {
    // LocalVariableGen domBuilder, newDom;
    // final String className = classGen.getClassName();
    //
    // // Push required parameters
    // il.append(classGen.loadTranslet());
    // if (classGen.isExternal()) {
    // il.append(new CHECKCAST(cpg.addClass(className)));
    // }
    // il.append(methodGen.loadDOM());
    //
    // // Create new instance of DOM class (with RTF_INITIAL_SIZE nodes)
    // il.append(methodGen.loadDOM());
    // int index = cpg.addInterfaceMethodref(DOM_INTF, "getResultTreeFrag",
    // "(IZ)" + DOM_INTF_SIG);
    // il.append(new PUSH(cpg, RTF_INITIAL_SIZE));
    // il.append(new PUSH(cpg, false));
    // il.append(new INVOKEINTERFACE(index, 3));
    // il.append(DUP);
    //
    // // Store new DOM into a local variable
    // newDom = methodGen.addLocalVariable("rt_to_reference_dom",
    // Util.getJCRefType(DOM_INTF_SIG), null, null);
    // il.append(new CHECKCAST(cpg.addClass(DOM_INTF_SIG)));
    // newDom.setStart(il.append(new ASTORE(newDom.getIndex())));
    //
    // // Overwrite old handler with DOM handler
    // index = cpg.addInterfaceMethodref(DOM_INTF, "getOutputDomBuilder", "()" +
    // TRANSLET_OUTPUT_SIG);
    //
    // il.append(new INVOKEINTERFACE(index, 1));
    // // index = cpg.addMethodref(DOM_IMPL,
    // // "getOutputDomBuilder",
    // // "()" + TRANSLET_OUTPUT_SIG);
    // // il.append(new INVOKEVIRTUAL(index));
    // il.append(DUP);
    // il.append(DUP);
    //
    // // Store DOM handler in a local in order to call endDocument()
    // domBuilder = methodGen.addLocalVariable("rt_to_reference_handler",
    // Util.getJCRefType(TRANSLET_OUTPUT_SIG), null,
    // null);
    // domBuilder.setStart(il.append(new ASTORE(domBuilder.getIndex())));
    //
    // // Call startDocument on the new handler
    // index = cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
    // "startDocument", "()V");
    // il.append(new INVOKEINTERFACE(index, 1));
    //
    // // Call the method that implements this result tree
    // index = cpg.addMethodref(className, _methodName, "(" + DOM_INTF_SIG +
    // TRANSLET_OUTPUT_SIG + ")V");
    // il.append(new INVOKEVIRTUAL(index));
    //
    // // Call endDocument on the DOM handler
    // domBuilder.setEnd(il.append(new ALOAD(domBuilder.getIndex())));
    // index = cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
    // "endDocument", "()V");
    // il.append(new INVOKEINTERFACE(index, 1));
    //
    // // Push the new DOM on the stack
    // newDom.setEnd(il.append(new ALOAD(newDom.getIndex())));
    // }
  }

  public JExpression compileTo(CompilerContext ctx, JExpression expr, ReferenceType type) {
    if (_methodName == null) {
      return expr;
    } else {
      // LocalVariableGen domBuilder, newDom;
      // final String className = classGen.getClassName();
      //
      // // Push required parameters
      // il.append(classGen.loadTranslet());
      // if (classGen.isExternal()) {
      // il.append(new CHECKCAST(cpg.addClass(className)));
      // }
      // il.append(methodGen.loadDOM());
      //
      // // Create new instance of DOM class (with RTF_INITIAL_SIZE nodes)
      // il.append(methodGen.loadDOM());
      // int index = cpg.addInterfaceMethodref(DOM_INTF, "getResultTreeFrag",
      // "(IZ)" + DOM_INTF_SIG);
      // il.append(new PUSH(cpg, RTF_INITIAL_SIZE));
      // il.append(new PUSH(cpg, false));
      // il.append(new INVOKEINTERFACE(index, 3));
      // il.append(DUP);
      //
      // // Store new DOM into a local variable
      // newDom = methodGen.addLocalVariable("rt_to_reference_dom",
      // Util.getJCRefType(DOM_INTF_SIG), null, null);
      // il.append(new CHECKCAST(cpg.addClass(DOM_INTF_SIG)));
      // newDom.setStart(il.append(new ASTORE(newDom.getIndex())));
      //
      // // Overwrite old handler with DOM handler
      // index = cpg.addInterfaceMethodref(DOM_INTF, "getOutputDomBuilder", "()"
      // + TRANSLET_OUTPUT_SIG);
      //
      // il.append(new INVOKEINTERFACE(index, 1));
      // // index = cpg.addMethodref(DOM_IMPL,
      // // "getOutputDomBuilder",
      // // "()" + TRANSLET_OUTPUT_SIG);
      // // il.append(new INVOKEVIRTUAL(index));
      // il.append(DUP);
      // il.append(DUP);
      //
      // // Store DOM handler in a local in order to call endDocument()
      // domBuilder = methodGen.addLocalVariable("rt_to_reference_handler",
      // Util.getJCRefType(TRANSLET_OUTPUT_SIG), null,
      // null);
      // domBuilder.setStart(il.append(new ASTORE(domBuilder.getIndex())));
      //
      // // Call startDocument on the new handler
      // index = cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "startDocument", "()V");
      // il.append(new INVOKEINTERFACE(index, 1));
      //
      // // Call the method that implements this result tree
      // index = cpg.addMethodref(className, _methodName, "(" + DOM_INTF_SIG +
      // TRANSLET_OUTPUT_SIG + ")V");
      // il.append(new INVOKEVIRTUAL(index));
      //
      // // Call endDocument on the DOM handler
      // domBuilder.setEnd(il.append(new ALOAD(domBuilder.getIndex())));
      // index = cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "endDocument", "()V");
      // il.append(new INVOKEINTERFACE(index, 1));
      //
      // // Push the new DOM on the stack
      // newDom.setEnd(il.append(new ALOAD(newDom.getIndex())));
      return JExpr._null();
    }
  }

  /**
   * Expects a result tree on the stack and pushes a node-set (iterator). Note that the produced iterator is an iterator
   * for the DOM that contains the result tree, and not the DOM that is currently in use. This conversion here will
   * therefore not directly work with elements such as {@literal <xsl:apply-templates>} and {@literal <xsl:for-each>}
   * without the DOM parameter/variable being updates as well.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param type An instance of NodeSetType (any)
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, NodeSetType type) {
    // DOM adapters containing a result tree are not initialised with translet-type to DOM-type mapping. This must be
    // done now for XPath expressions and patterns to work for the iterator we create. Pass the type mappings to the DOM
    // adapter
    ctx.currentBlock().invoke(expr, SETUP_MAPPING).arg(direct(NAMES_INDEX)).arg(direct(URIS_INDEX))
        .arg(direct(TYPES_INDEX)).arg(direct(NAMESPACE_INDEX));

    // Create an iterator for the root node of the DOM adapter
    return expr.invoke(GET_ITERATOR);
  }

  /**
   * Subsume result tree into ObjectType.
   * 
   * @param definedClass TODO
   * @param method TODO
   * @param type TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  public void translateTo(JDefinedClass definedClass, JMethod method, ObjectType type) {
    // FIXME
    // methodGen.getInstructionList().append(NOP);
  }

  /**
   * Translates a result tree to a Java type denoted by <code>clazz</code>. Expects a result tree on the stack and
   * pushes an object of the appropriate type after coercion. Result trees are translated to W3C Node or W3C NodeList
   * and the translation is done via node-set type.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param clazz An reference to the Class to translate to
   * @return TODO
   * @see de.lyca.xalan.xsltc.compiler.util.Type#compileTo
   */
  @Override
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    final String className = clazz.getName();
    if (className.equals("org.w3c.dom.Node")) {
      return invoke(expr, MAKE_NODE).arg(compileTo(ctx, expr, Type.NodeSet));
    } else if (className.equals("org.w3c.dom.NodeList")) {
      return invoke(expr, MAKE_NODE_LIST).arg(compileTo(ctx, expr, Type.NodeSet));
    } else if (className.equals("java.lang.Object")) {
      return expr;
    } else if (className.equals("java.lang.String")) {
      return compileTo(ctx, expr, Type.String);
    } else {
      final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, className), -1);
      ctx.xsltc().getParser().reportError(Constants.FATAL, err);
      return expr;
    }
  }

  /**
   * Returns the class name of an internal type's external representation.
   * 
   * @return the external class name
   */
  @Override
  public String getClassName() {
    return DOM_INTF;
  }

}
