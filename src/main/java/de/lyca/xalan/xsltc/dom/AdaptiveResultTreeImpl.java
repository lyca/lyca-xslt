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
package de.lyca.xalan.xsltc.dom;

import java.util.Map;

import javax.xml.transform.SourceLocator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.runtime.AttributeList;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.DTMAxisTraverser;
import de.lyca.xml.dtm.DTMWSFilter;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.utils.XMLString;

/**
 * AdaptiveResultTreeImpl is a adaptive DOM model for result tree fragments
 * (RTF). It is used in the case where the RTF is likely to be pure text yet it
 * can still be a DOM tree. It is designed for RTFs which have
 * &lt;xsl:call-template&gt; or &lt;xsl:apply-templates&gt; in the contents.
 * Example:
 * 
 * <pre>
 *    &lt;xsl:variable name = "x"&gt;
 *      &lt;xsl:call-template name = "test"&gt;
 *         &lt;xsl:with-param name="a" select="."/&gt;
 *      &lt;/xsl:call-template&gt;
 *    &lt;/xsl:variable>
 * </pre>
 * <p>
 * In this example the result produced by <xsl:call-template> is likely to be a
 * single Text node. But it can also be a DOM tree. This kind of RTF cannot be
 * modelled by SimpleResultTreeImpl.
 * <p>
 * AdaptiveResultTreeImpl can be considered as a smart switcher between
 * SimpleResultTreeImpl and SAXImpl. It treats the RTF as simple Text and uses
 * the SimpleResultTreeImpl model at the beginning. However, if it receives a
 * call which indicates that this is a DOM tree (e.g. startElement), it will
 * automatically transform itself into a wrapper around a SAXImpl. In this way
 * we can have a light-weight model when the result only contains simple text,
 * while at the same time it still works when the RTF is a DOM tree.
 * <p>
 * All methods in this class are overridden to delegate the action to the
 * wrapped SAXImpl object if it is non-null, or delegate the action to the
 * SimpleResultTreeImpl if there is no wrapped SAXImpl.
 * <p>
 * %REVISIT% Can we combine this class with SimpleResultTreeImpl? I think it is
 * possible, but it will make SimpleResultTreeImpl more expensive. I will use
 * two separate classes at this time.
 */
public class AdaptiveResultTreeImpl extends SimpleResultTreeImpl {

  // Document URI index, which increases by 1 at each getDocumentURI() call.
  private static int _documentURIIndex = 0;

  // The SAXImpl object wrapped by this class, if the RTF is a tree.
  private SAXImpl _dom;

  /** The following fields are only used for the nested SAXImpl **/

  // The whitespace filter
  private final DTMWSFilter _wsfilter;

  // The size of the RTF
  private final int _initSize;

  // True if we want to build the ID index table
  private final boolean _buildIdIndex;

  // The AttributeList
  private final AttributeList _attributes = new AttributeList();

  // The element name
  private String _openElementName;

  // Create a AdaptiveResultTreeImpl
  public AdaptiveResultTreeImpl(XSLTCDTMManager dtmManager, int documentID, DTMWSFilter wsfilter, int initSize,
          boolean buildIdIndex) {
    super(dtmManager, documentID);

    _wsfilter = wsfilter;
    _initSize = initSize;
    _buildIdIndex = buildIdIndex;
  }

  // Return the DOM object wrapped in this object.
  public DOM getNestedDOM() {
    return _dom;
  }

  // Return the document ID
  @Override
  public int getDocument() {
    if (_dom != null)
      return _dom.getDocument();
    else
      return super.getDocument();
  }

  // Return the String value of the RTF
  @Override
  public String getStringValue() {
    if (_dom != null)
      return _dom.getStringValue();
    else
      return super.getStringValue();
  }

  @Override
  public DTMAxisIterator getIterator() {
    if (_dom != null)
      return _dom.getIterator();
    else
      return super.getIterator();
  }

  @Override
  public DTMAxisIterator getChildren(final int node) {
    if (_dom != null)
      return _dom.getChildren(node);
    else
      return super.getChildren(node);
  }

