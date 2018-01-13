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
package de.lyca.xml.utils;

/**
 * A very simple table that stores a list of byte. Very similar API to our
 * IntVector class (same API); different internal storage.
 * 
 * This version uses an array-of-arrays solution. Read/write access is thus a
 * bit slower than the simple IntVector, and basic storage is a trifle higher
 * due to the top-level array -- but appending is O(1) fast rather than O(N**2)
 * slow, which will swamp those costs in situations where long vectors are being
 * built up.
 * 
 * Known issues:
 * 
 * Some methods are private because they haven't yet been tested properly.
 * 
 * If an element has not been set (because we skipped it), its value will
 * initially be 0. Shortening the vector does not clear old storage; if you then
 * skip values and setElementAt a higher index again, you may see old data
 * reappear in the truncated-and-restored section. Doing anything else would
 * have performance costs.
 */
public class SuballocatedByteVector {
  /** Size of blocks to allocate */
  protected int m_blocksize;

  /** Number of blocks to (over)allocate by */
  protected int m_numblocks = 32;

  /** Array of arrays of bytes */
  protected byte m_map[][];

  /** Number of bytes in array */
  protected int m_firstFree = 0;

  /** "Shortcut" handle to m_map[0] */
  protected byte m_map0[];

  /**
   * Default constructor. Note that the default block size is very small, for
   * small lists.
   */
  public SuballocatedByteVector() {
    this(2048);
  }

  /**
   * Construct a ByteVector, using the given block size.
   * 
   * @param blocksize
   *          Size of block to allocate
   */
  public SuballocatedByteVector(int blocksize) {
    m_blocksize = blocksize;
    m_map0 = new byte[blocksize];
    m_map = new byte[m_numblocks][];
    m_map[0] = m_map0;
  }

  /**
   * Construct a ByteVector, using the given block size.
   * 
   * @param blocksize
   *          Size of block to allocate
   */
  public SuballocatedByteVector(int blocksize, int increaseSize) {
    // increaseSize not currently used.
    this(blocksize);
  }

  /**
   * Get the length of the list.
   * 
   * @return length of the list
   */
  public int size() {
    return m_firstFree;
  }

  /**
   * Append a byte onto the vector.
   * 
   * @param value
   *          Byte to add to the list
   */
  public void addElement(byte value) {
    if (m_firstFree < m_blocksize) {
      m_map0[m_firstFree++] = value;
    } else {
      final int index = m_firstFree / m_blocksize;
      final int offset = m_firstFree % m_blocksize;
      ++m_firstFree;

      if (index >= m_map.length) {
        final int newsize = index + m_numblocks;
        final byte[][] newMap = new byte[newsize][];
        System.arraycopy(m_map, 0, newMap, 0, m_map.length);
        m_map = newMap;
      }
      byte[] block = m_map[index];
      if (null == block) {
        block = m_map[index] = new byte[m_blocksize];
      }
      block[offset] = value;
    }
  }

  /**
   * Wipe it out.
   */
  public void removeAllElements() {
    m_firstFree = 0;
  }

  /**
   * Sets the component at the specified index of this vector to be the
   * specified object. The previous component at that position is discarded.
   * 
   * The index must be a value greater than or equal to 0 and less than the
   * current size of the vector.
   * 
   * @param value
   * @param at
   *          Index of where to set the object
   */
  public void setElementAt(byte value, int at) {
    if (at < m_blocksize) {
      m_map0[at] = value;
      return;
    }

    final int index = at / m_blocksize;
    final int offset = at % m_blocksize;

    if (index >= m_map.length) {
      final int newsize = index + m_numblocks;
      final byte[][] newMap = new byte[newsize][];
      System.arraycopy(m_map, 0, newMap, 0, m_map.length);
      m_map = newMap;
    }

    byte[] block = m_map[index];
    if (null == block) {
      block = m_map[index] = new byte[m_blocksize];
    }
    block[offset] = value;

    if (at >= m_firstFree) {
      m_firstFree = at + 1;
    }
  }

  /**
   * Get the nth element. This is often at the innermost loop of an application,
   * so performance is critical.
   * 
   * @param i
   *          index of value to get
   * 
   * @return value at given index. If that value wasn't previously set, the
   *         result is undefined for performance reasons. It may throw an
   *         exception (see below), may return zero, or (if setSize has
   *         previously been used) may return stale data.
   * 
   * @throws ArrayIndexOutOfBoundsException
   *           if the index was _clearly_ unreasonable (negative, or past the
   *           highest block).
   * 
   * @throws NullPointerException
   *           if the index points to a block that could have existed (based on
   *           the highest index used) but has never had anything set into it.
   *           %REVIEW% Could add a catch to create the block in that case, or
   *           return 0. Try/Catch is _supposed_ to be nearly free when not
   *           thrown to. Do we believe that? Should we have a separate
   *           safeElementAt?
   */
  public byte elementAt(int i) {
    // %OPT% Does this really buy us anything? Test versus division for small,
    // test _plus_ division for big docs.
    if (i < m_blocksize)
      return m_map0[i];

    return m_map[i / m_blocksize][i % m_blocksize];
  }

  /**
   * Searches for the first occurence of the given argument, beginning the
   * search at index, and testing for equality using the equals method.
   * 
   * @param elem
   *          object to look for
   * @param index
   *          Index of where to begin search
   * @return the index of the first occurrence of the object argument in this
   *         vector at position index or later in the vector; returns -1 if the
   *         object is not found.
   */
  public int indexOf(byte elem, int index) {
    if (index >= m_firstFree)
      return -1;

    int bindex = index / m_blocksize;
    int boffset = index % m_blocksize;
    final int maxindex = m_firstFree / m_blocksize;
    byte[] block;

    for (; bindex < maxindex; ++bindex) {
      block = m_map[bindex];
      if (block != null) {
        for (int offset = boffset; offset < m_blocksize; ++offset)
          if (block[offset] == elem)
            return offset + bindex * m_blocksize;
      }
      boffset = 0; // after first
    }
    // Last block may need to stop before end
    final int maxoffset = m_firstFree % m_blocksize;
    block = m_map[maxindex];
    for (int offset = boffset; offset < maxoffset; ++offset)
      if (block[offset] == elem)
        return offset + maxindex * m_blocksize;

    return -1;
  }

  /**
   * Searches for the first occurence of the given argument, beginning the
   * search at index, and testing for equality using the equals method.
   * 
   * @param elem
   *          object to look for
   * @return the index of the first occurrence of the object argument in this
   *         vector at position index or later in the vector; returns -1 if the
   *         object is not found.
   */
  public int indexOf(byte elem) {
    return indexOf(elem, 0);
  }

}
