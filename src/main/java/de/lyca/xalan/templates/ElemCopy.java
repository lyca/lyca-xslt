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

import de.lyca.xalan.serialize.SerializerUtils;
import de.lyca.xalan.transformer.ClonerToResultTree;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xpath.XPathContext;

/**
 * Implement xsl:copy.
 * 
 * <pre>
 * <!ELEMENT xsl:copy %template;>
 * <!ATTLIST xsl:copy
 *   %space-att;
 *   use-attribute-sets %qnames; #IMPLIED
 * >
 * </pre>
 * 
 * @see <a href="http://www.w3.org/TR/xslt#copying">copying in XSLT
 *      Specification</a>
 * @xsl.usage advanced
 */
public class ElemCopy extends ElemUse {
  static final long serialVersionUID = 5478580783896941384L;

  /**
   * Get an int constant identifying the type of element.
   * 
   * @see de.lyca.xalan.templates.Constants
   * 
   * @return The token ID for this element
   */
  @Override
  public int getXSLToken() {
    return Constants.ELEMNAME_COPY;
  }

  /**
   * Return the node name.
   * 
   * @return This element's name
   */
  @Override
  public String getNodeName() {
    return Constants.ELEMNAME_COPY_STRING;
  }

  /**
   * The xsl:copy element provides an easy way of copying the current node.
   * Executing this function creates a copy of the current node into the result
   * tree.
   * <p>
   * The namespace nodes of the current node are automatically copied as well,
   * but the attributes and children of the node are not automatically copied.
   * The content of the xsl:copy element is a template for the attributes and
   * children of the created node; the content is instantiated only for nodes of
   * types that can have attributes or children (i.e. root nodes and element
   * nodes).
   * </p>
   * <p>
   * The root node is treated specially because the root node of the result tree
   * is created implicitly. When the current node is the root node, xsl:copy
   * will not create a root node, but will just use the content template.
   * </p>
   * 
   * @param transformer
   *          non-null reference to the the current transform-time state.
   * 
   * @throws TransformerException
   */
  @Override
  public void execute(TransformerImpl transformer) throws TransformerException {
    final XPathContext xctxt = transformer.getXPathContext();

    try {
      final int sourceNode = xctxt.getCurrentNode();
      xctxt.pushCurrentNode(sourceNode);
      final DTM dtm = xctxt.getDTM(sourceNode);
      final short nodeType = dtm.getNodeType(sourceNode);

      if (DTM.DOCUMENT_NODE != nodeType && DTM.DOCUMENT_FRAGMENT_NODE != nodeType) {
        final SerializationHandler rthandler = transformer.getSerializationHandler();

        if (transformer.getDebug()) {
          transformer.getTraceManager().fireTraceEvent(this);
        }

        // TODO: Process the use-attribute-sets stuff
        ClonerToResultTree.cloneToResultTree(sourceNode, nodeType, dtm, rthandler, false);

        if (DTM.ELEMENT_NODE == nodeType) {
          super.execute(transformer);
          SerializerUtils.processNSDecls(rthandler, sourceNode, nodeType, dtm);
          transformer.executeChildTemplates(this, true);

          final String ns = dtm.getNamespaceURI(sourceNode);
          final String localName = dtm.getLocalName(sourceNode);
          transformer.getResultTreeHandler().endElement(ns, localName, dtm.getNodeName(sourceNode));
        }
        if (transformer.getDebug()) {
          transformer.getTraceManager().fireTraceEndEvent(this);
        }
      } else {
        if (transformer.getDebug()) {
          transformer.getTraceManager().fireTraceEvent(this);
        }

        super.execute(transformer);
        transformer.executeChildTemplates(this, true);

        if (transformer.getDebug()) {
          transformer.getTraceManager().fireTraceEndEvent(this);
        }
      }
    } catch (final org.xml.sax.SAXException se) {
      throw new TransformerException(se);
    } finally {
      xctxt.popCurrentNode();
    }
  }
}
