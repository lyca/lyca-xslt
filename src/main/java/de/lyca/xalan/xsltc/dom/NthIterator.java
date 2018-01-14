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

/**
 * @author Jacek Ambroziak
 * @author Morten Jorgensen
 */
public final class NthIterator extends DTMAxisIteratorBase {
  // ...[N]
  private DTMAxisIterator _source;
  private final int _position;
  private boolean _ready;

  public NthIterator(DTMAxisIterator source, int n) {
    _source = source;
    _position = n;
  }

  @Override
  public void setRestartable(boolean isRestartable) {
    _isRestartable = isRestartable;
    _source.setRestartable(isRestartable);
  }

  @Override
  public DTMAxisIterator cloneIterator() {
    try {
      final NthIterator clone = (NthIterator) super.clone();
      clone._source = _source.cloneIterator(); // resets source
      clone._isRestartable = false;
      return clone;
    } catch (final CloneNotSupportedException e) {
      BasisLibrary.runTimeError(Messages.get().iteratorCloneErr(e.toString()));
      return null;
    }
  }

  @Override
  public int next() {
    if (_ready) {
      _ready = false;
      return _source.getNodeByPosition(_position);
    }
    return DTMAxisIterator.END;
    /*
     * if (_ready && _position > 0) { final int pos = _source.isReverse() ? _source.getLast() - _position + 1 :
     * _position;
     * 
     * _ready = false; int node; while ((node = _source.next()) != DTMAxisIterator.END) { if (pos ==
     * _source.getPosition()) { return node; } } } return DTMAxisIterator.END;
     */
  }

  @Override
  public DTMAxisIterator setStartNode(final int node) {
    if (_isRestartable) {
      _source.setStartNode(node);
      _ready = true;
    }
    return this;
  }

  @Override
  public DTMAxisIterator reset() {
    _source.reset();
    _ready = true;
    return this;
  }

  @Override
  public int getLast() {
    return 1;
  }

  @Override
  public int getPosition() {
    return 1;
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
