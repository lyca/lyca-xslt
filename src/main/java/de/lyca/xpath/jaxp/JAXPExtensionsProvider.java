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

// $Id$

package de.lyca.xpath.jaxp;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import de.lyca.xml.utils.WrappedRuntimeException;
import de.lyca.xpath.ExtensionsProvider;
import de.lyca.xpath.functions.FuncExtFunction;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.XPATHErrorResources;
import de.lyca.xpath.res.XPATHMessages;

/**
 * 
 * @author Ramesh Mandava ( ramesh.mandava@sun.com )
 */
public class JAXPExtensionsProvider implements ExtensionsProvider {

  private final XPathFunctionResolver resolver;
  private boolean extensionInvocationDisabled = false;

  public JAXPExtensionsProvider(XPathFunctionResolver resolver) {
    this.resolver = resolver;
    extensionInvocationDisabled = false;
  }

  public JAXPExtensionsProvider(XPathFunctionResolver resolver, boolean featureSecureProcessing) {
    this.resolver = resolver;
    extensionInvocationDisabled = featureSecureProcessing;
  }

  /**
   * Is the extension function available?
   */

  @Override
  public boolean functionAvailable(String ns, String funcName) throws TransformerException {
    try {
      if (funcName == null) {
        final String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                new Object[] { "Function Name" });
        throw new NullPointerException(fmsg);
      }
      // Find the XPathFunction corresponding to namespace and funcName
      final QName myQName = new QName(ns, funcName);
      final XPathFunction xpathFunction = resolver.resolveFunction(myQName, 0);
      if (xpathFunction == null)
        return false;
      return true;
    } catch (final Exception e) {
      return false;
    }

  }

  /**
   * Is the extension element available?
   */
  @Override
  public boolean elementAvailable(String ns, String elemName) throws TransformerException {
    return false;
  }

  /**
   * Execute the extension function.
   */
  @Override
  public Object extFunction(String ns, String funcName, List<?> argVec, Object methodKey) throws TransformerException {
    try {

      if (funcName == null) {
        final String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                new Object[] { "Function Name" });
        throw new NullPointerException(fmsg);
      }
      // Find the XPathFunction corresponding to namespace and funcName
      final QName myQName = new QName(ns, funcName);

      // JAXP 1.3 spec says When XMLConstants.FEATURE_SECURE_PROCESSING
      // feature is set then invocation of extension functions need to
      // throw XPathFunctionException
      if (extensionInvocationDisabled) {
        final String fmsg = XPATHMessages.createXPATHMessage(
                XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED, new Object[] { myQName.toString() });
        throw new XPathFunctionException(fmsg);
      }

      // Assuming user is passing all the needed parameters ( including
      // default values )
      final int arity = argVec.size();

      final XPathFunction xpathFunction = resolver.resolveFunction(myQName, arity);

      // not using methodKey
      final List<Object> argList = new ArrayList<>(arity);
      for (int i = 0; i < arity; i++) {
        final Object argument = argVec.get(i);
        // XNodeSet object() returns NodeVector and not NodeList
        // Explicitly getting NodeList by using nodelist()
        if (argument instanceof XNodeSet) {
          argList.add(i, ((XNodeSet) argument).nodelist());
        } else if (argument instanceof XObject) {
          final Object passedArgument = ((XObject) argument).object();
          argList.add(i, passedArgument);
        } else {
          argList.add(i, argument);
        }
      }

      return xpathFunction.evaluate(argList);
    } catch (final XPathFunctionException xfe) {
      // If we get XPathFunctionException then we want to terminate
      // further execution by throwing WrappedRuntimeException
      throw new WrappedRuntimeException(xfe);
    } catch (final Exception e) {
      throw new TransformerException(e);
    }

  }

  /**
   * Execute the extension function.
   */
  @Override
  public Object extFunction(FuncExtFunction extFunction, List<?> argVec) throws TransformerException {
    try {
      final String namespace = extFunction.getNamespace();
      final String functionName = extFunction.getFunctionName();
      final int arity = extFunction.getArgCount();
      final QName myQName = new QName(namespace, functionName);

      // JAXP 1.3 spec says When XMLConstants.FEATURE_SECURE_PROCESSING
      // feature is set then invocation of extension functions need to
      // throw XPathFunctionException
      if (extensionInvocationDisabled) {
        final String fmsg = XPATHMessages.createXPATHMessage(
                XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED, new Object[] { myQName.toString() });
        throw new XPathFunctionException(fmsg);
      }

      final XPathFunction xpathFunction = resolver.resolveFunction(myQName, arity);

      final List<Object> argList = new ArrayList<>(arity);
      for (int i = 0; i < arity; i++) {
        final Object argument = argVec.get(i);
        // XNodeSet object() returns NodeVector and not NodeList
        // Explicitly getting NodeList by using nodelist()
        if (argument instanceof XNodeSet) {
          argList.add(i, ((XNodeSet) argument).nodelist());
        } else if (argument instanceof XObject) {
          final Object passedArgument = ((XObject) argument).object();
          argList.add(i, passedArgument);
        } else {
          argList.add(i, argument);
        }
      }

      return xpathFunction.evaluate(argList);

    } catch (final XPathFunctionException xfe) {
      // If we get XPathFunctionException then we want to terminate
      // further execution by throwing WrappedRuntimeException
      throw new WrappedRuntimeException(xfe);
    } catch (final Exception e) {
      throw new TransformerException(e);
    }
  }

}
