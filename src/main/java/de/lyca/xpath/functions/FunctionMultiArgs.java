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
package de.lyca.xpath.functions;

import java.util.List;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.res.Messages;

/**
 * Base class for functions that accept an undetermined number of multiple arguments.
 */
public class FunctionMultiArgs extends Function3Args {
  static final long serialVersionUID = 7117257746138417181L;

  /**
   * Argument expressions that are at index 3 or greater.
   * 
   * @serial
   */
  Expression[] m_args;

  /**
   * Return an expression array containing arguments at index 3 or greater.
   * 
   * @return An array that contains the arguments at index 3 or greater.
   */
  public Expression[] getArgs() {
    return m_args;
  }

  /**
   * Set an argument expression for a function. This method is called by the XPath compiler.
   * 
   * @param arg non-null expression that represents the argument.
   * @param argNum The argument number index.
   * @throws WrongNumberArgsException If a derived class determines that the number of arguments is incorrect.
   */
  @Override
  public void setArg(Expression arg, int argNum) throws WrongNumberArgsException {

    if (argNum < 3) {
      super.setArg(arg, argNum);
    } else {
      if (null == m_args) {
        m_args = new Expression[1];
        m_args[0] = arg;
      } else {

        // Slow but space conservative.
        final Expression[] args = new Expression[m_args.length + 1];

        System.arraycopy(m_args, 0, args, 0, m_args.length);

        args[m_args.length] = arg;
        m_args = args;
      }
      arg.exprSetParent(this);
    }
  }

  /**
   * This function is used to fixup variables from QNames to stack frame indexes at stylesheet build time.
   * 
   * @param vars List of QNames that correspond to variables. This list should be searched backwards for the first
   *        qualified name that corresponds to the variable reference qname. The position of the QName in the list from
   *        the start of the list will be its position in the stack frame (but variables above the globalsTop value will
   *        need to be offset to the current stack frame).
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    super.fixupVariables(vars, globalsSize);
    if (null != m_args) {
      for (int i = 0; i < m_args.length; i++) {
        m_args[i].fixupVariables(vars, globalsSize);
      }
    }
  }

  /**
   * Check that the number of arguments passed to this function is correct.
   * 
   * @param argNum The number of arguments that is being passed to the function.
   * @throws WrongNumberArgsException
   */
  @Override
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
  }

  /**
   * Constructs and throws a WrongNumberArgException with the appropriate message for this function object. This class
   * supports an arbitrary number of arguments, so this method must never be called.
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
    final String fMsg = Messages.get().incorrectProgrammerAssertion(
        "Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called.");

    throw new RuntimeException(fMsg);
  }

  /**
   * Tell if this expression or it's subexpressions can traverse outside the current subtree.
   * 
   * @return true if traversal outside the context node's subtree can occur.
   */
  @Override
  public boolean canTraverseOutsideSubtree() {

    if (super.canTraverseOutsideSubtree())
      return true;
    else {
      final int n = m_args.length;

      for (int i = 0; i < n; i++) {
        if (m_args[i].canTraverseOutsideSubtree())
          return true;
      }

      return false;
    }
  }

  class ArgMultiOwner implements ExpressionOwner {
    int m_argIndex;

    ArgMultiOwner(int index) {
      m_argIndex = index;
    }

    /**
     * @see ExpressionOwner#getExpression()
     */
    @Override
    public Expression getExpression() {
      return m_args[m_argIndex];
    }

    /**
     * @see ExpressionOwner#setExpression(Expression)
     */
    @Override
    public void setExpression(Expression exp) {
      exp.exprSetParent(FunctionMultiArgs.this);
      m_args[m_argIndex] = exp;
    }
  }

  /**
   * @see de.lyca.xpath.XPathVisitable#callVisitors(ExpressionOwner, XPathVisitor)
   */
  @Override
  public void callArgVisitors(XPathVisitor visitor) {
    super.callArgVisitors(visitor);
    if (null != m_args) {
      final int n = m_args.length;
      for (int i = 0; i < n; i++) {
        m_args[i].callVisitors(new ArgMultiOwner(i), visitor);
      }
    }
  }

  /**
   * @see Expression#deepEquals(Expression)
   */
  @Override
  public boolean deepEquals(Expression expr) {
    if (!super.deepEquals(expr))
      return false;

    final FunctionMultiArgs fma = (FunctionMultiArgs) expr;
    if (null != m_args) {
      final int n = m_args.length;
      if (null == fma || fma.m_args.length != n)
        return false;

      for (int i = 0; i < n; i++) {
        if (!m_args[i].deepEquals(fma.m_args[i]))
          return false;
      }

    } else if (null != fma.m_args)
      return false;

    return true;
  }
}
