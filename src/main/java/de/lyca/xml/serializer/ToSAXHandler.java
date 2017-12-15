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
package de.lyca.xml.serializer;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class is used to provide a base behavior to be inherited by other
 * To...SAXHandler serializers.
 * 
 * This class is not a public API.
 * 
 * @xsl.usage internal
 */
public abstract class ToSAXHandler extends SerializerBase {
  public ToSAXHandler() {
  }

  public ToSAXHandler(ContentHandler hdlr, LexicalHandler lex, String encoding) {
    setContentHandler(hdlr);
    setLexHandler(lex);
    setEncoding(encoding);
  }

  public ToSAXHandler(ContentHandler handler, String encoding) {
    setContentHandler(handler);
    setEncoding(encoding);
  }

  /**
   * Underlying SAX handler. Taken from XSLTC
   */
  protected ContentHandler m_saxHandler;

  /**
   * Underlying LexicalHandler. Taken from XSLTC
   */
  protected LexicalHandler m_lexHandler;

  /**
   * A startPrefixMapping() call on a ToSAXHandler will pass that call on to the
   * wrapped ContentHandler, but should we also mirror these calls with matching
   * attributes, if so this field is true. For example if this field is true
   * then a call such as startPrefixMapping("prefix1","uri1") will also cause
   * the additional internally generated attribute xmlns:prefix1="uri1" to be
   * effectively added to the attributes passed to the wrapped ContentHandler.
   */
  private boolean m_shouldGenerateNSAttribute = true;

  /**
   * Pass callback to the SAX Handler
   */
  @Override
  protected void startDocumentInternal() throws SAXException {
    if (m_needToCallStartDocument) {
      super.startDocumentInternal();

      m_saxHandler.startDocument();
      m_needToCallStartDocument = false;
    }
  }

  /**
   * Do nothing.
   * 
   * @see org.xml.sax.ext.LexicalHandler#startDTD(String, String, String)
   */
  @Override
  public void startDTD(String arg0, String arg1, String arg2) throws SAXException {
    // do nothing for now
  }

  /**
   * Receive notification of character data.
   * 
   * @param characters
   *          The string of characters to process.
   * 
   * @throws org.xml.sax.SAXException
   * 
   * @see ExtendedContentHandler#characters(String)
   */
  @Override
  public void characters(String characters) throws SAXException {
    final int len = characters.length();
    if (len > m_charsBuff.length) {
      m_charsBuff = new char[len * 2 + 1];
    }
    characters.getChars(0, len, m_charsBuff, 0);
    characters(m_charsBuff, 0, len);
  }

  /**
   * Receive notification of a comment.
   * 
   * @see ExtendedLexicalHandler#comment(String)
   */
  @Override
  public void comment(String comment) throws SAXException {
    flushPending();

    // Ignore if a lexical handler has not been set
    if (m_lexHandler != null) {
      final int len = comment.length();
      if (len > m_charsBuff.length) {
        m_charsBuff = new char[len * 2 + 1];
      }
      comment.getChars(0, len, m_charsBuff, 0);
      m_lexHandler.comment(m_charsBuff, 0, len);
      // time to fire off comment event
      if (m_tracer != null) {
        super.fireCommentEvent(m_charsBuff, 0, len);
      }
    }

  }

