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

package de.lyca.xalan.xsltc.compiler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class QName {
  private final String _localname;
  private final String _prefix;
  private String _namespace;
  private final String _stringRep;
  private final int _hashCode;

  public QName(String namespace, String prefix, String localname) {
    _namespace = namespace;
    _prefix = prefix;
    _localname = localname;

    _stringRep = namespace != null && !namespace.isEmpty() ? namespace + ':' + localname : localname;

    _hashCode = _stringRep.hashCode() + 19; // cached for speed
  }

  public void clearNamespace() {
    _namespace = Constants.EMPTYSTRING;
  }

  @Override
  public String toString() {
    return _stringRep;
  }

  public String getStringRep() {
    return _stringRep;
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof QName && _stringRep.equals(((QName) other).getStringRep());
  }

  public String getLocalPart() {
    return _localname;
  }

  public String getNamespace() {
    return _namespace;
  }

  public String getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  public String dump() {
    return "QName: " + _namespace + "(" + _prefix + "):" + _localname;
  }
}
