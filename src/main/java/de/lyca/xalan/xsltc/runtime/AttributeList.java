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

package de.lyca.xalan.xsltc.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Jorgensen
 */
public class AttributeList implements org.xml.sax.Attributes {

  private final static String CDATASTRING = "CDATA";

  private Map<String, Integer> _attributes;
  private List<String> _names;
  private List<String> _qnames;
  private List<String> _values;
  private List<String> _uris;
  private int _length;

  /**
   * AttributeList constructor
   */
  public AttributeList() {
    _length = 0;
  }

  /**
   * Attributes clone constructor
   */
  public AttributeList(org.xml.sax.Attributes attributes) {
    this();
    if (attributes != null) {
      final int count = attributes.getLength();
      for (int i = 0; i < count; i++) {
        add(attributes.getQName(i), attributes.getValue(i));
      }
    }
  }

  /**
   * Allocate memory for the AttributeList %OPT% Use on-demand allocation for
   * the internal lists. The memory is only allocated when there is an
   * attribute. This reduces the cost of creating many small RTFs.
   */
  private void alloc() {
    _attributes = new HashMap<>();
    _names = new ArrayList<>();
    _values = new ArrayList<>();
    _qnames = new ArrayList<>();
    _uris = new ArrayList<>();
  }

  /**
   * SAX2: Return the number of attributes in the list.
   */
  @Override
  public int getLength() {
    return _length;
  }

  /**
   * SAX2: Look up an attribute's Namespace URI by index.
   */
  @Override
  public String getURI(int index) {
    if (index < _length)
      return _uris.get(index);
    else
      return null;
  }

  /**
   * SAX2: Look up an attribute's local name by index.
   */
  @Override
  public String getLocalName(int index) {
    if (index < _length)
      return _names.get(index);
    else
      return null;
  }

  /**
   * Return the name of an attribute in this list (by position).
   */
  @Override
  public String getQName(int pos) {
    if (pos < _length)
      return _qnames.get(pos);
    else
      return null;
  }

  /**
   * SAX2: Look up an attribute's type by index.
   */
  @Override
  public String getType(int index) {
    return CDATASTRING;
  }

  /**
   * SAX2: Look up the index of an attribute by Namespace name.
   */
  @Override
  public int getIndex(String namespaceURI, String localPart) {
    return -1;
  }

  /**
   * SAX2: Look up the index of an attribute by XML 1.0 qualified name.
   */
  @Override
  public int getIndex(String qname) {
    return -1;
  }

  /**
   * SAX2: Look up an attribute's type by Namespace name.
   */
  @Override
  public String getType(String uri, String localName) {
    return CDATASTRING;
  }

  /**
   * SAX2: Look up an attribute's type by qname.
   */
  @Override
  public String getType(String qname) {
    return CDATASTRING;
  }

  /**
   * SAX2: Look up an attribute's value by index.
   */
  @Override
  public String getValue(int pos) {
    if (pos < _length)
      return _values.get(pos);
    else
      return null;
  }

  /**
   * SAX2: Look up an attribute's value by qname.
   */
  @Override
  public String getValue(String qname) {
    if (_attributes != null) {
      final Integer obj = _attributes.get(qname);
      if (obj == null)
        return null;
      return getValue(obj.intValue());
    } else
      return null;
  }

  /**
   * SAX2: Look up an attribute's value by Namespace name - SLOW!
   */
  @Override
  public String getValue(String uri, String localName) {
    return getValue(uri + ':' + localName);
  }

  /**
   * Adds an attribute to the list
   */
  public void add(String qname, String value) {
    // Initialize the internal lists at the first usage.
    if (_attributes == null) {
      alloc();
    }

    // Stuff the QName into the names list & map
    Integer obj = _attributes.get(qname);
    if (obj == null) {
      _attributes.put(qname, obj = new Integer(_length++));
      _qnames.add(qname);
      _values.add(value);
      final int col = qname.lastIndexOf(':');
      if (col > -1) {
        _uris.add(qname.substring(0, col));
        _names.add(qname.substring(col + 1));
      } else {
        _uris.add("");
        _names.add(qname);
      }
    } else {
      final int index = obj.intValue();
      _values.set(index, value);
    }
  }

  /**
   * Clears the attribute list
   */
  public void clear() {
    _length = 0;
    if (_attributes != null) {
      _attributes.clear();
      _names.clear();
      _values.clear();
      _qnames.clear();
      _uris.clear();
    }
  }

}
