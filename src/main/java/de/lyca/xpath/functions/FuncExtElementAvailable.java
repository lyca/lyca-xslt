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
package de.lyca.xpath.functions;

import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.ExtensionsProvider;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XBoolean;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the ExtElementAvailable() function.
 * 
 * @xsl.usage advanced
 */
public class FuncExtElementAvailable extends FunctionOneArg {
  static final long serialVersionUID = -472533699257968546L;

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {

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

    if (namespace.equals(de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL)
            || namespace.equals(de.lyca.xml.utils.Constants.S_BUILTIN_EXTENSIONS_URL)) {
      try {
        final TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
        return transformer.getStylesheet().getAvailableElements().containsKey(new QName(namespace, methName)) ? XBoolean.S_TRUE
                : XBoolean.S_FALSE;
      } catch (final Exception e) {
        return XBoolean.S_FALSE;
      }
    } else {
      // dml
      final ExtensionsProvider extProvider = (ExtensionsProvider) xctxt.getOwnerObject();
      return extProvider.elementAvailable(namespace, methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
  }
}
