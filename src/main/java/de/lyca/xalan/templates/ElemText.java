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
package de.lyca.xalan.templates;

import org.w3c.dom.DOMException;

import de.lyca.xalan.res.XSLTErrorResources;

/**
 * Implement xsl:template. This primarily acts as a marker on the element stack
 * to signal that whitespace should be preserved.
 * 
 * <pre>
 * <!ELEMENT xsl:text (#PCDATA)>
 * <!ATTLIST xsl:text
 *   disable-output-escaping (yes|no) "no"
 * >
 * </pre>
 * 
 * @see <a
 *      href="http://www.w3.org/TR/xslt#section-Creating-Text">section-Creating-Text
 *      in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemText extends ElemTemplateElement {
  static final long serialVersionUID = 1383140876182316711L;

  /**
   * Tells if this element should disable escaping.
   * 
   * @serial
   */
  private boolean m_disableOutputEscaping = false;

  /**
   * Set the "disable-output-escaping" attribute. Normally, the xml output
   * method escapes & and < (and possibly other characters) when outputting text
   * nodes. This ensures that the output is well-formed XML. However, it is
   * sometimes convenient to be able to produce output that is almost, but not
   * quite well-formed XML; for example, the output may include ill-formed
   * sections which are intended to be transformed into well-formed XML by a
   * subsequent non-XML aware process. For this reason, XSLT provides a
   * mechanism for disabling output escaping. An xsl:value-of or xsl:text
   * element may have a disable-output-escaping attribute; the allowed values
   * are yes or no; the default is no; if the value is yes, then a text node
   * generated by instantiating the xsl:value-of or xsl:text element should be
   * output without any escaping.
   * 
   * @see <a
   *      href="http://www.w3.org/TR/xslt#disable-output-escaping">disable-output-escaping
   *      in XSLT Specification</a>
   * 
   * @param v
   *          Boolean flag indicating whether this element should disable
   *          escaping
   */
  public void setDisableOutputEscaping(boolean v) {
    m_disableOutputEscaping = v;
  }

  /**
   * Get the "disable-output-escaping" attribute. Normally, the xml output
   * method escapes & and < (and possibly other characters) when outputting text
   * nodes. This ensures that the output is well-formed XML. However, it is
   * sometimes convenient to be able to produce output that is almost, but not
   * quite well-formed XML; for example, the output may include ill-formed
   * sections which are intended to be transformed into well-formed XML by a
   * subsequent non-XML aware process. For this reason, XSLT provides a
   * mechanism for disabling output escaping. An xsl:value-of or xsl:text
   * element may have a disable-output-escaping attribute; the allowed values
   * are yes or no; the default is no; if the value is yes, then a text node
   * generated by instantiating the xsl:value-of or xsl:text element should be
   * output without any escaping.
   * 
   * @see <a
   *      href="http://www.w3.org/TR/xslt#disable-output-escaping">disable-output-escaping
   *      in XSLT Specification</a>
   * 
   * @return Boolean flag indicating whether this element should disable
   *         escaping
   */
  public boolean getDisableOutputEscaping() {
    return m_disableOutputEscaping;
  }

  /**
   * Get an integer representation of the element type.
   * 
   * @return An integer representation of the element, defined in the Constants
   *         class.
   * @see de.lyca.xalan.templates.Constants
   */
  @Override
  public int getXSLToken() {
    return Constants.ELEMNAME_TEXT;
  }

  /**
   * Return the node name.
   * 
   * @return The element's name
   */
  @Override
  public String getNodeName() {
    return Constants.ELEMNAME_TEXT_STRING;
  }

  /**
   * Add a child to the child list.
   * 
   * @param newChild
   *          Child to add to children list
   * 
   * @return Child added to children list
   * 
   * @throws DOMException
   */
  @Override
  public ElemTemplateElement appendChild(ElemTemplateElement newChild) {

    final int type = newChild.getXSLToken();

    switch (type) {
      case Constants.ELEMNAME_TEXTLITERALRESULT:
        break;
      default:
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[] { newChild.getNodeName(), this.getNodeName() }); // "Can not add "
                                                                                                              // +((ElemTemplateElement)newChild).m_elemName
                                                                                                              // +

        // " to " + this.m_elemName);
    }

    return super.appendChild(newChild);
  }
}
