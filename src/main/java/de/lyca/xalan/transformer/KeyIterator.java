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

import java.util.List;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.templates.KeyDeclaration;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.XPath;
import de.lyca.xpath.axes.OneStepIteratorForward;

/**
 * This class implements an optimized iterator for "key()" patterns, matching
 * each node to the match attribute in one or more xsl:key declarations.
 * 
 * @xsl.usage internal
 */
public class KeyIterator extends OneStepIteratorForward {
  static final long serialVersionUID = -1349109910100249661L;

  /**
   * Key name.
   * 
   * @serial
   */
  private final QName m_name;

  /**
   * Get the key name from a key declaration this iterator will process
   * 
   * 
   * @return Key name
   */
  public QName getName() {
    return m_name;
  }

  /**
   * Vector of Key declarations in the stylesheet.
   * 
   * @serial
   */
  private final List<KeyDeclaration> m_keyDeclarations;

  /**
   * Get the key declarations from the stylesheet
   * 
   * 
   * @return Vector containing the key declarations from the stylesheet
   */
  public List<KeyDeclaration> getKeyDeclarations() {
    return m_keyDeclarations;
  }

  /**
   * Create a KeyIterator object.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  KeyIterator(QName name, List<KeyDeclaration> keyDeclarations) {
    super(Axis.ALL);
    m_keyDeclarations = keyDeclarations;
    // m_prefixResolver = nscontext;
    m_name = name;
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
  @Override
  public short acceptNode(int testNode) {
    boolean foundKey = false;
    final KeyIterator ki = (KeyIterator) m_lpi;
    final de.lyca.xpath.XPathContext xctxt = ki.getXPathContext();
    final List<KeyDeclaration> keys = ki.getKeyDeclarations();

    final QName name = ki.getName();
    try {
      // System.out.println("lookupKey: "+lookupKey);
      final int nDeclarations = keys.size();

      // Walk through each of the declarations made with xsl:key
      for (int i = 0; i < nDeclarations; i++) {
        final KeyDeclaration kd = keys.get(i);

        // Only continue if the name on this key declaration
        // matches the name on the iterator for this walker.
        if (!kd.getName().equals(name)) {
          continue;
        }

        foundKey = true;
        // xctxt.setNamespaceContext(ki.getPrefixResolver());

        // See if our node matches the given key declaration according to
        // the match attribute on xsl:key.
        final XPath matchExpr = kd.getMatch();
        final double score = matchExpr.getMatchScore(xctxt, testNode);

        kd.getMatch();
        if (score == XPath.MATCH_SCORE_NONE) {
          continue;
        }

        return DTMIterator.FILTER_ACCEPT;

      } // end for(int i = 0; i < nDeclarations; i++)
    } catch (final TransformerException se) {

      // TODO: What to do?
    }

    if (!foundKey)
      throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_XSLKEY_DECLARATION,
              new Object[] { name.getLocalName() }));

    return DTMIterator.FILTER_REJECT;
  }

}
