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
package de.lyca.xalan.processor;

import javax.xml.transform.TransformerException;

import org.xml.sax.Attributes;

import de.lyca.xalan.templates.ElemAttributeSet;
import de.lyca.xalan.templates.ElemTemplateElement;

/**
 * This class processes parse events for an xsl:attribute-set.
 * 
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in
 *      XSLT Specification</a>
 */
class ProcessorAttributeSet extends XSLTElementProcessor {
  static final long serialVersionUID = -6473739251316787552L;

  /**
   * Receive notification of the start of an xsl:attribute-set element.
   * 
   * @param handler
   *          The calling StylesheetHandler/TemplatesBuilder.
   * @param uri
   *          The Namespace URI, or the empty string if the element has no
   *          Namespace URI or if Namespace processing is not being performed.
   * @param localName
   *          The local name (without prefix), or the empty string if Namespace
   *          processing is not being performed.
   * @param rawName
   *          The raw XML 1.0 name (with prefix), or the empty string if raw
   *          names are not available.
   * @param attributes
   *          The attributes attached to the element. If there are no
   *          attributes, it shall be an empty Attributes object.
   * 
   * @see de.lyca.xalan.processor.StylesheetHandler#startElement
   * @see org.xml.sax.ContentHandler#startElement
   * @see org.xml.sax.ContentHandler#endElement
   * @see org.xml.sax.Attributes
   */
  @Override
  public void startElement(StylesheetHandler handler, String uri, String localName, String rawName,
          Attributes attributes) throws org.xml.sax.SAXException {

    final ElemAttributeSet eat = new ElemAttributeSet();

    eat.setLocaterInfo(handler.getLocator());
    try {
      eat.setPrefixes(handler.getNamespaceSupport());
    } catch (final TransformerException te) {
      throw new org.xml.sax.SAXException(te);
    }

    eat.setDOMBackPointer(handler.getOriginatingNode());
    setPropertiesFromAttributes(handler, rawName, attributes, eat);
    handler.getStylesheet().setAttributeSet(eat);

    // handler.pushElemTemplateElement(eat);
    final ElemTemplateElement parent = handler.getElemTemplateElement();

    parent.appendChild(eat);
    handler.pushElemTemplateElement(eat);
  }

  /**
   * Receive notification of the end of an element.
   * 
   * @param name
   *          The element type name.
   * @param attributes
   *          The specified or defaulted attributes.
   * 
   * @param handler
   *          non-null reference to current StylesheetHandler that is
   *          constructing the Templates.
   * @param uri
   *          The Namespace URI, or an empty string.
   * @param localName
   *          The local name (without prefix), or empty string if not namespace
   *          processing.
   * @param rawName
   *          The qualified name (with prefix).
   */
  @Override
  public void endElement(StylesheetHandler handler, String uri, String localName, String rawName)
          throws org.xml.sax.SAXException {
    handler.popElemTemplateElement();
  }
}
