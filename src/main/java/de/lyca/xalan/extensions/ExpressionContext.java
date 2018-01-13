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
package de.lyca.xalan.extensions;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XObject;

/**
 * An object that implements this interface can supply information about the
 * current XPath expression context.
 */
public interface ExpressionContext {

  /**
   * Get the current context node.
   * 
   * @return The current context node.
   */
  Node getContextNode();

  /**
   * Get the current context node list.
   * 
   * @return An iterator for the current context list, as defined in XSLT.
   */
  NodeIterator getContextNodes();

  /**
   * Get the error listener.
   * 
   * @return The registered error listener.
   */
  ErrorListener getErrorListener();

  /**
   * Get the value of a node as a number.
   * 
   * @param n
   *          Node to be converted to a number. May be null.
   * @return value of n as a number.
   */
  double toNumber(Node n);

  /**
   * Get the value of a node as a string.
   * 
   * @param n
   *          Node to be converted to a string. May be null.
   * @return value of n as a string, or an empty string if n is null.
   */
  String toString(Node n);

  /**
   * Get a variable based on it's qualified name.
   * 
   * @param qname
   *          The qualified name of the variable.
   * 
   * @return The evaluated value of the variable.
   * 
   * @throws TransformerException
   *           TODO
   */
  XObject getVariableOrParam(QName qname) throws TransformerException;

  /**
   * Get the XPathContext that owns this ExpressionContext.
   * 
   * Note: exslt:function requires the XPathContext to access the variable stack
   * and TransformerImpl.
   * 
   * @return The current XPathContext.
   * @throws TransformerException
   *           TODO
   */
  XPathContext getXPathContext() throws TransformerException;

}
