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

import java.io.IOException;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.templates.Constants;
import de.lyca.xalan.templates.ElemExsltFuncResult;
import de.lyca.xalan.templates.ElemExsltFunction;
import de.lyca.xalan.templates.ElemTemplate;
import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xalan.templates.Stylesheet;
import de.lyca.xalan.templates.StylesheetRoot;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.ExpressionNode;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.functions.FuncExtFunction;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.objects.XString;

/**
 * Execute EXSLT functions, determine the availability of EXSLT functions, and
 * the availability of an EXSLT result element.
 */
public class ExtensionHandlerExsltFunction extends ExtensionHandler {
  private final String m_namespace;
  private final StylesheetRoot m_stylesheet;
  private static final QName RESULTQNAME = new QName(Constants.S_EXSLT_FUNCTIONS_URL,
          Constants.EXSLT_ELEMNAME_FUNCRESULT_STRING);

  /**
   * Constructor called from ElemExsltFunction runtimeInit().
   */
  public ExtensionHandlerExsltFunction(String ns, StylesheetRoot stylesheet) {
    super(ns, "xml"); // required by ExtensionHandler interface.
    m_namespace = ns;
    m_stylesheet = stylesheet;
  }

  /**
   * Required by ExtensionHandler (an abstract method). No-op.
   */
  @Override
  public void processElement(String localPart, ElemTemplateElement element, TransformerImpl transformer,
          Stylesheet stylesheetTree, Object methodKey) throws TransformerException, IOException {
  }

  /**
   * Get the ElemExsltFunction element associated with the function.
   * 
   * @param funcName
   *          Local name of the function.
   * @return the ElemExsltFunction element associated with the function, null if
   *         none exists.
   */
  public ElemExsltFunction getFunction(String funcName) {
    final QName qname = new QName(m_namespace, funcName);
    final ElemTemplate templ = m_stylesheet.getTemplateComposed(qname);
    if (templ != null && templ instanceof ElemExsltFunction)
      return (ElemExsltFunction) templ;
    else
      return null;
  }

  /**
   * Does the EXSLT function exist?
   * 
   * @param funcName
   *          Local name of the function.
   * @return true if the function exists.
   */
  @Override
  public boolean isFunctionAvailable(String funcName) {
    return getFunction(funcName) != null;
  }

  /**
   * If an element-available() call applies to an EXSLT result element within an
   * EXSLT function element, return true.
   * 
   * Note: The EXSLT function element is a template-level element, and
   * element-available() returns false for it.
   * 
   * @param elemName
   *          name of the element.
   * @return true if the function is available.
   */
  @Override
  public boolean isElementAvailable(String elemName) {
    if (!new QName(m_namespace, elemName).equals(RESULTQNAME))
      return false;
    else {
      ElemTemplateElement elem = m_stylesheet.getFirstChildElem();
      while (elem != null && elem != m_stylesheet) {
        if (elem instanceof ElemExsltFuncResult && ancestorIsFunction(elem))
          return true;
        ElemTemplateElement nextElem = elem.getFirstChildElem();
        if (nextElem == null) {
          nextElem = elem.getNextSiblingElem();
        }
        if (nextElem == null) {
          nextElem = elem.getParentElem();
        }
        elem = nextElem;
      }
    }
    return false;
  }

  /**
   * Determine whether the func:result element is within a func:function
   * element. If not, it is illegal.
   */
  private boolean ancestorIsFunction(ElemTemplateElement child) {
    while (child.getParentElem() != null && !(child.getParentElem() instanceof StylesheetRoot)) {
      if (child.getParentElem() instanceof ElemExsltFunction)
        return true;
      child = child.getParentElem();
    }
    return false;
  }

  /**
   * Execute the EXSLT function and return the result value.
   * 
   * @param funcName
   *          Name of the EXSLT function.
   * @param args
   *          The arguments of the function call.
   * @param methodKey
   *          Not used.
   * @param exprContext
   *          Used to get the XPathContext.
   * @return the return value of the function evaluation.
   * @throws TransformerException
   */
  @Override
  public Object callFunction(String funcName, Vector args, Object methodKey, ExpressionContext exprContext)
          throws TransformerException {
    throw new TransformerException("This method should not be called.");
  }

  /**
   * Execute the EXSLT function and return the result value.
   * 
   * @param extFunction
   *          The XPath extension function
   * @param args
   *          The arguments of the function call.
   * @param exprContext
   *          The context in which this expression is being executed.
   * @return the return value of the function evaluation.
   * @throws TransformerException
   */
  @Override
  public Object callFunction(FuncExtFunction extFunction, Vector args, ExpressionContext exprContext)
          throws TransformerException {
    // Find the template which invokes this EXSLT function.
    ExpressionNode parent = extFunction.exprGetParent();
    while (parent != null && !(parent instanceof ElemTemplate)) {
      parent = parent.exprGetParent();
    }

    final ElemTemplate callerTemplate = parent != null ? (ElemTemplate) parent : null;

    XObject[] methodArgs;
    methodArgs = new XObject[args.size()];
    try {
      for (int i = 0; i < methodArgs.length; i++) {
        methodArgs[i] = XObject.create(args.get(i));
      }

      final ElemExsltFunction elemFunc = getFunction(extFunction.getFunctionName());

      if (null != elemFunc) {
        final XPathContext context = exprContext.getXPathContext();
        final TransformerImpl transformer = (TransformerImpl) context.getOwnerObject();
        transformer.pushCurrentFuncResult(null);

        elemFunc.execute(transformer, methodArgs);

        final XObject val = (XObject) transformer.popCurrentFuncResult();
        return val == null ? new XString("") // value if no result element.
                : val;
      } else
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_FUNCTION_NOT_FOUND,
                new Object[] { extFunction.getFunctionName() }));
    } catch (final TransformerException e) {
      throw e;
    } catch (final Exception e) {
      throw new TransformerException(e);
    }
  }

}
