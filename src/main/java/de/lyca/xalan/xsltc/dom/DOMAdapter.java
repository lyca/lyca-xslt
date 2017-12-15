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
package de.lyca.xalan.xsltc.dom;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.DOMEnhancedForDTM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;

/**
 * @author Jacek Ambroziak
 * @author Morten Jorgensen
 */
public final class DOMAdapter implements DOM {

  // Mutually exclusive casting of DOM interface to known implementations
  private DOMEnhancedForDTM _enhancedDOM;

  private final DOM _dom;

  private String[] _namesArray;
  private String[] _urisArray;
  private int[] _typesArray;
  private String[] _namespaceArray;

  // Cached mappings
  private short[] _mapping = null;
  private int[] _reverse = null;
  private short[] _NSmapping = null;
  private short[] _NSreverse = null;

  private StripFilter _filter = null;

  private int _multiDOMMask;

  public DOMAdapter(DOM dom, String[] namesArray, String[] urisArray, int[] typesArray, String[] namespaceArray) {
    if (dom instanceof DOMEnhancedForDTM) {
      _enhancedDOM = (DOMEnhancedForDTM) dom;
    }

    _dom = dom;
    _namesArray = namesArray;
    _urisArray = urisArray;
    _typesArray = typesArray;
    _namespaceArray = namespaceArray;
  }

  @Override
  public void setupMapping(String[] names, String[] urisArray, int[] typesArray, String[] namespaces) {
    _namesArray = names;
    _urisArray = urisArray;
    _typesArray = typesArray;
    _namespaceArray = namespaces;
  }

  public String[] getNamesArray() {
    return _namesArray;
  }

  public String[] getUrisArray() {
    return _urisArray;
  }

  public int[] getTypesArray() {
    return _typesArray;
  }

  public String[] getNamespaceArray() {
    return _namespaceArray;
  }

  public DOM getDOMImpl() {
    return _dom;
  }

  private short[] getMapping() {
    if (_mapping == null) {
      if (_enhancedDOM != null) {
        _mapping = _enhancedDOM.getMapping(_namesArray, _urisArray, _typesArray);
      }
    }
    return _mapping;
  }

  private int[] getReverse() {
    if (_reverse == null) {
      if (_enhancedDOM != null) {
        _reverse = _enhancedDOM.getReverseMapping(_namesArray, _urisArray, _typesArray);
      }
    }
    return _reverse;
  }

  private short[] getNSMapping() {
    if (_NSmapping == null) {
      if (_enhancedDOM != null) {
        _NSmapping = _enhancedDOM.getNamespaceMapping(_namespaceArray);
      }
    }
    return _NSmapping;
  }

  private short[] getNSReverse() {
    if (_NSreverse == null) {
      if (_enhancedDOM != null) {
        _NSreverse = _enhancedDOM.getReverseNamespaceMapping(_namespaceArray);
      }
    }
    return _NSreverse;
  }

  /**
   * Returns singleton iterator containg the document root
   */
  @Override
  public DTMAxisIterator getIterator() {
    return _dom.getIterator();
  }

  @Override
  public String getStringValue() {
    return _dom.getStringValue();
  }

  @Override
  public DTMAxisIterator getChildren(final int node) {
    if (_enhancedDOM != null)
      return _enhancedDOM.getChildren(node);
    else {
      final DTMAxisIterator iterator = _dom.getChildren(node);
      return iterator.setStartNode(node);
    }
  }

  @Override
  public void setFilter(StripFilter filter) {
    _filter = filter;
  }

  @Override
  public DTMAxisIterator getTypedChildren(final int type) {
    final int[] reverse = getReverse();

    if (_enhancedDOM != null)
      return _enhancedDOM.getTypedChildren(reverse[type]);
    else
      return _dom.getTypedChildren(type);
  }

  @Override
  public DTMAxisIterator getNamespaceAxisIterator(final Axis axis, final int ns) {
    return _dom.getNamespaceAxisIterator(axis, getNSReverse()[ns]);
  }

  @Override
  public DTMAxisIterator getAxisIterator(final Axis axis) {
    if (_enhancedDOM != null)
      return _enhancedDOM.getAxisIterator(axis);
    else
      return _dom.getAxisIterator(axis);
  }

  @Override
  public DTMAxisIterator getTypedAxisIterator(final Axis axis, final int type) {
    final int[] reverse = getReverse();
    if (_enhancedDOM != null)
      return _enhancedDOM.getTypedAxisIterator(axis, reverse[type]);
    else
      return _dom.getTypedAxisIterator(axis, type);
  }

  public int getMultiDOMMask() {
    return _multiDOMMask;
  }

  public void setMultiDOMMask(int mask) {
    _multiDOMMask = mask;
  }

  @Override
  public DTMAxisIterator getNthDescendant(int type, int n, boolean includeself) {
    return _dom.getNthDescendant(getReverse()[type], n, includeself);
  }

