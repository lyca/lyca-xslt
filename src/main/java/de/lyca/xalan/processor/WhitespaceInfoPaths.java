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
package de.lyca.xalan.processor;

import java.util.List;

import de.lyca.xalan.templates.Stylesheet;
import de.lyca.xalan.templates.WhiteSpaceInfo;
import de.lyca.xpath.XPath;

public class WhitespaceInfoPaths extends WhiteSpaceInfo {
  static final long serialVersionUID = 5954766719577516723L;

  /**
   * Bean property to allow setPropertiesFromAttributes to get the elements
   * attribute.
   */
  private List<XPath> m_elements;

  /**
   * Set from the elements attribute. This is a list of whitespace delimited
   * element qualified names that specify preservation of whitespace.
   * 
   * @param elems
   *          Should be a non-null reference to a list of
   *          {@link de.lyca.xpath.XPath} objects.
   */
  public void setElements(List<XPath> elems) {
    m_elements = elems;
  }

  /**
   * Get the property set by setElements(). This is a list of whitespace
   * delimited element qualified names that specify preservation of whitespace.
   * 
   * @return A reference to a list of {@link de.lyca.xpath.XPath} objects, or
   *         null.
   */
  List<XPath> getElements() {
    return m_elements;
  }

  public void clearElements() {
    m_elements = null;
  }

  /**
   * Constructor WhitespaceInfoPaths
   * 
   * @param thisSheet
   *          The current stylesheet
   */
  public WhitespaceInfoPaths(Stylesheet thisSheet) {
    super(thisSheet);
    setStylesheet(thisSheet);
  }

}
