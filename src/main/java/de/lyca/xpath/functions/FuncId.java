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
package de.lyca.xpath.functions;

import java.util.StringTokenizer;

import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.StringVector;
import de.lyca.xpath.NodeSetDTM;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.XPATHErrorResources;

/**
 * Execute the Id() function.
 * 
 * @xsl.usage advanced
 */
public class FuncId extends FunctionOneArg {
  static final long serialVersionUID = 8930573966143567310L;

  /**
   * Fill in a list with nodes that match a space delimited list if ID ID
   * references.
   * 
   * @param xctxt
   *          The runtime XPath context.
   * @param docContext
   *          The document where the nodes are being looked for.
   * @param refval
   *          A space delimited list of ID references.
   * @param usedrefs
   *          List of references for which nodes were found.
   * @param nodeSet
   *          Node set where the nodes will be added to.
   * @param mayBeMore
   *          true if there is another set of nodes to be looked for.
   * 
   * @return The usedrefs value.
   */
  private StringVector getNodesByID(XPathContext xctxt, int docContext, String refval, StringVector usedrefs,
          NodeSetDTM nodeSet, boolean mayBeMore) {

    if (null != refval) {
      String ref = null;
      // DOMHelper dh = xctxt.getDOMHelper();
      final StringTokenizer tokenizer = new StringTokenizer(refval);
      boolean hasMore = tokenizer.hasMoreTokens();
      final DTM dtm = xctxt.getDTM(docContext);

      while (hasMore) {
        ref = tokenizer.nextToken();
        hasMore = tokenizer.hasMoreTokens();

        if (null != usedrefs && usedrefs.contains(ref)) {
          ref = null;

          continue;
        }

        final int node = dtm.getElementById(ref);

        if (DTM.NULL != node) {
          nodeSet.addNodeInDocOrder(node, xctxt);
        }

        if (null != ref && (hasMore || mayBeMore)) {
          if (null == usedrefs) {
            usedrefs = new StringVector();
          }

          usedrefs.addElement(ref);
        }
      }
    }

    return usedrefs;
  }

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {

    final int context = xctxt.getCurrentNode();
    final DTM dtm = xctxt.getDTM(context);
    final int docContext = dtm.getDocument();

    if (DTM.NULL == docContext) {
      error(xctxt, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC, null);
    }

    final XObject arg = m_arg0.execute(xctxt);
    final int argType = arg.getType();
    final XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
    final NodeSetDTM nodeSet = nodes.mutableNodeset();

    if (XObject.CLASS_NODESET == argType) {
      final DTMIterator ni = arg.iter();
      StringVector usedrefs = null;
      int pos = ni.nextNode();

      while (DTM.NULL != pos) {
        final DTM ndtm = ni.getDTM(pos);
        final String refval = ndtm.getStringValue(pos).toString();

        pos = ni.nextNode();
        usedrefs = getNodesByID(xctxt, docContext, refval, usedrefs, nodeSet, DTM.NULL != pos);
      }
      // ni.detach();
    } else if (XObject.CLASS_NULL == argType)
      return nodes;
    else {
      final String refval = arg.str();

      getNodesByID(xctxt, docContext, refval, null, nodeSet, false);
    }

    return nodes;
  }
}
