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

import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XNumber;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the round() function.
 */
public class FuncRound extends FunctionOneArg {
  static final long serialVersionUID = -7970583902573826611L;

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws TransformerException TODO
   */
  @Override
  public XObject execute(XPathContext xctxt) throws TransformerException {
    final XObject obj = m_arg0.execute(xctxt);
    final double val = obj.num();
    if (val >= -0.5 && val < 0)
      return new XNumber(-0.0);
    if (val == 0.0)
      return new XNumber(val);
    return new XNumber(java.lang.Math.floor(val + 0.5));
  }
}
