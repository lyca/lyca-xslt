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
package de.lyca.xml.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import de.lyca.xml.res.Messages;

/**
 * This class takes SAX events (in addition to some extra events that SAX doesn't handle yet) and adds the result to a
 * document or document fragment.
 */
public class DOMBuilder implements ContentHandler, LexicalHandler {

  /** Root document */
  public Document m_doc;

  /** Current node */
  protected Node m_currentNode = null;

  /** The root node */
  protected Node m_root = null;

  /** The next sibling node */
  protected Node m_nextSibling = null;

  /** First node of document fragment or null if not a DocumentFragment */
  public DocumentFragment m_docFrag = null;

  /** Deque of element nodes */
  protected Deque<Node> m_elemStack = new ArrayDeque<>();

  /** Namespace support */
  protected List<String> m_prefixMappings = new ArrayList<>();

  /** Used to build text nodes efficiently */
  private StringBuilder m_textBuffer = new StringBuilder();

  /**
   * DOMBuilder instance constructor... it will add the DOM nodes to the document fragment.
   * 
   * @param doc Root document
   * @param node Current node
   */
  public DOMBuilder(Document doc, Node node) {
    m_doc = doc;
    m_currentNode = m_root = node;

    if (node instanceof Element) {
      m_elemStack.push(node);
    }
  }

  /**
   * DOMBuilder instance constructor... it will add the DOM nodes to the document fragment.
   * 
   * @param doc Root document
   * @param docFrag Document fragment
   */
  public DOMBuilder(Document doc, DocumentFragment docFrag) {
    m_doc = doc;
    m_docFrag = docFrag;
  }

  /**
   * DOMBuilder instance constructor... it will add the DOM nodes to the document.
   * 
   * @param doc Root document
   */
  public DOMBuilder(Document doc) {
    m_doc = doc;
  }

  /**
   * Get the root document or DocumentFragment of the DOM being created.
   * 
   * @return The root document or document fragment if not null
   */
  public Node getRootDocument() {
    return null != m_docFrag ? (Node) m_docFrag : (Node) m_doc;
  }

  /**
   * Get the root node of the DOM tree.
   */
  public Node getRootNode() {
    return m_root;
  }

  /**
   * Get the node currently being processed.
   * 
   * @return the current node being processed
   */
  public Node getCurrentNode() {
    return m_currentNode;
  }

  /**
   * Set the next sibling node, which is where the result nodes should be inserted before.
   * 
   * @param nextSibling the next sibling node.
   */
  public void setNextSibling(Node nextSibling) {
    m_nextSibling = nextSibling;
  }

  /**
   * Return the next sibling node.
   * 
   * @return the next sibling node.
   */
  public Node getNextSibling() {
    return m_nextSibling;
  }

  /**
   * Return null since there is no Writer for this class.
   * 
   * @return null
   */
  public java.io.Writer getWriter() {
    return null;
  }

  /**
   * Append a node to the current container.
   * 
   * @param newNode New node to append
   * @throws SAXException TODO
   */
  protected void append(Node newNode) throws SAXException {

    final Node currentNode = m_currentNode;

    if (null != currentNode) {
      if (currentNode == m_root && m_nextSibling != null) {
        currentNode.insertBefore(newNode, m_nextSibling);
      } else {
        currentNode.appendChild(newNode);
      }

      // System.out.println(newNode.getNodeName());
    } else if (null != m_docFrag) {
      if (m_nextSibling != null) {
        m_docFrag.insertBefore(newNode, m_nextSibling);
      } else {
        m_docFrag.appendChild(newNode);
      }
    } else {
      boolean ok = true;
      final short type = newNode.getNodeType();

      if (type == Node.TEXT_NODE) {
        final String data = newNode.getNodeValue();

        if (null != data && data.trim().length() > 0)
          // "Warning: can't output text before document element! Ignoring...");
          throw new SAXException(Messages.get().cantOutputTextBeforeDoc());

        ok = false;
      } else if (type == Node.ELEMENT_NODE) {
        if (m_doc.getDocumentElement() != null) {
          ok = false;
          // "Can't have more than one root on a DOM!");
          throw new SAXException(Messages.get().cantHaveMoreThanOneRoot());
        }
      }

      if (ok) {
        if (m_nextSibling != null) {
          m_doc.insertBefore(newNode, m_nextSibling);
        } else {
          m_doc.appendChild(newNode);
        }
      }
    }
  }

