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
package de.lyca.xml.serializer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class keeps track of the currently defined namespaces. Conceptually the
 * prefix/uri/depth triplets are pushed on a stack pushed on a stack. The depth
 * indicates the nesting depth of the element for which the mapping was made.
 * 
 * <p>
 * For example:
 * 
 * <pre>
 * &lt;chapter xmlns:p1="def"&gt;
 *   &lt;paragraph xmlns:p2="ghi"&gt;
 *      &lt;sentence xmlns:p3="jkl"&gt;
 *      &lt;/sentence&gt;
 *    &lt;/paragraph&gt;
 *    &lt;paragraph xlmns:p4="mno"&gt;
 *    &lt;/paragraph&gt;
 * &lt;/chapter&gt;
 * </pre>
 * 
 * When the {@literal <chapter>} element is encountered the prefix "p1" associated with uri
 * "def" is pushed on the stack with depth 1. When the first {@literal <paragraph>} is
 * encountered "p2" and "ghi" are pushed with depth 2. When the {@literal <sentence>} is
 * encountered "p3" and "jkl" are pushed with depth 3. When {@literal </sentence>} occurs
 * the popNamespaces(3) will pop "p3"/"jkl" off the stack. Of course
 * popNamespaces(2) would pop anything with depth 2 or greater.
 * 
 * So prefix/uri pairs are pushed and popped off the stack as elements are
 * processed. At any given moment of processing the currently visible prefixes
 * are on the stack and a prefix can be found given a uri, or a uri can be found
 * given a prefix.
 * 
 * This class is intended for internal use only. However, it is made public
 * because other packages require it.
 */
public class NamespaceMappings {
  /**
   * This member is continually incremented when new prefixes need to be
   * generated. ("ns0" "ns1" ...)
   */
  private int count = 0;

  /**
   * Each entry (prefix) in this hashtable points to a Stack of URIs This table
   * maps a prefix (String) to a Stack of NamespaceNodes. All Namespace nodes in
   * that retrieved stack have the same prefix, though possibly different URI's
   * or depths. Such a stack must have mappings at deeper depths push later on
   * such a stack. Mappings pushed earlier on the stack will have smaller values
   * for MappingRecord.m_declarationDepth.
   */
  private final Map<String, Deque<MappingRecord>> m_namespaces = new HashMap<>();

  /**
   * This stack is used as a convenience. It contains the pushed NamespaceNodes
   * (shallowest to deepest) and is used to delete NamespaceNodes when leaving
   * the current element depth to returning to the parent. The mappings of the
   * deepest depth can be popped of the top and the same node can be removed
   * from the appropriate prefix stack.
   * 
   * All prefixes pushed at the current depth can be removed at the same time by
   * using this stack to ensure prefix/uri map scopes are closed correctly.
   */
  private final Deque<MappingRecord> m_nodeStack = new ArrayDeque<>();

  private static final String XML_PREFIX = "xml"; // was "xmlns"

  /**
   * Default constructor
   * 
   * @see java.lang.Object#Object()
   */
  public NamespaceMappings() {
    initNamespaces();
  }

  /**
   * This method initializes the namespace object with appropriate stacks and
   * predefines a few prefix/uri pairs which always exist.
   */
  private void initNamespaces() {
    // The initial prefix mappings will never be deleted because they are at
    // element depth -1
    // (a kludge)

    // Define the default namespace (initially maps to "" uri)
    final MappingRecord emptyRecord = new MappingRecord("", "", -1);
    final Deque<MappingRecord> emptyRecordStack = new ArrayDeque<>();
    emptyRecordStack.push(emptyRecord);
    m_namespaces.put("", emptyRecordStack);

    // define "xml" namespace
    final MappingRecord xmlRecord = new MappingRecord(XML_PREFIX, "http://www.w3.org/XML/1998/namespace", -1);
    final Deque<MappingRecord> xmlRecordStack = new ArrayDeque<>();
    xmlRecordStack.push(xmlRecord);
    m_namespaces.put(XML_PREFIX, xmlRecordStack);
  }

  /**
   * Use a namespace prefix to lookup a namespace URI.
   * 
   * @param prefix
   *          String the prefix of the namespace
   * @return the URI corresponding to the prefix, returns "" if there is no
   *         visible mapping.
   */
  public String lookupNamespace(String prefix) {
    String uri = null;
    final Deque<MappingRecord> stack = m_namespaces.get(prefix);
    if (stack == null || stack.isEmpty()) {
      uri = "";
    } else {
      uri = stack.peek().m_uri;
    }
    return uri;
  }

  MappingRecord getMappingFromPrefix(String prefix) {
    final Deque<MappingRecord> stack = m_namespaces.get(prefix);
    return stack == null || stack.isEmpty() ? null : stack.peek();
  }

  /**
   * Given a namespace uri, and the namespaces mappings for the current element,
   * return the current prefix for that uri.
   * 
   * @param uri
   *          the namespace URI to be search for
   * @return an existing prefix that maps to the given URI, null if no prefix
   *         maps to the given namespace URI.
   */
  public String lookupPrefix(String uri) {
    String foundPrefix = null;
    for (final String prefix : m_namespaces.keySet()) {
      final String uri2 = lookupNamespace(prefix);
      if (uri2 != null && uri2.equals(uri)) {
        foundPrefix = prefix;
        break;
      }
    }
    return foundPrefix;
  }

