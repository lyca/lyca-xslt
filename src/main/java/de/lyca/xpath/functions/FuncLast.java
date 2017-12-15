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
package de.lyca.xpath.functions;

import java.util.List;

import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.axes.SubContextList;
import de.lyca.xpath.compiler.Compiler;
import de.lyca.xpath.objects.XNumber;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the Last() function.
 * 
 * @xsl.usage advanced
 */
public class FuncLast extends Function {
  static final long serialVersionUID = 9205812403085432943L;

  private boolean m_isTopLevel;

  /**
   * Figure out if we're executing a toplevel expression. If so, we can't be
   * inside of a predicate.
   */
  @Override
  public void postCompileStep(Compiler compiler) {
    m_isTopLevel = compiler.getLocationPathDepth() == -1;
  }

  /**
   * Get the position in the current context node list.
   * 
   * @param xctxt
   *          non-null reference to XPath runtime context.
   * 
   * @return The number of nodes in the list.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  public int getCountOfContextNodeList(XPathContext xctxt) throws javax.xml.transform.TransformerException {

    // assert(null != m_contextNodeList, "m_contextNodeList must be non-null");
    // If we're in a predicate, then this will return non-null.
    final SubContextList iter = m_isTopLevel ? null : xctxt.getSubContextList();

    // System.out.println("iter: "+iter);
    if (null != iter)
      return iter.getLastPos(xctxt);

    final DTMIterator cnl = xctxt.getContextNodeList();
    int count;
    if (null != cnl) {
      count = cnl.getLength();
      // System.out.println("count: "+count);
    } else {
      count = 0;
    }
    return count;
  }

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {
    final XNumber xnum = new XNumber(getCountOfContextNodeList(xctxt));
    // System.out.println("last: "+xnum.num());
    return xnum;
  }

  /**
   * No arguments to process, so this does nothing.
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    // no-op
  }

}
