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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.templates.KeyDeclaration;

/**
 * TransformerFactory for xsl:key markup.
 * 
 * <pre>
 * <!ELEMENT xsl:key EMPTY>
 * <!ATTLIST xsl:key
 *   name %qname; #REQUIRED
 *   match %pattern; #REQUIRED
 *   use %expr; #REQUIRED
 * >
 * </pre>
 * 
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
 */
class ProcessorKey extends XSLTElementProcessor {
  static final long serialVersionUID = 4285205417566822979L;

  /**
   * Receive notification of the start of an xsl:key element.
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

    final KeyDeclaration kd = new KeyDeclaration(handler.getStylesheet(), handler.nextUid());

    kd.setDOMBackPointer(handler.getOriginatingNode());
    kd.setLocaterInfo(handler.getLocator());
    setPropertiesFromAttributes(handler, rawName, attributes, kd);
    handler.getStylesheet().setKey(kd);
  }

  /**
   * Set the properties of an object from the given attribute list.
   * 
   * @param handler
   *          The stylesheet's Content handler, needed for error reporting.
   * @param rawName
   *          The raw name of the owner element, needed for error reporting.
   * @param attributes
   *          The list of attributes.
   * @param target
   *          The target element where the properties will be set.
   */
  @Override
  void setPropertiesFromAttributes(StylesheetHandler handler, String rawName, Attributes attributes,
          de.lyca.xalan.templates.ElemTemplateElement target) throws org.xml.sax.SAXException {

    final XSLTElementDef def = getElemDef();

    // Keep track of which XSLTAttributeDefs have been processed, so
    // I can see which default values need to be set.
    final List<XSLTAttributeDef> processedDefs = new ArrayList<>();
    final int nAttrs = attributes.getLength();

    for (int i = 0; i < nAttrs; i++) {
      final String attrUri = attributes.getURI(i);
      final String attrLocalName = attributes.getLocalName(i);
      final XSLTAttributeDef attrDef = def.getAttributeDef(attrUri, attrLocalName);

      if (null == attrDef) {

        // Then barf, because this element does not allow this attribute.
        handler.error(attributes.getQName(i) + "attribute is not allowed on the " + rawName + " element!", null);
      } else {
        final String valueString = attributes.getValue(i);

        if (valueString.indexOf(de.lyca.xpath.compiler.Keywords.FUNC_KEY_STRING + "(") >= 0) {
          handler.error(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_KEY_CALL, null), null);
        }

        processedDefs.add(attrDef);
        attrDef.setAttrValue(handler, attrUri, attrLocalName, attributes.getQName(i), attributes.getValue(i), target);
      }
    }

    final XSLTAttributeDef[] attrDefs = def.getAttributes();
    final int nAttrDefs = attrDefs.length;

    for (int i = 0; i < nAttrDefs; i++) {
      final XSLTAttributeDef attrDef = attrDefs[i];
      final String defVal = attrDef.getDefault();

      if (null != defVal) {
        if (!processedDefs.contains(attrDef)) {
          attrDef.setDefAttrValue(handler, target);
        }
      }

      if (attrDef.getRequired()) {
        if (!processedDefs.contains(attrDef)) {
          handler.error(
                  XSLMessages.createMessage(XSLTErrorResources.ER_REQUIRES_ATTRIB,
                          new Object[] { rawName, attrDef.getName() }), null);
        }
      }
    }
  }
}