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
package de.lyca.xalan.transformer;

import java.util.Vector;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.templates.KeyDeclaration;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;

/**
 * This class filters nodes from a key iterator, according to whether or not the
 * use value matches the ref value.
 * 
 * @xsl.usage internal
 */
public class KeyRefIterator extends de.lyca.xpath.axes.ChildTestIterator {
  static final long serialVersionUID = 3837456451659435102L;

  /**
   * Constructor KeyRefIterator
   * 
   * 
   * @param ref
   *          Key value to match
   * @param ki
   *          The main key iterator used to walk the source tree
   */
  public KeyRefIterator(QName name, XMLString ref, Vector keyDecls, DTMIterator ki) {
    super(null);
    m_name = name;
    m_ref = ref;
    m_keyDeclarations = keyDecls;
    m_keysNodes = ki;
    setWhatToShow(de.lyca.xml.dtm.DTMFilter.SHOW_ALL);
  }

  DTMIterator m_keysNodes;

  /**
   * Get the next node via getNextXXX. Bottlenecked for derived class override.
   * 
   * @return The next node on the axis, or DTM.NULL.
   */
  @Override
  protected int getNextNode() {
    int next;
    while (DTM.NULL != (next = m_keysNodes.nextNode())) {
      if (DTMIterator.FILTER_ACCEPT == filterNode(next)) {
        break;
      }
    }
    m_lastFetched = next;

    return next;
  }

  /**
   * Test whether a specified node is visible in the logical view of a
   * TreeWalker or NodeIterator. This function will be called by the
   * implementation of TreeWalker and NodeIterator; it is not intended to be
   * called directly from user code.
   * 
   * @param testNode
   *          The node to check to see if it passes the filter or not.
   * 
   * @return a constant to determine whether the node is accepted, rejected, or
   *         skipped, as defined above .
   */
  public short filterNode(int testNode) {
    boolean foundKey = false;
    final Vector keys = m_keyDeclarations;

    final QName name = m_name;
    final KeyIterator ki = (KeyIterator) ((XNodeSet) m_keysNodes).getContainedIter();
    final de.lyca.xpath.XPathContext xctxt = ki.getXPathContext();

    if (null == xctxt) {
      assertion(false, "xctxt can not be null here!");
    }

    try {
      final XMLString lookupKey = m_ref;

      // System.out.println("lookupKey: "+lookupKey);
      final int nDeclarations = keys.size();

      // Walk through each of the declarations made with xsl:key
      for (int i = 0; i < nDeclarations; i++) {
        final KeyDeclaration kd = (KeyDeclaration) keys.elementAt(i);

        // Only continue if the name on this key declaration
        // matches the name on the iterator for this walker.
        if (!kd.getName().equals(name)) {
          continue;
        }

        foundKey = true;
        // xctxt.setNamespaceContext(ki.getPrefixResolver());

        // Query from the node, according the the select pattern in the
        // use attribute in xsl:key.
        final XObject xuse = kd.getUse().execute(xctxt, testNode, ki.getPrefixResolver());

        if (xuse.getType() != XObject.CLASS_NODESET) {
          final XMLString exprResult = xuse.xstr();

          if (lookupKey.equals(exprResult))
            return DTMIterator.FILTER_ACCEPT;
        } else {
          final DTMIterator nl = ((XNodeSet) xuse).iterRaw();
          int useNode;

          while (DTM.NULL != (useNode = nl.nextNode())) {
            final DTM dtm = getDTM(useNode);
            final XMLString exprResult = dtm.getStringValue(useNode);
            if (null != exprResult && lookupKey.equals(exprResult))
              return DTMIterator.FILTER_ACCEPT;
          }
        }

      } // end for(int i = 0; i < nDeclarations; i++)
    } catch (final javax.xml.transform.TransformerException te) {
      throw new de.lyca.xml.utils.WrappedRuntimeException(te);
    }

    if (!foundKey)
      throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_XSLKEY_DECLARATION,
              new Object[] { name.getLocalName() }));
    return DTMIterator.FILTER_REJECT;
  }

  protected XMLString m_ref;
  protected QName m_name;

  /**
   * Vector of Key declarations in the stylesheet.
   * 
   * @serial
   */
  protected Vector m_keyDeclarations;

}