  @Override
  public DTMAxisIterator getTypedChildren(final int type) {
    if (_dom != null)
      return _dom.getTypedChildren(type);
    else
      return super.getTypedChildren(type);
  }

  @Override
  public DTMAxisIterator getAxisIterator(final int axis) {
    if (_dom != null)
      return _dom.getAxisIterator(axis);
    else
      return super.getAxisIterator(axis);
  }

  @Override
  public DTMAxisIterator getTypedAxisIterator(final int axis, final int type) {
    if (_dom != null)
      return _dom.getTypedAxisIterator(axis, type);
    else
      return super.getTypedAxisIterator(axis, type);
  }

  @Override
  public DTMAxisIterator getNthDescendant(int node, int n, boolean includeself) {
    if (_dom != null)
      return _dom.getNthDescendant(node, n, includeself);
    else
      return super.getNthDescendant(node, n, includeself);
  }

  @Override
  public DTMAxisIterator getNamespaceAxisIterator(final int axis, final int ns) {
    if (_dom != null)
      return _dom.getNamespaceAxisIterator(axis, ns);
    else
      return super.getNamespaceAxisIterator(axis, ns);
  }

  @Override
  public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iter, int returnType, String value, boolean op) {
    if (_dom != null)
      return _dom.getNodeValueIterator(iter, returnType, value, op);
    else
      return super.getNodeValueIterator(iter, returnType, value, op);
  }

  @Override
  public DTMAxisIterator orderNodes(DTMAxisIterator source, int node) {
    if (_dom != null)
      return _dom.orderNodes(source, node);
    else
      return super.orderNodes(source, node);
  }

  @Override
  public String getNodeName(final int node) {
    if (_dom != null)
      return _dom.getNodeName(node);
    else
      return super.getNodeName(node);
  }

  @Override
  public String getNodeNameX(final int node) {
    if (_dom != null)
      return _dom.getNodeNameX(node);
    else
      return super.getNodeNameX(node);
  }

  @Override
  public String getNamespaceName(final int node) {
    if (_dom != null)
      return _dom.getNamespaceName(node);
    else
      return super.getNamespaceName(node);
  }

  // Return the expanded type id of a given node
  @Override
  public int getExpandedTypeID(final int nodeHandle) {
    if (_dom != null)
      return _dom.getExpandedTypeID(nodeHandle);
    else
      return super.getExpandedTypeID(nodeHandle);
  }

  @Override
  public int getNamespaceType(final int node) {
    if (_dom != null)
      return _dom.getNamespaceType(node);
    else
      return super.getNamespaceType(node);
  }

  @Override
  public int getParent(final int nodeHandle) {
    if (_dom != null)
      return _dom.getParent(nodeHandle);
    else
      return super.getParent(nodeHandle);
  }

  @Override
  public int getAttributeNode(final int gType, final int element) {
    if (_dom != null)
      return _dom.getAttributeNode(gType, element);
    else
      return super.getAttributeNode(gType, element);
  }

  @Override
  public String getStringValueX(final int nodeHandle) {
    if (_dom != null)
      return _dom.getStringValueX(nodeHandle);
    else
      return super.getStringValueX(nodeHandle);
  }

  @Override
  public void copy(final int node, SerializationHandler handler) throws TransletException {
    if (_dom != null) {
      _dom.copy(node, handler);
    } else {
      super.copy(node, handler);
    }
  }

  @Override
  public void copy(DTMAxisIterator nodes, SerializationHandler handler) throws TransletException {
    if (_dom != null) {
      _dom.copy(nodes, handler);
    } else {
      super.copy(nodes, handler);
    }
  }

  @Override
  public String shallowCopy(final int node, SerializationHandler handler) throws TransletException {
    if (_dom != null)
      return _dom.shallowCopy(node, handler);
    else
      return super.shallowCopy(node, handler);
  }

  @Override
  public boolean lessThan(final int node1, final int node2) {
    if (_dom != null)
      return _dom.lessThan(node1, node2);
    else
      return super.lessThan(node1, node2);
  }

  /**
   * Dispatch the character content of a node to an output handler.
   * 
   * The escape setting should be taken care of when outputting to a handler.
   */
  @Override
  public void characters(final int node, SerializationHandler handler) throws TransletException {
    if (_dom != null) {
      _dom.characters(node, handler);
    } else {
      super.characters(node, handler);
    }
  }

  @Override
  public Node makeNode(int index) {
    if (_dom != null)
      return _dom.makeNode(index);
    else
      return super.makeNode(index);
  }

  @Override
  public Node makeNode(DTMAxisIterator iter) {
    if (_dom != null)
      return _dom.makeNode(iter);
    else
      return super.makeNode(iter);
  }

  @Override
  public NodeList makeNodeList(int index) {
    if (_dom != null)
      return _dom.makeNodeList(index);
    else
      return super.makeNodeList(index);
  }

  @Override
  public NodeList makeNodeList(DTMAxisIterator iter) {
    if (_dom != null)
      return _dom.makeNodeList(iter);
    else
      return super.makeNodeList(iter);
  }

  @Override
  public String getLanguage(int node) {
    if (_dom != null)
      return _dom.getLanguage(node);
    else
      return super.getLanguage(node);
  }

  @Override
  public int getSize() {
    if (_dom != null)
      return _dom.getSize();
    else
      return super.getSize();
  }

  @Override
  public String getDocumentURI(int node) {
    if (_dom != null)
      return _dom.getDocumentURI(node);
    else
      return "adaptive_rtf" + _documentURIIndex++;
  }

  @Override
  public void setFilter(StripFilter filter) {
    if (_dom != null) {
      _dom.setFilter(filter);
    } else {
      super.setFilter(filter);
    }
  }

  @Override
  public void setupMapping(String[] names, String[] uris, int[] types, String[] namespaces) {
    if (_dom != null) {
      _dom.setupMapping(names, uris, types, namespaces);
    } else {
      super.setupMapping(names, uris, types, namespaces);
    }
  }

  @Override
  public boolean isElement(final int node) {
    if (_dom != null)
      return _dom.isElement(node);
    else
      return super.isElement(node);
  }

  @Override
  public boolean isAttribute(final int node) {
    if (_dom != null)
      return _dom.isAttribute(node);
    else
      return super.isAttribute(node);
  }

  @Override
  public String lookupNamespace(int node, String prefix) throws TransletException {
    if (_dom != null)
      return _dom.lookupNamespace(node, prefix);
    else
      return super.lookupNamespace(node, prefix);
  }

  /**
   * Return the node identity from a node handle.
   */
  @Override
  public final int getNodeIdent(final int nodehandle) {
    if (_dom != null)
      return _dom.getNodeIdent(nodehandle);
    else
      return super.getNodeIdent(nodehandle);
  }

  /**
   * Return the node handle from a node identity.
   */
  @Override
  public final int getNodeHandle(final int nodeId) {
    if (_dom != null)
      return _dom.getNodeHandle(nodeId);
    else
      return super.getNodeHandle(nodeId);
  }

  @Override
  public DOM getResultTreeFrag(int initialSize, int rtfType) {
    if (_dom != null)
      return _dom.getResultTreeFrag(initialSize, rtfType);
    else
      return super.getResultTreeFrag(initialSize, rtfType);
  }

  @Override
  public SerializationHandler getOutputDomBuilder() {
    return this;
  }

  @Override
  public int getNSType(int node) {
    if (_dom != null)
      return _dom.getNSType(node);
    else
      return super.getNSType(node);
  }

  @Override
  public String getUnparsedEntityURI(String name) {
    if (_dom != null)
      return _dom.getUnparsedEntityURI(name);
    else
      return super.getUnparsedEntityURI(name);
  }

  @Override
  public Map<String, Integer> getElementsWithIDs() {
    if (_dom != null)
      return _dom.getElementsWithIDs();
    else
      return super.getElementsWithIDs();
  }

  /** Implementation of the SerializationHandler interfaces **/

  /** The code in some of the following interfaces are copied from SAXAdapter. **/

  private void maybeEmitStartElement() throws SAXException {
    if (_openElementName != null) {

      int index;
      if ((index = _openElementName.indexOf(":")) < 0) {
        _dom.startElement(null, _openElementName, _openElementName, _attributes);
      } else {
        _dom.startElement(null, _openElementName.substring(index + 1), _openElementName, _attributes);
      }

      _openElementName = null;
    }
  }

  // Create and initialize the wrapped SAXImpl object
  private void prepareNewDOM() throws SAXException {
    _dom = (SAXImpl) _dtmManager.getDTM(null, true, _wsfilter, true, false, false, _initSize, _buildIdIndex);
    _dom.startDocument();
    // Flush pending Text nodes to SAXImpl
    for (int i = 0; i < _size; i++) {
      final String str = _textArray[i];
      _dom.characters(str.toCharArray(), 0, str.length());
    }
    _size = 0;
  }

  @Override
  public void startDocument() throws SAXException {
  }

  @Override
  public void endDocument() throws SAXException {
    if (_dom != null) {
      _dom.endDocument();
    } else {
      super.endDocument();
    }
  }

  @Override
  public void characters(String str) throws SAXException {
    if (_dom != null) {
      characters(str.toCharArray(), 0, str.length());
    } else {
      super.characters(str);
    }
  }

  @Override
  public void characters(char[] ch, int offset, int length) throws SAXException {
    if (_dom != null) {
      maybeEmitStartElement();
      _dom.characters(ch, offset, length);
    } else {
      super.characters(ch, offset, length);
    }
  }

  @Override
  public boolean setEscaping(boolean escape) throws SAXException {
    if (_dom != null)
      return _dom.setEscaping(escape);
    else
      return super.setEscaping(escape);
  }

  @Override
  public void startElement(String elementName) throws SAXException {
    if (_dom == null) {
      prepareNewDOM();
    }

    maybeEmitStartElement();
    _openElementName = elementName;
    _attributes.clear();
  }

  @Override
  public void startElement(String uri, String localName, String qName) throws SAXException {
    startElement(qName);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    startElement(qName);
  }

  @Override
  public void endElement(String elementName) throws SAXException {
    maybeEmitStartElement();
    _dom.endElement(null, null, elementName);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    endElement(qName);
  }

  @Override
  public void addUniqueAttribute(String qName, String value, int flags) throws SAXException {
    addAttribute(qName, value);
  }

  @Override
  public void addAttribute(String name, String value) {
    if (_openElementName != null) {
      _attributes.add(name, value);
    } else {
      BasisLibrary.runTimeError(BasisLibrary.STRAY_ATTRIBUTE_ERR, name);
    }
  }

  @Override
  public void namespaceAfterStartElement(String prefix, String uri) throws SAXException {
    if (_dom == null) {
      prepareNewDOM();
    }

    _dom.startPrefixMapping(prefix, uri);
  }

  @Override
  public void comment(String comment) throws SAXException {
    if (_dom == null) {
      prepareNewDOM();
    }

    maybeEmitStartElement();
    final char[] chars = comment.toCharArray();
    _dom.comment(chars, 0, chars.length);
  }

  @Override
  public void comment(char[] chars, int offset, int length) throws SAXException {
    if (_dom == null) {
      prepareNewDOM();
    }

    maybeEmitStartElement();
    _dom.comment(chars, offset, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    if (_dom == null) {
      prepareNewDOM();
    }

    maybeEmitStartElement();
    _dom.processingInstruction(target, data);
  }

  /** Implementation of the DTM interfaces **/

  @Override
  public void setFeature(String featureId, boolean state) {
    if (_dom != null) {
      _dom.setFeature(featureId, state);
    }
  }

  @Override
  public void setProperty(String property, Object value) {
    if (_dom != null) {
      _dom.setProperty(property, value);
    }
  }

  @Override
  public DTMAxisTraverser getAxisTraverser(final int axis) {
    if (_dom != null)
      return _dom.getAxisTraverser(axis);
    else
      return super.getAxisTraverser(axis);
  }

  @Override
  public boolean hasChildNodes(int nodeHandle) {
    if (_dom != null)
      return _dom.hasChildNodes(nodeHandle);
    else
      return super.hasChildNodes(nodeHandle);
  }

  @Override
  public int getFirstChild(int nodeHandle) {
    if (_dom != null)
      return _dom.getFirstChild(nodeHandle);
    else
      return super.getFirstChild(nodeHandle);
  }

  @Override
  public int getLastChild(int nodeHandle) {
    if (_dom != null)
      return _dom.getLastChild(nodeHandle);
    else
      return super.getLastChild(nodeHandle);
  }

  @Override
  public int getAttributeNode(int elementHandle, String namespaceURI, String name) {
    if (_dom != null)
      return _dom.getAttributeNode(elementHandle, namespaceURI, name);
    else
      return super.getAttributeNode(elementHandle, namespaceURI, name);
  }

  @Override
  public int getFirstAttribute(int nodeHandle) {
    if (_dom != null)
      return _dom.getFirstAttribute(nodeHandle);
    else
      return super.getFirstAttribute(nodeHandle);
  }

  @Override
  public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
    if (_dom != null)
      return _dom.getFirstNamespaceNode(nodeHandle, inScope);
    else
      return super.getFirstNamespaceNode(nodeHandle, inScope);
  }

  @Override
  public int getNextSibling(int nodeHandle) {
    if (_dom != null)
      return _dom.getNextSibling(nodeHandle);
    else
      return super.getNextSibling(nodeHandle);
  }

  @Override
  public int getPreviousSibling(int nodeHandle) {
    if (_dom != null)
      return _dom.getPreviousSibling(nodeHandle);
    else
      return super.getPreviousSibling(nodeHandle);
  }

  @Override
  public int getNextAttribute(int nodeHandle) {
    if (_dom != null)
      return _dom.getNextAttribute(nodeHandle);
    else
      return super.getNextAttribute(nodeHandle);
  }

  @Override
  public int getNextNamespaceNode(int baseHandle, int namespaceHandle, boolean inScope) {
    if (_dom != null)
      return _dom.getNextNamespaceNode(baseHandle, namespaceHandle, inScope);
    else
      return super.getNextNamespaceNode(baseHandle, namespaceHandle, inScope);
  }

  @Override
  public int getOwnerDocument(int nodeHandle) {
    if (_dom != null)
      return _dom.getOwnerDocument(nodeHandle);
    else
      return super.getOwnerDocument(nodeHandle);
  }

  @Override
  public int getDocumentRoot(int nodeHandle) {
    if (_dom != null)
      return _dom.getDocumentRoot(nodeHandle);
    else
      return super.getDocumentRoot(nodeHandle);
  }

  @Override
  public XMLString getStringValue(int nodeHandle) {
    if (_dom != null)
      return _dom.getStringValue(nodeHandle);
    else
      return super.getStringValue(nodeHandle);
  }

  @Override
  public int getStringValueChunkCount(int nodeHandle) {
    if (_dom != null)
      return _dom.getStringValueChunkCount(nodeHandle);
    else
      return super.getStringValueChunkCount(nodeHandle);
  }

  @Override
  public char[] getStringValueChunk(int nodeHandle, int chunkIndex, int[] startAndLen) {
    if (_dom != null)
      return _dom.getStringValueChunk(nodeHandle, chunkIndex, startAndLen);
    else
      return super.getStringValueChunk(nodeHandle, chunkIndex, startAndLen);
  }

  @Override
  public int getExpandedTypeID(String namespace, String localName, int type) {
    if (_dom != null)
      return _dom.getExpandedTypeID(namespace, localName, type);
    else
      return super.getExpandedTypeID(namespace, localName, type);
  }

  @Override
  public String getLocalNameFromExpandedNameID(int ExpandedNameID) {
    if (_dom != null)
      return _dom.getLocalNameFromExpandedNameID(ExpandedNameID);
    else
      return super.getLocalNameFromExpandedNameID(ExpandedNameID);
  }

  @Override
  public String getNamespaceFromExpandedNameID(int ExpandedNameID) {
    if (_dom != null)
      return _dom.getNamespaceFromExpandedNameID(ExpandedNameID);
    else
      return super.getNamespaceFromExpandedNameID(ExpandedNameID);
  }

  @Override
  public String getLocalName(int nodeHandle) {
    if (_dom != null)
      return _dom.getLocalName(nodeHandle);
    else
      return super.getLocalName(nodeHandle);
  }

  @Override
  public String getPrefix(int nodeHandle) {
    if (_dom != null)
      return _dom.getPrefix(nodeHandle);
    else
      return super.getPrefix(nodeHandle);
  }

  @Override
  public String getNamespaceURI(int nodeHandle) {
    if (_dom != null)
      return _dom.getNamespaceURI(nodeHandle);
    else
      return super.getNamespaceURI(nodeHandle);
  }

  @Override
  public String getNodeValue(int nodeHandle) {
    if (_dom != null)
      return _dom.getNodeValue(nodeHandle);
    else
      return super.getNodeValue(nodeHandle);
  }

  @Override
  public short getNodeType(int nodeHandle) {
    if (_dom != null)
      return _dom.getNodeType(nodeHandle);
    else
      return super.getNodeType(nodeHandle);
  }

  @Override
  public short getLevel(int nodeHandle) {
    if (_dom != null)
      return _dom.getLevel(nodeHandle);
    else
      return super.getLevel(nodeHandle);
  }

  @Override
  public boolean isSupported(String feature, String version) {
    if (_dom != null)
      return _dom.isSupported(feature, version);
    else
      return super.isSupported(feature, version);
  }

  @Override
  public String getDocumentBaseURI() {
    if (_dom != null)
      return _dom.getDocumentBaseURI();
    else
      return super.getDocumentBaseURI();
  }

  @Override
  public void setDocumentBaseURI(String baseURI) {
    if (_dom != null) {
      _dom.setDocumentBaseURI(baseURI);
    } else {
      super.setDocumentBaseURI(baseURI);
    }
  }

  @Override
  public String getDocumentSystemIdentifier(int nodeHandle) {
    if (_dom != null)
      return _dom.getDocumentSystemIdentifier(nodeHandle);
    else
      return super.getDocumentSystemIdentifier(nodeHandle);
  }

  @Override
  public String getDocumentEncoding(int nodeHandle) {
    if (_dom != null)
      return _dom.getDocumentEncoding(nodeHandle);
    else
      return super.getDocumentEncoding(nodeHandle);
  }

  @Override
  public String getDocumentStandalone(int nodeHandle) {
    if (_dom != null)
      return _dom.getDocumentStandalone(nodeHandle);
    else
      return super.getDocumentStandalone(nodeHandle);
  }

  @Override
  public String getDocumentVersion(int documentHandle) {
    if (_dom != null)
      return _dom.getDocumentVersion(documentHandle);
    else
      return super.getDocumentVersion(documentHandle);
  }

  @Override
  public boolean getDocumentAllDeclarationsProcessed() {
    if (_dom != null)
      return _dom.getDocumentAllDeclarationsProcessed();
    else
      return super.getDocumentAllDeclarationsProcessed();
  }

  @Override
  public String getDocumentTypeDeclarationSystemIdentifier() {
    if (_dom != null)
      return _dom.getDocumentTypeDeclarationSystemIdentifier();
    else
      return super.getDocumentTypeDeclarationSystemIdentifier();
  }

  @Override
  public String getDocumentTypeDeclarationPublicIdentifier() {
    if (_dom != null)
      return _dom.getDocumentTypeDeclarationPublicIdentifier();
    else
      return super.getDocumentTypeDeclarationPublicIdentifier();
  }

  @Override
  public int getElementById(String elementId) {
    if (_dom != null)
      return _dom.getElementById(elementId);
    else
      return super.getElementById(elementId);
  }

  @Override
  public boolean supportsPreStripping() {
    if (_dom != null)
      return _dom.supportsPreStripping();
    else
      return super.supportsPreStripping();
  }

  @Override
  public boolean isNodeAfter(int firstNodeHandle, int secondNodeHandle) {
    if (_dom != null)
      return _dom.isNodeAfter(firstNodeHandle, secondNodeHandle);
    else
      return super.isNodeAfter(firstNodeHandle, secondNodeHandle);
  }

  @Override
  public boolean isCharacterElementContentWhitespace(int nodeHandle) {
    if (_dom != null)
      return _dom.isCharacterElementContentWhitespace(nodeHandle);
    else
      return super.isCharacterElementContentWhitespace(nodeHandle);
  }

  @Override
  public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {
    if (_dom != null)
      return _dom.isDocumentAllDeclarationsProcessed(documentHandle);
    else
      return super.isDocumentAllDeclarationsProcessed(documentHandle);
  }

  @Override
  public boolean isAttributeSpecified(int attributeHandle) {
    if (_dom != null)
      return _dom.isAttributeSpecified(attributeHandle);
    else
      return super.isAttributeSpecified(attributeHandle);
  }

  @Override
  public void dispatchCharactersEvents(int nodeHandle, org.xml.sax.ContentHandler ch, boolean normalize)
          throws org.xml.sax.SAXException {
    if (_dom != null) {
      _dom.dispatchCharactersEvents(nodeHandle, ch, normalize);
    } else {
      super.dispatchCharactersEvents(nodeHandle, ch, normalize);
    }
  }

  @Override
  public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch) throws org.xml.sax.SAXException {
    if (_dom != null) {
      _dom.dispatchToEvents(nodeHandle, ch);
    } else {
      super.dispatchToEvents(nodeHandle, ch);
    }
  }

  @Override
  public org.w3c.dom.Node getNode(int nodeHandle) {
    if (_dom != null)
      return _dom.getNode(nodeHandle);
    else
      return super.getNode(nodeHandle);
  }

  @Override
  public boolean needsTwoThreads() {
    if (_dom != null)
      return _dom.needsTwoThreads();
    else
      return super.needsTwoThreads();
  }

  @Override
  public org.xml.sax.ContentHandler getContentHandler() {
    if (_dom != null)
      return _dom.getContentHandler();
    else
      return super.getContentHandler();
  }

  @Override
  public org.xml.sax.ext.LexicalHandler getLexicalHandler() {
    if (_dom != null)
      return _dom.getLexicalHandler();
    else
      return super.getLexicalHandler();
  }

  @Override
  public org.xml.sax.EntityResolver getEntityResolver() {
    if (_dom != null)
      return _dom.getEntityResolver();
    else
      return super.getEntityResolver();
  }

  @Override
  public org.xml.sax.DTDHandler getDTDHandler() {
    if (_dom != null)
      return _dom.getDTDHandler();
    else
      return super.getDTDHandler();
  }

  @Override
  public org.xml.sax.ErrorHandler getErrorHandler() {
    if (_dom != null)
      return _dom.getErrorHandler();
    else
      return super.getErrorHandler();
  }

  @Override
  public org.xml.sax.ext.DeclHandler getDeclHandler() {
    if (_dom != null)
      return _dom.getDeclHandler();
    else
      return super.getDeclHandler();
  }

  @Override
  public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
    if (_dom != null) {
      _dom.appendChild(newChild, clone, cloneDepth);
    } else {
      super.appendChild(newChild, clone, cloneDepth);
    }
  }

  @Override
  public void appendTextChild(String str) {
    if (_dom != null) {
      _dom.appendTextChild(str);
    } else {
      super.appendTextChild(str);
    }
  }

  @Override
  public SourceLocator getSourceLocatorFor(int node) {
    if (_dom != null)
      return _dom.getSourceLocatorFor(node);
    else
      return super.getSourceLocatorFor(node);
  }

  @Override
  public void documentRegistration() {
    if (_dom != null) {
      _dom.documentRegistration();
    } else {
      super.documentRegistration();
    }
  }

  @Override
  public void documentRelease() {
    if (_dom != null) {
      _dom.documentRelease();
    } else {
      super.documentRelease();
    }
  }

}
