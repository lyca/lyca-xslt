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

import java.util.List;

import javax.xml.transform.TransformerException;

import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.Expression;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.res.Messages;

/**
 * This class makes an select statement act like an result tree fragment.
 */
public class XRTreeFragSelectWrapper extends XRTreeFrag implements Cloneable {
  static final long serialVersionUID = -6526177905590461251L;

  public XRTreeFragSelectWrapper(Expression expr) {
    super(expr);
  }

  /**
   * This function is used to fixup variables from QNames to stack frame indexes at stylesheet build time.
   * 
   * @param vars List of QNames that correspond to variables. This list should be searched backwards for the first
   *        qualified name that corresponds to the variable reference qname. The position of the QName in the list from
   *        the start of the list will be its position in the stack frame (but variables above the globalsTop value will
   *        need to be offset to the current stack frame).
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    ((Expression) m_obj).fixupVariables(vars, globalsSize);
  }

  /**
   * For support of literal objects in xpaths.
   * 
   * @param xctxt The XPath execution context.
   * @return the result of executing the select expression
   * @throws TransformerException TODO
   */
  @Override
  public XObject execute(XPathContext xctxt) throws TransformerException {
    XObject m_selected;
    m_selected = ((Expression) m_obj).execute(xctxt);
    m_selected.allowDetachToRelease(m_allowRelease);
    if (m_selected.getType() == CLASS_STRING)
      return m_selected;
    else
      return new XString(m_selected.str());
  }

  /**
   * Detaches the <code>DTMIterator</code> from the set which it iterated over, releasing any computational resources
   * and placing the iterator in the INVALID state. After <code>detach</code> has been invoked, calls to
   * <code>nextNode</code> or <code>previousNode</code> will raise a runtime exception. In general, detach should only
   * be called once on the object.
   */
  @Override
  public void detach() {
    throw new RuntimeException(Messages.get().detachNotSupportedXrtreefragselectwrapper());
  }

  /**
   * Cast result object to a number.
   * 
   * @return The result tree fragment as a number or NaN
   */
  @Override
  public double num() throws TransformerException {
    throw new RuntimeException(Messages.get().numNotSupportedXrtreefragselectwrapper());
  }

  /**
   * Cast result object to an XMLString.
   * 
   * @return The document fragment node data or the empty string.
   */
  @Override
  public XMLString xstr() {
    throw new RuntimeException(Messages.get().xstrNotSupportedXrtreefragselectwrapper());
  }

  /**
   * Cast result object to a string.
   * 
   * @return The document fragment node data or the empty string.
   */
  @Override
  public String str() {
    throw new RuntimeException(Messages.get().strNotSupportedXrtreefragselectwrapper());
  }

  /**
   * Tell what kind of class this is.
   * 
   * @return the string type
   */
  @Override
  public int getType() {
    return CLASS_STRING;
  }

  /**
   * Cast result object to a result tree fragment.
   * 
   * @return The document fragment this wraps
   */
  @Override
  public int rtf() {
    throw new RuntimeException(Messages.get().rtfNotSupportedXrtreefragselectwrapper());
  }

  /**
   * Cast result object to a DTMIterator.
   * 
   * @return The document fragment as a DTMIterator
   */
  @Override
  public DTMIterator asNodeIterator() {
    throw new RuntimeException(Messages.get().rtfNotSupportedXrtreefragselectwrapper());
  }

}
