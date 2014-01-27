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

import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;

import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.transformer.TransformerImpl;

/**
 * Implement xsl:comment.
 * 
 * <pre>
 * <!ELEMENT xsl:comment %char-template;>
 * <!ATTLIST xsl:comment %space-att;>
 * </pre>
 * 
 * @see <a
 *      href="http://www.w3.org/TR/xslt#section-Creating-Comments">section-Creating-Comments
 *      in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemComment extends ElemTemplateElement {
  static final long serialVersionUID = -8813199122875770142L;

  /**
   * Get an int constant identifying the type of element.
   * 
   * @see de.lyca.xalan.templates.Constants
   * 
   * @return The token ID for this element
   */
  @Override
  public int getXSLToken() {
    return Constants.ELEMNAME_COMMENT;
  }

  /**
   * Return the node name.
   * 
   * @return This element's name
   */
  @Override
  public String getNodeName() {
    return Constants.ELEMNAME_COMMENT_STRING;
  }

  /**
   * Execute the xsl:comment transformation
   * 
   * 
   * @param transformer
   *          non-null reference to the the current transform-time state.
   * 
   * @throws TransformerException
   */
  @Override
  public void execute(TransformerImpl transformer) throws TransformerException {
    if (transformer.getDebug()) {
      transformer.getTraceManager().fireTraceEvent(this);
    }
    try {
      // Note the content model is:
      // <!ENTITY % instructions "
      // %char-instructions;
      // | xsl:processing-instruction
      // | xsl:comment
      // | xsl:element
      // | xsl:attribute
      // ">
      final String data = transformer.transformToString(this);

      transformer.getResultTreeHandler().comment(data);
    } catch (final org.xml.sax.SAXException se) {
      throw new TransformerException(se);
    } finally {
      if (transformer.getDebug()) {
        transformer.getTraceManager().fireTraceEndEvent(this);
      }
    }
  }

  /**
   * Add a child to the child list.
   * 
   * @param newChild
   *          Child to add to this node's child list
   * 
   * @return Child that was just added to child list
   * 
   * @throws DOMException
   */
  @Override
  public ElemTemplateElement appendChild(ElemTemplateElement newChild) {

    final int type = newChild.getXSLToken();

    switch (type) {

    // char-instructions
      case Constants.ELEMNAME_TEXTLITERALRESULT:
      case Constants.ELEMNAME_APPLY_TEMPLATES:
      case Constants.ELEMNAME_APPLY_IMPORTS:
      case Constants.ELEMNAME_CALLTEMPLATE:
      case Constants.ELEMNAME_FOREACH:
      case Constants.ELEMNAME_VALUEOF:
      case Constants.ELEMNAME_COPY_OF:
      case Constants.ELEMNAME_NUMBER:
      case Constants.ELEMNAME_CHOOSE:
      case Constants.ELEMNAME_IF:
      case Constants.ELEMNAME_TEXT:
      case Constants.ELEMNAME_COPY:
      case Constants.ELEMNAME_VARIABLE:
      case Constants.ELEMNAME_MESSAGE:

        // instructions
        // case Constants.ELEMNAME_PI:
        // case Constants.ELEMNAME_COMMENT:
        // case Constants.ELEMNAME_ELEMENT:
        // case Constants.ELEMNAME_ATTRIBUTE:
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
