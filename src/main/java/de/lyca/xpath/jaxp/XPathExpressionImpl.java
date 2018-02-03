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
package de.lyca.xpath.jaxp;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.Messages;

/**
 * The XPathExpression interface encapsulates a (compiled) XPath expression.
 * 
 * @version $Revision$
 * @author Ramesh Mandava
 */
public class XPathExpressionImpl implements javax.xml.xpath.XPathExpression {

  private XPathFunctionResolver functionResolver;
  private XPathVariableResolver variableResolver;
  private JAXPPrefixResolver prefixResolver;
  private de.lyca.xpath.XPath xpath;

  // By default Extension Functions are allowed in XPath Expressions. If
  // Secure Processing Feature is set on XPathFactory then the invocation of
  // extensions function need to throw XPathFunctionException
  private boolean featureSecureProcessing = false;

  /**
   * Protected constructor to prevent direct instantiation; use compile() from the context.
   */
  protected XPathExpressionImpl() {
  };

  protected XPathExpressionImpl(de.lyca.xpath.XPath xpath, JAXPPrefixResolver prefixResolver,
      XPathFunctionResolver functionResolver, XPathVariableResolver variableResolver) {
    this.xpath = xpath;
    this.prefixResolver = prefixResolver;
    this.functionResolver = functionResolver;
    this.variableResolver = variableResolver;
    featureSecureProcessing = false;
  };

  protected XPathExpressionImpl(de.lyca.xpath.XPath xpath, JAXPPrefixResolver prefixResolver,
      XPathFunctionResolver functionResolver, XPathVariableResolver variableResolver, boolean featureSecureProcessing) {
    this.xpath = xpath;
    this.prefixResolver = prefixResolver;
    this.functionResolver = functionResolver;
    this.variableResolver = variableResolver;
    this.featureSecureProcessing = featureSecureProcessing;
  };

  public void setXPath(de.lyca.xpath.XPath xpath) {
    this.xpath = xpath;
  }

  public Object eval(Object item, QName returnType) throws TransformerException {
    final XObject resultObject = eval(item);
    return getResultAsType(resultObject, returnType);
  }

  private XObject eval(Object contextItem) throws TransformerException {
    XPathContext xpathSupport = null;

    // Create an XPathContext that doesn't support pushing and popping of
    // variable resolution scopes. Sufficient for simple XPath 1.0
    // expressions.
    if (functionResolver != null) {
      final JAXPExtensionsProvider jep = new JAXPExtensionsProvider(functionResolver, featureSecureProcessing);
      xpathSupport = new XPathContext(jep, false);
    } else {
      xpathSupport = new XPathContext(false);
    }

    xpathSupport.setVarStack(new JAXPVariableStack(variableResolver));
    XObject xobj = null;

    Node contextNode = (Node) contextItem;
    // We always need to have a ContextNode with Xalan XPath implementation
    // To allow simple expression evaluation like 1+1 we are setting
    // dummy Document as Context Node
    if (contextNode == null) {
      contextNode = getDummyDocument();
    }

    xobj = xpath.execute(xpathSupport, contextNode, prefixResolver);
    return xobj;
  }

  /**
   * <p>
   * Evaluate the compiled XPath expression in the specified context and return the result as the specified type.
   * </p>
   * <p>
   * See "Evaluation of XPath Expressions" section of JAXP 1.3 spec for context item evaluation, variable, function and
   * QName resolution and return type conversion.
   * </p>
   * <p>
   * If <code>returnType</code> is not one of the types defined in {@link XPathConstants}, then an
   * <code>IllegalArgumentException</code> is thrown.
   * </p>
   * <p>
   * If a <code>null</code> value is provided for <code>item</code>, an empty document will be used for the context. If
   * <code>returnType</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.
   * </p>
   * 
   * @param item The starting context (node or node list, for example).
   * @param returnType The desired return type.
   * @return The <code>Object</code> that is the result of evaluating the expression and converting the result to
   *         <code>returnType</code>.
   * @throws XPathExpressionException If the expression cannot be evaluated.
   * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in
   *         {@link XPathConstants}.
   * @throws NullPointerException If <code>returnType</code> is <code>null</code>.
   */
  @Override
  public Object evaluate(Object item, QName returnType) throws XPathExpressionException {
    // Validating parameters to enforce constraints defined by JAXP spec
    if (returnType == null) {
      // Throwing NullPointerException as defined in spec
      final String fmsg = Messages.get().argCannotBeNull("returnType");
      throw new NullPointerException(fmsg);
    }
    // Checking if requested returnType is supported. returnType need to be
    // defined in XPathConstants
    if (!isSupported(returnType)) {
      final String fmsg = Messages.get().unsupportedReturnType(returnType);
      throw new IllegalArgumentException(fmsg);
    }
    try {
      return eval(item, returnType);
    } catch (final NullPointerException npe) {
      // If VariableResolver returns null Or if we get
      // NullPointerException at this stage for some other reason
      // then we have to reurn XPathException
      throw new XPathExpressionException(npe);
    } catch (final TransformerException te) {
      final Throwable nestedException = te.getException();
      if (nestedException instanceof XPathFunctionException)
        throw (XPathFunctionException) nestedException;
      else
        // For any other exceptions we need to throw
        // XPathExpressionException ( as per spec )
        throw new XPathExpressionException(te);
    }

  }

