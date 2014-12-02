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

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.RealType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.Operators;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class RelationalExpr extends Expression {

  private int _op;
  private Expression _left, _right;

  public RelationalExpr(int op, Expression left, Expression right) {
    _op = op;
    (_left = left).setParent(this);
    (_right = right).setParent(this);
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _left.setParser(parser);
    _right.setParser(parser);
  }

  /**
   * Returns true if this expressions contains a call to position(). This is
   * needed for context changes in node steps containing multiple predicates.
   */
  @Override
  public boolean hasPositionCall() {
    if (_left.hasPositionCall())
      return true;
    if (_right.hasPositionCall())
      return true;
    return false;
  }

  /**
   * Returns true if this expressions contains a call to last()
   */
  @Override
  public boolean hasLastCall() {
    return _left.hasLastCall() || _right.hasLastCall();
  }

  public boolean hasReferenceArgs() {
    return _left.getType() instanceof ReferenceType || _right.getType() instanceof ReferenceType;
  }

  public boolean hasNodeArgs() {
    return _left.getType() instanceof NodeType || _right.getType() instanceof NodeType;
  }

  public boolean hasNodeSetArgs() {
    return _left.getType() instanceof NodeSetType || _right.getType() instanceof NodeSetType;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    Type tleft = _left.typeCheck(stable);
    Type tright = _right.typeCheck(stable);

    // bug fix # 2838, cast to reals if both are result tree fragments
    if (tleft instanceof ResultTreeType && tright instanceof ResultTreeType) {
      _right = new CastExpr(_right, Type.Real);
      _left = new CastExpr(_left, Type.Real);
      return _type = Type.Boolean;
    }

    // If one is of reference type, then convert the other too
    if (hasReferenceArgs()) {
      Type type = null;
      Type typeL = null;
      Type typeR = null;
      if (tleft instanceof ReferenceType) {
        if (_left instanceof VariableRefBase) {
          final VariableRefBase ref = (VariableRefBase) _left;
          final VariableBase var = ref.getVariable();
          typeL = var.getType();
        }
      }
      if (tright instanceof ReferenceType) {
        if (_right instanceof VariableRefBase) {
          final VariableRefBase ref = (VariableRefBase) _right;
          final VariableBase var = ref.getVariable();
          typeR = var.getType();
        }
      }
      // bug fix # 2838
      if (typeL == null) {
        type = typeR;
      } else if (typeR == null) {
        type = typeL;
      } else {
        type = Type.Real;
      }
      if (type == null) {
        type = Type.Real;
      }

      _right = new CastExpr(_right, type);
      _left = new CastExpr(_left, type);
      return _type = Type.Boolean;
    }

    if (hasNodeSetArgs()) {
      // Ensure that the node-set is the left argument
      if (tright instanceof NodeSetType) {
        final Expression temp = _right;
        _right = _left;
        _left = temp;
        _op = _op == Operators.GT ? Operators.LT : _op == Operators.LT ? Operators.GT
                : _op == Operators.GE ? Operators.LE : Operators.GE;
        tright = _right.getType();
      }

      // Promote nodes to node sets
      if (tright instanceof NodeType) {
        _right = new CastExpr(_right, Type.NodeSet);
      }
      // Promote integer to doubles to have fewer compares
      if (tright instanceof IntType) {
        _right = new CastExpr(_right, Type.Real);
      }
      // Promote result-trees to strings
      if (tright instanceof ResultTreeType) {
        _right = new CastExpr(_right, Type.String);
      }
      return _type = Type.Boolean;
    }

    // In the node-boolean case, convert node to boolean first
    if (hasNodeArgs()) {
      if (tleft instanceof BooleanType) {
        _right = new CastExpr(_right, Type.Boolean);
        tright = Type.Boolean;
      }
      if (tright instanceof BooleanType) {
        _left = new CastExpr(_left, Type.Boolean);
        tleft = Type.Boolean;
      }
    }

    // Lookup the table of primops to find the best match
    final MethodType ptype = lookupPrimop(stable, Operators.getOpNames(_op), new MethodType(Type.Void, tleft, tright));

    if (ptype != null) {
      final Type arg1 = ptype.argsType().get(0);
      if (!arg1.identicalTo(tleft)) {
        _left = new CastExpr(_left, arg1);
      }
      final Type arg2 = ptype.argsType().get(1);
      if (!arg2.identicalTo(tright)) {
        _right = new CastExpr(_right, arg1);
      }
      return _type = ptype.resultType();
    }
    throw new TypeCheckError(this);
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method) {
 // FIXME
//    if (hasNodeSetArgs() || hasReferenceArgs()) {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final InstructionList il = methodGen.getInstructionList();
//
//      // Call compare() from the BasisLibrary
//      _left.translate(classGen, methodGen);
//      _left.startIterator(classGen, methodGen);
//      _right.translate(classGen, methodGen);
//      _right.startIterator(classGen, methodGen);
//
//      il.append(new PUSH(cpg, _op));
//      il.append(methodGen.loadDOM());
//
//      final int index = cpg.addMethodref(BASIS_LIBRARY_CLASS, "compare", "(" + _left.getType().toSignature()
//              + _right.getType().toSignature() + "I" + DOM_INTF_SIG + ")Z");
//      il.append(new INVOKESTATIC(index));
//    } else {
//      translateDesynthesized(classGen, methodGen);
//      synthesize(classGen, methodGen);
//    }
  }

  @Override
  public void translateDesynthesized(JDefinedClass definedClass, JMethod method) {
 // FIXME
//    if (hasNodeSetArgs() || hasReferenceArgs()) {
//      translate(classGen, methodGen);
//      desynthesize(classGen, methodGen);
//    } else {
//      BranchInstruction bi = null;
//      final InstructionList il = methodGen.getInstructionList();
//
//      _left.translate(classGen, methodGen);
//      _right.translate(classGen, methodGen);
//
//      // TODO: optimize if one of the args is 0
//
//      boolean tozero = false;
//      Type tleft = _left.getType();
//
//      if (tleft instanceof RealType) {
//        il.append(tleft.CMP(_op == Operators.LT || _op == Operators.LE));
//        tleft = Type.Int;
//        tozero = true;
//      }
//
//      switch (_op) {
//        case Operators.LT:
//          bi = tleft.GE(tozero);
//          break;
//
//        case Operators.GT:
//          bi = tleft.LE(tozero);
//          break;
//
//        case Operators.LE:
//          bi = tleft.GT(tozero);
//          break;
//
//        case Operators.GE:
//          bi = tleft.LT(tozero);
//          break;
//
//        default:
//          final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_RELAT_OP_ERR, this);
//          getParser().reportError(Constants.FATAL, msg);
//      }
//
//      _falseList.add(il.append(bi)); // must be backpatched
//    }
  }

  @Override
  public String toString() {
    return Operators.getOpNames(_op) + '(' + _left + ", " + _right + ')';
  }
}
