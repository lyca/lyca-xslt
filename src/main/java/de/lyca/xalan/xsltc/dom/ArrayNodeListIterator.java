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

package de.lyca.xalan.xsltc.dom;

import de.lyca.xml.dtm.DTMAxisIterator;

public class ArrayNodeListIterator implements DTMAxisIterator {

  private int _pos = 0;

  private int _mark = 0;

  private int _nodes[];

  private static final int[] EMPTY = {};

  public ArrayNodeListIterator(int[] nodes) {
    _nodes = nodes;
  }

  @Override
  public int next() {
    return _pos < _nodes.length ? _nodes[_pos++] : END;
  }

  @Override
  public DTMAxisIterator reset() {
    _pos = 0;
    return this;
  }

  @Override
  public int getLast() {
    return _nodes.length;
  }

  @Override
  public int getPosition() {
    return _pos;
  }

  @Override
  public void setMark() {
    _mark = _pos;
  }

  @Override
  public void gotoMark() {
    _pos = _mark;
  }

  @Override
  public DTMAxisIterator setStartNode(int node) {
    if (node == END) {
      _nodes = EMPTY;
    }
    return this;
  }

  @Override
  public int getStartNode() {
    return END;
  }

  @Override
  public boolean isReverse() {
    return false;
  }

  @Override
  public DTMAxisIterator cloneIterator() {
    return new ArrayNodeListIterator(_nodes);
  }

  @Override
  public void setRestartable(boolean isRestartable) {
  }

  @Override
  public int getNodeByPosition(int position) {
    return _nodes[position - 1];
  }

}
