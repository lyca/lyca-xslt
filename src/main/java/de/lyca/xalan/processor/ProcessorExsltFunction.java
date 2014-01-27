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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lyca.xalan.templates.ElemApplyImport;
import de.lyca.xalan.templates.ElemApplyTemplates;
import de.lyca.xalan.templates.ElemAttribute;
import de.lyca.xalan.templates.ElemCallTemplate;
import de.lyca.xalan.templates.ElemComment;
import de.lyca.xalan.templates.ElemCopy;
import de.lyca.xalan.templates.ElemCopyOf;
import de.lyca.xalan.templates.ElemElement;
import de.lyca.xalan.templates.ElemExsltFuncResult;
import de.lyca.xalan.templates.ElemExsltFunction;
import de.lyca.xalan.templates.ElemFallback;
import de.lyca.xalan.templates.ElemLiteralResult;
import de.lyca.xalan.templates.ElemMessage;
import de.lyca.xalan.templates.ElemNumber;
import de.lyca.xalan.templates.ElemPI;
import de.lyca.xalan.templates.ElemParam;
import de.lyca.xalan.templates.ElemTemplate;
import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xalan.templates.ElemText;
import de.lyca.xalan.templates.ElemTextLiteral;
import de.lyca.xalan.templates.ElemValueOf;
import de.lyca.xalan.templates.ElemVariable;
import de.lyca.xalan.templates.Stylesheet;

/**
 * This class processes parse events for an exslt func:function element.
 * 
 * @xsl.usage internal
 */
public class ProcessorExsltFunction extends ProcessorTemplateElem {
  static final long serialVersionUID = 2411427965578315332L;

  /**
   * Start an ElemExsltFunction. Verify that it is top level and that it has a
   * name attribute with a namespace.
   */
  @Override
  public void startElement(StylesheetHandler handler, String uri, String localName, String rawName,
          Attributes attributes) throws SAXException {
    // System.out.println("ProcessorFunction.startElement()");
    String msg = "";
    if (!(handler.getElemTemplateElement() instanceof Stylesheet)) {
      msg = "func:function element must be top level.";
      handler.error(msg, new SAXException(msg));
    }
    super.startElement(handler, uri, localName, rawName, attributes);

    final String val = attributes.getValue("name");
    final int indexOfColon = val.indexOf(":");
    if (indexOfColon > 0) {
      // String prefix = val.substring(0, indexOfColon);
      // String localVal = val.substring(indexOfColon + 1);
      // String ns = handler.getNamespaceSupport().getURI(prefix);
      // if (ns.length() > 0)
      // System.out.println("fullfuncname " + ns + localVal);
    } else {
      msg = "func:function name must have namespace";
      handler.error(msg, new SAXException(msg));
    }
  }

  /**
   * Must include; super doesn't suffice!
   */
  @Override
  protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
    // System.out.println("ProcessorFunction appendAndPush()" + elem);
    super.appendAndPush(handler, elem);
    // System.out.println("originating node " + handler.getOriginatingNode());
    elem.setDOMBackPointer(handler.getOriginatingNode());
    handler.getStylesheet().setTemplate((ElemTemplate) elem);
  }

  /**
   * End an ElemExsltFunction, and verify its validity.
   */
  @Override
  public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
    final ElemTemplateElement function = handler.getElemTemplateElement();
    validate(function, handler); // may throw exception
    super.endElement(handler, uri, localName, rawName);
  }

  /**
   * Non-recursive traversal of FunctionElement tree based on TreeWalker to
   * verify that there are no literal result elements except within a
   * func:result element and that the func:result element does not contain any
   * following siblings except xsl:fallback.
   */
  public void validate(ElemTemplateElement elem, StylesheetHandler handler) throws SAXException {
    String msg = "";
    while (elem != null) {
      // System.out.println("elem " + elem);
      if (elem instanceof ElemExsltFuncResult && elem.getNextSiblingElem() != null
              && !(elem.getNextSiblingElem() instanceof ElemFallback)) {
        msg = "func:result has an illegal following sibling (only xsl:fallback allowed)";
        handler.error(msg, new SAXException(msg));
      }

      if ((elem instanceof ElemApplyImport || elem instanceof ElemApplyTemplates || elem instanceof ElemAttribute
              || elem instanceof ElemCallTemplate || elem instanceof ElemComment || elem instanceof ElemCopy
              || elem instanceof ElemCopyOf || elem instanceof ElemElement || elem instanceof ElemLiteralResult
              || elem instanceof ElemNumber || elem instanceof ElemPI || elem instanceof ElemText
              || elem instanceof ElemTextLiteral || elem instanceof ElemValueOf)
              && !ancestorIsOk(elem)) {
        msg = "misplaced literal result in a func:function container.";
        handler.error(msg, new SAXException(msg));
      }
      ElemTemplateElement nextElem = elem.getFirstChildElem();
      while (nextElem == null) {
        nextElem = elem.getNextSiblingElem();
        if (nextElem == null) {
          elem = elem.getParentElem();
        }
        if (elem == null || elem instanceof ElemExsltFunction)
          return; // ok
      }
      elem = nextElem;
    }
  }

  /**
   * Verify that a literal result belongs to a result element, a variable, or a
   * parameter.
   */

  boolean ancestorIsOk(ElemTemplateElement child) {
    while (child.getParentElem() != null && !(child.getParentElem() instanceof ElemExsltFunction)) {
      final ElemTemplateElement parent = child.getParentElem();
      if (parent instanceof ElemExsltFuncResult || parent instanceof ElemVariable || parent instanceof ElemParam
              || parent instanceof ElemMessage)
        return true;
      child = parent;
    }
    return false;
  }

}
