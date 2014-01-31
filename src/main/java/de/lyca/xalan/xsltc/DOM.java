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

import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public interface DOM {
  final static int FIRST_TYPE = 0;

  final static int NO_TYPE = -1;

  // 0 is reserved for NodeIterator.END
  final static int NULL = 0;

  // used by some node iterators to know which node to return
  final static int RETURN_CURRENT = 0;
  final static int RETURN_PARENT = 1;

  // Constants used by getResultTreeFrag to indicate the types of the RTFs.
  final static int SIMPLE_RTF = 0;
  final static int ADAPTIVE_RTF = 1;
  final static int TREE_RTF = 2;

  /** returns singleton iterator containg the document root */
  DTMAxisIterator getIterator();

  String getStringValue();

  DTMAxisIterator getChildren(final int node);

  DTMAxisIterator getTypedChildren(final int type);

  DTMAxisIterator getAxisIterator(final int axis);

  DTMAxisIterator getTypedAxisIterator(final int axis, final int type);

  DTMAxisIterator getNthDescendant(int node, int n, boolean includeself);

  DTMAxisIterator getNamespaceAxisIterator(final int axis, final int ns);

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
