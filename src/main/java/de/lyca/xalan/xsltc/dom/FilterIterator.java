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
import de.lyca.xml.dtm.DTMFilter;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;

/**
 * Similar to a CurrentNodeListIterator except that the filter has a simpler interface (only needs the node, no
 * position, last, etc.) It takes a source iterator and a Filter object and returns nodes from the source after
 * filtering them by calling filter.test(node).
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class FilterIterator extends DTMAxisIteratorBase {

  /**
   * Reference to source iterator.
   */
  private DTMAxisIterator _source;

  /**
   * Reference to a filter object that to be applied to each node.
   */
  private final DTMFilter _filter;

  /**
   * A flag indicating if position is reversed.
   */
  private final boolean _isReverse;

  public FilterIterator(DTMAxisIterator source, DTMFilter filter) {
    _source = source;
    // System.out.println("FI souce = " + source + " this = " + this);
    _filter = filter;
    _isReverse = source.isReverse();
  }

  @Override
  public boolean isReverse() {
    return _isReverse;
  }

  @Override
  public void setRestartable(boolean isRestartable) {
    _isRestartable = isRestartable;
    _source.setRestartable(isRestartable);
  }

  @Override
  public DTMAxisIterator cloneIterator() {

    try {
      final FilterIterator clone = (FilterIterator) super.clone();
      clone._source = _source.cloneIterator();
      clone._isRestartable = false;
      return clone.reset();
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
  public int next() {
    int node;
    while ((node = _source.next()) != END) {
      if (_filter.acceptNode(node, DTMFilter.SHOW_ALL) == DTMIterator.FILTER_ACCEPT)
        return returnNode(node);
    }
    return END;
  }

  @Override
  public DTMAxisIterator setStartNode(int node) {
    if (_isRestartable) {
      _source.setStartNode(_startNode = node);
      return resetPosition();
    }
    return this;
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
