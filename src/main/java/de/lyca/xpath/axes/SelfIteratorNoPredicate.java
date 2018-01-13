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
package de.lyca.xpath.axes;

import javax.xml.transform.TransformerException;

import de.lyca.xml.dtm.DTM;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.compiler.Compiler;

/**
 * This class implements an optimized iterator for "." patterns, that is, the
 * self axes without any predicates.
 * 
 * @see de.lyca.xpath.axes.LocPathIterator
 */
public class SelfIteratorNoPredicate extends LocPathIterator {
  static final long serialVersionUID = -4226887905279814201L;

  /**
   * Create a SelfIteratorNoPredicate object.
   * 
   * @param compiler
   *          A reference to the Compiler that contains the op map.
   * @param opPos
   *          The position within the op map, which contains the location path
   *          expression for this itterator.
   * @param analysis
   *          Analysis bits.
   * 
   * @throws TransformerException TODO
   */
  SelfIteratorNoPredicate(Compiler compiler, int opPos, int analysis) throws TransformerException {
    super(compiler, opPos, analysis, false);
  }

  /**
   * Create a SelfIteratorNoPredicate object.
   * 
   * @throws TransformerException TODO
   */
  public SelfIteratorNoPredicate() throws TransformerException {
    super(null);
  }

  /**
   * Returns the next node in the set and advances the position of the iterator
   * in the set. After a NodeIterator is created, the first call to nextNode()
   * returns the first node in the set.
   * 
   * @return The next <code>Node</code> in the set being iterated over, or
   *         <code>null</code> if there are no more members in that set.
   */
  @Override
  public int nextNode() {
    if (m_foundLast)
      return DTM.NULL;

    int next;

    m_lastFetched = next = DTM.NULL == m_lastFetched ? m_context : DTM.NULL;

    // m_lastFetched = next;
    if (DTM.NULL != next) {
      m_pos++;

      return next;
    } else {
      m_foundLast = true;

      return DTM.NULL;
    }
  }

  /**
   * Return the first node out of the nodeset, if this expression is a nodeset
   * expression. This is the default implementation for nodesets. Derived
   * classes should try and override this and return a value without having to
   * do a clone operation.
   * 
   * @param xctxt
   *          The XPath runtime context.
   * @return the first node out of the nodeset, or DTM.NULL.
   * @throws TransformerException TODO
   */
  @Override
  public int asNode(XPathContext xctxt) throws TransformerException {
    return xctxt.getCurrentNode();
  }

  /**
   * Get the index of the last node that can be itterated to. This probably will
   * need to be overridded by derived classes.
   * 
   * @param xctxt
   *          XPath runtime context.
   * 
   * @return the index of the last node that can be itterated to.
   */
  @Override
  public int getLastPos(XPathContext xctxt) {
    return 1;
  }

}
