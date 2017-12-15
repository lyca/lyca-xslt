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
package de.lyca.xalan.xsltc.trax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import de.lyca.xalan.xsltc.runtime.Constants;

/**
 * @author G. Todd Miller
 */
public class SAX2DOM implements ContentHandler, LexicalHandler, Constants {

  private Node _root = null;
  private Document _document = null;
  private Node _nextSibling = null;
  private final Deque<Node> _nodeStk = new ArrayDeque<>();
  private List<String> _namespaceDecls = null;
  private StringBuilder _textBuffer = new StringBuilder();
  private Node _nextSiblingCache = null;

  public SAX2DOM() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    _document = factory.newDocumentBuilder().newDocument();
    _root = _document;
  }

  public SAX2DOM(Node root, Node nextSibling) throws ParserConfigurationException {
    _root = root;
    if (root instanceof Document) {
      _document = (Document) root;
    } else if (root != null) {
      _document = root.getOwnerDocument();
    } else {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      _document = factory.newDocumentBuilder().newDocument();
      _root = _document;
    }

    _nextSibling = nextSibling;
  }

  public SAX2DOM(Node root) throws ParserConfigurationException {
    this(root, null);
  }

  public Node getDOM() {
    return _root;
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    final Node last = _nodeStk.peek();

    // No text nodes can be children of root (DOM006 exception)
    if (last != _document) {
      _nextSiblingCache = _nextSibling;
      _textBuffer.append(ch, start, length);
    }
  }

  private void appendTextNode() {
    if (_textBuffer.length() > 0) {
      final Node last = (Node) _nodeStk.peek();
      if (last == _root && _nextSiblingCache != null) {
        last.insertBefore(_document.createTextNode(_textBuffer.toString()), _nextSiblingCache);
      } else {
        last.appendChild(_document.createTextNode(_textBuffer.toString()));
      }
      _textBuffer.setLength(0);
    }
  }

  @Override
  public void startDocument() {
    _nodeStk.push(_root);
  }

  @Override
  public void endDocument() {
    _nodeStk.pop();
  }

  @Override
  public void startElement(String namespace, String localName, String qName, Attributes attrs) {
    appendTextNode();
    final Element tmp = _document.createElementNS(namespace, qName);

    // Add namespace declarations first
    if (_namespaceDecls != null) {
      final int nDecls = _namespaceDecls.size();
      for (int i = 0; i < nDecls; i++) {
        final String prefix = _namespaceDecls.get(i++);

        if (prefix == null || prefix.isEmpty()) {
          tmp.setAttributeNS(XMLNS_URI, XMLNS_PREFIX, _namespaceDecls.get(i));
        } else {
          tmp.setAttributeNS(XMLNS_URI, XMLNS_STRING + prefix, _namespaceDecls.get(i));
        }
      }
      _namespaceDecls.clear();
    }

    // Add attributes to element
    final int nattrs = attrs.getLength();
    for (int i = 0; i < nattrs; i++) {
      if (attrs.getLocalName(i) == null) {
        tmp.setAttribute(attrs.getQName(i), attrs.getValue(i));
      } else {
        tmp.setAttributeNS(attrs.getURI(i), attrs.getQName(i), attrs.getValue(i));
      }
    }

    // Append this new node onto current stack node
    final Node last = _nodeStk.peek();

    // If the SAX2DOM is created with a non-null next sibling node,
    // insert the result nodes before the next sibling under the root.
    if (last == _root && _nextSibling != null) {
      last.insertBefore(tmp, _nextSibling);
    } else {
      last.appendChild(tmp);
    }

    // Push this node onto stack
    _nodeStk.push(tmp);
  }

  @Override
  public void endElement(String namespace, String localName, String qName) {
    appendTextNode();
    _nodeStk.pop();
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) {
    if (_namespaceDecls == null) {
      _namespaceDecls = new ArrayList<>(2);
    }
    _namespaceDecls.add(prefix);
    _namespaceDecls.add(uri);
  }

  @Override
  public void endPrefixMapping(String prefix) {
    // do nothing
  }

  /**
   * This class is only used internally so this method should never be called.
   */
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) {
  }

  /**
   * adds processing instruction node to DOM.
   */
  @Override
  public void processingInstruction(String target, String data) {
    appendTextNode();
    final Node last = _nodeStk.peek();
    final ProcessingInstruction pi = _document.createProcessingInstruction(target, data);
    if (pi != null) {
      if (last == _root && _nextSibling != null) {
        last.insertBefore(pi, _nextSibling);
      } else {
        last.appendChild(pi);
      }
    }
  }

  /**
   * This class is only used internally so this method should never be called.
   */
  @Override
  public void setDocumentLocator(Locator locator) {
  }

  /**
   * This class is only used internally so this method should never be called.
   */
  @Override
  public void skippedEntity(String name) {
  }

  /**
   * Lexical Handler method to create comment node in DOM tree.
   */
  @Override
  public void comment(char[] ch, int start, int length) {
    appendTextNode();
    final Node last = _nodeStk.peek();
    final Comment comment = _document.createComment(new String(ch, start, length));
    if (comment != null) {
      if (last == _root && _nextSibling != null) {
        last.insertBefore(comment, _nextSibling);
      } else {
        last.appendChild(comment);
      }
    }
  }

  // Lexical Handler methods- not implemented
  @Override
  public void startCDATA() {
  }

  @Override
  public void endCDATA() {
  }

  @Override
  public void startEntity(java.lang.String name) {
  }

  @Override
  public void endDTD() {
  }

  @Override
  public void endEntity(String name) {
  }

  @Override
  public void startDTD(String name, String publicId, String systemId) throws SAXException {
  }

}
