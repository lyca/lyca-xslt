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
import de.lyca.xpath.objects.XBoolean;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the Contains() function.
 */
public class FuncContains extends Function2Args {
  static final long serialVersionUID = 5084753781887919723L;

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

    final String s1 = m_arg0.execute(xctxt).str();
    final String s2 = m_arg1.execute(xctxt).str();

    // Add this check for JDK consistency for empty strings.
    if (s1.length() == 0 && s2.length() == 0)
      return XBoolean.S_TRUE;

    final int index = s1.indexOf(s2);

    return index > -1 ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
