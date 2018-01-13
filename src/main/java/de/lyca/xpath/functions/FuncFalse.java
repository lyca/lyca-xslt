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

import javax.xml.transform.TransformerException;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XBoolean;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the False() function.
 */
public class FuncFalse extends Function {
  static final long serialVersionUID = 6150918062759769887L;

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
    return XBoolean.S_FALSE;
  }

  /**
   * No arguments to process, so this does nothing.
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    // no-op
  }

}