  @Override
  public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iterator, int type, String value, boolean op) {
    return _dom.getNodeValueIterator(iterator, type, value, op);
  }

  @Override
  public DTMAxisIterator orderNodes(DTMAxisIterator source, int node) {
    return _dom.orderNodes(source, node);
  }

  @Override
  public int getExpandedTypeID(final int node) {
//  TODO  if (node == DTM.NULL) return DTM.NULL;

    final short[] mapping = getMapping();
    final int type;
    if (_enhancedDOM != null) {
      type = mapping[_enhancedDOM.getExpandedTypeID2(node)];
    } else {
      if (null != mapping) {
        type = mapping[_dom.getExpandedTypeID(node)];
      } else {
        type = _dom.getExpandedTypeID(node);
      }
    }
    return type;
  }

  @Override
  public int getNamespaceType(final int node) {
    return getNSMapping()[_dom.getNSType(node)];
  }

  @Override
  public int getNSType(int node) {
    return _dom.getNSType(node);
  }

  @Override
  public int getParent(final int node) {
    return _dom.getParent(node);
  }

  @Override
  public int getAttributeNode(final int type, final int element) {
    return _dom.getAttributeNode(getReverse()[type], element);
  }

  @Override
  public String getNodeName(final int node) {
    if (node == DTM.NULL)
      return "";
    return _dom.getNodeName(node);
  }

  @Override
  public String getNodeNameX(final int node) {
    if (node == DTM.NULL)
      return "";
    return _dom.getNodeNameX(node);
  }

  @Override
  public String getNamespaceName(final int node) {
    if (node == DTM.NULL)
      return "";
    return _dom.getNamespaceName(node);
  }

  @Override
  public String getStringValueX(final int node) {
    if (_enhancedDOM != null)
      return _enhancedDOM.getStringValueX(node);
    else {
      if (node == DTM.NULL)
        return "";
      return _dom.getStringValueX(node);
    }
  }

  @Override
  public void copy(final int node, SerializationHandler handler) throws TransletException {
    _dom.copy(node, handler);
  }

  @Override
  public void copy(DTMAxisIterator nodes, SerializationHandler handler) throws TransletException {
    _dom.copy(nodes, handler);
  }

  @Override
  public String shallowCopy(final int node, SerializationHandler handler) throws TransletException {
    if (_enhancedDOM != null)
      return _enhancedDOM.shallowCopy(node, handler);
    else
      return _dom.shallowCopy(node, handler);
  }

  @Override
  public boolean lessThan(final int node1, final int node2) {
    return _dom.lessThan(node1, node2);
  }

  @Override
  public void characters(final int textNode, SerializationHandler handler) throws TransletException {
    if (_enhancedDOM != null) {
      _enhancedDOM.characters(textNode, handler);
    } else {
      _dom.characters(textNode, handler);
    }
  }

  @Override
  public Node makeNode(int index) {
    return _dom.makeNode(index);
  }

  @Override
  public Node makeNode(DTMAxisIterator iter) {
    return _dom.makeNode(iter);
  }

  @Override
  public NodeList makeNodeList(int index) {
    return _dom.makeNodeList(index);
  }

  @Override
  public NodeList makeNodeList(DTMAxisIterator iter) {
    return _dom.makeNodeList(iter);
  }

  @Override
  public String getLanguage(int node) {
    return _dom.getLanguage(node);
  }

  @Override
  public int getSize() {
    return _dom.getSize();
  }

  public void setDocumentURI(String uri) {
    if (_enhancedDOM != null) {
      _enhancedDOM.setDocumentURI(uri);
    }
  }

  public String getDocumentURI() {
    if (_enhancedDOM != null)
      return _enhancedDOM.getDocumentURI();
    else
      return "";
  }

  @Override
  public String getDocumentURI(int node) {
    return _dom.getDocumentURI(node);
  }

  @Override
  public int getDocument() {
    return _dom.getDocument();
  }

  @Override
  public boolean isElement(final int node) {
    return _dom.isElement(node);
  }

  @Override
  public boolean isAttribute(final int node) {
    return _dom.isAttribute(node);
  }

  @Override
  public int getNodeIdent(int nodeHandle) {
    return _dom.getNodeIdent(nodeHandle);
  }

  @Override
  public int getNodeHandle(int nodeId) {
    return _dom.getNodeHandle(nodeId);
  }

  /**
   * Return a instance of a DOM class to be used as an RTF
   */
  @Override
  public DOM getResultTreeFrag(int initSize, int rtfType) {
    if (_enhancedDOM != null)
      return _enhancedDOM.getResultTreeFrag(initSize, rtfType);
    else
      return _dom.getResultTreeFrag(initSize, rtfType);
  }

  /**
   * Return a instance of a DOM class to be used as an RTF
   */
  @Override
  public DOM getResultTreeFrag(int initSize, int rtfType, boolean addToManager) {
    if (_enhancedDOM != null)
      return _enhancedDOM.getResultTreeFrag(initSize, rtfType, addToManager);
    else
      return _dom.getResultTreeFrag(initSize, rtfType, addToManager);
  }

  /**
   * Returns a SerializationHandler class wrapped in a SAX adapter.
   */
  @Override
  public SerializationHandler getOutputDomBuilder() {
    return _dom.getOutputDomBuilder();
  }

  @Override
  public String lookupNamespace(int node, String prefix) throws TransletException {
    return _dom.lookupNamespace(node, prefix);
  }

  @Override
  public String getUnparsedEntityURI(String entity) {
    return _dom.getUnparsedEntityURI(entity);
  }

  @Override
  public Map<String, Integer> getElementsWithIDs() {
    return _dom.getElementsWithIDs();
  }
}
