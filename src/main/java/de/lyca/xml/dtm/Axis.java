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
package de.lyca.xml.dtm;

/**
 * Specifies values related to XPath Axes.
 * <p>
 * The ancestor, descendant, following, preceding and self axes partition a
 * document (ignoring attribute and namespace nodes): they do not overlap and
 * together they contain all the nodes in the document.
 * </p>
 * 
 */
public enum Axis {

  /**
   * The ancestor axis contains the ancestors of the context node; the ancestors
   * of the context node consist of the parent of context node and the parent's
   * parent and so on; thus, the ancestor axis will always include the root
   * node, unless the context node is the root node.
   */
  ANCESTOR("ancestor", true),

  /**
   * the ancestor-or-self axis contains the context node and the ancestors of
   * the context node; thus, the ancestor axis will always include the root
   * node.
   */
  ANCESTORORSELF("ancestor-or-self", true),

  /**
   * the attribute axis contains the attributes of the context node; the axis
   * will be empty unless the context node is an element.
   */
  ATTRIBUTE("attribute"),

  /** The child axis contains the children of the context node. */
  CHILD("child"),

  /**
   * The descendant axis contains the descendants of the context node; a
   * descendant is a child or a child of a child and so on; thus the descendant
   * axis never contains attribute or namespace nodes.
   */
  DESCENDANT("descendant"),

  /**
   * The descendant-or-self axis contains the context node and the descendants
   * of the context node.
   */
  DESCENDANTORSELF("descendant-or-self"),

  /**
   * the following axis contains all nodes in the same document as the context
   * node that are after the context node in document order, excluding any
   * descendants and excluding attribute nodes and namespace nodes.
   */
  FOLLOWING("following"),

  /**
   * The following-sibling axis contains all the following siblings of the
   * context node; if the context node is an attribute node or namespace node,
   * the following-sibling axis is empty.
   */
  FOLLOWINGSIBLING("following-sibling"),

  /**
   * The namespace axis contains the namespace nodes of the context node; the
   * axis will be empty unless the context node is an element.
   */
  NAMESPACEDECLS("namespace-decls"),

  /**
   * The namespace axis contains the namespace nodes of the context node; the
   * axis will be empty unless the context node is an element.
   */
  NAMESPACE("namespace"),

  /**
   * The parent axis contains the parent of the context node, if there is one.
   */
  PARENT("parent"),

  /**
   * The preceding axis contains all nodes in the same document as the context
   * node that are before the context node in document order, excluding any
   * ancestors and excluding attribute nodes and namespace nodes
   */
  PRECEDING("preceding", true),

  /**
   * The preceding-sibling axis contains all the preceding siblings of the
   * context node; if the context node is an attribute node or namespace node,
   * the preceding-sibling axis is empty.
   */
  PRECEDINGSIBLING("preceding-sibling", true),

  /** The self axis contains just the context node itself. */
  SELF("self"),

  /**
   * A non-xpath axis, traversing the subtree including the subtree root,
   * descendants, attributes, and namespace node decls.
   */
  ALLFROMNODE("all-from-node"),

  /**
   * A non-xpath axis, traversing the the preceding and the ancestor nodes,
   * needed for inverseing select patterns to match patterns.
   */
  PRECEDINGANDANCESTOR("preceding-and-ancestor"),

  // ===========================================
  // All axis past this are absolute.

  /**
   * A non-xpath axis, returns all nodes in the tree from and including the
   * root.
   */
  ALL("all", false, true),

  /**
   * A non-xpath axis, returns all nodes that aren't namespaces or attributes,
   * from and including the root.
   */
  DESCENDANTSFROMROOT("descendants-from-root", false, true),

  /**
   * A non-xpath axis, returns all nodes that aren't namespaces or attributes,
   * from and including the root.
   */
  DESCENDANTSORSELFFROMROOT("descendants-or-self-from-root", false, true),

  /**
   * A non-xpath axis, returns root only.
   */
  ROOT("root", false, true),

  /**
   * A non-xpath axis, for functions.
   */
  FILTEREDLIST("filtered-list", false, true);

  private final String name;
  private final boolean isReverse;
  private final boolean isAbsolute;

  private Axis(String name) {
    this(name, false, false);
  }

  private Axis(String name, boolean isReverse) {
    this(name, isReverse, false);
  }

  private Axis(String name, boolean isReverse, boolean isAbsolute) {
    this.name = name;
    this.isReverse = isReverse;
    this.isAbsolute = isAbsolute;
  }

  public String getName() {
    return name;
  }

  public boolean isReverse() {
    return isReverse;
  }

  public boolean isAbsolute() {
    return isAbsolute;
  }

}
