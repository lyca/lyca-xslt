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
import org.xml.sax.SAXException;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.DTMAxisTraverser;
import de.lyca.xml.dtm.DTMManager;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;
import de.lyca.xml.dtm.ref.DTMManagerDefault;
import de.lyca.xml.serializer.EmptySerializer;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.utils.XMLString;
import de.lyca.xml.utils.XMLStringDefault;

/**
 * This class represents a light-weight DOM model for simple result tree
 * fragment(RTF). A simple RTF is an RTF that has only one Text node. The Text
 * node can be produced by a combination of Text, xsl:value-of and xsl:number
 * instructions. It can also be produced by a control structure (xsl:if or
 * xsl:choose) whose body is pure Text.
 * <p>
 * A SimpleResultTreeImpl has only two nodes, i.e. the ROOT node and its Text
 * child. All DOM interfaces are overridden with this in mind. For example, the
 * getStringValue() interface returns the value of the Text node. This class
 * receives the character data from the characters() interface.
 * <p>
 * This class implements DOM and SerializationHandler. It also implements the
 * DTM interface for support in MultiDOM. The nested iterators (SimpleIterator
 * and SingletonIterator) are used to support the nodeset() extension function.
 */
public class SimpleResultTreeImpl extends EmptySerializer implements DOM, DTM {

  /**
   * The SimpleIterator is designed to support the nodeset() extension function.
   * It has a traversal direction parameter. The DOWN direction is used for
   * child and descendant axes, while the UP direction is used for parent and
   * ancestor axes.
   * 
   * This iterator only handles two nodes (RTF_ROOT and RTF_TEXT). If the type
   * is set, it will also match the node type with the given type.
   */
  public final class SimpleIterator extends DTMAxisIteratorBase {
    static final int DIRECTION_UP = 0;
    static final int DIRECTION_DOWN = 1;
    static final int NO_TYPE = -1;

    // The direction of traversal (default to DOWN).
    // DOWN is for child and descendant. UP is for parent and ancestor.
    int _direction = DIRECTION_DOWN;

    int _type = NO_TYPE;
    int _currentNode;

    public SimpleIterator() {
    }

    public SimpleIterator(int direction) {
      _direction = direction;
    }

    public SimpleIterator(int direction, int type) {
      _direction = direction;
      _type = type;
    }

    @Override
    public int next() {
      // Increase the node ID for down traversal. Also match the node type
      // if the type is given.
      if (_direction == DIRECTION_DOWN) {
        while (_currentNode < NUMBER_OF_NODES) {
          if (_type != NO_TYPE) {
            if (_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE || _currentNode == RTF_TEXT
                    && _type == DTM.TEXT_NODE)
              return returnNode(getNodeHandle(_currentNode++));
            else {
              _currentNode++;
            }
          } else
            return returnNode(getNodeHandle(_currentNode++));
        }

        return END;
      }
      // Decrease the node ID for up traversal.
      else {
        while (_currentNode >= 0) {
          if (_type != NO_TYPE) {
            if (_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE || _currentNode == RTF_TEXT
                    && _type == DTM.TEXT_NODE)
              return returnNode(getNodeHandle(_currentNode--));
            else {
              _currentNode--;
            }
          } else
            return returnNode(getNodeHandle(_currentNode--));
        }

        return END;
      }
    }

    @Override
    public DTMAxisIterator setStartNode(int nodeHandle) {
      int nodeID = getNodeIdent(nodeHandle);
      _startNode = nodeID;

      // Increase the node ID by 1 if self is not included.
      if (!_includeSelf && nodeID != DTM.NULL) {
        if (_direction == DIRECTION_DOWN) {
          nodeID++;
        } else if (_direction == DIRECTION_UP) {
          nodeID--;
        }
      }

      _currentNode = nodeID;
      return this;
    }

    @Override
    public void setMark() {
      _markedNode = _currentNode;
    }

