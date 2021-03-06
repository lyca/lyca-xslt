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

import static com.sun.codemodel.JOp.cand;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JOp;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class LogicalExpr extends Expression {

  public static final int OR = 0;
  public static final int AND = 1;

  private final int _op; // operator
  private Expression _left; // first operand
  private Expression _right; // second operand

  private static final String[] Ops = { "or", "and" };

  /**
   * Creates a new logical expression - either OR or AND. Note that the left-
   * and right-hand side expressions can also be logical expressions, thus
   * creating logical trees representing structures such as (a and (b or c) and
   * d), etc...
   */
  public LogicalExpr(int op, Expression left, Expression right) {
    _op = op;
    (_left = left).setParent(this);
    (_right = right).setParent(this);
  }

  /**
   * Returns true if this expressions contains a call to position(). This is
   * needed for context changes in node steps containing multiple predicates.
   */
  @Override
  public boolean hasPositionCall() {
    return _left.hasPositionCall() || _right.hasPositionCall();
  }

  /**
   * Returns true if this expressions contains a call to last()
   */
  @Override
  public boolean hasLastCall() {
    return _left.hasLastCall() || _right.hasLastCall();
  }

  /**
   * Returns an object representing the compile-time evaluation of an
   * expression. We are only using this for function-available and
   * element-available at this time.
   */
  @Override
  public Object evaluateAtCompileTime() {
    final Object leftb = _left.evaluateAtCompileTime();
    final Object rightb = _right.evaluateAtCompileTime();

    // Return null if we can't evaluate at compile time
    if (leftb == null || rightb == null)
      return null;

    if (_op == AND)
      return leftb == Boolean.TRUE && rightb == Boolean.TRUE ? Boolean.TRUE : Boolean.FALSE;
    else
      return leftb == Boolean.TRUE || rightb == Boolean.TRUE ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Returns this logical expression's operator - OR or AND represented by 0 and
   * 1 respectively.
   */
  public int getOp() {
    return _op;
  }

  /**
   * Override the SyntaxTreeNode.setParser() method to make sure that the parser
   * is set for sub-expressions
   */
  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _left.setParser(parser);
    _right.setParser(parser);
  }

  /**
   * Returns a string describing this expression
   */
  @Override
  public String toString() {
    return Ops[_op] + '(' + _left + ", " + _right + ')';
  }

  /**
   * Type-check this expression, and possibly child expressions.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // Get the left and right operand types
    final Type tleft = _left.typeCheck(stable);
    final Type tright = _right.typeCheck(stable);

    // Check if the operator supports the two operand types
    final MethodType wantType = new MethodType(Type.Void, tleft, tright);
    final MethodType haveType = lookupPrimop(stable, Ops[_op], wantType);

    // Yes, the operation is supported
    if (haveType != null) {
      // Check if left-hand side operand must be type casted
      final Type arg1 = haveType.argsType().get(0);
      if (!arg1.identicalTo(tleft)) {
        _left = new CastExpr(_left, arg1);
      }
      // Check if right-hand side operand must be type casted
      final Type arg2 = haveType.argsType().get(1);
      if (!arg2.identicalTo(tright)) {
        _right = new CastExpr(_right, arg1);
      }
      // Return the result type for the operator we will use
      return _type = haveType.resultType();
    }
    throw new TypeCheckError(this);
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression left = _left.toJExpression(ctx);
    JExpression right = _right.toJExpression(ctx);
    return _op == AND ? cand(left, right) : JOp.cor(left, right);
  }

}
