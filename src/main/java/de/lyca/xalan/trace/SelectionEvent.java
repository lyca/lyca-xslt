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
package de.lyca.xalan.trace;

import org.w3c.dom.Node;

import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xpath.XPath;
import de.lyca.xpath.objects.XObject;

/**
 * Event triggered by selection of a node in the style stree.
 * 
 * @xsl.usage advanced
 */
public class SelectionEvent implements java.util.EventListener {

  /**
   * The node in the style tree where the event occurs.
   */
  public final ElemTemplateElement m_styleNode;

  /**
   * The XSLT processor instance.
   */
  public final TransformerImpl m_processor;

  /**
   * The current context node.
   */
  public final Node m_sourceNode;

  /**
   * The attribute name from which the selection is made.
   */
  public final String m_attributeName;

  /**
   * The XPath that executed the selection.
   */
  public final XPath m_xpath;

  /**
   * The result of the selection.
   */
  public final XObject m_selection;

  /**
   * Create an event originating at the given node of the style tree.
   * 
   * @param processor
   *          The XSLT TransformerFactory.
   * @param sourceNode
   *          The current context node.
   * @param styleNode
   *          node in the style tree reference for the event. Should not be
   *          null. That is not enforced.
   * @param attributeName
   *          The attribute name from which the selection is made.
   * @param xpath
   *          The XPath that executed the selection.
   * @param selection
   *          The result of the selection.
   */
  public SelectionEvent(TransformerImpl processor, Node sourceNode, ElemTemplateElement styleNode,
          String attributeName, XPath xpath, XObject selection) {

    m_processor = processor;
    m_sourceNode = sourceNode;
    m_styleNode = styleNode;
    m_attributeName = attributeName;
    m_xpath = xpath;
    m_selection = selection;
  }
}