    @Override
    public void gotoMark() {
      _currentNode = _markedNode;
    }

  } // END of SimpleIterator

  /**
   * The SingletonIterator is used for the self axis.
   */
  public final class SingletonIterator extends DTMAxisIteratorBase {
    static final int NO_TYPE = -1;
    int _type = NO_TYPE;
    int _currentNode;

    public SingletonIterator() {
    }

    public SingletonIterator(int type) {
      _type = type;
    }

    @Override
    public void setMark() {
      _markedNode = _currentNode;
    }

    @Override
    public void gotoMark() {
      _currentNode = _markedNode;
    }

    @Override
    public DTMAxisIterator setStartNode(int nodeHandle) {
      _currentNode = _startNode = getNodeIdent(nodeHandle);
      return this;
    }

    @Override
    public int next() {
      if (_currentNode == END)
        return END;

      _currentNode = END;

      if (_type != NO_TYPE) {
        if (_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE || _currentNode == RTF_TEXT && _type == DTM.TEXT_NODE)
          return getNodeHandle(_currentNode);
      } else
        return getNodeHandle(_currentNode);

      return END;
    }

  } // END of SingletonIterator

  // empty iterator to be returned when there are no children
  private final static DTMAxisIterator EMPTY_ITERATOR = new DTMAxisIteratorBase() {
    @Override
    public DTMAxisIterator reset() {
      return this;
    }

    @Override
    public DTMAxisIterator setStartNode(int node) {
      return this;
    }

    @Override
    public int next() {
      return DTM.NULL;
    }

    @Override
    public void setMark() {
    }

    @Override
    public void gotoMark() {
    }

    @Override
    public int getLast() {
      return 0;
    }

    @Override
    public int getPosition() {
      return 0;
    }

    @Override
    public DTMAxisIterator cloneIterator() {
      return this;
    }

    @Override
    public void setRestartable(boolean isRestartable) {
    }
  };

  // The root node id of the simple RTF
  public static final int RTF_ROOT = 0;

  // The Text node id of the simple RTF (simple RTF has only one Text node).
  public static final int RTF_TEXT = 1;

  // The number of nodes.
  public static final int NUMBER_OF_NODES = 2;

  // Document URI index, which increases by 1 at each getDocumentURI() call.
  private static int _documentURIIndex = 0;

  // Constant for empty String
  private static final String EMPTY_STR = "";

  // The String value of the Text node.
  // This is set at the endDocument() call.
  private String _text;

  // The array of Text items, which is built by the characters() call.
  // The characters() interface can be called multiple times. Each character
  // item
  // can have different escape settings.
  protected String[] _textArray;

  // The DTMManager
  protected XSLTCDTMManager _dtmManager;

  // Number of character items
  protected int _size = 0;

  // The document ID
  private final int _documentID;

  // A BitArray, each bit holding the escape setting for a character item.
  private BitArray _dontEscape = null;

  // The current escape setting
  private boolean _escaping = true;

  // Create a SimpleResultTreeImpl from a DTMManager and a document ID.
  public SimpleResultTreeImpl(XSLTCDTMManager dtmManager, int documentID) {
    _dtmManager = dtmManager;
    _documentID = documentID;
    _textArray = new String[4];
  }

  public DTMManagerDefault getDTMManager() {
    return _dtmManager;
  }

  // Return the document ID
  @Override
  public int getDocument() {
    return _documentID;
  }

  // Return the String value of the RTF
  @Override
  public String getStringValue() {
    return _text;
  }

  @Override
  public DTMAxisIterator getIterator() {
    return new SingletonIterator(getDocument());
  }

  @Override
  public DTMAxisIterator getChildren(final int node) {
    return new SimpleIterator().setStartNode(node);
  }

