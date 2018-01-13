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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Simple static utility to convert Map to a Node.
 * 
 * @see de.lyca.xalan.xslt.EnvironmentCheck
 * @see de.lyca.xalan.lib.Extensions
 * @author shane_curcuru@us.ibm.com
 * @version $Id$
 */
public abstract class Map2Node {

  /**
   * Convert a Map into a Node tree.
   * 
   * <p>
   * The hash may have either Maps as values (in which case we recurse) or other
   * values, in which case we print them as &lt;item&gt; elements, with a 'key'
   * attribute with the value of the key, and the element contents as the value.
   * </p>
   * 
   * <p>
   * If args are null we simply return without doing anything. If we encounter
   * an error, we will attempt to add an 'ERROR' Element with exception info; if
   * that doesn't work we simply return without doing anything else byt
   * printStackTrace().
   * </p>
   * 
   * @param map
   *          to get info from (may have sub-maps)
   * @param name
   *          to use as parent element for appended node futurework could have
   *          namespace and prefix as well
   * @param container
   *          Node to append our report to
   * @param factory
   *          Document providing createElement, etc. services
   */
  public static void appendMapToNode(Map<String, Object> map, String name, Node container, Document factory) {
    // Required arguments must not be null
    if (null == container || null == factory || null == map)
      return;

    // name we will provide a default value for
    String elemName = null;
    if (null == name || name.isEmpty()) {
      elemName = "appendHashToNode";
    } else {
      elemName = name;
    }

    try {
      final Element hashNode = factory.createElement(elemName);
      container.appendChild(hashNode);

      final List<Object> v = new ArrayList<>();
      for (final String key : map.keySet()) {
        final Object item = map.get(key);

        if (item instanceof Map) {
          // Ensure a pre-order traversal; add this hashes
          // items before recursing to child hashes
          // Save name and hash in two steps
          v.add(key);
          v.add(item);
        } else {
          try {
            // Add item to node
            final Element node = factory.createElement("item");
            node.setAttribute("key", key);
            node.appendChild(factory.createTextNode((String) item));
            hashNode.appendChild(node);
          } catch (final Exception e) {
            final Element node = factory.createElement("item");
            node.setAttribute("key", key);
            node.appendChild(factory.createTextNode("ERROR: Reading " + key + " threw: " + e.toString()));
            hashNode.appendChild(node);
          }
        }
      }

      // Now go back and do the saved hashes
      final Iterator<Object> it = v.iterator();
      while (it.hasNext()) {
        // Retrieve name and hash in two steps
        final String n = (String) it.next();
        @SuppressWarnings("unchecked")
        final Map<String, Object> h = (Map<String, Object>) it.next();
        appendMapToNode(h, n, hashNode, factory);
      }
    } catch (final Exception e2) {
      // Ooops, just bail (suggestions for a safe thing
      // to do in this case appreciated)
      e2.printStackTrace();
    }
  }
}
