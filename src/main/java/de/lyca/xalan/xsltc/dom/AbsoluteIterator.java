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
package de.lyca.xalan.xsltc.dom;

import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xalan.xsltc.runtime.Messages;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;
import de.lyca.xml.dtm.ref.DTMDefaultBase;

/**
 * Absolute iterators ignore the node that is passed to setStartNode(). Instead, they always start from the root node.
 * The node passed to setStartNode() is not totally useless, though. It is needed to obtain the DOM mask, i.e. the index
 * into the MultiDOM table that corresponds to the DOM "owning" the node.
 * 
 * The DOM mask is cached, so successive calls to setStartNode() passing nodes from other DOMs will have no effect (i.e.
 * this iterator cannot migrate between DOMs).
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class AbsoluteIterator extends DTMAxisIteratorBase {

  /**
   * Source for this iterator.
   */
  private DTMAxisIterator _source;

  public AbsoluteIterator(DTMAxisIterator source) {
    _source = source;
    // System.out.println("AI source = " + source + " this = " + this);
  }

  @Override
  public void setRestartable(boolean isRestartable) {
    _isRestartable = isRestartable;
    _source.setRestartable(isRestartable);
  }

  @Override
  public DTMAxisIterator setStartNode(int node) {
    _startNode = DTMDefaultBase.ROOTNODE;
    if (_isRestartable) {
      _source.setStartNode(_startNode);
      resetPosition();
    }
    return this;
  }

  @Override
  public int next() {
    return returnNode(_source.next());
  }

  @Override
  public DTMAxisIterator cloneIterator() {
    try {
      final AbsoluteIterator clone = (AbsoluteIterator) super.clone();
      clone._source = _source.cloneIterator(); // resets source
      clone.resetPosition();
      clone._isRestartable = false;
      return clone;
    } catch (final CloneNotSupportedException e) {
      BasisLibrary.runTimeError(Messages.get().iteratorCloneErr(e.toString()));
      return null;
    }
  }

  @Override
  public DTMAxisIterator reset() {
    _source.reset();
    return resetPosition();
  }

  @Override
  public void setMark() {
    _source.setMark();
  }

  @Override
  public void gotoMark() {
    _source.gotoMark();
  }
}