  @Override
  public DTMAxisIterator getTypedChildren(final int type) {
    return new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type);
  }

  // Return the axis iterator for a given axis.
  // The SimpleIterator is used for the child, descendant, parent and ancestor
  // axes.
  @Override
  public DTMAxisIterator getAxisIterator(final Axis axis) {
    switch (axis) {
      case CHILD:
      case DESCENDANT:
        return new SimpleIterator(SimpleIterator.DIRECTION_DOWN);
      case PARENT:
      case ANCESTOR:
        return new SimpleIterator(SimpleIterator.DIRECTION_UP);
      case ANCESTORORSELF:
        return new SimpleIterator(SimpleIterator.DIRECTION_UP).includeSelf();
      case DESCENDANTORSELF:
        return new SimpleIterator(SimpleIterator.DIRECTION_DOWN).includeSelf();
      case SELF:
        return new SingletonIterator();
      default:
        return EMPTY_ITERATOR;
    }
  }

  @Override
  public DTMAxisIterator getTypedAxisIterator(final Axis axis, final int type) {
    switch (axis) {
      case CHILD:
      case DESCENDANT:
        return new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type);
      case PARENT:
      case ANCESTOR:
        return new SimpleIterator(SimpleIterator.DIRECTION_UP, type);
      case ANCESTORORSELF:
        return new SimpleIterator(SimpleIterator.DIRECTION_UP, type).includeSelf();
      case DESCENDANTORSELF:
        return new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type).includeSelf();
      case SELF:
        return new SingletonIterator(type);
      default:
        return EMPTY_ITERATOR;
    }
  }

  // %REVISIT% Can this one ever get used?
  @Override
  public DTMAxisIterator getNthDescendant(int node, int n, boolean includeself) {
    return null;
  }

  @Override
  public DTMAxisIterator getNamespaceAxisIterator(final Axis axis, final int ns) {
    return null;
  }

  // %REVISIT% Can this one ever get used?
  @Override
  public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iter, int returnType, String value, boolean op) {
    return null;
  }

  @Override
  public DTMAxisIterator orderNodes(DTMAxisIterator source, int node) {
    return source;
  }

  @Override
  public String getNodeName(final int node) {
    if (getNodeIdent(node) == RTF_TEXT)
      return "#text";
    else
      return EMPTY_STR;
  }

  @Override
  public String getNodeNameX(final int node) {
    return EMPTY_STR;
  }

  @Override
  public String getNamespaceName(final int node) {
    return EMPTY_STR;
  }

  // Return the expanded type id of a given node
  @Override
  public int getExpandedTypeID(final int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    if (nodeID == RTF_TEXT)
      return DTM.TEXT_NODE;
    else if (nodeID == RTF_ROOT)
      return DTM.ROOT_NODE;
    else
      return DTM.NULL;
  }

  @Override
  public int getNamespaceType(final int node) {
    return 0;
  }

  @Override
  public int getParent(final int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    return nodeID == RTF_TEXT ? getNodeHandle(RTF_ROOT) : DTM.NULL;
  }

  @Override
  public int getAttributeNode(final int gType, final int element) {
    return DTM.NULL;
  }

  @Override
  public String getStringValueX(final int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    if (nodeID == RTF_ROOT || nodeID == RTF_TEXT)
      return _text;
    else
      return EMPTY_STR;
  }

  @Override
  public void copy(final int node, SerializationHandler handler) throws TransletException {
    characters(node, handler);
  }

  @Override
  public void copy(DTMAxisIterator nodes, SerializationHandler handler) throws TransletException {
    int node;
    while ((node = nodes.next()) != DTM.NULL) {
      copy(node, handler);
    }
  }

  @Override
  public String shallowCopy(final int node, SerializationHandler handler) throws TransletException {
    characters(node, handler);
    return null;
  }

  @Override
  public boolean lessThan(final int node1, final int node2) {
    if (node1 == DTM.NULL)
      return false;
    else if (node2 == DTM.NULL)
      return true;
    else
      return node1 < node2;
  }

  /**
   * Dispatch the character content of a node to an output handler.
   * 
   * The escape setting should be taken care of when outputting to a handler.
   */
  @Override
  public void characters(final int node, SerializationHandler handler) throws TransletException {
    final int nodeID = getNodeIdent(node);
    if (nodeID == RTF_ROOT || nodeID == RTF_TEXT) {
      boolean escapeBit = false;
      boolean oldEscapeSetting = false;

      try {
        for (int i = 0; i < _size; i++) {

          if (_dontEscape != null) {
            escapeBit = _dontEscape.getBit(i);
            if (escapeBit) {
              oldEscapeSetting = handler.setEscaping(false);
            }
          }

          handler.characters(_textArray[i]);

          if (escapeBit) {
            handler.setEscaping(oldEscapeSetting);
          }
        }
      } catch (final SAXException e) {
        throw new TransletException(e);
      }
    }
  }

  // %REVISIT% Can the makeNode() and makeNodeList() interfaces ever get used?
  @Override
  public Node makeNode(int index) {
    return null;
  }

  @Override
  public Node makeNode(DTMAxisIterator iter) {
    return null;
  }

  @Override
  public NodeList makeNodeList(int index) {
    return null;
  }

  @Override
  public NodeList makeNodeList(DTMAxisIterator iter) {
    return null;
  }

  @Override
  public String getLanguage(int node) {
    return null;
  }

  @Override
  public int getSize() {
    return 2;
  }

  @Override
  public String getDocumentURI(int node) {
    return "simple_rtf" + _documentURIIndex++;
  }

  @Override
  public void setFilter(StripFilter filter) {
  }

  @Override
  public void setupMapping(String[] names, String[] uris, int[] types, String[] namespaces) {
  }

  @Override
  public boolean isElement(final int node) {
    return false;
  }

  @Override
  public boolean isAttribute(final int node) {
    return false;
  }

  @Override
  public String lookupNamespace(int node, String prefix) throws TransletException {
    return null;
  }

  /**
   * Return the node identity from a node handle.
   */
  @Override
  public int getNodeIdent(final int nodehandle) {
    return nodehandle != DTM.NULL ? nodehandle - _documentID : DTM.NULL;
  }

  /**
   * Return the node handle from a node identity.
   */
  @Override
  public int getNodeHandle(final int nodeId) {
    return nodeId != DTM.NULL ? nodeId + _documentID : DTM.NULL;
  }

  @Override
  public DOM getResultTreeFrag(int initialSize, int rtfType) {
    return null;
  }

  @Override
  public DOM getResultTreeFrag(int initialSize, int rtfType, boolean addToManager) {
    return null;
  }

  @Override
  public SerializationHandler getOutputDomBuilder() {
    return this;
  }

  @Override
  public int getNSType(int node) {
    return 0;
  }

  @Override
  public String getUnparsedEntityURI(String name) {
    return null;
  }

  @Override
  public Map<String, Integer> getElementsWithIDs() {
    return null;
  }

  /** Implementation of the SerializationHandler interfaces **/

  /**
   * We only need to override the endDocument, characters, and setEscaping
   * interfaces. A simple RTF does not have element nodes. We do not need to
   * touch startElement and endElement.
   */

  @Override
  public void startDocument() throws SAXException {

  }

  @Override
  public void endDocument() throws SAXException {
    // Set the String value when the document is built.
    if (_size == 1) {
      _text = _textArray[0];
    } else {
      final StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < _size; i++) {
        buffer.append(_textArray[i]);
      }
      _text = buffer.toString();
    }
  }

  @Override
  public void characters(String str) throws SAXException {
    // Resize the text array if necessary
    if (_size >= _textArray.length) {
      final String[] newTextArray = new String[_textArray.length * 2];
      System.arraycopy(_textArray, 0, newTextArray, 0, _textArray.length);
      _textArray = newTextArray;
    }

    // If the escape setting is false, set the corresponding bit in
    // the _dontEscape BitArray.
    if (!_escaping) {
      // The _dontEscape array is only created when needed.
      if (_dontEscape == null) {
        _dontEscape = new BitArray(8);
      }

      // Resize the _dontEscape array if necessary
      if (_size >= _dontEscape.size()) {
        _dontEscape.resize(_dontEscape.size() * 2);
      }

      _dontEscape.setBit(_size);
    }

    _textArray[_size++] = str;
  }

  @Override
  public void characters(char[] ch, int offset, int length) throws SAXException {
    if (_size >= _textArray.length) {
      final String[] newTextArray = new String[_textArray.length * 2];
      System.arraycopy(_textArray, 0, newTextArray, 0, _textArray.length);
      _textArray = newTextArray;
    }

    if (!_escaping) {
      if (_dontEscape == null) {
        _dontEscape = new BitArray(8);
      }

      if (_size >= _dontEscape.size()) {
        _dontEscape.resize(_dontEscape.size() * 2);
      }

      _dontEscape.setBit(_size);
    }

    _textArray[_size++] = new String(ch, offset, length);

  }

  @Override
  public boolean setEscaping(boolean escape) throws SAXException {
    final boolean temp = _escaping;
    _escaping = escape;
    return temp;
  }

  /** Implementation of the DTM interfaces **/

  /**
   * The DTM interfaces are not used in this class. Implementing the DTM
   * interface is a requirement from MultiDOM. If we have a better way of
   * handling multiple documents, we can get rid of the DTM dependency.
   * 
   * The following interfaces are just placeholders. The implementation does not
   * have an impact because they will not be used.
   */

  @Override
  public void setFeature(String featureId, boolean state) {
  }

  @Override
  public void setProperty(String property, Object value) {
  }

  @Override
  public DTMAxisTraverser getAxisTraverser(final Axis axis) {
    return null;
  }

  @Override
  public boolean hasChildNodes(int nodeHandle) {
    return getNodeIdent(nodeHandle) == RTF_ROOT;
  }

  @Override
  public int getFirstChild(int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    if (nodeID == RTF_ROOT)
      return getNodeHandle(RTF_TEXT);
    else
      return DTM.NULL;
  }

  @Override
  public int getLastChild(int nodeHandle) {
    return getFirstChild(nodeHandle);
  }

  @Override
  public int getAttributeNode(int elementHandle, String namespaceURI, String name) {
    return DTM.NULL;
  }

  @Override
  public int getFirstAttribute(int nodeHandle) {
    return DTM.NULL;
  }

  @Override
  public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
    return DTM.NULL;
  }

  @Override
  public int getNextSibling(int nodeHandle) {
    return DTM.NULL;
  }

  @Override
  public int getPreviousSibling(int nodeHandle) {
    return DTM.NULL;
  }

  @Override
  public int getNextAttribute(int nodeHandle) {
    return DTM.NULL;
  }

  @Override
  public int getNextNamespaceNode(int baseHandle, int namespaceHandle, boolean inScope) {
    return DTM.NULL;
  }

  @Override
  public int getOwnerDocument(int nodeHandle) {
    return getDocument();
  }

  @Override
  public int getDocumentRoot(int nodeHandle) {
    return getDocument();
  }

  @Override
  public XMLString getStringValue(int nodeHandle) {
    return new XMLStringDefault(getStringValueX(nodeHandle));
  }

  @Override
  public int getStringValueChunkCount(int nodeHandle) {
    return 0;
  }

  @Override
  public char[] getStringValueChunk(int nodeHandle, int chunkIndex, int[] startAndLen) {
    return null;
  }

  @Override
  public int getExpandedTypeID(String namespace, String localName, int type) {
    return DTM.NULL;
  }

  @Override
  public String getLocalNameFromExpandedNameID(int ExpandedNameID) {
    return EMPTY_STR;
  }

  @Override
  public String getNamespaceFromExpandedNameID(int ExpandedNameID) {
    return EMPTY_STR;
  }

  @Override
  public String getLocalName(int nodeHandle) {
    return EMPTY_STR;
  }

  @Override
  public String getPrefix(int nodeHandle) {
    return null;
  }

  @Override
  public String getNamespaceURI(int nodeHandle) {
    return EMPTY_STR;
  }

  @Override
  public String getNodeValue(int nodeHandle) {
    return getNodeIdent(nodeHandle) == RTF_TEXT ? _text : null;
  }

  @Override
  public short getNodeType(int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    if (nodeID == RTF_TEXT)
      return DTM.TEXT_NODE;
    else if (nodeID == RTF_ROOT)
      return DTM.ROOT_NODE;
    else
      return DTM.NULL;

  }

  @Override
  public short getLevel(int nodeHandle) {
    final int nodeID = getNodeIdent(nodeHandle);
    if (nodeID == RTF_TEXT)
      return 2;
    else if (nodeID == RTF_ROOT)
      return 1;
    else
      return DTM.NULL;
  }

  @Override
  public boolean isSupported(String feature, String version) {
    return false;
  }

  @Override
  public String getDocumentBaseURI() {
    return EMPTY_STR;
  }

  @Override
  public void setDocumentBaseURI(String baseURI) {
  }

  @Override
  public String getDocumentSystemIdentifier(int nodeHandle) {
    return null;
  }

  @Override
  public String getDocumentEncoding(int nodeHandle) {
    return null;
  }

  @Override
  public String getDocumentStandalone(int nodeHandle) {
    return null;
  }

  @Override
  public String getDocumentVersion(int documentHandle) {
    return null;
  }

  @Override
  public boolean getDocumentAllDeclarationsProcessed() {
    return false;
  }

  @Override
  public String getDocumentTypeDeclarationSystemIdentifier() {
    return null;
  }

  @Override
  public String getDocumentTypeDeclarationPublicIdentifier() {
    return null;
  }

  @Override
  public int getElementById(String elementId) {
    return DTM.NULL;
  }

  @Override
  public boolean supportsPreStripping() {
    return false;
  }

  @Override
  public boolean isNodeAfter(int firstNodeHandle, int secondNodeHandle) {
    return lessThan(firstNodeHandle, secondNodeHandle);
  }

  @Override
  public boolean isCharacterElementContentWhitespace(int nodeHandle) {
    return false;
  }

  @Override
  public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {
    return false;
  }

  @Override
  public boolean isAttributeSpecified(int attributeHandle) {
    return false;
  }

  @Override
  public void dispatchCharactersEvents(int nodeHandle, org.xml.sax.ContentHandler ch, boolean normalize)
          throws org.xml.sax.SAXException {
  }

  @Override
  public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch) throws org.xml.sax.SAXException {
  }

  @Override
  public org.w3c.dom.Node getNode(int nodeHandle) {
    return makeNode(nodeHandle);
  }

  @Override
  public boolean needsTwoThreads() {
    return false;
  }

  @Override
  public org.xml.sax.ContentHandler getContentHandler() {
    return null;
  }

  @Override
  public org.xml.sax.ext.LexicalHandler getLexicalHandler() {
    return null;
  }

  @Override
  public org.xml.sax.EntityResolver getEntityResolver() {
    return null;
  }

  @Override
  public org.xml.sax.DTDHandler getDTDHandler() {
    return null;
  }

  @Override
  public org.xml.sax.ErrorHandler getErrorHandler() {
    return null;
  }

  @Override
  public org.xml.sax.ext.DeclHandler getDeclHandler() {
    return null;
  }

  @Override
  public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
  }

  @Override
  public void appendTextChild(String str) {
  }

  @Override
  public SourceLocator getSourceLocatorFor(int node) {
    return null;
  }

  @Override
  public void documentRegistration() {
  }

  @Override
  public void documentRelease() {
  }

  @Override
  public void migrateTo(DTMManager manager) {
  }
}
