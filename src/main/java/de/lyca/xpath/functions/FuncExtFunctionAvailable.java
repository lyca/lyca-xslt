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

import de.lyca.xpath.ExtensionsProvider;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.compiler.FunctionTable;
import de.lyca.xpath.objects.XBoolean;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the ExtFunctionAvailable() function.
 */
public class FuncExtFunctionAvailable extends FunctionOneArg {
  static final long serialVersionUID = 5118814314918592241L;

  transient private FunctionTable m_functionTable = null;

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

    String prefix;
    String namespace;
    String methName;

    final String fullName = m_arg0.execute(xctxt).str();
    final int indexOfNSSep = fullName.indexOf(':');

    if (indexOfNSSep < 0) {
      prefix = "";
      namespace = de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL;
      methName = fullName;
    } else {
      prefix = fullName.substring(0, indexOfNSSep);
      namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
      if (null == namespace)
        return XBoolean.S_FALSE;
      methName = fullName.substring(indexOfNSSep + 1);
    }

    if (namespace.equals(de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL)) {
      try {
        if (null == m_functionTable) {
          m_functionTable = new FunctionTable();
        }
        return m_functionTable.functionAvailable(methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
      } catch (final Exception e) {
        return XBoolean.S_FALSE;
      }
    } else {
      // dml
      final ExtensionsProvider extProvider = (ExtensionsProvider) xctxt.getOwnerObject();
      return extProvider.functionAvailable(namespace, methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
  }

  /**
   * The function table is an instance field. In order to access this instance
   * field during evaluation, this method is called at compilation time to
   * insert function table information for later usage. It should only be used
   * during compiling of XPath expressions.
   * 
   * @param aTable
   *          an instance of the function table
   */
  public void setFunctionTable(FunctionTable aTable) {
    m_functionTable = aTable;
  }
}
