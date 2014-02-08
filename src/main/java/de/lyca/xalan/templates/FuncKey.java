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

import java.util.HashMap;
import java.util.Map;

import de.lyca.xalan.transformer.KeyManager;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.axes.UnionPathIterator;
import de.lyca.xpath.functions.Function2Args;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the Key() function.
 * 
 * @xsl.usage advanced
 */
public class FuncKey extends Function2Args {
  static final long serialVersionUID = 9089293100115347340L;

  /** Dummy value to be used in usedrefs hashtable */
  static private Boolean ISTRUE = Boolean.TRUE;

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

    // TransformerImpl transformer = (TransformerImpl)xctxt;
    final TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
    XNodeSet nodes = null;
    final int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    final int docContext = dtm.getDocumentRoot(context);

    if (DTM.NULL == docContext) {

      // path.error(context, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC);
      // //"context does not have an owner document!");
    }

    final String xkeyname = getArg0().execute(xctxt).str();
    final QName keyname = new QName(xkeyname, xctxt.getNamespaceContext());
    final XObject arg = getArg1().execute(xctxt);
    boolean argIsNodeSetDTM = XObject.CLASS_NODESET == arg.getType();
    final KeyManager kmgr = transformer.getKeyManager();

    // Don't bother with nodeset logic if the thing is only one node.
    if (argIsNodeSetDTM) {
      final XNodeSet ns = (XNodeSet) arg;
      ns.setShouldCacheNodes(true);
      final int len = ns.getLength();
      if (len <= 1) {
        argIsNodeSetDTM = false;
      }
    }

    if (argIsNodeSetDTM) {
      Map<XMLString,Boolean> usedrefs = null;
      final DTMIterator ni = arg.iter();
      int pos;
      final UnionPathIterator upi = new UnionPathIterator();
      upi.exprSetParent(this);

      while (DTM.NULL != (pos = ni.nextNode())) {
        dtm = xctxt.getDTM(pos);
        final XMLString ref = dtm.getStringValue(pos);

        if (null == ref) {
          continue;
        }

        if (null == usedrefs) {
          usedrefs = new HashMap<>();
        }

        if (usedrefs.get(ref) != null) {
          continue; // We already have 'em.
        } else {

          // ISTRUE being used as a dummy value.
          usedrefs.put(ref, ISTRUE);
        }

        final XNodeSet nl = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref, xctxt.getNamespaceContext());

        nl.setRoot(xctxt.getCurrentNode(), xctxt);

        // try
        // {
        upi.addIterator(nl);
        // }
        // catch(CloneNotSupportedException cnse)
        // {
        // // will never happen.
        // }
        // mnodeset.addNodesInDocOrder(nl, xctxt); needed??
      }

      final int current = xctxt.getCurrentNode();
      upi.setRoot(current, xctxt);

      nodes = new XNodeSet(upi);
    } else {
      final XMLString ref = arg.xstr();
      nodes = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref, xctxt.getNamespaceContext());
      nodes.setRoot(xctxt.getCurrentNode(), xctxt);
    }

    return nodes;
  }
}