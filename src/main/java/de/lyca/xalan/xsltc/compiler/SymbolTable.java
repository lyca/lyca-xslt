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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.lyca.xalan.xsltc.compiler.util.MethodType;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class SymbolTable {

  // These Maps are used for all stylesheets
  private final Map<QName, Stylesheet> _stylesheets = new HashMap<>();
  private final Map<String, List<MethodType>> _primops = new HashMap<>();

  // These Maps are used for some stylesheets
  private Map<String, VariableBase> _variables = null;
  private Map<QName, Template> _templates = null;
  private Map<QName, AttributeSet> _attributeSets = null;
  private Map<String, String> _aliases = null;
  private Map<String, Integer> _excludedURI = null;
  private Deque<Map<String, Integer>> _excludedURIStack = null;
  private Map<QName, DecimalFormatting> _decimalFormats = null;
  private Map<QName, Key> _keys = null;

  public DecimalFormatting getDecimalFormatting(QName name) {
    return _decimalFormats == null ? null : _decimalFormats.get(name);
  }

  public void addDecimalFormatting(QName name, DecimalFormatting symbols) {
    if (_decimalFormats == null) {
      _decimalFormats = new HashMap<>();
    }
    _decimalFormats.put(name, symbols);
  }

  public Key getKey(QName name) {
    return _keys == null ? null : _keys.get(name);
  }

  public void addKey(QName name, Key key) {
    if (_keys == null) {
      _keys = new HashMap<>();
    }
    _keys.put(name, key);
  }

  public Stylesheet addStylesheet(QName name, Stylesheet node) {
    return _stylesheets.put(name, node);
  }

  public Stylesheet lookupStylesheet(QName name) {
    return _stylesheets.get(name);
  }

  public Template addTemplate(Template template) {
    final QName name = template.getName();
    if (_templates == null) {
      _templates = new HashMap<>();
    }
    return _templates.put(name, template);
  }

  public Template lookupTemplate(QName name) {
    return _templates == null ? null : _templates.get(name);
  }

  public Variable addVariable(Variable variable) {
    if (_variables == null) {
      _variables = new HashMap<>();
    }
    final String name = variable.getName().getStringRep();
    return (Variable) _variables.put(name, variable);
  }

  public Param addParam(Param parameter) {
    if (_variables == null) {
      _variables = new HashMap<>();
    }
    final String name = parameter.getName().getStringRep();
    return (Param) _variables.put(name, parameter);
  }

  public Variable lookupVariable(QName qname) {
    if (_variables == null)
      return null;
    final String name = qname.getStringRep();
    final Object obj = _variables.get(name);
    return obj instanceof Variable ? (Variable) obj : null;
  }

  public Param lookupParam(QName qname) {
    if (_variables == null)
      return null;
    final String name = qname.getStringRep();
    final Object obj = _variables.get(name);
    return obj instanceof Param ? (Param) obj : null;
  }

  public SyntaxTreeNode lookupName(QName qname) {
    if (_variables == null)
      return null;
    final String name = qname.getStringRep();
    return _variables.get(name);
  }

  public AttributeSet addAttributeSet(AttributeSet atts) {
    if (_attributeSets == null) {
      _attributeSets = new HashMap<>();
    }
    return _attributeSets.put(atts.getName(), atts);
  }

  public AttributeSet lookupAttributeSet(QName name) {
    if (_attributeSets == null)
      return null;
    return _attributeSets.get(name);
  }

  /**
   * Add a primitive operator or function to the symbol table. To avoid name
   * clashes with user-defined names, the prefix <tt>PrimopPrefix</tt> is
   * prepended.
   */
  public void addPrimop(String name, MethodType mtype) {
    List<MethodType> methods = _primops.get(name);
    if (methods == null) {
      _primops.put(name, methods = new ArrayList<MethodType>());
    }
    methods.add(mtype);
  }

  /**
   * Lookup a primitive operator or function in the symbol table by prepending
   * the prefix <tt>PrimopPrefix</tt>.
   */
  public List<MethodType> lookupPrimop(String name) {
    return _primops.get(name);
  }

  /**
   * This is used for xsl:attribute elements that have a "namespace" attribute
   * that is currently not defined using xmlns:
   */
  private int _nsCounter = 0;

  public String generateNamespacePrefix() {
    return "ns" + _nsCounter++;
  }

  /**
   * Use a namespace prefix to lookup a namespace URI
   */
  private SyntaxTreeNode _current = null;

  public void setCurrentNode(SyntaxTreeNode node) {
    _current = node;
  }

  public String lookupNamespace(String prefix) {
    if (_current == null)
      return Constants.EMPTYSTRING;
    return _current.lookupNamespace(prefix);
  }

  /**
   * Adds an alias for a namespace prefix
   */
  public void addPrefixAlias(String prefix, String alias) {
    if (_aliases == null) {
      _aliases = new HashMap<>();
    }
    _aliases.put(prefix, alias);
  }

  /**
   * Retrieves any alias for a given namespace prefix
   */
  public String lookupPrefixAlias(String prefix) {
    if (_aliases == null)
      return null;
    return _aliases.get(prefix);
  }

  /**
   * Register a namespace URI so that it will not be declared in the output
   * unless it is actually referenced in the output.
   */
  public void excludeURI(String uri) {
    // The null-namespace cannot be excluded
    if (uri == null)
      return;

    // Create new Map of exlcuded URIs if none exists
    if (_excludedURI == null) {
      _excludedURI = new HashMap<>();
    }

    // Register the namespace URI
    Integer refcnt = _excludedURI.get(uri);
    if (refcnt == null) {
      refcnt = new Integer(1);
    } else {
      refcnt = new Integer(refcnt.intValue() + 1);
    }
    _excludedURI.put(uri, refcnt);
  }

  /**
   * Exclude a series of namespaces given by a list of whitespace separated
   * namespace prefixes.
   */
  public void excludeNamespaces(String prefixes) {
    if (prefixes != null) {
      final StringTokenizer tokens = new StringTokenizer(prefixes);
      while (tokens.hasMoreTokens()) {
        final String prefix = tokens.nextToken();
        final String uri;
        if (prefix.equals("#default")) {
          uri = lookupNamespace(Constants.EMPTYSTRING);
        } else {
          uri = lookupNamespace(prefix);
        }
        if (uri != null) {
          excludeURI(uri);
        }
      }
    }
  }

  /**
   * Check if a namespace should not be declared in the output (unless used)
   */
  public boolean isExcludedNamespace(String uri) {
    if (uri != null && _excludedURI != null) {
      final Integer refcnt = _excludedURI.get(uri);
      return refcnt != null && refcnt.intValue() > 0;
    }
    return false;
  }

  /**
   * Turn of namespace declaration exclusion
   */
  public void unExcludeNamespaces(String prefixes) {
    if (_excludedURI == null)
      return;
    if (prefixes != null) {
      final StringTokenizer tokens = new StringTokenizer(prefixes);
      while (tokens.hasMoreTokens()) {
        final String prefix = tokens.nextToken();
        final String uri;
        if (prefix.equals("#default")) {
          uri = lookupNamespace(Constants.EMPTYSTRING);
        } else {
          uri = lookupNamespace(prefix);
        }
        final Integer refcnt = _excludedURI.get(uri);
        if (refcnt != null) {
          _excludedURI.put(uri, new Integer(refcnt.intValue() - 1));
        }
      }
    }
  }

  /**
   * Exclusion of namespaces by a stylesheet does not extend to any stylesheet
   * imported or included by the stylesheet. Upon entering the context of a new
   * stylesheet, a call to this method is needed to clear the current set of
   * excluded namespaces temporarily. Every call to this method requires a
   * corresponding call to {@link #popExcludedNamespacesContext()}.
   */
  public void pushExcludedNamespacesContext() {
    if (_excludedURIStack == null) {
      _excludedURIStack = new LinkedList<>();
    }
    _excludedURIStack.push(_excludedURI);
    _excludedURI = null;
  }

  /**
   * Exclusion of namespaces by a stylesheet does not extend to any stylesheet
   * imported or included by the stylesheet. Upon exiting the context of a
   * stylesheet, a call to this method is needed to restore the set of excluded
   * namespaces that was in effect prior to entering the context of the current
   * stylesheet.
   */
  public void popExcludedNamespacesContext() {
    _excludedURI = _excludedURIStack.pop();
    if (_excludedURIStack.isEmpty()) {
      _excludedURIStack = null;
    }
  }

}
