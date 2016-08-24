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

import static de.lyca.xalan.xsltc.compiler.Constants.XMLNS_PREFIX;
import static de.lyca.xalan.xsltc.compiler.Constants.XSLT_URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.Stylesheet.OutputMethod;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.serializer.ElemDesc;
import de.lyca.xml.serializer.ToHTMLStream;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class LiteralElement extends Instruction {

  private String _name;
  private LiteralElement _literalElemParent = null;
  private List<SyntaxTreeNode> _attributeElements = null;
  private Map<String, String> _accessedPrefixes = null;

  // True if all attributes of this LRE are unique, i.e. they all have
  // different names. This flag is set to false if some attribute
  // names are not known at compile time.
  private boolean _allAttributesUnique = false;

  private final static String XMLNS_STRING = "xmlns";

  /**
   * Returns the QName for this literal element
   */
  public QName getName() {
    return _qname;
  }

  /**
   * Displays the contents of this literal element
   */
  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("LiteralElement name = " + _name);
    displayContents(indent + IndentIncrement);
  }

  /**
   * Returns the namespace URI for which a prefix is pointing to
   */
  private String accessedNamespace(String prefix) {
    if (_literalElemParent != null) {
      final String result = _literalElemParent.accessedNamespace(prefix);
      if (result != null)
        return result;
    }
    return _accessedPrefixes != null ? (String) _accessedPrefixes.get(prefix) : null;
  }

  /**
   * Method used to keep track of what namespaces that are references by this
   * literal element and its attributes. The output must contain a definition
   * for each namespace, so we stuff them in a Map.
   */
  public void registerNamespace(String prefix, String uri, SymbolTable stable, boolean declared) {

    // Check if the parent has a declaration for this namespace
    if (_literalElemParent != null) {
      final String parentUri = _literalElemParent.accessedNamespace(prefix);
      if (parentUri != null && parentUri.equals(uri))
        return;
    }

    // Check if we have any declared namesaces
    if (_accessedPrefixes == null) {
      _accessedPrefixes = new HashMap<>();
    } else {
      if (!declared) {
        // Check if this node has a declaration for this namespace
        final String old = _accessedPrefixes.get(prefix);
        if (old != null) {
          if (old.equals(uri))
            return;
          else {
            prefix = stable.generateNamespacePrefix();
          }
        }
      }
    }

    if (!prefix.equals("xml")) {
      _accessedPrefixes.put(prefix, uri);
    }
  }

  /**
   * Translates the prefix of a QName according to the rules set in the
   * attributes of xsl:stylesheet. Also registers a QName to assure that the
   * output element contains the necessary namespace declarations.
   */
  private String translateQName(QName qname, SymbolTable stable) {
    // Break up the QName and get prefix:localname strings
    final String localname = qname.getLocalPart();
    String prefix = qname.getPrefix();

    // Treat default namespace as "" and not null
    if (prefix == null) {
      prefix = "";
    } else if (prefix.equals(XMLNS_STRING))
      return XMLNS_STRING;

    // Check if we must translate the prefix
    final String alternative = stable.lookupPrefixAlias(prefix);
    if (alternative != null) {
      stable.excludeNamespaces(prefix);
      prefix = alternative;
    }

    // Get the namespace this prefix refers to
    final String uri = lookupNamespace(prefix);
    if (uri == null)
      return localname;

    // Register the namespace as accessed
    registerNamespace(prefix, uri, stable, false);

    // Construct the new name for the element (may be unchanged)
    if (prefix != "")
      return prefix + ":" + localname;
    else
      return localname;
  }

  /**
   * Add an attribute to this element
   */
  public void addAttribute(SyntaxTreeNode attribute) {
    if (_attributeElements == null) {
      _attributeElements = new ArrayList<>(2);
    }
    _attributeElements.add(attribute);
  }

  /**
   * Set the first attribute of this element
   */
  public void setFirstAttribute(SyntaxTreeNode attribute) {
    if (_attributeElements == null) {
      _attributeElements = new ArrayList<>(2);
    }
    _attributeElements.add(0, attribute);
  }

  /**
   * Type-check the contents of this element. The element itself does not need
   * any type checking as it leaves nothign on the JVM's stack.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // Type-check all attributes
    if (_attributeElements != null) {
      for (final SyntaxTreeNode node : _attributeElements) {
        node.typeCheck(stable);
      }
    }
    typeCheckContents(stable);
    return Type.Void;
  }

  /**
   * This method starts at a given node, traverses all namespace mappings, and
   * assembles a list of all prefixes that (for the given node) maps to _ANY_
   * namespace URI. Used by literal result elements to determine
   */
  public Set<String> getNamespaceScope(SyntaxTreeNode node) {
    final Set<String> all = new HashSet<>();
    while (node != null) {
      final Map<String, String> mapping = node.getPrefixMapping();
      if (mapping != null) {
        all.addAll(mapping.keySet());
      }
      node = node.getParent();
    }
    return all;
  }

  /**
   * Determines the final QName for the element and its attributes. Registers
   * all namespaces that are used by the element/attributes
   */
  @Override
  public void parseContents(Parser parser) {
    final SymbolTable stable = parser.getSymbolTable();
    stable.setCurrentNode(this);

    // Check if in a literal element context
    final SyntaxTreeNode parent = getParent();
    if (parent != null && parent instanceof LiteralElement) {
      _literalElemParent = (LiteralElement) parent;
    }

    _name = translateQName(_qname, stable);

    // Process all attributes and register all namespaces they use
    final int count = _attributes.getLength();
    for (int i = 0; i < count; i++) {
      final QName qname = parser.getQName(_attributes.getQName(i));
      final String uri = qname.getNamespace();
      final String val = _attributes.getValue(i);

      // Handle xsl:use-attribute-sets. Attribute sets are placed first
      // in the list or attributes to make sure that later local
      // attributes can override an attributes in the set.
      if (qname.equals(parser.getUseAttributeSets())) {
        if (!Util.isValidQNames(val)) {
          final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, val, this);
          parser.reportError(Constants.ERROR, err);
        }
        setFirstAttribute(new UseAttributeSets(val, parser));
      }
      // Handle xsl:extension-element-prefixes
      else if (qname.equals(parser.getExtensionElementPrefixes())) {
        stable.excludeNamespaces(val);
      }
      // Handle xsl:exclude-result-prefixes
      else if (qname.equals(parser.getExcludeResultPrefixes())) {
        stable.excludeNamespaces(val);
      } else {
        // Ignore special attributes (e.g. xmlns:prefix and xmlns)
        final String prefix = qname.getPrefix();
        if (prefix != null && prefix.equals(XMLNS_PREFIX) || prefix == null && qname.getLocalPart().equals("xmlns")
                || uri != null && uri.equals(XSLT_URI)) {
          continue;
        }

        // Handle all other literal attributes
        final String name = translateQName(qname, stable);
        final LiteralAttribute attr = new LiteralAttribute(name, val, parser, this);
        addAttribute(attr);
        attr.setParent(this);
        attr.parseContents(parser);
      }
    }

    // Register all namespaces that are in scope, except for those that
    // are listed in the xsl:stylesheet element's *-prefixes attributes
    final Set<String> include = getNamespaceScope(this);
    for (final String prefix : include) {
      if (!prefix.equals("xml")) {
        final String uri = lookupNamespace(prefix);
        if (uri != null && !stable.isExcludedNamespace(uri)) {
          registerNamespace(prefix, uri, stable, true);
        }
      }
    }

    parseChildren(parser);

    // Process all attributes and register all namespaces they use
    for (int i = 0; i < count; i++) {
      final QName qname = parser.getQName(_attributes.getQName(i));
      final String val = _attributes.getValue(i);

      // Handle xsl:extension-element-prefixes
      if (qname.equals(parser.getExtensionElementPrefixes())) {
        stable.unExcludeNamespaces(val);
      }
      // Handle xsl:exclude-result-prefixes
      else if (qname.equals(parser.getExcludeResultPrefixes())) {
        stable.unExcludeNamespaces(val);
      }
    }
  }

  @Override
  protected boolean contextDependent() {
    return dependentContents();
  }

  /**
   * Compiles code that emits the literal element to the output handler, first
   * the start tag, then namespace declaration, then attributes, then the
   * element contents, and then the element end tag. Since the value of an
   * attribute may depend on a variable, variables must be compiled first.
   */
  @Override
  public void translate(CompilerContext ctx) {
    // Check whether all attributes are unique.
    _allAttributesUnique = checkAttributesUnique();

    // Compile code to emit element start tag
    JExpression handler = ctx.currentHandler();// param(TRANSLET_OUTPUT_PNAME);
    ctx.currentBlock().add(handler.invoke("startElement").arg(_name));

    // The value of an attribute may depend on a (sibling) variable
    int j = 0;
    while (j < elementCount()) {
      final SyntaxTreeNode item = elementAt(j);
      if (item instanceof Variable) {
        item.translate(ctx);
      }
      j++;
    }

    // Compile code to emit namespace attributes
    if (_accessedPrefixes != null) {
      boolean declaresDefaultNS = false;
      for (final Map.Entry<String, String> entry : _accessedPrefixes.entrySet()) {
        final String prefix = entry.getKey();
        final String uri = entry.getValue();
        if (uri != "" || prefix != "") {
          if (prefix == "") {
            declaresDefaultNS = true;
          }
          ctx.currentBlock().add(handler.invoke("namespaceAfterStartElement").arg(prefix).arg(uri));
        }
      }

      /*
       * If our XslElement parent redeclares the default NS, and this element
       * doesn't, it must be redeclared one more time.
       */
      if (!declaresDefaultNS && _parent instanceof XslElement && ((XslElement) _parent).declaresDefaultNS()) {
        ctx.currentBlock().add(
            handler.invoke("namespaceAfterStartElement").arg("").arg(""));
      }
    }

    // Output all attributes
    if (_attributeElements != null) {
      for (final SyntaxTreeNode node : _attributeElements) {
        if (!(node instanceof XslAttribute)) {
          node.translate(ctx);
        }
      }
    }

    // Compile code to emit attributes and child elements
    translateContents(ctx);

    // Compile code to emit element end tag
    ctx.currentBlock().add(handler.invoke("endElement").arg(_name));
  }

  /**
   * Return true if the output method is html.
   */
  private boolean isHTMLOutput() {
    return getStylesheet().getOutputMethod() == OutputMethod.HTML;
  }

  /**
   * Return the ElemDesc object for an HTML element. Return null if the output
   * method is not HTML or this is not a valid HTML element.
   */
  public ElemDesc getElemDesc() {
    if (isHTMLOutput())
      return ToHTMLStream.getElemDesc(_name);
    else
      return null;
  }

  /**
   * Return true if all attributes of this LRE have unique names.
   */
  public boolean allAttributesUnique() {
    return _allAttributesUnique;
  }

  /**
   * Check whether all attributes are unique.
   */
  private boolean checkAttributesUnique() {
    final boolean hasHiddenXslAttribute = canProduceAttributeNodes(this, true);
    if (hasHiddenXslAttribute)
      return false;

    if (_attributeElements != null) {
      final int numAttrs = _attributeElements.size();
      Map<String, Instruction> attrsTable = null;
      for (int i = 0; i < numAttrs; i++) {
        final SyntaxTreeNode node = _attributeElements.get(i);

        if (node instanceof UseAttributeSets)
          return false;
        else if (node instanceof XslAttribute) {
          if (attrsTable == null) {
            attrsTable = new HashMap<>();
            for (int k = 0; k < i; k++) {
              final SyntaxTreeNode n = _attributeElements.get(k);
              if (n instanceof LiteralAttribute) {
                final LiteralAttribute literalAttr = (LiteralAttribute) n;
                attrsTable.put(literalAttr.getName(), literalAttr);
              }
            }
          }

          final XslAttribute xslAttr = (XslAttribute) node;
          final AttributeValue attrName = xslAttr.getName();
          if (attrName instanceof AttributeValueTemplate)
            return false;
          else if (attrName instanceof SimpleAttributeValue) {
            final SimpleAttributeValue simpleAttr = (SimpleAttributeValue) attrName;
            final String name = simpleAttr.toString();
            if (name != null && attrsTable.get(name) != null)
              return false;
            else if (name != null) {
              attrsTable.put(name, xslAttr);
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * Return true if the instructions under the given SyntaxTreeNode can produce
   * attribute nodes to an element. Only return false when we are sure that no
   * attribute node is produced. Return true if we are not sure. If the flag
   * ignoreXslAttribute is true, the direct <xsl:attribute> children of the
   * current node are not included in the check.
   */
  private boolean canProduceAttributeNodes(SyntaxTreeNode node, boolean ignoreXslAttribute) {
    final List<SyntaxTreeNode> contents = node.getContents();
    for (final SyntaxTreeNode child : contents) {
      if (child instanceof Text) {
        final Text text = (Text) child;
        if (text.isIgnore()) {
          continue;
        } else
          return false;
      }
      // Cannot add an attribute to an element after children have been added to
      // it.
      // We can safely return false when the instruction can produce an output
      // node.
      else if (child instanceof LiteralElement || child instanceof ValueOf || child instanceof XslElement
              || child instanceof Comment || child instanceof Number || child instanceof ProcessingInstruction)
        return false;
      else if (child instanceof XslAttribute) {
        if (ignoreXslAttribute) {
          continue;
        } else
          return true;
      }
      // In general, there is no way to check whether <xsl:call-template> or
      // <xsl:apply-templates> can produce attribute nodes. <xsl:copy> and
      // <xsl:copy-of> can also copy attribute nodes to an element. Return
      // true in those cases to be safe.
      else if (child instanceof CallTemplate || child instanceof ApplyTemplates || child instanceof Copy
              || child instanceof CopyOf)
        return true;
      else if ((child instanceof If || child instanceof ForEach) && canProduceAttributeNodes(child, false))
        return true;
      else if (child instanceof Choose) {
        final List<SyntaxTreeNode> chooseContents = child.getContents();
        for (final SyntaxTreeNode chooseChild : chooseContents) {
          if (chooseChild instanceof When || chooseChild instanceof Otherwise) {
            if (canProduceAttributeNodes(chooseChild, false))
              return true;
          }
        }
      }
    }
    return false;
  }

}
