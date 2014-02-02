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

import java.util.List;

import org.xml.sax.Attributes;

import de.lyca.xalan.templates.Stylesheet;
import de.lyca.xalan.templates.WhiteSpaceInfo;
import de.lyca.xpath.XPath;

/**
 * TransformerFactory for xsl:strip-space markup.
 * 
 * <pre>
 * <!ELEMENT xsl:strip-space EMPTY>
 * <!ATTLIST xsl:strip-space elements CDATA #REQUIRED>
 * </pre>
 */
class ProcessorStripSpace extends ProcessorPreserveSpace {
  static final long serialVersionUID = -5594493198637899591L;

  /**
   * Receive notification of the start of an strip-space element.
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
   */
  @Override
  public void startElement(StylesheetHandler handler, String uri, String localName, String rawName,
          Attributes attributes) throws org.xml.sax.SAXException {
    final Stylesheet thisSheet = handler.getStylesheet();
    final WhitespaceInfoPaths paths = new WhitespaceInfoPaths(thisSheet);
    setPropertiesFromAttributes(handler, rawName, attributes, paths);

    final List<XPath> xpaths = paths.getElements();

    for (int i = 0; i < xpaths.size(); i++) {
      final WhiteSpaceInfo wsi = new WhiteSpaceInfo(xpaths.get(i), true, thisSheet);
      wsi.setUid(handler.nextUid());

      thisSheet.setStripSpaces(wsi);
    }
    paths.clearElements();

  }
}
