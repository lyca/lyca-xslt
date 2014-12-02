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

package de.lyca.xalan.xsltc.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.SIPUSH;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.dtm.Axis;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class CastExpr extends Expression {
  private final Expression _left;

  /**
   * Legal conversions between internal types.
   */
  private static Map<Type, Set<Type>> InternalTypeMap = new HashMap<>();

  static {
    // Possible type conversions between internal types
    InternalTypeMap.put(
            Type.Boolean,
            new HashSet<>(Arrays
                    .asList(new Type[] { Type.Boolean, Type.Real, Type.String, Type.Reference, Type.Object })));

    InternalTypeMap.put(
            Type.Real,
            new HashSet<>(Arrays.asList(new Type[] { Type.Real, Type.Int, Type.Boolean, Type.String, Type.Reference,
                    Type.Object })));

    InternalTypeMap.put(
            Type.Int,
            new HashSet<>(Arrays.asList(new Type[] { Type.Int, Type.Real, Type.Boolean, Type.String, Type.Reference,
                    Type.Object })));

    InternalTypeMap.put(
            Type.String,
            new HashSet<>(Arrays
                    .asList(new Type[] { Type.String, Type.Boolean, Type.Real, Type.Reference, Type.Object })));

    InternalTypeMap.put(
            Type.NodeSet,
            new HashSet<>(Arrays.asList(new Type[] { Type.NodeSet, Type.Boolean, Type.Real, Type.String, Type.Node,
                    Type.Reference, Type.Object })));

    InternalTypeMap.put(
            Type.Node,
            new HashSet<>(Arrays.asList(new Type[] { Type.Node, Type.Boolean, Type.Real, Type.String, Type.NodeSet,
                    Type.Reference, Type.Object })));

    InternalTypeMap.put(
            Type.ResultTree,
            new HashSet<>(Arrays.asList(new Type[] { Type.ResultTree, Type.Boolean, Type.Real, Type.String,
                    Type.NodeSet, Type.Reference, Type.Object })));

    InternalTypeMap.put(
            Type.Reference,
            new HashSet<>(Arrays.asList(new Type[] { Type.Reference, Type.Boolean, Type.Int, Type.Real, Type.String,
                    Type.Node, Type.NodeSet, Type.ResultTree, Type.Object })));

    InternalTypeMap.put(Type.Object, new HashSet<>(Arrays.asList(new Type[] { Type.String })));

    InternalTypeMap.put(Type.Void, new HashSet<>(Arrays.asList(new Type[] { Type.String })));
  }

  private static boolean maps(Type from, Type to) {
    if (from == null)
      return false;
    final Collection<Type> collection = InternalTypeMap.get(from);
    if (collection != null)
      return collection.contains(to);
    return false;
  }

  private boolean _typeTest = false;

  /**
   * Construct a cast expression and check that the conversion is valid by
   * calling typeCheck().
   */
  public CastExpr(Expression left, Type type) throws TypeCheckError {
    _left = left;
    _type = type; // use inherited field

    if (_left instanceof Step && _type == Type.Boolean) {
      final Step step = (Step) _left;
      if (step.getAxis() == Axis.SELF && step.getNodeType() != -1) {
        _typeTest = true;
      }
    }

    // check if conversion is valid
    setParser(left.getParser());
    setParent(left.getParent());
    left.setParent(this);
    typeCheck(left.getParser().getSymbolTable());
  }

  public Expression getExpr() {
    return _left;
  }

  /**
   * Returns true if this expressions contains a call to position(). This is
   * needed for context changes in node steps containing multiple predicates.
   */
  @Override
  public boolean hasPositionCall() {
    return _left.hasPositionCall();
  }

  @Override
  public boolean hasLastCall() {
    return _left.hasLastCall();
  }

  @Override
  public String toString() {
    return "cast(" + _left + ", " + _type + ")";
  }

  /**
   * Type checking a cast expression amounts to verifying that the type
   * conversion is legal. Cast expressions are created during type checking, but
   * typeCheck() is usually not called on them. As a result, this method is
   * called from the constructor.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    Type tleft = _left.getType();
    if (tleft == null) {
      tleft = _left.typeCheck(stable);
    }
    if (tleft instanceof NodeType) {
      tleft = Type.Node; // multiple instances
    } else if (tleft instanceof ResultTreeType) {
      tleft = Type.ResultTree; // multiple instances
    }
    if (maps(tleft, _type))
      return _type;
    // throw new TypeCheckError(this);
    throw new TypeCheckError(new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR, tleft.toString(), _type.toString()));
  }

  @Override
  public void translateDesynthesized(JDefinedClass definedClass, JMethod method) {
    // FIXME
//    FlowList fl;
//    final Type ltype = _left.getType();
//
//    // This is a special case for the self:: axis. Instead of letting
//    // the Step object create and iterator that we cast back to a single
//    // node, we simply ask the DOM for the node type.
//    if (_typeTest) {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final InstructionList il = methodGen.getInstructionList();
//
//      final int idx = cpg.addInterfaceMethodref(DOM_INTF, "getExpandedTypeID", "(I)I");
//      il.append(new SIPUSH((short) ((Step) _left).getNodeType()));
//      il.append(methodGen.loadDOM());
//      il.append(methodGen.loadContextNode());
//      il.append(new INVOKEINTERFACE(idx, 2));
//      _falseList.add(il.append(new IF_ICMPNE(null)));
//    } else {
//
//      _left.translate(classGen, methodGen);
//      if (_type != ltype) {
//        _left.startIterator(classGen, methodGen);
//        if (_type instanceof BooleanType) {
//          fl = ltype.translateToDesynthesized(classGen, methodGen, _type);
//          if (fl != null) {
//            _falseList.append(fl);
//          }
//        } else {
//          ltype.translateTo(classGen, methodGen, _type);
//        }
//      }
//    }
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method) {
    // FIXME
//    final Type ltype = _left.getType();
//    _left.translate(classGen, methodGen);
//    if (_type.identicalTo(ltype) == false) {
//      _left.startIterator(classGen, methodGen);
//      ltype.translateTo(classGen, methodGen, _type);
//    }
  }
}
