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

package de.lyca.xalan.xsltc;

import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public interface DOM {
  int FIRST_TYPE = 0;

  int NO_TYPE = -1;

  // 0 is reserved for NodeIterator.END
  int NULL = 0;

  // used by some node iterators to know which node to return
  int RETURN_CURRENT = 0;
  int RETURN_PARENT = 1;

  // Constants used by getResultTreeFrag to indicate the types of the RTFs.
  int SIMPLE_RTF = 0;
  int ADAPTIVE_RTF = 1;
  int TREE_RTF = 2;

  String GET_ITERATOR = "getIterator";
  String GET_STRING_VALUE = "getStringValue";
  String GET_CHILDREN = "getChildren";
//  String GET_TYPED_CHILDREN = "getTypedChildren";
  String GET_AXIS_ITERATOR = "getAxisIterator";
  String GET_TYPED_AXIS_ITERATOR = "getTypedAxisIterator";
  String GET_NTH_DESCENDANT = "getNthDescendant"; // TODO
  String GET_NAMESPACE_AXIS_ITERATOR = "getNamespaceAxisIterator"; // TODO
  String GET_NODE_VALUE_ITERATOR = "getNodeValueIterator";
  String ORDER_NODES = "orderNodes";
  String GET_NODE_NAME = "getNodeName";
  String GET_NODE_NAME_X = "getNodeNameX";
  String GET_NAMESPACE_NAME = "getNamespaceName";
  String GET_EXPANDED_TYPE_ID = "getExpandedTypeID";
  String GET_NAMESPACE_TYPE = "getNamespaceType";
  String GET_PARENT = "getParent";
//  String GET_ATTRIBUTE_NODE = "getAttributeNode";
  String GET_STRING_VALUE_X = "getStringValueX";
  String COPY = "copy";
  String SHALLOW_COPY = "shallowCopy";
//  String LESS_THAN = "lessThan";
  String CHARACTERS = "characters"; // TODO
  String MAKE_NODE = "makeNode";
  String MAKE_NODE_LIST = "makeNodeList";
//  String GET_LANGUAGE = "getLanguage";
//  String GET_SIZE = "getSize";
//  String GET_DOCUMENT_URI = "getDocumentURI";
  String SET_FILTER = "setFilter";
  String SETUP_MAPPING = "setupMapping";
  String IS_ELEMENT = "isElement";
  String IS_ATTRIBUTE = "isAttribute";
//  String LOOKUP_NAMESPACE = "lookupNamespace";
//  String GET_NODE_IDENT = "getNodeIdent";
//  String GET_NODE_HANDLE = "getNodeHandle";
  String GET_RESULT_TREE_FRAG = "getResultTreeFrag";
  String GET_OUTPUT_DOM_BUILDER = "getOutputDomBuilder";
//  String GET_NS_TYPE = "getNSType";
  String GET_DOCUMENT = "getDocument";
  String GET_UNPARSED_ENTITY_URI = "getUnparsedEntityURI";
//  String GET_ELEMENTS_WITH_IDS = "getElementsWithIDs";

  /** returns singleton iterator containg the document root */
  DTMAxisIterator getIterator();

  String getStringValue();

  DTMAxisIterator getChildren(final int node);

  DTMAxisIterator getTypedChildren(final int type);

  DTMAxisIterator getAxisIterator(final Axis axis);

  DTMAxisIterator getTypedAxisIterator(final Axis axis, final int type);

  DTMAxisIterator getNthDescendant(int node, int n, boolean includeself);

  DTMAxisIterator getNamespaceAxisIterator(final Axis axis, final int ns);

  DTMAxisIterator getNodeValueIterator(DTMAxisIterator iter, int returnType, String value, boolean op);

  DTMAxisIterator orderNodes(DTMAxisIterator source, int node);

  String getNodeName(final int node);

  String getNodeNameX(final int node);

  String getNamespaceName(final int node);

  int getExpandedTypeID(final int node);

  int getNamespaceType(final int node);

  int getParent(final int node);

  int getAttributeNode(final int gType, final int element);

  String getStringValueX(final int node);

  void copy(final int node, SerializationHandler handler) throws TransletException;

  void copy(DTMAxisIterator nodes, SerializationHandler handler) throws TransletException;

  String shallowCopy(final int node, SerializationHandler handler) throws TransletException;

  boolean lessThan(final int node1, final int node2);

  void characters(final int textNode, SerializationHandler handler) throws TransletException;

  Node makeNode(int index);

  Node makeNode(DTMAxisIterator iter);

  NodeList makeNodeList(int index);

  NodeList makeNodeList(DTMAxisIterator iter);

  String getLanguage(int node);

  int getSize();

  String getDocumentURI(int node);

  void setFilter(StripFilter filter);

  void setupMapping(String[] names, String[] urisArray, int[] typesArray, String[] namespaces);

  boolean isElement(final int node);

  boolean isAttribute(final int node);

  String lookupNamespace(int node, String prefix) throws TransletException;

  int getNodeIdent(final int nodehandle);

  int getNodeHandle(final int nodeId);

  DOM getResultTreeFrag(int initialSize, int rtfType);

  DOM getResultTreeFrag(int initialSize, int rtfType, boolean addToDTMManager);

  SerializationHandler getOutputDomBuilder();

  int getNSType(int node);

  int getDocument();

  String getUnparsedEntityURI(String name);

  Map<String, Integer> getElementsWithIDs();
}
