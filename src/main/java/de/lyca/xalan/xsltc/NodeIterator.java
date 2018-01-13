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
package de.lyca.xalan.xsltc;

import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public interface NodeIterator extends Cloneable {
  public static final int END = DTM.NULL;

  /**
   * Callers should not call next() after it returns END.
   * 
   * @return TODO
   */
  public int next();

  /**
   * Resets the iterator to the last start node.
   * 
   * @return TODO
   */
  public NodeIterator reset();

  /**
   * Returns the number of elements in this iterator.
   * 
   * @return TODO
   */
  public int getLast();

  /**
   * Returns the position of the current node in the set.
   * 
   * @return TODO
   */
  public int getPosition();

  /**
   * Remembers the current node for the next call to gotoMark().
   */
  public void setMark();

  /**
   * Restores the current node remembered by setMark().
   */
  public void gotoMark();

  /**
   * Set start to END should 'close' the iterator, i.e. subsequent call to
   * next() should return END.
   * 
   * @param node TODO
   * @return TODO
   */
  public NodeIterator setStartNode(int node);

  /**
   * True if this iterator has a reversed axis.
   * 
   * @return TODO
   */
  public boolean isReverse();

  /**
   * Returns a deep copy of this iterator.
   * 
   * @return TODO
   */
  public NodeIterator cloneIterator();

  /**
   * Prevents or allows iterator restarts.
   * 
   * @param isRestartable TODO
   */
  public void setRestartable(boolean isRestartable);

}
