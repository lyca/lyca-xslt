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

import java.text.Collator;
import java.util.Locale;

import de.lyca.xalan.ObjectFactory;
import de.lyca.xalan.ObjectFactory.ConfigurationError;
import de.lyca.xalan.xsltc.CollatorFactory;
import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.utils.StringComparable;

/**
 * Base class for sort records containing application specific sort keys
 */
public abstract class NodeSortRecord {
  public static final int COMPARE_STRING = 0;
  public static final int COMPARE_NUMERIC = 1;

  public static final int COMPARE_ASCENDING = 0;
  public static final int COMPARE_DESCENDING = 1;

  protected Collator[] collators;

  protected CollatorFactory collatorFactory;

  protected SortSettings settings;

  private DOM dom = null;
  private int node; // The position in the current iterator
  private int last = 0; // Number of nodes in the current iterator
  private int scanned = 0; // Number of key levels extracted from DOM

  private Object[] values; // Contains Comparable objects

  /**
   * This constructor is run by a call to ClassLoader in the makeNodeSortRecord
   * method in the NodeSortRecordFactory class. Since we cannot pass any
   * parameters to the constructor in that case we just set the default values
   * here and wait for new values through initialize().
   */
  public NodeSortRecord(int node) {
    this.node = node;
  }

  public NodeSortRecord() {
    this(0);
  }

  /**
   * This method allows the caller to set the values that could not be passed to
   * the default constructor.
   */
  public final void initialize(int node, int last, DOM dom, SortSettings settings) throws TransletException {
    this.dom = dom;
    this.node = node;
    this.last = last;
    this.settings = settings;

    final int levels = settings.getSortOrders().length;
    values = new Object[levels];

    // -- W. Eliot Kimber (eliot@isogen.com)
    final String collatorFactoryClassName = System.getProperty("de.lyca.xalan.xsltc.COLLATOR_FACTORY");

    if (collatorFactoryClassName == null) {
      collators = settings.getCollators();
    } else {
      try {
        collatorFactory = (CollatorFactory) ObjectFactory.newInstance(collatorFactoryClassName,
            ObjectFactory.findClassLoader(), true);
      } catch (final ConfigurationError e) {
        throw new TransletException(e.getException());
      }
      final Locale[] locales = settings.getLocales();
      collators = new Collator[levels];
      for (int i = 0; i < levels; i++) {
        collators[i] = collatorFactory.getCollator(locales[i]);
      }
    }
  }

  /**
   * Returns the node for this sort object
   */
  public final int getNode() {
    return node;
  }

  public final int compareDocOrder(NodeSortRecord other) {
    return node - other.node;
  }

  /**
   * Get the string or numeric value of a specific level key for this sort
   * element. The value is extracted from the DOM if it is not already in our
   * sort key vector.
   */
  private final Comparable stringValue(int level) throws TransletException {
    // Get value from our array if possible
    if (scanned <= level) {
      final AbstractTranslet translet = settings.getTranslet();
      final Locale[] locales = settings.getLocales();
      final String[] caseOrder = settings.getCaseOrders();

      // Get value from DOM if accessed for the first time
      final String str = extractValueFromDOM(dom, node, level, translet, last);
      final Comparable key = StringComparable.getComparator(str, locales[level], collators[level], caseOrder[level]);
      values[scanned++] = key;
      return key;
    }
    return (Comparable) values[level];
  }

  private final Double numericValue(int level) throws TransletException {
    // Get value from our vector if possible
    if (scanned <= level) {
      final AbstractTranslet translet = settings.getTranslet();

      // Get value from DOM if accessed for the first time
      final String str = extractValueFromDOM(dom, node, level, translet, last);
      Double num;
      try {
        num = new Double(str);
      }
      // Treat number as NaN if it cannot be parsed as a double
      catch (final NumberFormatException e) {
        num = new Double(Double.NEGATIVE_INFINITY);
      }
      values[scanned++] = num;
      return num;
    }
    return (Double) values[level];
  }

  /**
   * Compare this sort element to another. The first level is checked first, and
   * we proceed to the next level only if the first level keys are identical
   * (and so the key values may not even be extracted from the DOM)
   * 
   * !!!!MUST OPTIMISE - THIS IS REALLY, REALLY SLOW!!!!
   */
  public int compareTo(NodeSortRecord other) throws TransletException {
    int cmp, level;
    final int[] sortOrder = settings.getSortOrders();
    final int levels = settings.getSortOrders().length;
    final int[] compareTypes = settings.getTypes();

    for (level = 0; level < levels; level++) {
      // Compare the two nodes either as numeric or text values
      if (compareTypes[level] == COMPARE_NUMERIC) {
        final Double our = numericValue(level);
        final Double their = other.numericValue(level);
        cmp = our.compareTo(their);
      } else {
        final Comparable our = stringValue(level);
        final Comparable their = other.stringValue(level);
        cmp = our.compareTo(their);
      }

      // Return inverse compare value if inverse sort order
      if (cmp != 0)
        return sortOrder[level] == COMPARE_DESCENDING ? 0 - cmp : cmp;
    }
    // Compare based on document order if all sort keys are equal
    return node - other.node;
  }

  /**
   * Returns the array of Collators used for text comparisons in this object.
   * May be overridden by inheriting classes
   */
  public Collator[] getCollator() {
    return collators;
  }

  /**
   * Extract the sort value for a level of this key.
   */
  public abstract String extractValueFromDOM(DOM dom, int current, int level, AbstractTranslet translet, int last)
      throws TransletException;

}
