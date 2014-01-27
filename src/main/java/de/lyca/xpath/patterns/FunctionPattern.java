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
package de.lyca.xpath.patterns;

import java.util.List;

import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.objects.XNumber;
import de.lyca.xpath.objects.XObject;

/**
 * Match pattern step that contains a function.
 * 
 * @xsl.usage advanced
 */
public class FunctionPattern extends StepPattern {
  static final long serialVersionUID = -5426793413091209944L;

  /**
   * Construct a FunctionPattern from a {@link de.lyca.xpath.functions.Function
   * expression}.
   * 
   * NEEDSDOC @param expr
   */
  public FunctionPattern(Expression expr, int axis, int predaxis) {

    super(0, null, null, axis, predaxis);

    m_functionExpr = expr;
  }

  /**
   * Static calc of match score.
   */
  @Override
  public final void calcScore() {

    m_score = SCORE_OTHER;

    if (null == m_targetString) {
      calcTargetString();
    }
  }

  /**
   * Should be a {@link de.lyca.xpath.functions.Function expression}.
   * 
   * @serial
   */
  Expression m_functionExpr;

  /**
   * This function is used to fixup variables from QNames to stack frame indexes
   * at stylesheet build time.
   * 
   * @param vars
   *          List of QNames that correspond to variables. This list should be
   *          searched backwards for the first qualified name that corresponds
   *          to the variable reference qname. The position of the QName in the
   *          list from the start of the list will be its position in the stack
   *          frame (but variables above the globalsTop value will need to be
   *          offset to the current stack frame).
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    super.fixupVariables(vars, globalsSize);
    m_functionExpr.fixupVariables(vars, globalsSize);
  }

  /**
   * Test a node to see if it matches the given node test.
   * 
   * @param xctxt
   *          XPath runtime context.
   * 
   * @return {@link de.lyca.xpath.patterns.NodeTest#SCORE_NODETEST},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NONE},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NSWILD},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_QNAME}, or
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_OTHER}.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt, int context) throws javax.xml.transform.TransformerException {

    final DTMIterator nl = m_functionExpr.asIterator(xctxt, context);
    XNumber score = SCORE_NONE;

    if (null != nl) {
      int n;

      while (DTM.NULL != (n = nl.nextNode())) {
        score = n == context ? SCORE_OTHER : SCORE_NONE;

        if (score == SCORE_OTHER) {
          context = n;

          break;
        }
      }

      // nl.detach();
    }
    nl.detach();

    return score;
  }

  /**
   * Test a node to see if it matches the given node test.
   * 
   * @param xctxt
   *          XPath runtime context.
   * 
   * @return {@link de.lyca.xpath.patterns.NodeTest#SCORE_NODETEST},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NONE},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NSWILD},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_QNAME}, or
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_OTHER}.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt, int context, DTM dtm, int expType)
          throws javax.xml.transform.TransformerException {

    final DTMIterator nl = m_functionExpr.asIterator(xctxt, context);
    XNumber score = SCORE_NONE;

    if (null != nl) {
      int n;

      while (DTM.NULL != (n = nl.nextNode())) {
        score = n == context ? SCORE_OTHER : SCORE_NONE;

        if (score == SCORE_OTHER) {
          context = n;

          break;
        }
      }

      nl.detach();
    }

    return score;
  }

  /**
   * Test a node to see if it matches the given node test.
   * 
   * @param xctxt
   *          XPath runtime context.
   * 
   * @return {@link de.lyca.xpath.patterns.NodeTest#SCORE_NODETEST},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NONE},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NSWILD},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_QNAME}, or
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_OTHER}.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {

    int context = xctxt.getCurrentNode();
    final DTMIterator nl = m_functionExpr.asIterator(xctxt, context);
    XNumber score = SCORE_NONE;

    if (null != nl) {
      int n;

      while (DTM.NULL != (n = nl.nextNode())) {
        score = n == context ? SCORE_OTHER : SCORE_NONE;

        if (score == SCORE_OTHER) {
          context = n;

          break;
        }
      }

      nl.detach();
    }

    return score;
  }

  class FunctionOwner implements ExpressionOwner {
    /**
     * @see ExpressionOwner#getExpression()
     */
    @Override
    public Expression getExpression() {
      return m_functionExpr;
    }

    /**
     * @see ExpressionOwner#setExpression(Expression)
     */
    @Override
    public void setExpression(Expression exp) {
      exp.exprSetParent(FunctionPattern.this);
      m_functionExpr = exp;
    }
  }

  /**
   * Call the visitor for the function.
   */
  @Override
  protected void callSubtreeVisitors(XPathVisitor visitor) {
    m_functionExpr.callVisitors(new FunctionOwner(), visitor);
    super.callSubtreeVisitors(visitor);
  }

}