  /**
   * <p>
   * Evaluate the compiled XPath expression in the specified context and return the result as a <code>String</code>.
   * </p>
   * <p>
   * This method calls {@link #evaluate(Object item, QName returnType)} with a <code>returnType</code> of
   * {@link XPathConstants#STRING}.
   * </p>
   * <p>
   * See "Evaluation of XPath Expressions" section of JAXP 1.3 spec for context item evaluation, variable, function and
   * QName resolution and return type conversion.
   * </p>
   * <p>
   * If a <code>null</code> value is provided for <code>item</code>, an empty document will be used for the context.
   * 
   * @param item The starting context (node or node list, for example).
   * @return The <code>String</code> that is the result of evaluating the expression and converting the result to a
   *         <code>String</code>.
   * @throws XPathExpressionException If the expression cannot be evaluated.
   */
  @Override
  public String evaluate(Object item) throws XPathExpressionException {
    return (String) this.evaluate(item, XPathConstants.STRING);
  }

  static DocumentBuilderFactory dbf = null;
  static DocumentBuilder db = null;
  static Document d = null;

  /**
   * <p>
   * Evaluate the compiled XPath expression in the context of the specified <code>InputSource</code> and return the
   * result as the specified type.
   * </p>
   * <p>
   * This method builds a data model for the {@link InputSource} and calls
   * {@link #evaluate(Object item, QName returnType)} on the resulting document object.
   * </p>
   * <p>
   * See "Evaluation of XPath Expressions" section of JAXP 1.3 spec for context item evaluation, variable, function and
   * QName resolution and return type conversion.
   * </p>
   * <p>
   * If <code>returnType</code> is not one of the types defined in {@link XPathConstants}, then an
   * <code>IllegalArgumentException</code> is thrown.
   * </p>
   * <p>
   * If <code>source</code> or <code>returnType</code> is <code>null</code>, then a <code>NullPointerException</code> is
   * thrown.
   * </p>
   * 
   * @param source The <code>InputSource</code> of the document to evaluate over.
   * @param returnType The desired return type.
   * @return The <code>Object</code> that is the result of evaluating the expression and converting the result to
   *         <code>returnType</code>.
   * @throws XPathExpressionException If the expression cannot be evaluated.
   * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in
   *         {@link XPathConstants}.
   * @throws NullPointerException If <code>source</code> or <code>returnType</code> is <code>null</code>.
   */
  @Override
  public Object evaluate(InputSource source, QName returnType) throws XPathExpressionException {
    if (source == null || returnType == null) {
      final String fmsg = Messages.get().sourceReturnTypeCannotBeNull();
      throw new NullPointerException(fmsg);
    }
    // Checking if requested returnType is supported. returnType need to be
    // defined in XPathConstants
    if (!isSupported(returnType)) {
      final String fmsg = Messages.get().unsupportedReturnType(returnType);
      throw new IllegalArgumentException(fmsg);
    }
    try {
      if (dbf == null) {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
      }
      db = dbf.newDocumentBuilder();
      final Document document = db.parse(source);
      return eval(document, returnType);
    } catch (final Exception e) {
      throw new XPathExpressionException(e);
    }
  }

  /**
   * <p>
   * Evaluate the compiled XPath expression in the context of the specified <code>InputSource</code> and return the
   * result as a <code>String</code>.
   * </p>
   * <p>
   * This method calls {@link #evaluate(InputSource source, QName returnType)} with a <code>returnType</code> of
   * {@link XPathConstants#STRING}.
   * </p>
   * <p>
   * See "Evaluation of XPath Expressions" section of JAXP 1.3 spec for context item evaluation, variable, function and
   * QName resolution and return type conversion.
   * </p>
   * <p>
   * If <code>source</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.
   * </p>
   * 
   * @param source The <code>InputSource</code> of the document to evaluate over.
   * @return The <code>String</code> that is the result of evaluating the expression and converting the result to a
   *         <code>String</code>.
   * @throws XPathExpressionException If the expression cannot be evaluated.
   * @throws NullPointerException If <code>source</code> is <code>null</code>.
   */
  @Override
  public String evaluate(InputSource source) throws XPathExpressionException {
    return (String) this.evaluate(source, XPathConstants.STRING);
  }

  private boolean isSupported(QName returnType) {
    // XPathConstants.STRING
    if (returnType.equals(XPathConstants.STRING) || returnType.equals(XPathConstants.NUMBER)
        || returnType.equals(XPathConstants.BOOLEAN) || returnType.equals(XPathConstants.NODE)
        || returnType.equals(XPathConstants.NODESET))
      return true;
    return false;
  }

  private Object getResultAsType(XObject resultObject, QName returnType) throws TransformerException {
    // XPathConstants.STRING
    if (returnType.equals(XPathConstants.STRING))
      return resultObject.str();
    // XPathConstants.NUMBER
    if (returnType.equals(XPathConstants.NUMBER))
      return new Double(resultObject.num());
    // XPathConstants.BOOLEAN
    if (returnType.equals(XPathConstants.BOOLEAN))
      return resultObject.bool() ? Boolean.TRUE : Boolean.FALSE;
    // XPathConstants.NODESET ---ORdered, UNOrdered???
    if (returnType.equals(XPathConstants.NODESET))
      return resultObject.nodelist();
    // XPathConstants.NODE
    if (returnType.equals(XPathConstants.NODE)) {
      final NodeIterator ni = resultObject.nodeset();
      // Return the first node, or null
      return ni.nextNode();
    }
    // If isSupported check is already done then the execution path
    // shouldn't come here. Being defensive
    final String fmsg = Messages.get().unsupportedReturnType(returnType);
    throw new IllegalArgumentException(fmsg);
  }

  private static Document getDummyDocument() {
    try {
      if (dbf == null) {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
      }
      db = dbf.newDocumentBuilder();

      final DOMImplementation dim = db.getDOMImplementation();
      d = dim.createDocument("http://java.sun.com/jaxp/xpath", "dummyroot", null);
      return d;
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
