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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.templates.KeyDeclaration;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.PrefixResolver;
import de.lyca.xml.utils.QName;
import de.lyca.xml.utils.WrappedRuntimeException;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;

/**
 * Table of element keys, keyed by document node. An instance of this class is
 * keyed by a Document node that should be matched with the root of the current
 * context.
 * 
 * @xsl.usage advanced
 */
public class KeyTable {
  /**
   * The document key. This table should only be used with contexts whose
   * Document roots match this key.
   */
  private final int m_docKey;

  /**
   * Vector of KeyDeclaration instances holding the key declarations.
   */
  private final List<KeyDeclaration> m_keyDeclarations;

  /**
   * Hold a cache of key() function result for each ref. Key is XMLString, the
   * ref value Value is XNodeSet, the key() function result for the given ref
   * value.
   */
  private Map<XMLString, XNodeSet> m_refsTable = null;

  /**
   * Get the document root matching this key.
   * 
   * @return the document root matching this key
   */
  public int getDocKey() {
    return m_docKey;
  }

  /**
   * The main iterator that will walk through the source tree for this key.
   */
  private final XNodeSet m_keyNodes;

  KeyIterator getKeyIterator() {
    return (KeyIterator) m_keyNodes.getContainedIter();
  }

  /**
   * Build a keys table.
   * 
   * @param doc
   *          The owner document key.
   * @param nscontext
   *          The stylesheet's namespace context.
   * @param name
   *          The key name
   * @param keyDeclarations
   *          The stylesheet's xsl:key declarations.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  public KeyTable(int doc, PrefixResolver nscontext, QName name, List<KeyDeclaration> keyDeclarations, XPathContext xctxt)
          throws TransformerException {
    m_docKey = doc;
    m_keyDeclarations = keyDeclarations;
    final KeyIterator ki = new KeyIterator(name, keyDeclarations);

    m_keyNodes = new XNodeSet(ki);
    m_keyNodes.allowDetachToRelease(false);
    m_keyNodes.setRoot(doc, xctxt);
  }

  /**
   * Given a valid element key, return the corresponding node list.
   * 
   * @param name
   *          The name of the key, which must match the 'name' attribute on
   *          xsl:key.
   * @param ref
   *          The value that must match the value found by the 'match' attribute
   *          on xsl:key.
   * @return a set of nodes referenced by the key named <CODE>name</CODE> and
   *         the reference <CODE>ref</CODE>. If no node is referenced by this
   *         key, an empty node set is returned.
   */
  public XNodeSet getNodeSetDTMByKey(QName name, XMLString ref)

  {
    XNodeSet refNodes = getRefsTable().get(ref);
    // clone wiht reset the node set
    try {
      if (refNodes != null) {
        refNodes = (XNodeSet) refNodes.cloneWithReset();
      }
    } catch (final CloneNotSupportedException e) {
      refNodes = null;
    }

    if (refNodes == null) {
      // create an empty XNodeSet
      final KeyIterator ki = (KeyIterator) m_keyNodes.getContainedIter();
      final XPathContext xctxt = ki.getXPathContext();
      refNodes = new XNodeSet(xctxt.getDTMManager()) {
        @Override
        public void setRoot(int nodeHandle, Object environment) {
          // Root cannot be set on non-iterated node sets. Ignore it.
        }
      };
      refNodes.reset();
    }

    return refNodes;
  }

  /**
   * Get Key Name for this KeyTable
   * 
   * @return Key name
   */
  public QName getKeyTableName() {
    return getKeyIterator().getName();
  }

  /**
   * @return key declarations for the key associated to this KeyTable
   */
  private List<KeyDeclaration> getKeyDeclarations() {
    final int nDeclarations = m_keyDeclarations.size();
    final List<KeyDeclaration> keyDecls = new ArrayList<>(nDeclarations);

    // Walk through each of the declarations made with xsl:key
    for (int i = 0; i < nDeclarations; i++) {
      final KeyDeclaration kd = m_keyDeclarations.get(i);

      // Add the declaration if the name on this key declaration
      // matches the name on the iterator for this walker.
      if (kd.getName().equals(getKeyTableName())) {
        keyDecls.add(kd);
      }
    }

    return keyDecls;
  }

  /**
   * @return lazy initialized refs table associating evaluation of key function
   *         with a XNodeSet
   */
  private Map<XMLString, XNodeSet> getRefsTable() {
    if (m_refsTable == null) {
      // initial capacity set to a prime number to improve hash performance
      m_refsTable = new HashMap<>(89);

      final KeyIterator ki = (KeyIterator) m_keyNodes.getContainedIter();
      final XPathContext xctxt = ki.getXPathContext();

      final List<KeyDeclaration> keyDecls = getKeyDeclarations();
      final int nKeyDecls = keyDecls.size();

      int currentNode;
      m_keyNodes.reset();
      while (DTM.NULL != (currentNode = m_keyNodes.nextNode())) {
        try {
          for (int keyDeclIdx = 0; keyDeclIdx < nKeyDecls; keyDeclIdx++) {
            final KeyDeclaration keyDeclaration = keyDecls.get(keyDeclIdx);
            final XObject xuse = keyDeclaration.getUse().execute(xctxt, currentNode, ki.getPrefixResolver());

            if (xuse.getType() != XObject.CLASS_NODESET) {
              final XMLString exprResult = xuse.xstr();
              addValueInRefsTable(xctxt, exprResult, currentNode);
            } else {
              final DTMIterator i = ((XNodeSet) xuse).iterRaw();
              int currentNodeInUseClause;

              while (DTM.NULL != (currentNodeInUseClause = i.nextNode())) {
                final DTM dtm = xctxt.getDTM(currentNodeInUseClause);
                final XMLString exprResult = dtm.getStringValue(currentNodeInUseClause);
                addValueInRefsTable(xctxt, exprResult, currentNode);
              }
            }
          }
        } catch (final TransformerException te) {
          throw new WrappedRuntimeException(te);
        }
      }
    }
    return m_refsTable;
  }

  /**
   * Add an association between a ref and a node in the m_refsTable. Requires
   * that m_refsTable != null
   * 
   * @param xctxt
   *          XPath context
   * @param ref
   *          the value of the use clause of the current key for the given node
   * @param node
   *          the node to reference
   */
  private void addValueInRefsTable(XPathContext xctxt, XMLString ref, int node) {

    XNodeSet nodes = m_refsTable.get(ref);
    if (nodes == null) {
      nodes = new XNodeSet(node, xctxt.getDTMManager());
      nodes.nextNode();
      m_refsTable.put(ref, nodes);
    } else {
      // Nodes are passed to this method in document order. Since we need to
      // suppress duplicates, we only need to check against the last entry
      // in each nodeset. We use nodes.nextNode after each entry so we can
      // easily compare node against the current node.
      if (nodes.getCurrentNode() != node) {
        nodes.mutableNodeset().addNode(node);
        nodes.nextNode();
      }
    }
  }
}
