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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.ObjectFactory;
import de.lyca.xalan.SecuritySupport;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.objects.XString;
import de.lyca.xpath.res.Messages;

/**
 * Execute the SystemProperty() function.
 */
public class FuncSystemProperty extends FunctionOneArg {
  static final long serialVersionUID = 3694874980992204867L;
  /**
   * The path/filename of the property file: XSLTInfo.properties Maintenance note: see also
   * de.lyca.xalan.processor.TransformerFactoryImpl.XSLT_PROPERTIES
   */
  static final String XSLT_PROPERTIES = "de/lyca/xalan/res/XSLTInfo.properties";

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt The current execution context.
   * @return A valid XObject.
   * 
   * @throws TransformerException TODO
   */
  @Override
  public XObject execute(XPathContext xctxt) throws TransformerException {

    final String fullName = m_arg0.execute(xctxt).str();
    final int indexOfNSSep = fullName.indexOf(':');
    String result = null;
    String propName = "";

    // List of properties where the name of the
    // property argument is to be looked for.
    final Properties xsltInfo = new Properties();

    loadPropertyFile(XSLT_PROPERTIES, xsltInfo);

    if (indexOfNSSep > 0) {
      final String prefix = indexOfNSSep >= 0 ? fullName.substring(0, indexOfNSSep) : "";
      String namespace;

      namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
      propName = indexOfNSSep < 0 ? fullName : fullName.substring(indexOfNSSep + 1);

      if (namespace.startsWith("http://www.w3.org/XSL/Transform")
          || namespace.equals("http://www.w3.org/1999/XSL/Transform")) {
        result = xsltInfo.getProperty(propName);

        if (null == result) {
          warn(xctxt, Messages.get().propertyNotSupported(fullName));

          return XString.EMPTY;
        }
      } else {
        warn(xctxt, Messages.get().dontDoAnythingWithNs(namespace, fullName));

        try {
          // if secure procession is enabled only handle required properties do not not map any valid system property
          if (!xctxt.isSecureProcessing()) {
            result = System.getProperty(propName);
          } else {
            warn(xctxt, Messages.get().securityException(fullName));
          }
          if (null == result) {
            return XString.EMPTY;
          }
        } catch (final SecurityException se) {
          warn(xctxt, Messages.get().securityException(fullName));

          return XString.EMPTY;
        }
      }
    } else {
      try {
        // if secure procession is enabled only handle required properties do not not map any valid system property
        if (!xctxt.isSecureProcessing()) {
          result = System.getProperty(fullName);
        } else {
          warn(xctxt, Messages.get().securityException(fullName));
        }
        if (null == result) {
          return XString.EMPTY;
        }
      } catch (final SecurityException se) {
        warn(xctxt, Messages.get().securityException(fullName));

        return XString.EMPTY;
      }
    }

    if (propName.equals("version") && result.length() > 0) {
      try {
        // Needs to return the version number of the spec we conform to.
        return new XString("1.0");
      } catch (final Exception ex) {
        return new XString(result);
      }
    } else
      return new XString(result);
  }

  /**
   * Retrieve a propery bundle from a specified file
   * 
   * @param file The string name of the property file. The name should already be fully qualified as path/filename
   * @param target The target property bag the file will be placed into.
   */
  public void loadPropertyFile(String file, Properties target) {
    try {
      // Use SecuritySupport class to provide privileged access to property file
      final InputStream is = SecuritySupport.getResourceAsStream(ObjectFactory.findClassLoader(), file);

      // get a buffered version
      final BufferedInputStream bis = new BufferedInputStream(is);

      target.load(bis); // and load up the property bag from this
      bis.close(); // close out after reading
    } catch (final Exception ex) {
      // ex.printStackTrace();
      throw new de.lyca.xml.utils.WrappedRuntimeException(ex);
    }
  }
}
