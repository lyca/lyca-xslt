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

import javax.xml.transform.TransformerException;

import de.lyca.xml.dtm.DTM;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XString;
import de.lyca.xpath.res.Messages;

/**
 * Base class for functions that accept one argument that can be defaulted if not specified.
 */
public class FunctionDef1Arg extends FunctionOneArg {
  static final long serialVersionUID = 2325189412814149264L;

  /**
   * Execute the first argument expression that is expected to return a nodeset. If the argument is null, then return
   * the current context node.
   * 
   * @param xctxt Runtime XPath context.
   * @return The first node of the executed nodeset, or the current context node if the first argument is null.
   * @throws TransformerException if an error occurs while executing the argument expression.
   */
  protected int getArg0AsNode(XPathContext xctxt) throws TransformerException {

    return null == m_arg0 ? xctxt.getCurrentNode() : m_arg0.asNode(xctxt);
  }

  /**
   * Tell if the expression is a nodeset expression.
   * 
   * @return true if the expression can be represented as a nodeset.
   */
  public boolean Arg0IsNodesetExpr() {
    return null == m_arg0 ? true : m_arg0.isNodesetExpr();
  }

  /**
   * Execute the first argument expression that is expected to return a string. If the argument is null, then get the
   * string value from the current context node.
   * 
   * @param xctxt Runtime XPath context.
   * @return The string value of the first argument, or the string value of the current context node if the first
   *         argument is null.
   * @throws TransformerException if an error occurs while executing the argument expression.
   */
  protected XMLString getArg0AsString(XPathContext xctxt) throws TransformerException {
    if (null == m_arg0) {
      final int currentNode = xctxt.getCurrentNode();
      if (DTM.NULL == currentNode)
        return XString.EMPTY;
      else {
        final DTM dtm = xctxt.getDTM(currentNode);
        return dtm.getStringValue(currentNode);
      }

    } else
      return m_arg0.execute(xctxt).xstr();
  }

  /**
   * Execute the first argument expression that is expected to return a number. If the argument is null, then get the
   * number value from the current context node.
   * 
   * @param xctxt Runtime XPath context.
   * @return The number value of the first argument, or the number value of the current context node if the first
   *         argument is null.
   * @throws TransformerException if an error occurs while executing the argument expression.
   */
  protected double getArg0AsNumber(XPathContext xctxt) throws TransformerException {

    if (null == m_arg0) {
      final int currentNode = xctxt.getCurrentNode();
      if (DTM.NULL == currentNode)
        return 0;
      else {
        final DTM dtm = xctxt.getDTM(currentNode);
        final XMLString str = dtm.getStringValue(currentNode);
        return str.toDouble();
      }

    } else
      return m_arg0.execute(xctxt).num();
  }

  /**
   * Check that the number of arguments passed to this function is correct.
   * 
   * @param argNum The number of arguments that is being passed to the function.
   * @throws WrongNumberArgsException if the number of arguments is not 0 or 1.
   */
  @Override
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
    if (argNum > 1) {
      reportWrongNumberArgs();
    }
  }

  /**
   * Constructs and throws a WrongNumberArgException with the appropriate message for this function object.
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
    throw new WrongNumberArgsException(Messages.get().zeroOrOne());
  }

  /**
   * Tell if this expression or it's subexpressions can traverse outside the current subtree.
   * 
   * @return true if traversal outside the context node's subtree can occur.
   */
  @Override
  public boolean canTraverseOutsideSubtree() {
    return null == m_arg0 ? false : super.canTraverseOutsideSubtree();
  }
}
