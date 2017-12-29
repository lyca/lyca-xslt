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
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;

/**
 * A step iterator is used to evaluate expressions like "BOOK/TITLE". A better
 * name for this iterator would have been ParentIterator since both "BOOK" and
 * "TITLE" are steps in XPath lingo. Step iterators are constructed from two
 * other iterators which we are going to refer to as "outer" and "inner". Every
 * node from the outer iterator (the one for BOOK in our example) is used to
 * initialize the inner iterator. After this initialization, every node from the
 * inner iterator is returned (in essence, implementing a "nested loop").
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 * @author Morten Jorgensen
 */
public class StepIterator extends DTMAxisIteratorBase {

  /**
   * A reference to the "outer" iterator.
   */
  protected DTMAxisIterator _source;

  /**
   * A reference to the "inner" iterator.
   */
  protected DTMAxisIterator _iterator;

  /**
   * Temp variable to store a marked position.
   */
  private int _pos = -1;

  public StepIterator(DTMAxisIterator source, DTMAxisIterator iterator) {
    _source = source;
    _iterator = iterator;
    // System.out.println("SI source = " + source + " this = " + this);
    // System.out.println("SI iterator = " + iterator + " this = " + this);
  }

  @Override
  public void setRestartable(boolean isRestartable) {
    _isRestartable = isRestartable;
    _source.setRestartable(isRestartable);
    _iterator.setRestartable(true); // must be restartable
  }

  @Override
  public DTMAxisIterator cloneIterator() {
    _isRestartable = false;
    try {
      final StepIterator clone = (StepIterator) super.clone();
      clone._source = _source.cloneIterator();
      clone._iterator = _iterator.cloneIterator();
      clone._iterator.setRestartable(true); // must be restartable
      clone._isRestartable = false;
      return clone.reset();
    } catch (final CloneNotSupportedException e) {
      BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
      return null;
    }
  }

  @Override
  public DTMAxisIterator setStartNode(int node) {
    if (_isRestartable) {
      // Set start node for left-hand iterator...
      _source.setStartNode(_startNode = node);

      // ... and get start node for right-hand iterator from left-hand,
      // with special case for //* path - see ParentLocationPath
      _iterator.setStartNode(_includeSelf ? _startNode : _source.next());
      return resetPosition();
    }
    return this;
  }

  @Override
  public DTMAxisIterator reset() {
    _source.reset();
    // Special case for //* path - see ParentLocationPath
    _iterator.setStartNode(_includeSelf ? _startNode : _source.next());
    return resetPosition();
  }

  @Override
  public int next() {
    for (int node;;) {
      // Try to get another node from the right-hand iterator
      if ((node = _iterator.next()) != END)
        return returnNode(node);
      else if ((node = _source.next()) == END)
        return END;
      else {
        _iterator.setStartNode(node);
      }
    }
  }

  @Override
  public void setMark() {
    _source.setMark();
    _iterator.setMark();
    _pos = _position;
  }

  @Override
  public void gotoMark() {
    _source.gotoMark();
    _iterator.gotoMark();
    _position = _pos;
  }
}