  MappingRecord getMappingFromURI(String uri) {
    MappingRecord foundMap = null;
    for (final String prefix : m_namespaces.keySet()) {
      final MappingRecord map2 = getMappingFromPrefix(prefix);
      if (map2 != null && map2.m_uri.equals(uri)) {
        foundMap = map2;
        break;
      }
    }
    return foundMap;
  }

  /**
   * Undeclare the namespace that is currently pointed to by a given prefix
   */
  boolean popNamespace(String prefix) {
    // Prefixes "xml" and "xmlns" cannot be redefined
    if (prefix.startsWith(XML_PREFIX))
      return false;

    final Deque<MappingRecord> stack = m_namespaces.get(prefix);
    if (stack != null) {
      stack.pop();
      return true;
    }
    return false;
  }

  /**
   * Declare a mapping of a prefix to namespace URI at the given element depth.
   * 
   * @param prefix
   *          a String with the prefix for a qualified name
   * @param uri
   *          a String with the uri to which the prefix is to map
   * @param elemDepth
   *          the depth of current declaration
   */
  public boolean pushNamespace(String prefix, String uri, int elemDepth) {
    // Prefixes "xml" and "xmlns" cannot be redefined
    if (prefix.startsWith(XML_PREFIX))
      return false;

    Deque<MappingRecord> stack = m_namespaces.get(prefix);
    // Get the stack that contains URIs for the specified prefix
    if (stack == null) {
      m_namespaces.put(prefix, stack = new ArrayDeque<>());
    }

    if (!stack.isEmpty()) {
      final MappingRecord mr = stack.peek();
      if (uri.equals(mr.m_uri) || elemDepth == mr.m_declarationDepth)
        // If the same prefix/uri mapping is already on the stack
        // don't push this one.
        // Or if we have a mapping at the same depth
        // don't replace by pushing this one.
        return false;
    }
    final MappingRecord map = new MappingRecord(prefix, uri, elemDepth);
    stack.push(map);
    m_nodeStack.push(map);
    return true;
  }

  /**
   * Pop, or undeclare all namespace definitions that are currently declared at
   * the given element depth, or deepter.
   * 
   * @param elemDepth
   *          the element depth for which mappings declared at this depth or
   *          deeper will no longer be valid
   * @param saxHandler
   *          The ContentHandler to notify of any endPrefixMapping() calls. This
   *          parameter can be null.
   */
  void popNamespaces(int elemDepth, ContentHandler saxHandler) {
    while (true) {
      if (m_nodeStack.isEmpty())
        return;
      final MappingRecord map = m_nodeStack.peek();
      if (elemDepth < 1 || map.m_declarationDepth < elemDepth) {
        break;
        /*
         * the depth of the declared mapping is elemDepth or deeper so get rid
         * of it
         */
      }

      final MappingRecord nm1 = m_nodeStack.pop();
      // pop the node from the stack
      final String prefix = map.m_prefix;

      final Deque<MappingRecord> prefixStack = m_namespaces.get(prefix);
      final MappingRecord nm2 = prefixStack.peek();
      if (nm1 == nm2) {
        // It would be nice to always pop() but we
        // need to check that the prefix stack still has
        // the node we want to get rid of. This is because
        // the optimization of essentially this situation:
        // <a xmlns:x="abc"><b xmlns:x="" xmlns:x="abc" /></a>
        // will remove both mappings in <b> because the
        // new mapping is the same as the masked one and we get
        // <a xmlns:x="abc"><b/></a>
        // So we are only removing xmlns:x="" or
        // xmlns:x="abc" from the depth of element <b>
        // when going back to <a> if in fact they have
        // not been optimized away.
        //
        prefixStack.pop();
        if (saxHandler != null) {
          try {
            saxHandler.endPrefixMapping(prefix);
          } catch (final SAXException e) {
            // not much we can do if they aren't willing to listen
          }
        }
      }

    }
  }

  /**
   * Generate a new namespace prefix ( ns0, ns1 ...) not used before
   * 
   * @return String a new namespace prefix ( ns0, ns1, ns2 ...)
   */
  public String generateNextPrefix() {
    return "ns" + count++;
  }

  final void reset() {
    count = 0;
    m_namespaces.clear();
    m_nodeStack.clear();

    initNamespaces();
  }

  /**
   * Just a little class that ties the 3 fields together into one object, and
   * this simplifies the pushing and popping of namespaces to one push or one
   * pop on one stack rather than on 3 separate stacks.
   */
  static class MappingRecord {
    final String m_prefix; // the prefix
    final String m_uri; // the uri, possibly "" but never null
    // the depth of the element where declartion was made
    final int m_declarationDepth;

    MappingRecord(String prefix, String uri, int depth) {
      m_prefix = prefix;
      m_uri = uri == null ? "" : uri;
      m_declarationDepth = depth;
    }
  }

  /**
   * Given a namespace uri, get all prefixes bound to the Namespace URI in the
   * current scope.
   * 
   * @param uri
   *          the namespace URI to be search for
   * @return An array of Strings which are all prefixes bound to the namespace
   *         URI in the current scope. An array of zero elements is returned if
   *         no prefixes map to the given namespace URI.
   */
  public String[] lookupAllPrefixes(String uri) {
    final List<String> foundPrefixes = new ArrayList<>();
    for (final String prefix : m_namespaces.keySet()) {
      final String uri2 = lookupNamespace(prefix);
      if (uri2 != null && uri2.equals(uri)) {
        foundPrefixes.add(prefix);
      }
    }
    return foundPrefixes.toArray(new String[0]);
  }
}
