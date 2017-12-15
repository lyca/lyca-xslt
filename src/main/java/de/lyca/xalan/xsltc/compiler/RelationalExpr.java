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
package de.lyca.xalan.xsltc.compiler;

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.gt;
import static com.sun.codemodel.JOp.gte;
import static com.sun.codemodel.JOp.lt;
import static com.sun.codemodel.JOp.lte;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;
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
  public JExpression toJExpression(CompilerContext ctx) {
    if (hasNodeSetArgs() || hasReferenceArgs()) {
      // Call compare() from the BasisLibrary
      JExpression leftExp = _left.startIterator(ctx, _left.toJExpression(ctx));
      JExpression rightExp = _right.toJExpression(ctx);
      JClass basisLib = ctx.ref(BasisLibrary.class);
      return basisLib.staticInvoke("compare").arg(leftExp).arg(rightExp).arg(lit(_op)).arg(ctx.currentDom());
    } else {
      JExpression leftExp = _left.toJExpression(ctx);
      JExpression rightExp = _right.toJExpression(ctx);

      switch (_op) {
        case Operators.LT:
          return lt(leftExp, rightExp);
        case Operators.GT:
          return gt(leftExp, rightExp);
        case Operators.LE:
          return lte(leftExp, rightExp);
        case Operators.GE:
          return gte(leftExp, rightExp);
        default:
          final ErrorMsg msg = new ErrorMsg(ErrorMsg.ILLEGAL_RELAT_OP_ERR, this);
          getParser().reportError(Constants.FATAL, msg);
      }
    }
    return _null();
  }

  @Override
  public String toString() {
    return Operators.getOpNames(_op) + '(' + _left + ", " + _right + ')';
  }
}
