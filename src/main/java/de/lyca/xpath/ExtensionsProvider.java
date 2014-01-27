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
package de.lyca.xpath;

import java.util.List;

import javax.xml.transform.TransformerException;

import de.lyca.xpath.functions.FuncExtFunction;

/**
 * Interface that XPath objects can call to obtain access to an ExtensionsTable.
 * 
 */
public interface ExtensionsProvider {
  /**
   * Is the extension function available?
   */

  public boolean functionAvailable(String ns, String funcName) throws TransformerException;

  /**
   * Is the extension element available?
   */
  public boolean elementAvailable(String ns, String elemName) throws TransformerException;

  /**
   * Execute the extension function.
   */
  public Object extFunction(String ns, String funcName, List argVec, Object methodKey) throws TransformerException;

  /**
   * Execute the extension function.
   */
  public Object extFunction(FuncExtFunction extFunction, List argVec) throws TransformerException;

}