  /**
   * Do nothing as this is an abstract class. All subclasses will need to define
   * their behavior if it is different.
   * 
   * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
   */
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    // Redefined in SAXXMLOutput
  }

  protected void closeStartTag() throws SAXException {
  }

  protected void closeCDATA() throws SAXException {
    // Redefined in SAXXMLOutput
  }

  /**
   * Receive notification of the beginning of an element, although this is a SAX
   * method additional namespace or attribute information can occur before or
   * after this call, that is associated with this element.
   * 
   * @throws org.xml.sax.SAXException
   *           Any SAX exception, possibly wrapping another exception.
   * @see org.xml.sax.ContentHandler#startElement
   * @see org.xml.sax.ContentHandler#endElement
   * @see org.xml.sax.AttributeList
   * 
   * @throws org.xml.sax.SAXException
   * 
   * @see org.xml.sax.ContentHandler#startElement(String,String,String,Attributes)
   */
  @Override
  public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
    // fire off the start element event
    if (m_tracer != null) {
      super.fireStartElem(arg2);
    }
  }

  /**
   * Sets the LexicalHandler.
   * 
   * @param _lexHandler
   *          The LexicalHandler to set
   */
  public void setLexHandler(LexicalHandler _lexHandler) {
    m_lexHandler = _lexHandler;
  }

  /**
   * Sets the SAX ContentHandler.
   * 
   * @param _saxHandler
   *          The ContentHandler to set
   */
  @Override
  public void setContentHandler(ContentHandler _saxHandler) {
    m_saxHandler = _saxHandler;
    if (m_lexHandler == null && _saxHandler instanceof LexicalHandler) {
      // we are not overwriting an existing LexicalHandler, and _saxHandler
      // is also implements LexicalHandler, so lets use it
      m_lexHandler = (LexicalHandler) _saxHandler;
    }
  }

  /**
   * Does nothing. The setting of CDATA section elements has an impact on stream
   * serializers.
   * 
   * @see SerializationHandler#setCdataSectionElements(java.util.List)
   */
  @Override
  public void setCdataSectionElements(List<String> URI_and_localNames) {
    // do nothing
  }

  /**
   * Set whether or not namespace declarations (e.g. xmlns:foo) should appear as
   * attributes of elements
   * 
   * @param doOutputNSAttr
   *          whether or not namespace declarations should appear as attributes
   */
  public void setShouldOutputNSAttr(boolean doOutputNSAttr) {
    m_shouldGenerateNSAttribute = doOutputNSAttr;
  }

  /**
   * Returns true if namespace declarations from calls such as
   * startPrefixMapping("prefix1","uri1") should also be mirrored with self
   * generated additional attributes of elements that declare the namespace, for
   * example the attribute xmlns:prefix1="uri1"
   */
  boolean getShouldOutputNSAttr() {
    return m_shouldGenerateNSAttribute;
  }

  /**
   * This method flushes any pending events, which can be startDocument()
   * closing the opening tag of an element, or closing an open CDATA section.
   */
  @Override
  public void flushPending() throws SAXException {

    if (m_needToCallStartDocument) {
      startDocumentInternal();
      m_needToCallStartDocument = false;
    }

    if (m_elemContext.m_startTagOpen) {
      closeStartTag();
      m_elemContext.m_startTagOpen = false;
    }

    if (m_cdataTagOpen) {
      closeCDATA();
      m_cdataTagOpen = false;
    }

  }

  /**
   * Receives notification that an element starts, but attributes are not fully
   * known yet.
   * 
   * @param uri
   *          the URI of the namespace of the element (optional)
   * @param localName
   *          the element name, but without prefix (optional)
   * @param qName
   *          the element name, with prefix, if any (required)
   * 
   * @see ExtendedContentHandler#startElement(String, String, String)
   */
  @Override
  public void startElement(String uri, String localName, String qName) throws SAXException {
    // fire off the start element event
    if (m_tracer != null) {
      super.fireStartElem(qName);
    }
  }

  /**
   * An element starts, but attributes are not fully known yet.
   * 
   * @param qName
   *          the element name, with prefix (if any).
   * 
   * @see ExtendedContentHandler#startElement(String)
   */
  @Override
  public void startElement(String qName) throws SAXException {
    // fire off the start element event
    if (m_tracer != null) {
      super.fireStartElem(qName);
    }
  }

  /**
   * This method gets the node's value as a String and uses that String as if it
   * were an input character notification.
   * 
   * @param node
   *          the Node to serialize
   * @throws org.xml.sax.SAXException
   */
  @Override
  public void characters(org.w3c.dom.Node node) throws org.xml.sax.SAXException {
    // Get the node's value as a String and use that String as if
    // it were an input character notification.
    final String data = node.getNodeValue();
    if (data != null) {
      this.characters(data);
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
   */
  @Override
  public void fatalError(SAXParseException exc) throws SAXException {
    super.fatalError(exc);

    m_needToCallStartDocument = false;

    if (m_saxHandler instanceof ErrorHandler) {
      ((ErrorHandler) m_saxHandler).fatalError(exc);
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(SAXParseException)
   */
  @Override
  public void error(SAXParseException exc) throws SAXException {
    super.error(exc);

    if (m_saxHandler instanceof ErrorHandler) {
      ((ErrorHandler) m_saxHandler).error(exc);
    }

  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
   */
  @Override
  public void warning(SAXParseException exc) throws SAXException {
    super.warning(exc);

    if (m_saxHandler instanceof ErrorHandler) {
      ((ErrorHandler) m_saxHandler).warning(exc);
    }
  }

  /**
   * Try's to reset the super class and reset this class for re-use, so that you
   * don't need to create a new serializer (mostly for performance reasons).
   * 
   * @return true if the class was successfuly reset.
   * @see Serializer#reset()
   */
  @Override
  public boolean reset() {
    boolean wasReset = false;
    if (super.reset()) {
      resetToSAXHandler();
      wasReset = true;
    }
    return wasReset;
  }

  /**
   * Reset all of the fields owned by ToSAXHandler class
   * 
   */
  private void resetToSAXHandler() {
    m_lexHandler = null;
    m_saxHandler = null;
    m_shouldGenerateNSAttribute = false;
  }

  /**
   * Add a unique attribute
   */
  @Override
  public void addUniqueAttribute(String qName, String value, int flags) throws SAXException {
    addAttribute(qName, value);
  }
}
