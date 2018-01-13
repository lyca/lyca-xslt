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

import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xpath.compiler.Compiler;

/**
 * This class implements an optimized iterator for attribute axes patterns.
 * 
 * @see ChildTestIterator
 */
public class AttributeIterator extends ChildTestIterator {
  static final long serialVersionUID = -8417986700712229686L;

  /**
   * Create a AttributeIterator object.
   * 
   * @param compiler
   *          A reference to the Compiler that contains the op map.
   * @param opPos
   *          The position within the op map, which contains the location path
   *          expression for this itterator.
   * 
   * @throws TransformerException
   *           TODO
   */
  AttributeIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
    super(compiler, opPos, analysis);
  }

  /**
   * Get the next node via getFirstAttribute {@literal &&} getNextAttribute.
   */
  @Override
  protected int getNextNode() {
    m_lastFetched = DTM.NULL == m_lastFetched ? m_cdtm.getFirstAttribute(m_context)
        : m_cdtm.getNextAttribute(m_lastFetched);
    return m_lastFetched;
  }

  /**
   * Returns the axis being iterated, if it is known.
   * 
   * @return Axis.CHILD, etc., or -1 if the axis is not known or is of multiple
   *         types.
   */
  @Override
  public Axis getAxis() {
    return Axis.ATTRIBUTE;
  }

}
