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

import de.lyca.xalan.xsltc.util.IntegerArray;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;

/**
 * CachedNodeListIterator is used for select expressions in a variable or
 * parameter. This iterator caches all nodes in an IntegerArray. Its
 * cloneIterator() method is overridden to return an object of
 * ClonedNodeListIterator.
 */
public final class CachedNodeListIterator extends DTMAxisIteratorBase {

  /**
   * Source for this iterator.
   */
  private final DTMAxisIterator _source;
  private final IntegerArray _nodes = new IntegerArray();
  private int _numCachedNodes = 0;
  private int _index = 0;
  private boolean _isEnded = false;

  public CachedNodeListIterator(DTMAxisIterator source) {
    _source = source;
  }

  @Override
  public void setRestartable(boolean isRestartable) {
    // _isRestartable = isRestartable;
    // _source.setRestartable(isRestartable);
  }

  @Override
  public DTMAxisIterator setStartNode(int node) {
    if (_isRestartable) {
      _startNode = node;
      _source.setStartNode(node);
      resetPosition();

      _isRestartable = false;
    }
    return this;
  }

  @Override
  public int next() {
    return getNode(_index++);
  }

  @Override
  public int getPosition() {
    return _index == 0 ? 1 : _index;
  }

  @Override
  public int getNodeByPosition(int pos) {
    return getNode(pos);
  }

  public int getNode(int index) {
    if (index < _numCachedNodes)
      return _nodes.at(index);
    else if (!_isEnded) {
      final int node = _source.next();
      if (node != END) {
        _nodes.add(node);
        _numCachedNodes++;
      } else {
        _isEnded = true;
      }
      return node;
    } else
      return END;
  }

  @Override
  public DTMAxisIterator cloneIterator() {
    final ClonedNodeListIterator clone = new ClonedNodeListIterator(this);
    return clone;
  }

  @Override
  public DTMAxisIterator reset() {
    _index = 0;
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
