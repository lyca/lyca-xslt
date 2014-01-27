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
package de.lyca.xalan.extensions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xalan.templates.Stylesheet;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xpath.functions.FuncExtFunction;

/**
 * Abstract base class for handling an extension namespace for XPath. Provides
 * functions to test a function's existence and call a function. Also provides
 * functions for calling an element and testing for an element's existence.
 * 
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @xsl.usage internal
 */
public abstract class ExtensionHandler {

  /** uri of the extension namespace */
  protected String m_namespaceUri;

  /** scripting language of implementation */
  protected String m_scriptLang;

  /**
   * This method loads a class using the context class loader if we're running
   * under Java2 or higher.
   * 
   * @param className
   *          Name of the class to load
   */
  static Class getClassForName(String className) throws ClassNotFoundException {
    // Hack for backwards compatibility with XalanJ1 stylesheets
    if (className.equals("de.lyca.xalan.xslt.extensions.Redirect")) {
      className = "de.lyca.xalan.lib.Redirect";
    }

    return ObjectFactory.findProviderClass(className, ObjectFactory.findClassLoader(), true);
  }

  /**
   * Construct a new extension namespace handler given all the information
   * needed.
   * 
   * @param namespaceUri
   *          the extension namespace URI that I'm implementing
   * @param scriptLang
   *          language of code implementing the extension
   */
  protected ExtensionHandler(String namespaceUri, String scriptLang) {
    m_namespaceUri = namespaceUri;
    m_scriptLang = scriptLang;
  }

  /**
   * Tests whether a certain function name is known within this namespace.
   * 
   * @param function
   *          name of the function being tested
   * @return true if its known, false if not.
   */
  public abstract boolean isFunctionAvailable(String function);

  /**
   * Tests whether a certain element name is known within this namespace.
   * 
   * @param element
   *          Name of element to check
   * @return true if its known, false if not.
   */
  public abstract boolean isElementAvailable(String element);

  /**
   * Process a call to a function.
   * 
   * @param funcName
   *          Function name.
   * @param args
   *          The arguments of the function call.
   * @param methodKey
   *          A key that uniquely identifies this class and method call.
   * @param exprContext
   *          The context in which this expression is being executed.
   * 
   * @return the return value of the function evaluation.
   * 
   * @throws TransformerException
   *           if parsing trouble
   */
  public abstract Object callFunction(String funcName, Vector args, Object methodKey, ExpressionContext exprContext)
          throws TransformerException;

  /**
   * Process a call to a function.
   * 
   * @param extFunction
   *          The XPath extension function.
   * @param args
   *          The arguments of the function call.
   * @param exprContext
   *          The context in which this expression is being executed.
   * 
   * @return the return value of the function evaluation.
   * 
   * @throws TransformerException
   *           if parsing trouble
   */
  public abstract Object callFunction(FuncExtFunction extFunction, Vector args, ExpressionContext exprContext)
          throws TransformerException;

  /**
   * Process a call to this extension namespace via an element. As a side
   * effect, the results are sent to the TransformerImpl's result tree.
   * 
   * @param localPart
   *          Element name's local part.
   * @param element
   *          The extension element being processed.
   * @param transformer
   *          Handle to TransformerImpl.
   * @param stylesheetTree
   *          The compiled stylesheet tree.
   * @param methodKey
   *          A key that uniquely identifies this class and method call.
   * 
   * @throws XSLProcessorException
   *           thrown if something goes wrong while running the extension
   *           handler.
   * @throws MalformedURLException
   *           if loading trouble
   * @throws FileNotFoundException
   *           if loading trouble
   * @throws IOException
   *           if loading trouble
   * @throws TransformerException
   *           if parsing trouble
   */
  public abstract void processElement(String localPart, ElemTemplateElement element, TransformerImpl transformer,
          Stylesheet stylesheetTree, Object methodKey) throws TransformerException, IOException;
}