  /**
   * Receive an object for locating the origin of SAX document events.
   * 
   * <p>
   * SAX parsers are strongly encouraged (though not absolutely required) to supply a locator: if it does so, it must
   * supply the locator to the application by invoking this method before invoking any of the other methods in the
   * ContentHandler interface.
   * </p>
   * 
   * <p>
   * The locator allows the application to determine the end position of any document-related event, even if the parser
   * is not reporting an error. Typically, the application will use this information for reporting its own errors (such
   * as character content that does not match an application's business rules). The information returned by the locator
   * is probably not sufficient for use with a search engine.
   * </p>
   * 
   * <p>
   * Note that the locator will return correct information only during the invocation of the events in this interface.
   * The application should not attempt to use it at any other time.
   * </p>
   * 
   * @param locator An object that can return the location of any SAX document event.
   * @see org.xml.sax.Locator
   */
  @Override
  public void setDocumentLocator(Locator locator) {

    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, before any other methods in this interface or in DTDHandler
   * (except for setDocumentLocator).
   * </p>
   * 
   * @throws SAXException TODO
   */
  @Override
  public void startDocument() throws SAXException {
    // No action for the moment.
  }

  /**
   * Receive notification of the end of a document.
   * 
   * <p>
   * The SAX parser will invoke this method only once, and it will be the last method invoked during the parse. The
   * parser shall not invoke this method until it has either abandoned parsing (because of an unrecoverable error) or
   * reached the end of input.
   * </p>
   * 
   * @throws SAXException TODO
   */
  @Override
  public void endDocument() throws SAXException {
    // No action for the moment.
  }

  /**
   * Receive notification of the beginning of an element.
   * 
   * <p>
   * The Parser will invoke this method at the beginning of every element in the XML document; there will be a
   * corresponding endElement() event for every startElement() event (even when the element is empty). All of the
   * element's content will be reported, in order, before the corresponding endElement() event.
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be attached. Note that the attribute list
   * provided will contain only attributes with explicit values (specified or defaulted): #IMPLIED attributes will be
   * omitted.
   * </p>
   * 
   * 
   * @param ns The namespace of the node
   * @param localName The local part of the qualified name
   * @param name The element name.
   * @param atts The attributes attached to the element, if any.
   * @throws SAXException TODO
   * @see #endElement
   * @see org.xml.sax.Attributes
   */
  @Override
  public void startElement(String ns, String localName, String name, Attributes atts) throws SAXException {
    appendTextNode();
    Element elem;

    // Note that the namespace-aware call must be used to correctly
    // construct a Level 2 DOM, even for non-namespaced nodes.
    if (null == ns || ns.length() == 0) {
      elem = m_doc.createElementNS(null, name);
    } else {
      elem = m_doc.createElementNS(ns, name);
    }

    append(elem);

    try {
      final int nAtts = atts.getLength();

      if (0 != nAtts) {
        for (int i = 0; i < nAtts; i++) {

          // System.out.println("type " + atts.getType(i) + " name " +
          // atts.getLocalName(i) );
          // First handle a possible ID attribute
          if (atts.getType(i).equalsIgnoreCase("ID")) {
            setIDAttribute(atts.getValue(i), elem);
          }

          String attrNS = atts.getURI(i);

          if ("".equals(attrNS)) {
            attrNS = null; // DOM represents no-namespace as null
          }

          // System.out.println("attrNS: "+attrNS+", localName: "+atts.getQName(i)
          // +", qname: "+atts.getQName(i)+", value: "+atts.getValue(i));
          // Crimson won't let us set an xmlns: attribute on the DOM.
          final String attrQName = atts.getQName(i);

          // In SAX, xmlns[:] attributes have an empty namespace, while in DOM
          // they
          // should have the xmlns namespace
          if (attrQName.startsWith("xmlns:") || attrQName.equals("xmlns")) {
            attrNS = "http://www.w3.org/2000/xmlns/";
          }

          // ALWAYS use the DOM Level 2 call!
          elem.setAttributeNS(attrNS, attrQName, atts.getValue(i));
        }
      }

      /*
       * Adding namespace nodes to the DOM tree;
       */
      final int nDecls = m_prefixMappings.size();

      String prefix, declURL;

      for (int i = 0; i < nDecls; i += 2) {
        prefix = m_prefixMappings.get(i);

        if (prefix == null) {
          continue;
        }

        declURL = m_prefixMappings.get(i + 1);

        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", prefix, declURL);
      }

      m_prefixMappings.clear();

      // append(elem);

      m_elemStack.push(elem);

      m_currentNode = elem;

      // append(elem);
    } catch (final java.lang.Exception de) {
      // de.printStackTrace();
      throw new SAXException(de);
    }

  }

  /**
   * Receive notification of the end of an element.
   * 
   * <p>
   * The SAX parser will invoke this method at the end of every element in the XML document; there will be a
   * corresponding startElement() event for every endElement() event (even when the element is empty).
   * </p>
   * 
   * <p>
   * If the element name has a namespace prefix, the prefix will still be attached to the name.
   * </p>
   * 
   * @param ns the namespace of the element
   * @param localName The local part of the qualified name of the element
   * @param name The element name
   * @throws SAXException TODO
   */
  @Override
  public void endElement(String ns, String localName, String name) throws SAXException {
    appendTextNode();
    m_elemStack.pop();
    m_currentNode = m_elemStack.isEmpty() ? null : (Node) m_elemStack.peek();
  }

  /**
   * Set an ID string to node association in the ID table.
   * 
   * @param id The ID string.
   * @param elem The associated ID.
   */
  public void setIDAttribute(String id, Element elem) {
    // Do nothing. This method is meant to be overiden.
  }

  /**
   * Receive notification of character data.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data. SAX parsers may return all contiguous
   * character data in a single chunk, or they may split it into several chunks; however, all of the characters in any
   * single event must come from the same external entity, so that the Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the ignorableWhitespace() method rather than this one
   * (validating parsers must do so).
   * </p>
   * 
   * @param ch The characters from the XML document.
   * @param start The start position in the array.
   * @param length The number of characters to read from the array.
   * @throws SAXException TODO
   * @see #ignorableWhitespace
   * @see org.xml.sax.Locator
   */
  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    if (isOutsideDocElem() && de.lyca.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    if (m_inCData) {
      cdata(ch, start, length);

      return;
    }

    m_textBuffer.append(ch, start, length);
  }

  private void appendTextNode() throws SAXException {
    if (m_textBuffer.length() > 0) {
      Text text = m_doc.createTextNode(m_textBuffer.toString());
      append(text);
      m_textBuffer.setLength(0);
    }
  }

  /**
   * If available, when the disable-output-escaping attribute is used, output raw text without escaping. A PI will be
   * inserted in front of the node with the name "lotusxsl-next-is-raw" and a value of "formatter-to-dom".
   * 
   * @param ch Array containing the characters
   * @param start Index to start of characters in the array
   * @param length Number of characters in the array
   * @throws SAXException TODO
   */
  public void charactersRaw(char ch[], int start, int length) throws SAXException {
    if (isOutsideDocElem() && de.lyca.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    final String s = new String(ch, start, length);

    append(m_doc.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
    append(m_doc.createTextNode(s));
  }

  /**
   * Report the beginning of an entity.
   * 
   * The start and end of the document entity are not reported. The start and end of the external DTD subset are
   * reported using the pseudo-name "[dtd]". All other events must be properly nested within start/end entity events.
   * 
   * @param name The name of the entity. If it is a parameter entity, the name will begin with '%'.
   * @throws SAXException TODO
   * @see #endEntity
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
   */
  @Override
  public void startEntity(String name) throws SAXException {

    // Almost certainly the wrong behavior...
    // entityReference(name);
  }

  /**
   * Report the end of an entity.
   * 
   * @param name The name of the entity that is ending.
   * @throws SAXException TODO
   * @see #startEntity
   */
  @Override
  public void endEntity(String name) throws SAXException {
  }

  /**
   * Receive notivication of a entityReference.
   * 
   * @param name name of the entity reference
   * @throws SAXException TODO
   */
  public void entityReference(String name) throws SAXException {
    append(m_doc.createEntityReference(name));
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   * 
   * <p>
   * Validating Parsers must use this method to report each chunk of ignorable whitespace (see the W3C XML 1.0
   * recommendation, section 2.10): non-validating parsers may also use this method if they are capable of parsing and
   * using content models.
   * </p>
   * 
   * <p>
   * SAX parsers may return all contiguous whitespace in a single chunk, or they may split it into several chunks;
   * however, all of the characters in any single event must come from the same external entity, so that the Locator
   * provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * @param ch The characters from the XML document.
   * @param start The start position in the array.
   * @param length The number of characters to read from the array.
   * @throws SAXException TODO
   * @see #characters
   */
  @Override
  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    if (isOutsideDocElem())
      return; // avoid DOM006 Hierarchy request error

    final String s = new String(ch, start, length);

    append(m_doc.createTextNode(s));
  }

  /**
   * Tell if the current node is outside the document element.
   * 
   * @return true if the current node is outside the document element.
   */
  private boolean isOutsideDocElem() {
    return null == m_docFrag && m_elemStack.size() == 0
        && (null == m_currentNode || m_currentNode.getNodeType() == Node.DOCUMENT_NODE);
  }

  /**
   * Receive notification of a processing instruction.
   * 
   * <p>
   * The Parser will invoke this method once for each processing instruction found: note that processing instructions
   * may occur before or after the main document element.
   * </p>
   * 
   * <p>
   * A SAX parser should never report an XML declaration (XML 1.0, section 2.8) or a text declaration (XML 1.0, section
   * 4.3.1) using this method.
   * </p>
   * 
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if none was supplied.
   * @throws SAXException TODO
   */
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    appendTextNode();
    append(m_doc.createProcessingInstruction(target, data));
  }

  /**
   * Report an XML comment anywhere in the document.
   * 
   * This callback will be used for comments inside or outside the document element, including comments in the external
   * DTD subset (if read).
   * 
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException TODO
   */
  @Override
  public void comment(char ch[], int start, int length) throws SAXException {
    appendTextNode();
    append(m_doc.createComment(new String(ch, start, length)));
  }

  /** Flag indicating that we are processing a CData section */
  protected boolean m_inCData = false;

  /**
   * Report the start of a CDATA section.
   * 
   * @throws SAXException TODO
   * @see #endCDATA
   */
  @Override
  public void startCDATA() throws SAXException {
    m_inCData = true;
    append(m_doc.createCDATASection(""));
  }

  /**
   * Report the end of a CDATA section.
   * 
   * @throws SAXException TODO
   * @see #startCDATA
   */
  @Override
  public void endCDATA() throws SAXException {
    m_inCData = false;
  }

  /**
   * Receive notification of cdata.
   * 
   * <p>
   * The Parser will call this method to report each chunk of character data. SAX parsers may return all contiguous
   * character data in a single chunk, or they may split it into several chunks; however, all of the characters in any
   * single event must come from the same external entity, so that the Locator provides useful information.
   * </p>
   * 
   * <p>
   * The application must not attempt to read from the array outside of the specified range.
   * </p>
   * 
   * <p>
   * Note that some parsers will report whitespace using the ignorableWhitespace() method rather than this one
   * (validating parsers must do so).
   * </p>
   * 
   * @param ch The characters from the XML document.
   * @param start The start position in the array.
   * @param length The number of characters to read from the array.
   * @throws SAXException TODO
   * @see #ignorableWhitespace
   * @see org.xml.sax.Locator
   */
  public void cdata(char ch[], int start, int length) throws SAXException {
    if (isOutsideDocElem() && de.lyca.xml.utils.XMLCharacterRecognizer.isWhiteSpace(ch, start, length))
      return; // avoid DOM006 Hierarchy request error

    final String s = new String(ch, start, length);

    final CDATASection section = (CDATASection) m_currentNode.getLastChild();
    section.appendData(s);
  }

  /**
   * Report the start of DTD declarations, if any.
   * 
   * Any declarations are assumed to be in the internal subset unless otherwise indicated.
   * 
   * @param name The document type name.
   * @param publicId The declared public identifier for the external DTD subset, or null if none was declared.
   * @param systemId The declared system identifier for the external DTD subset, or null if none was declared.
   * @throws SAXException TODO
   * @see #endDTD
   * @see #startEntity
   */
  @Override
  public void startDTD(String name, String publicId, String systemId) throws SAXException {

    // Do nothing for now.
  }

  /**
   * Report the end of DTD declarations.
   * 
   * @throws SAXException TODO
   * @see #startDTD
   */
  @Override
  public void endDTD() throws SAXException {

    // Do nothing for now.
  }

  /**
   * Begin the scope of a prefix-URI Namespace mapping.
   * 
   * <p>
   * The information from this event is not necessary for normal Namespace processing: the SAX XML reader will
   * automatically replace prefixes for element and attribute names when the http://xml.org/sax/features/namespaces
   * feature is true (the default).
   * </p>
   * 
   * <p>
   * There are cases, however, when applications need to use prefixes in character data or in attribute values, where
   * they cannot safely be expanded automatically; the start/endPrefixMapping event supplies the information to the
   * application to expand prefixes in those contexts itself, if necessary.
   * </p>
   * 
   * <p>
   * Note that start/endPrefixMapping events are not guaranteed to be properly nested relative to each-other: all
   * startPrefixMapping events will occur before the corresponding startElement event, and all endPrefixMapping events
   * will occur after the corresponding endElement event, but their order is not guaranteed.
   * </p>
   * 
   * @param prefix The Namespace prefix being declared.
   * @param uri The Namespace URI the prefix is mapped to.
   * @throws SAXException TODO
   * @see #endPrefixMapping
   * @see #startElement
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (null == prefix || prefix.length() == 0) {
      prefix = "xmlns";
    } else {
      prefix = "xmlns:" + prefix;
    }
    m_prefixMappings.add(prefix);
    m_prefixMappings.add(uri);
  }

  /**
   * End the scope of a prefix-URI mapping.
   * 
   * <p>
   * See startPrefixMapping for details. This event will always occur after the corresponding endElement event, but the
   * order of endPrefixMapping events is not otherwise guaranteed.
   * </p>
   * 
   * @param prefix The prefix that was being mapping.
   * @throws SAXException TODO
   * @see #startPrefixMapping
   * @see #endElement
   */
  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
  }

  /**
   * Receive notification of a skipped entity.
   * 
   * <p>
   * The Parser will invoke this method once for each entity skipped. Non-validating processors may skip entities if
   * they have not seen the declarations (because, for example, the entity was declared in an external DTD subset). All
   * processors may skip external entities, depending on the values of the
   * http://xml.org/sax/features/external-general-entities and the
   * http://xml.org/sax/features/external-parameter-entities properties.
   * </p>
   * 
   * @param name The name of the skipped entity. If it is a parameter entity, the name will begin with '%'.
   * @throws SAXException TODO
   */
  @Override
  public void skippedEntity(String name) throws SAXException {
  }
}
