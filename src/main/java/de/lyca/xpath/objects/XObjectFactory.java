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
package de.lyca.xpath.objects;

import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.axes.OneStepIterator;

public class XObjectFactory {

  /**
   * Create the right XObject based on the type of the object passed. This
   * function can not make an XObject that exposes DOM Nodes, NodeLists, and
   * NodeIterators to the XSLT stylesheet as node-sets.
   * 
   * @param val
   *          The java object which this object will wrap.
   * 
   * @return the right XObject based on the type of the object passed.
   */
  static public XObject create(Object val) {

    XObject result;

    if (val instanceof XObject) {
      result = (XObject) val;
    } else if (val instanceof String) {
      result = new XString((String) val);
    } else if (val instanceof Boolean) {
      result = new XBoolean((Boolean) val);
    } else if (val instanceof Double) {
      result = new XNumber((Double) val);
    } else {
      result = new XObject(val);
    }

    return result;
  }

  /**
   * Create the right XObject based on the type of the object passed. This
   * function <em>can</em> make an XObject that exposes DOM Nodes,
   * NodeLists, and NodeIterators to the XSLT stylesheet as node-sets.
   * 
   * @param val
   *          The java object which this object will wrap.
   * @param xctxt
   *          The XPath context.
   * 
   * @return the right XObject based on the type of the object passed.
   */
  static public XObject create(Object val, XPathContext xctxt) {

    XObject result;

    if (val instanceof XObject) {
      result = (XObject) val;
    } else if (val instanceof String) {
      result = new XString((String) val);
    } else if (val instanceof Boolean) {
      result = new XBoolean((Boolean) val);
    } else if (val instanceof Number) {
      result = new XNumber((Number) val);
    } else if (val instanceof DTM) {
      final DTM dtm = (DTM) val;
      try {
        final int dtmRoot = dtm.getDocument();
        final DTMAxisIterator iter = dtm.getAxisIterator(Axis.SELF);
        iter.setStartNode(dtmRoot);
        final DTMIterator iterator = new OneStepIterator(iter, Axis.SELF);
        iterator.setRoot(dtmRoot, xctxt);
        result = new XNodeSet(iterator);
      } catch (final Exception ex) {
        throw new de.lyca.xml.utils.WrappedRuntimeException(ex);
      }
    } else if (val instanceof DTMAxisIterator) {
      final DTMAxisIterator iter = (DTMAxisIterator) val;
      try {
        final DTMIterator iterator = new OneStepIterator(iter, Axis.SELF);
        iterator.setRoot(iter.getStartNode(), xctxt);
        result = new XNodeSet(iterator);
      } catch (final Exception ex) {
        throw new de.lyca.xml.utils.WrappedRuntimeException(ex);
      }
    } else if (val instanceof DTMIterator) {
      result = new XNodeSet((DTMIterator) val);
    }
    // This next three instanceofs are a little worrysome, since a NodeList
    // might also implement a Node!
    else if (val instanceof org.w3c.dom.Node) {
      result = new XNodeSetForDOM((org.w3c.dom.Node) val, xctxt);
    }
    // This must come after org.w3c.dom.Node, since many Node implementations
    // also implement NodeList.
    else if (val instanceof org.w3c.dom.NodeList) {
      result = new XNodeSetForDOM((org.w3c.dom.NodeList) val, xctxt);
    } else if (val instanceof org.w3c.dom.traversal.NodeIterator) {
      result = new XNodeSetForDOM((org.w3c.dom.traversal.NodeIterator) val, xctxt);
    } else {
      result = new XObject(val);
    }

    return result;
  }
}
