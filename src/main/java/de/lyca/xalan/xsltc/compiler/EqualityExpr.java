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

import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.not;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JOp;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.NumberType;
import de.lyca.xalan.xsltc.compiler.util.RealType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xalan.xsltc.runtime.Operators;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class EqualityExpr extends Expression {

  private final int _op;
  private Expression _left;
  private Expression _right;

  public EqualityExpr(int op, Expression left, Expression right) {
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

  @Override
  public String toString() {
    return Operators.getOpNames(_op) + '(' + _left + ", " + _right + ')';
  }

  public Expression getLeft() {
    return _left;
  }

  public Expression getRight() {
    return _right;
  }

  public boolean getOp() {
    return _op != Operators.NE;
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

  @Override
  public boolean hasLastCall() {
    if (_left.hasLastCall())
      return true;
    if (_right.hasLastCall())
      return true;
    return false;
  }

  private void swapArguments() {
    final Expression temp = _left;
    _left = _right;
    _right = temp;
  }

  /**
   * Typing rules: see XSLT Reference by M. Kay page 345.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type tleft = _left.typeCheck(stable);
    final Type tright = _right.typeCheck(stable);

    if (tleft.isSimple() && tright.isSimple()) {
      if (tleft != tright) {
        if (tleft instanceof BooleanType) {
          _right = new CastExpr(_right, Type.Boolean);
        } else if (tright instanceof BooleanType) {
          _left = new CastExpr(_left, Type.Boolean);
        } else if (tleft instanceof NumberType || tright instanceof NumberType) {
          _left = new CastExpr(_left, Type.Real);
          _right = new CastExpr(_right, Type.Real);
        } else { // both compared as strings
          _left = new CastExpr(_left, Type.String);
          _right = new CastExpr(_right, Type.String);
        }
      }
    } else if (tleft instanceof ReferenceType) {
      _right = new CastExpr(_right, Type.Reference);
    } else if (tright instanceof ReferenceType) {
      _left = new CastExpr(_left, Type.Reference);
    }
    // the following 2 cases optimize @attr|.|.. = 'string'
    else if (tleft instanceof NodeType && tright == Type.String) {
      _left = new CastExpr(_left, Type.String);
    } else if (tleft == Type.String && tright instanceof NodeType) {
      _right = new CastExpr(_right, Type.String);
    }
    // optimize node/node
    else if (tleft instanceof NodeType && tright instanceof NodeType) {
      _left = new CastExpr(_left, Type.String);
      _right = new CastExpr(_right, Type.String);
    } else if (tleft instanceof NodeType && tright instanceof NodeSetType) {
      // compare(Node, NodeSet) will be invoked
    } else if (tleft instanceof NodeSetType && tright instanceof NodeType) {
      swapArguments(); // for compare(Node, NodeSet)
    } else {
      // At least one argument is of type node, node-set or result-tree

      // Promote an expression of type node to node-set
      if (tleft instanceof NodeType) {
        _left = new CastExpr(_left, Type.NodeSet);
      }
      if (tright instanceof NodeType) {
        _right = new CastExpr(_right, Type.NodeSet);
      }

      // If one arg is a node-set then make it the left one
      if (tleft.isSimple() || tleft instanceof ResultTreeType && tright instanceof NodeSetType) {
        swapArguments();
      }

      // Promote integers to doubles to have fewer compares
      if (_right.getType() instanceof IntType) {
        _right = new CastExpr(_right, Type.Real);
      }
    }
    return _type = Type.Boolean;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    final Type tleft = _left.getType();
    Type tright = _right.getType();

    JExpression leftExpr = _left.toJExpression(ctx);
    JExpression rightExpr = _right.toJExpression(ctx);

    if (tleft instanceof BooleanType || tleft instanceof NumberType) {
      return _op == Operators.EQ ? JOp.eq(leftExpr, rightExpr) : JOp.ne(leftExpr, rightExpr);
    }

    if (tleft instanceof StringType) {
      JExpression result = leftExpr.invoke("equals").arg(rightExpr);
      if (_op == Operators.NE) {
        return JOp.not(result);
      }
      return result;
    }

    if (tleft instanceof ResultTreeType) {
      if (tright instanceof BooleanType) {
        if (_op == Operators.NE) {
          return not(rightExpr);
        }
        return rightExpr;
      }

      if (tright instanceof RealType) {
        leftExpr = tleft.compileTo(ctx, leftExpr, Type.Real);
        return _op == Operators.EQ ? JOp.eq(leftExpr, rightExpr) : JOp.ne(leftExpr, rightExpr);
      }

      // Next, result-tree/string and result-tree/result-tree comparisons
      leftExpr = tleft.compileTo(ctx, leftExpr, Type.String);

      if (tright instanceof ResultTreeType) {
        rightExpr = tright.compileTo(ctx, rightExpr, Type.String);
      }

      JExpression result = leftExpr.invoke("equals").arg(rightExpr);
      if (_op == Operators.NE) {
        return JOp.not(result);
      }
      return result;
    }

    if (tleft instanceof NodeSetType && tright instanceof BooleanType) {
      leftExpr = _left.startIterator(ctx, leftExpr);
      leftExpr = Type.NodeSet.compileTo(ctx, leftExpr, Type.Boolean);

      return _op == Operators.NE ? leftExpr.ne(rightExpr) : leftExpr.eq(rightExpr);
    }

    if (tleft instanceof NodeSetType && tright instanceof StringType) {
      leftExpr = _left.startIterator(ctx, leftExpr); // needed ?
      return ctx.ref(BasisLibrary.class).staticInvoke("compare").arg(leftExpr).arg(rightExpr).arg(lit(_op))
          .arg(ctx.currentDom());
    }

    // Next, node-set/t for t in {real, string, node-set, result-tree}
    leftExpr = _left.startIterator(ctx, leftExpr);
    rightExpr = _right.startIterator(ctx, rightExpr);

    // Cast a result tree to a string to use an existing compare
    if (tright instanceof ResultTreeType) {
      rightExpr = tright.compileTo(ctx, rightExpr, Type.String);
      tright = Type.String;
    }

    // Call the appropriate compare() from the BasisLibrary
    return ctx.ref(BasisLibrary.class).staticInvoke("compare").arg(leftExpr).arg(rightExpr).arg(lit(_op))
        .arg(ctx.currentDom());
  }

}
