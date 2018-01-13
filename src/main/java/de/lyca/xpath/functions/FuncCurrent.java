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

import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.axes.LocPathIterator;
import de.lyca.xpath.axes.PredicatedNodeTest;
import de.lyca.xpath.axes.SubContextList;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.patterns.StepPattern;

/**
 * Execute the current() function.
 */
public class FuncCurrent extends Function {
  static final long serialVersionUID = 5715316804877715008L;

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws TransformerException TODO
   */
  @Override
  public XObject execute(XPathContext xctxt) throws TransformerException {

    final SubContextList subContextList = xctxt.getCurrentNodeList();
    int currentNode = DTM.NULL;

    if (null != subContextList) {
      if (subContextList instanceof PredicatedNodeTest) {
        final LocPathIterator iter = ((PredicatedNodeTest) subContextList).getLocPathIterator();
        currentNode = iter.getCurrentContextNode();
      } else if (subContextList instanceof StepPattern)
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSOR_ERROR, null));
    } else {
      // not predicate => ContextNode == CurrentNode
      currentNode = xctxt.getContextNode();
    }
    return new XNodeSet(currentNode, xctxt.getDTMManager());
  }

  /**
   * No arguments to process, so this does nothing.
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    // no-op
  }

}
