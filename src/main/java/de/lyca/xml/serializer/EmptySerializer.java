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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class is an adapter class. Its only purpose is to be extended and for
 * that extended class to over-ride all methods that are to be used.
 * 
 * This class is not a public API, it is only public because it is used across
 * package boundaries.
 */
public class EmptySerializer implements SerializationHandler {
  protected static final String ERR = "EmptySerializer method not over-ridden";

  /**
   * @see SerializationHandler#asContentHandler()
   */

  protected void couldThrowIOException() throws IOException {
    return; // don't do anything.
  }

  protected void couldThrowSAXException() throws SAXException {
    return; // don't do anything.
  }

  protected void couldThrowSAXException(char[] chars, int off, int len) throws SAXException {
    return; // don't do anything.
  }

  protected void couldThrowSAXException(String elemQName) throws SAXException {
    return; // don't do anything.
  }

  protected void couldThrowException() throws Exception {
    return; // don't do anything.
  }

  void aMethodIsCalled() {

    // throw new RuntimeException(err);
    return;
  }

  /**
   * @see SerializationHandler#asContentHandler()
   */
  @Override
  public ContentHandler asContentHandler() throws IOException {
    couldThrowIOException();
    return null;
  }

  /**
   * @see SerializationHandler#setContentHandler(org.xml.sax.ContentHandler)
   */
  @Override
  public void setContentHandler(ContentHandler ch) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#close()
   */
  @Override
  public void close() {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#getOutputFormat()
   */
  @Override
  public Properties getOutputFormat() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see SerializationHandler#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see SerializationHandler#getWriter()
   */
  @Override
  public Writer getWriter() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see SerializationHandler#reset()
   */
  @Override
  public boolean reset() {
    aMethodIsCalled();
    return false;
  }

  /**
   * @see SerializationHandler#serialize(org.w3c.dom.Node)
   */
  @Override
  public void serialize(Node node) throws IOException {
    couldThrowIOException();
  }

  /**
   * @see SerializationHandler#setCdataSectionElements(java.util.List)
   */
  @Override
  public void setCdataSectionElements(List<String> URI_and_localNames) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setEscaping(boolean)
   */
  @Override
  public boolean setEscaping(boolean escape) throws SAXException {
    couldThrowSAXException();
    return false;
  }

  /**
   * @see SerializationHandler#setIndent(boolean)
   */
  @Override
  public void setIndent(boolean indent) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setIndentAmount(int)
   */
  @Override
  public void setIndentAmount(int spaces) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setOutputFormat(java.util.Properties)
   */
  @Override
  public void setOutputFormat(Properties format) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setOutputStream(java.io.OutputStream)
   */
  @Override
  public void setOutputStream(OutputStream output) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setVersion(java.lang.String)
   */
  @Override
  public void setVersion(String version) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setWriter(java.io.Writer)
   */
  @Override
  public void setWriter(Writer writer) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#setTransformer(javax.xml.transform.Transformer)
   */
  @Override
  public void setTransformer(Transformer transformer) {
    aMethodIsCalled();
  }

  /**
   * @see SerializationHandler#getTransformer()
   */
  @Override
  public Transformer getTransformer() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see SerializationHandler#flushPending()
   */
  @Override
  public void flushPending() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#addAttribute(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addAttribute(String uri, String localName, String rawName, String type, String value, boolean XSLAttribute)
          throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#addAttributes(org.xml.sax.Attributes)
   */
  @Override
  public void addAttributes(Attributes atts) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#addAttribute(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addAttribute(String name, String value) {
    aMethodIsCalled();
  }

  /**
   * @see ExtendedContentHandler#characters(java.lang.String)
   */
  @Override
  public void characters(String chars) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#endElement(java.lang.String)
   */
  @Override
  public void endElement(String elemName) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#startDocument()
   */
  @Override
  public void startDocument() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void startElement(String uri, String localName, String qName) throws SAXException {
    couldThrowSAXException(qName);
  }

  /**
   * @see ExtendedContentHandler#startElement(java.lang.String)
   */
  @Override
  public void startElement(String qName) throws SAXException {
    couldThrowSAXException(qName);
  }

