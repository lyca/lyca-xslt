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
package de.lyca.xml.dtm;

/**
 * This interface is meant to be implemented by a client of the DTM, and allows
 * stripping of whitespace nodes.
 */
public interface DTMWSFilter {

  public enum Mode {
    /**
     * Do not strip whitespace child nodes of this element.
     */
    NOTSTRIP,

    /**
     * Strip whitespace child nodes of this element.
     */
    STRIP,

    /**
     * Inherit whitespace stripping behavior of the parent node.
     */
    INHERIT
  }

  /**
   * Test whether whitespace-only text nodes are visible in the logical view of
   * <code>DTM</code>. Normally, this function will be called by the
   * implementation of <code>DTM</code>; it is not normally called directly from
   * user code.
   * 
   * @param elementHandle
   *          int Handle of the element.
   * @return one of NOTSTRIP, STRIP, or INHERIT.
   */
  Mode getShouldStripSpace(int elementHandle, DTM dtm);

}
