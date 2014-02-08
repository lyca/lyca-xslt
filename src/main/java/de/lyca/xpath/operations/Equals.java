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
package de.lyca.xpath.operations;

import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XBoolean;
import de.lyca.xpath.objects.XObject;

/**
 * The '=' operation expression executer.
 */
public class Equals extends Operation {
  static final long serialVersionUID = -2658315633903426134L;

  /**
   * Apply the operation to two operands, and return the result.
   * 
   * 
   * @param left
   *          non-null reference to the evaluated left operand.
   * @param right
   *          non-null reference to the evaluated right operand.
   * 
   * @return non-null reference to the XObject that represents the result of the
   *         operation.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject operate(XObject left, XObject right) throws javax.xml.transform.TransformerException {
    return left.equals(right) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }

  /**
   * Execute a binary operation by calling execute on each of the operands, and
   * then calling the operate method on the derived class.
   * 
   * 
   * @param xctxt
   *          The runtime execution context.
   * 
   * @return The XObject result of the operation.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public boolean bool(XPathContext xctxt) throws javax.xml.transform.TransformerException {
    final XObject left = m_left.execute(xctxt, true);
    final XObject right = m_right.execute(xctxt, true);

    final boolean result = left.equals(right) ? true : false;
    left.detach();
    right.detach();
    return result;
  }

}