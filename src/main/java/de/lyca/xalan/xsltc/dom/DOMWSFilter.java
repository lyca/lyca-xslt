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
package de.lyca.xalan.xsltc.dom;

import java.util.HashMap;
import java.util.Map;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.DOMEnhancedForDTM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMWSFilter;

/**
 * A wrapper class that adapts the {@link de.lyca.xml.dtm.DTMWSFilter
 * DTMWSFilter} interface to the XSLTC DOM
 * {@link de.lyca.xalan.xsltc.StripFilter StripFilter} interface.
 */
public class DOMWSFilter implements DTMWSFilter {

  private final AbstractTranslet m_translet;
  private StripFilter m_filter;

  // The Hashtable for DTM to mapping array
  private final Map<DTM, short[]> m_mappings;

  // Cache the DTM and mapping that are used last time
  private DTM m_currentDTM;
  private short[] m_currentMapping;

  /**
   * Construct an adapter connecting the <code>DTMWSFilter</code> interface to
   * the <code>StripFilter</code> interface.
   * 
   * @param translet
   *          A translet that also implements the StripFilter interface.
   * 
   * @see de.lyca.xml.dtm.DTMWSFilter
   * @see de.lyca.xalan.xsltc.StripFilter
   */
  public DOMWSFilter(AbstractTranslet translet) {
    m_translet = translet;
    m_mappings = new HashMap<>();

    if (translet instanceof StripFilter) {
      m_filter = (StripFilter) translet;
    }
  }

  /**
   * Test whether whitespace-only text nodes are visible in the logical view of
   * <code>DTM</code>. Normally, this function will be called by the
   * implementation of <code>DTM</code>; it is not normally called directly from
   * user code.
   * 
   * @param node
   *          int handle of the node.
   * @param dtm
   *          the DTM that owns this node
   * @return one of <code>NOTSTRIP</code>, <code>STRIP</code> or
   *         <code>INHERIT</code>.
   */
  @Override
  public short getShouldStripSpace(int node, DTM dtm) {
    if (m_filter != null && dtm instanceof DOM) {
      final DOM dom = (DOM) dtm;
      int type = 0;

      if (dtm instanceof DOMEnhancedForDTM) {
        final DOMEnhancedForDTM mappableDOM = (DOMEnhancedForDTM) dtm;

        short[] mapping;
        if (dtm == m_currentDTM) {
          mapping = m_currentMapping;
        } else {
          mapping = m_mappings.get(dtm);
          if (mapping == null) {
            mapping = mappableDOM.getMapping(m_translet.getNamesArray(), m_translet.getUrisArray(),
                    m_translet.getTypesArray());
            m_mappings.put(dtm, mapping);
            m_currentDTM = dtm;
            m_currentMapping = mapping;
          }
        }

        final int expType = mappableDOM.getExpandedTypeID(node);

        // %OPT% The mapping array does not have information about all the
        // exptypes. However it does contain enough information about all names
        // in the translet's namesArray. If the expType does not fall into the
        // range of the mapping array, it means that the expType is not for one
        // of the recognized names. In this case we can just set the type to -1.
        if (expType >= 0 && expType < mapping.length) {
          type = mapping[expType];
        } else {
          type = -1;
        }

      } else
        return INHERIT;

      if (m_filter.stripSpace(dom, node, type))
        return STRIP;
      else
        return NOTSTRIP;
    } else
      return NOTSTRIP;
  }
}
