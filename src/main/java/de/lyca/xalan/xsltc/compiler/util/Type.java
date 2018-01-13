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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;

import de.lyca.xalan.xsltc.compiler.Constants;
import de.lyca.xalan.xsltc.compiler.NodeTest;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
public abstract class Type {
  protected static final JCodeModel JCM = new JCodeModel();

  public static final Type Int = new IntType();
  public static final Type Real = new RealType();
  public static final Type Boolean = new BooleanType();
  public static final Type NodeSet = new NodeSetType();
  public static final Type String = new StringType();
  public static final Type ResultTree = new ResultTreeType();
  public static final Type Reference = new ReferenceType();
  public static final Type Void = new VoidType();
  public static final Type Object = new ObjectType(java.lang.Object.class);
  public static final Type Axis = new ObjectType(de.lyca.xml.dtm.Axis.class);

  public static final Type Node = new NodeType(NodeTest.ANODE);
  public static final Type Root = new NodeType(NodeTest.ROOT);
  public static final Type Element = new NodeType(NodeTest.ELEMENT);
  public static final Type Attribute = new NodeType(NodeTest.ATTRIBUTE);
  public static final Type Text = new NodeType(NodeTest.TEXT);
  public static final Type Comment = new NodeType(NodeTest.COMMENT);
  public static final Type Processing_Instruction = new NodeType(NodeTest.PI);

  /**
   * Factory method to instantiate object types. Returns a pre-defined instance for "java.lang.Object" and
   * "java.lang.String".
   * 
   * @param javaClassName TODO
   * @return TODO
   */
  public static Type newObjectType(String javaClassName) {
    if (javaClassName == "java.lang.Object")
      return Type.Object;
    else if (javaClassName == "java.lang.String")
      return Type.String;
    else
      return new ObjectType(javaClassName);
  }

  /**
   * Factory method to instantiate object types. Returns a pre-defined instance for java.lang.Object.class and
   * java.lang.String.class.
   * 
   * @param clazz TODO
   * @return TODO
   */
  public static Type newObjectType(Class<?> clazz) {
    if (clazz == java.lang.Object.class)
      return Type.Object;
    else if (clazz == java.lang.String.class)
      return Type.String;
    else
      return new ObjectType(clazz);
  }

  /**
   * Returns a string representation of this type.
   * 
   * @return TODO
   */
  @Override
  public abstract String toString();

  /**
   * Returns true if this and other are identical types.
   * 
   * @param other TODO
   * @return TODO
   */
  public abstract boolean identicalTo(Type other);

  /**
   * Returns true if this type is a numeric type. Redefined in NumberType.
   * 
   * @return TODO
   */
  public boolean isNumber() {
    return false;
  }

  /**
   * Returns true if this type has no object representaion. Redefined in ResultTreeType.
   * 
   * @return TODO
   */
  public boolean implementedAsMethod() {
    return false;
  }

  /**
   * Returns true if this type is a simple type. Redefined in NumberType, BooleanType and StringType.
   * 
   * @return TODO
   */
  public boolean isSimple() {
    return false;
  }

  public abstract JType toJCType();

  /**
   * Returns the distance between two types. This measure is used to select overloaded functions/operators. This method
   * is typically redefined by the subclasses.
   * 
   * @param type TODO
   * @return TODO
   */
  public int distanceTo(Type type) {
    return type == this ? 0 : Integer.MAX_VALUE;
  }

  /**
   * Compiles an expression of this type to an expression of type <code>type</code>. Expects an expression of the former
   * type and returns an expression of the latter.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param type TODO
   * @return TODO
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Type type) {
    final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, type), -1);
    ctx.xsltc().getParser().reportError(Constants.FATAL, err);
    return expr;
  }

  /**
   * Translates an object of this type to the external (Java) type denoted by <code>clazz</code>. This method is used to
   * translate parameters when external functions are called.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param clazz TODO
   * @return TODO
   */
  public JExpression compileTo(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(this, clazz), -1);
    ctx.xsltc().getParser().reportError(Constants.FATAL, err);
    return expr;
  }

  /**
   * Translates an external (Java) type denoted by <code>clazz</code> to an object of this type. This method is used to
   * translate return values when external functions are called.
   * 
   * @param ctx TODO
   * @param expr TODO
   * @param clazz TODO
   * @return TODO
   * 
   */
  public JExpression compileFrom(CompilerContext ctx, JExpression expr, Class<?> clazz) {
    final ErrorMsg err = new ErrorMsg(Messages.get().dataConversionErr(clazz, this), -1);
    ctx.xsltc().getParser().reportError(Constants.FATAL, err);
    return expr;
  }

  /**
   * Returns the class name of an internal type's external representation.
   * 
   * @return TODO
   */
  public String getClassName() {
    return "";
  }

}
