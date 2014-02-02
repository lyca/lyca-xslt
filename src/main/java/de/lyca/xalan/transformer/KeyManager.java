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
import java.util.List;

import de.lyca.xalan.templates.ElemTemplateElement;
import de.lyca.xml.utils.PrefixResolver;
import de.lyca.xml.utils.QName;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XNodeSet;

/**
 * This class manages the key tables.
 */
public class KeyManager {

  /**
   * Table of tables of element keys.
   * 
   * @see de.lyca.xalan.transformer.KeyTable
   */
  private transient List<KeyTable> m_key_tables = null;

  /**
   * Given a valid element key, return the corresponding node list.
   * 
   * @param xctxt
   *          The XPath runtime state
   * @param doc
   *          The document node
   * @param name
   *          The key element name
   * @param ref
   *          The key value we're looking for
   * @param nscontext
   *          The prefix resolver for the execution context
   * 
   * @return A nodelist of nodes mathing the given key
   * 
   * @throws javax.xml.transform.TransformerException
   */
  public XNodeSet getNodeSetDTMByKey(XPathContext xctxt, int doc, QName name, XMLString ref, PrefixResolver nscontext)
          throws javax.xml.transform.TransformerException {

    XNodeSet nl = null;
    final ElemTemplateElement template = (ElemTemplateElement) nscontext; // yuck
                                                                          // -sb

    if (null != template && null != template.getStylesheetRoot().getKeysComposed()) {
      boolean foundDoc = false;

      if (null == m_key_tables) {
        m_key_tables = new ArrayList<>(4);
      } else {
        final int nKeyTables = m_key_tables.size();

        for (int i = 0; i < nKeyTables; i++) {
          final KeyTable kt = m_key_tables.get(i);

          if (kt.getKeyTableName().equals(name) && doc == kt.getDocKey()) {
            nl = kt.getNodeSetDTMByKey(name, ref);

            if (nl != null) {
              foundDoc = true;

              break;
            }
          }
        }
      }

      if (null == nl && !foundDoc /* && m_needToBuildKeysTable */) {
        final KeyTable kt = new KeyTable(doc, nscontext, name, template.getStylesheetRoot().getKeysComposed(), xctxt);

        m_key_tables.add(kt);

        if (doc == kt.getDocKey()) {
          foundDoc = true;
          nl = kt.getNodeSetDTMByKey(name, ref);
        }
      }
    }

    return nl;
  }
}