  /**
   * @see ExtendedContentHandler#namespaceAfterStartElement(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void namespaceAfterStartElement(String uri, String prefix) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#startPrefixMapping(java.lang.String,
   *      java.lang.String, boolean)
   */
  @Override
  public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush) throws SAXException {
    couldThrowSAXException();
    return false;
  }

  /**
   * @see ExtendedContentHandler#entityReference(java.lang.String)
   */
  @Override
  public void entityReference(String entityName) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#getNamespaceMappings()
   */
  @Override
  public NamespaceMappings getNamespaceMappings() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see ExtendedContentHandler#getPrefix(java.lang.String)
   */
  @Override
  public String getPrefix(String uri) {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see ExtendedContentHandler#getNamespaceURI(java.lang.String, boolean)
   */
  @Override
  public String getNamespaceURI(String name, boolean isElement) {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see ExtendedContentHandler#getNamespaceURIFromPrefix(java.lang.String)
   */
  @Override
  public String getNamespaceURIFromPrefix(String prefix) {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  @Override
  public void setDocumentLocator(Locator arg0) {
    aMethodIsCalled();
  }

  /**
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
  public void endDocument() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void startPrefixMapping(String arg0, String arg1) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  @Override
  public void endPrefixMapping(String arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(String arg0, String arg1, String arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
  public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
    couldThrowSAXException(arg0, arg1, arg2);
  }

  /**
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  @Override
  public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void processingInstruction(String arg0, String arg1) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  @Override
  public void skippedEntity(String arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedLexicalHandler#comment(java.lang.String)
   */
  @Override
  public void comment(String comment) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void startDTD(String arg0, String arg1, String arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#endDTD()
   */
  @Override
  public void endDTD() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
   */
  @Override
  public void startEntity(String arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
   */
  @Override
  public void endEntity(String arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#startCDATA()
   */
  @Override
  public void startCDATA() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#endCDATA()
   */
  @Override
  public void endCDATA() throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
   */
  @Override
  public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see XSLOutputAttributes#getDoctypePublic()
   */
  @Override
  public String getDoctypePublic() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#getDoctypeSystem()
   */
  @Override
  public String getDoctypeSystem() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#getEncoding()
   */
  @Override
  public String getEncoding() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#getIndent()
   */
  @Override
  public boolean getIndent() {
    aMethodIsCalled();
    return false;
  }

  /**
   * @see XSLOutputAttributes#getIndentAmount()
   */
  @Override
  public int getIndentAmount() {
    aMethodIsCalled();
    return 0;
  }

  /**
   * @see XSLOutputAttributes#getMediaType()
   */
  @Override
  public String getMediaType() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#getOmitXMLDeclaration()
   */
  @Override
  public boolean getOmitXMLDeclaration() {
    aMethodIsCalled();
    return false;
  }

  /**
   * @see XSLOutputAttributes#getStandalone()
   */
  @Override
  public String getStandalone() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#getVersion()
   */
  @Override
  public String getVersion() {
    aMethodIsCalled();
    return null;
  }

  /**
   * @see XSLOutputAttributes#setDoctype(java.lang.String, java.lang.String)
   */
  @Override
  public void setDoctype(String system, String pub) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setDoctypePublic(java.lang.String)
   */
  @Override
  public void setDoctypePublic(String doctype) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setDoctypeSystem(java.lang.String)
   */
  @Override
  public void setDoctypeSystem(String doctype) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setEncoding(java.lang.String)
   */
  @Override
  public void setEncoding(String encoding) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setMediaType(java.lang.String)
   */
  @Override
  public void setMediaType(String mediatype) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setOmitXMLDeclaration(boolean)
   */
  @Override
  public void setOmitXMLDeclaration(boolean b) {
    aMethodIsCalled();
  }

  /**
   * @see XSLOutputAttributes#setStandalone(java.lang.String)
   */
  @Override
  public void setStandalone(String standalone) {
    aMethodIsCalled();
  }

  /**
   * @see org.xml.sax.ext.DeclHandler#elementDecl(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void elementDecl(String arg0, String arg1) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.DeclHandler#attributeDecl(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void attributeDecl(String arg0, String arg1, String arg2, String arg3, String arg4) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void internalEntityDecl(String arg0, String arg1) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void externalEntityDecl(String arg0, String arg1, String arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  @Override
  public void warning(SAXParseException arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  @Override
  public void error(SAXParseException arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  @Override
  public void fatalError(SAXParseException arg0) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see Serializer#asDOMSerializer()
   */
  @Override
  public DOMSerializer asDOMSerializer() throws IOException {
    couldThrowIOException();
    return null;
  }

  /**
   * @see SerializationHandler#setNamespaceMappings(NamespaceMappings)
   */
  @Override
  public void setNamespaceMappings(NamespaceMappings mappings) {
    aMethodIsCalled();
  }

  /**
   * @see ExtendedContentHandler#setSourceLocator(javax.xml.transform.SourceLocator)
   */
  @Override
  public void setSourceLocator(SourceLocator locator) {
    aMethodIsCalled();
  }

  /**
   * @see ExtendedContentHandler#addUniqueAttribute(java.lang.String,
   *      java.lang.String, int)
   */
  @Override
  public void addUniqueAttribute(String name, String value, int flags) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#characters(org.w3c.dom.Node)
   */
  @Override
  public void characters(Node node) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see ExtendedContentHandler#addXSLAttribute(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void addXSLAttribute(String qName, String value, String uri) {
    aMethodIsCalled();
  }

  /**
   * @see ExtendedContentHandler#addAttribute(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addAttribute(String uri, String localName, String rawName, String type, String value) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
    couldThrowSAXException();
  }

  /**
   * @see SerializationHandler#setDTDEntityExpansion(boolean)
   */
  @Override
  public void setDTDEntityExpansion(boolean expand) {
    aMethodIsCalled();

  }

  @Override
  public String getOutputProperty(String name) {
    aMethodIsCalled();
    return null;
  }

  @Override
  public String getOutputPropertyDefault(String name) {
    aMethodIsCalled();
    return null;
  }

  @Override
  public void setOutputProperty(String name, String val) {
    aMethodIsCalled();

  }

  @Override
  public void setOutputPropertyDefault(String name, String val) {
    aMethodIsCalled();

  }

  /**
   * @see de.lyca.xml.serializer.Serializer#asDOM3Serializer()
   */
  @Override
  public Object asDOM3Serializer() throws IOException {
    couldThrowIOException();
    return null;
  }
}
