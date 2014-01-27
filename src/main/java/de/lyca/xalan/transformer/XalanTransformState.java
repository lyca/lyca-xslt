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

package de.lyca.xalan.transformer;

import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import de.lyca.xalan.templates.ElemTemplate;
import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;

/**
 * Before the serializer merge, the TransformState interface was implemented by
 * ResultTreeHandler.
 */
public class XalanTransformState implements TransformState {

  Node m_node = null;
  ElemTemplateElement m_currentElement = null;
  ElemTemplate m_currentTemplate = null;
  ElemTemplate m_matchedTemplate = null;
  int m_currentNodeHandle = DTM.NULL;
  Node m_currentNode = null;
  int m_matchedNode = DTM.NULL;
  DTMIterator m_contextNodeList = null;
  boolean m_elemPending = false;
  TransformerImpl m_transformer = null;

  /**
   * @see de.lyca.xml.serializer.TransformStateSetter#setCurrentNode(Node)
   */
  @Override
  public void setCurrentNode(Node n) {
    m_node = n;
  }

  /**
   * @see de.lyca.xml.serializer.TransformStateSetter#resetState(Transformer)
   */
  @Override
  public void resetState(Transformer transformer) {
    if (transformer != null && transformer instanceof TransformerImpl) {
      m_transformer = (TransformerImpl) transformer;
      m_currentElement = m_transformer.getCurrentElement();
      m_currentTemplate = m_transformer.getCurrentTemplate();
      m_matchedTemplate = m_transformer.getMatchedTemplate();
      final int currentNodeHandle = m_transformer.getCurrentNode();
      final DTM dtm = m_transformer.getXPathContext().getDTM(currentNodeHandle);
      m_currentNode = dtm.getNode(currentNodeHandle);
      m_matchedNode = m_transformer.getMatchedNode();
      m_contextNodeList = m_transformer.getContextNodeList();
    }
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getCurrentElement()
   */
  @Override
  public ElemTemplateElement getCurrentElement() {
    if (m_elemPending)
      return m_currentElement;
    else
      return m_transformer.getCurrentElement();
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getCurrentNode()
   */
  @Override
  public Node getCurrentNode() {
    if (m_currentNode != null)
      return m_currentNode;
    else {
      final DTM dtm = m_transformer.getXPathContext().getDTM(m_transformer.getCurrentNode());
      return dtm.getNode(m_transformer.getCurrentNode());
    }
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getCurrentTemplate()
   */
  @Override
  public ElemTemplate getCurrentTemplate() {
    if (m_elemPending)
      return m_currentTemplate;
    else
      return m_transformer.getCurrentTemplate();
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getMatchedTemplate()
   */
  @Override
  public ElemTemplate getMatchedTemplate() {
    if (m_elemPending)
      return m_matchedTemplate;
    else
      return m_transformer.getMatchedTemplate();
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getMatchedNode()
   */
  @Override
  public Node getMatchedNode() {

    if (m_elemPending) {
      final DTM dtm = m_transformer.getXPathContext().getDTM(m_matchedNode);
      return dtm.getNode(m_matchedNode);
    } else {
      final DTM dtm = m_transformer.getXPathContext().getDTM(m_transformer.getMatchedNode());
      return dtm.getNode(m_transformer.getMatchedNode());
    }
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getContextNodeList()
   */
  @Override
  public NodeIterator getContextNodeList() {
    if (m_elemPending)
      return new de.lyca.xml.dtm.ref.DTMNodeIterator(m_contextNodeList);
    else
      return new de.lyca.xml.dtm.ref.DTMNodeIterator(m_transformer.getContextNodeList());
  }

  /**
   * @see de.lyca.xalan.transformer.TransformState#getTransformer()
   */
  @Override
  public Transformer getTransformer() {
    return m_transformer;
  }

}
