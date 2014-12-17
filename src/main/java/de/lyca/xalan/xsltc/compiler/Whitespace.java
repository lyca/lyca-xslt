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

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Morten Jorgensen
 */
final class Whitespace extends TopLevelElement {
  // Three possible actions for the translet:
  public static final int USE_PREDICATE = 0;
  public static final int STRIP_SPACE = 1;
  public static final int PRESERVE_SPACE = 2;

  // The 3 different categories of strip/preserve rules (order important)
  public static final int RULE_NONE = 0;
  public static final int RULE_ELEMENT = 1; // priority 0
  public static final int RULE_NAMESPACE = 2; // priority -1/4
  public static final int RULE_ALL = 3; // priority -1/2

  private String _elementList;
  private int _action;
  private int _importPrecedence;

  /**
   * Auxillary class for encapsulating a single strip/preserve rule
   */
  protected final static class WhitespaceRule implements Comparable<WhitespaceRule> {
    private final int _action;
    private String _namespace; // Should be replaced by NS type (int)
    private String _element; // Should be replaced by node type (int)
    private int _type;
    private int _priority;

    /**
     * Strip/preserve rule constructor
     */
    public WhitespaceRule(int action, String element, int precedence) {
      // Determine the action (strip or preserve) for this rule
      _action = action;

      // Get the namespace and element name for this rule
      final int colon = element.lastIndexOf(':');
      if (colon >= 0) {
        _namespace = element.substring(0, colon);
        _element = element.substring(colon + 1, element.length());
      } else {
        _namespace = Constants.EMPTYSTRING;
        _element = element;
      }

      // Determine the initial priority for this rule
      _priority = precedence << 2;

      // Get the strip/preserve type; either "NS:EL", "NS:*" or "*"
      if (_element.equals("*")) {
        if (_namespace == Constants.EMPTYSTRING) {
          _type = RULE_ALL; // Strip/preserve _all_ elements
          _priority += 2; // Lowest priority
        } else {
          _type = RULE_NAMESPACE; // Strip/reserve elements within NS
          _priority += 1; // Medium priority
        }
      } else {
        _type = RULE_ELEMENT; // Strip/preserve single element
      }
    }

    /**
     * For sorting rules depending on priority
     */
    @Override
    public int compareTo(WhitespaceRule other) {
      return _priority < other._priority ? -1 : _priority > other._priority ? 1 : 0;
    }

    public int getAction() {
      return _action;
    }

    public int getStrength() {
      return _type;
    }

    public int getPriority() {
      return _priority;
    }

    public String getElement() {
      return _element;
    }

    public String getNamespace() {
      return _namespace;
    }
  }

  /**
   * Parse the attributes of the xsl:strip/preserve-space element. The element
   * should have not contents (ignored if any).
   */
  @Override
  public void parseContents(Parser parser) {
    // Determine if this is an xsl:strip- or preserve-space element
    _action = _qname.getLocalPart().endsWith("strip-space") ? STRIP_SPACE : PRESERVE_SPACE;

    // Determine the import precedence
    _importPrecedence = parser.getCurrentImportPrecedence();

    // Get the list of elements to strip/preserve
    _elementList = getAttribute("elements");
    if (_elementList == null || _elementList.length() == 0) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "elements");
      return;
    }

    final StringTokenizer list = new StringTokenizer(_elementList);
    final StringBuilder elements = new StringBuilder(Constants.EMPTYSTRING);

    while (list.hasMoreElements()) {
      final String token = list.nextToken();
      String namespace;
      final int col = token.indexOf(':');

      if (col != -1) {
        namespace = lookupNamespace(token.substring(0, col));
        if (namespace != null) {
          elements.append(namespace + ":" + token.substring(col + 1, token.length()));
        } else {
          elements.append(token);
        }
      } else {
        elements.append(token);
      }

      if (list.hasMoreElements()) {
        elements.append(" ");
      }
    }
    _elementList = elements.toString();
  }

  /**
   * De-tokenize the elements listed in the 'elements' attribute and instanciate
   * a set of strip/preserve rules.
   */
  public List<WhitespaceRule> getRules() {
    final List<WhitespaceRule> rules = new ArrayList<>();
    // Go through each element and instanciate strip/preserve-object
    final StringTokenizer list = new StringTokenizer(_elementList);
    while (list.hasMoreElements()) {
      rules.add(new WhitespaceRule(_action, list.nextToken(), _importPrecedence));
    }
    return rules;
  }

  /**
   * Scans through the rules list and looks for a rule of higher priority that
   * contradicts the current rule.
   */
  private static WhitespaceRule findContradictingRule(List<WhitespaceRule> rules, WhitespaceRule rule) {
    // Get the next rule in the prioritized list
    for (WhitespaceRule currentRule : rules) {
      // We only consider rules with higher priority
      if (currentRule == rule)
        return null;

      /*
       * See if there is a contradicting rule with higher priority. If the rules
       * has the same action then this rule is redundant, if they have different
       * action then this rule will never win.
       */
      switch (currentRule.getStrength()) {
      case RULE_ALL:
        return currentRule;

      case RULE_ELEMENT:
        if (!rule.getElement().equals(currentRule.getElement())) {
          break;
        }
        // intentional fall-through
      case RULE_NAMESPACE:
        if (rule.getNamespace().equals(currentRule.getNamespace()))
          return currentRule;
        break;
      }
    }
    return null;
  }

  /**
   * Orders a set or rules by priority, removes redundant rules and rules that
   * are shadowed by stronger, contradicting rules.
   */
  private static int prioritizeRules(List<WhitespaceRule> rules) {
    WhitespaceRule currentRule;
    int defaultAction = PRESERVE_SPACE;

    // Sort all rules with regard to priority
    quicksort(rules, 0, rules.size() - 1);

    // Check if there are any "xsl:strip-space" elements at all.
    // If there are no xsl:strip elements we can ignore all xsl:preserve
    // elements and signal that all whitespaces should be preserved
    boolean strip = false;
    for (int i = 0; i < rules.size(); i++) {
      currentRule = rules.get(i);
      if (currentRule.getAction() == STRIP_SPACE) {
        strip = true;
      }
    }
    // Return with default action: PRESERVE_SPACE
    if (!strip) {
      rules.clear();
      return PRESERVE_SPACE;
    }

    // Remove all rules that are contradicted by rules with higher priority
    for (int idx = 0; idx < rules.size();) {
      currentRule = rules.get(idx);

      // Remove this single rule if it has no purpose
      if (findContradictingRule(rules, currentRule) != null) {
        rules.remove(idx);
      } else {
        // Remove all following rules if this one overrides all
        if (currentRule.getStrength() == RULE_ALL) {
          defaultAction = currentRule.getAction();
          for (int i = idx; i < rules.size(); i++) {
            rules.remove(i);
          }
        }
        // Skip to next rule (there might not be any)...
        idx++;
      }
    }

    // The rules list could be empty if first rule has strength RULE_ALL
    if (rules.size() == 0)
      return defaultAction;

    // Now work backwards and strip away all rules that have the same
    // action as the default rule (no reason the check them at the end).
    do {
      currentRule = rules.get(rules.size() - 1);
      if (currentRule.getAction() == defaultAction) {
        rules.remove(rules.size() - 1);
      } else {
        break;
      }
    } while (rules.size() > 0);

    // Signal that whitespace detection predicate must be used.
    return defaultAction;
  }

  public static JExpression compileStripSpace(JExpression[] strip, int sCount) {
    // final InstructionHandle target = il.append(ICONST_1);
    // il.append(IRETURN);
    JExpression result = strip[0];
    for (int i = 1; i < sCount; i++) {
      result = result.cor(strip[i]);
    }
    return result;
  }

  public static JExpression compilePreserveSpace(JExpression preserve[], int pCount) {
    // final InstructionHandle target = il.append(ICONST_0);
    // il.append(IRETURN);
    JExpression result = preserve[0];
    for (int i = 1; i < pCount; i++) {
      result = result.cor(preserve[i]);
    }
    return result;
  }

  /*
   * private static void compileDebug(ClassGenerator classGen, InstructionList
   * il) { final ConstantPoolGen cpg = classGen.getConstantPool(); final int prt
   * = cpg.addMethodref("java/lang/System/out", "println",
   * "(Ljava/lang/String;)V"); il.append(DUP); il.append(new INVOKESTATIC(prt));
   * }
   */

  /**
   * Compiles the predicate method
   */
  private static void compilePredicate(List<WhitespaceRule> rules, int defaultAction, JDefinedClass definedClass,
      XSLTC xsltc) {
    JMethod stripSpace = definedClass.method(JMod.PUBLIC | JMod.FINAL, boolean.class, "stripSpace");
    JVar dom = stripSpace.param(DOM.class, "dom");
    JVar node = stripSpace.param(int.class, "node");
    JVar type = stripSpace.param(int.class, "type");
    definedClass._implements(StripFilter.class);

    final JExpression[] strip = new JExpression[rules.size()];
    final JExpression[] preserve = new JExpression[rules.size()];
    int sCount = 0;
    int pCount = 0;

    // Traverse all strip/preserve rules
    for (int i = 0; i < rules.size(); i++) {
      // Get the next rule in the prioritised list
      final WhitespaceRule rule = rules.get(i);

      // Handle elements="ns:*" type rule
      if (rule.getStrength() == RULE_NAMESPACE) {
        // Returns the namespace for a node in the DOM
        JExpression namespaceRule = invoke(dom, "getNamespaceName").arg(node).invoke("compareTo")
            .arg(rule.getNamespace()).eq(lit(0));

        if (rule.getAction() == STRIP_SPACE) {
          strip[sCount++] = namespaceRule;
        } else {
          preserve[pCount++] = namespaceRule;
        }
      }
      // Handle elements="ns:el" type rule
      else if (rule.getStrength() == RULE_ELEMENT) {
        // Create the QName for the element
        final Parser parser = xsltc.getParser();
        QName qname;
        if (rule.getNamespace() != Constants.EMPTYSTRING) {
          qname = parser.getQName(rule.getNamespace(), null, rule.getElement());
        } else {
          qname = parser.getQName(rule.getElement());
        }

        // Register the element.
        final int elementType = xsltc.registerElement(qname);
        JExpression elementRule = type.eq(lit(elementType));

        // Compare current node type with wanted element type
        if (rule.getAction() == STRIP_SPACE) {
          strip[sCount++] = elementRule;
        } else {
          preserve[pCount++] = elementRule;
        }
      }
    }
    JBlock body = stripSpace.body();
    if (defaultAction == STRIP_SPACE) {
      JConditional conditional = body._if(compilePreserveSpace(preserve, pCount));
      conditional._then()._return(FALSE);
      conditional._else()._return(TRUE);
    } else {
      JConditional conditional = body._if(compileStripSpace(strip, sCount));
      conditional._then()._return(TRUE);
      conditional._else()._return(FALSE);
    }
  }

  /**
   * Compiles the predicate method
   */
  private static void compileDefault(int defaultAction, JDefinedClass definedClass) {
    JMethod stripSpace = definedClass.method(JMod.PUBLIC | JMod.FINAL, boolean.class, "stripSpace");
    stripSpace.param(DOM.class, "dom");
    stripSpace.param(int.class, "node");
    stripSpace.param(int.class, "type");
    stripSpace.body()._return(lit(defaultAction == STRIP_SPACE));
    definedClass._implements(StripFilter.class);
  }

  /**
   * Takes a list of WhitespaceRule objects and generates a predicate method.
   * This method returns the translets default action for handling whitespace
   * text-nodes: - USE_PREDICATE (run the method generated by this method) -
   * STRIP_SPACE (always strip whitespace text-nodes) - PRESERVE_SPACE (always
   * preserve whitespace text-nodes)
   */
  public static int translateRules(List<WhitespaceRule> rules, JDefinedClass definedClass, XSLTC xsltc) {
    // Get the core rules in prioritized order
    final int defaultAction = prioritizeRules(rules);
    // The rules list may be empty after prioritising
    if (rules.size() == 0) {
      compileDefault(defaultAction, definedClass);
      return defaultAction;
    }
    // Now - create a predicate method and sequence through rules...
    compilePredicate(rules, defaultAction, definedClass, xsltc);
    // Return with the translets required action (
    return USE_PREDICATE;
  }

  /**
   * Sorts a range of rules with regard to PRIORITY only
   */
  private static void quicksort(List<WhitespaceRule> rules, int p, int r) {
    while (p < r) {
      final int q = partition(rules, p, r);
      quicksort(rules, p, q);
      p = q + 1;
    }
  }

  /**
   * Used with quicksort method above
   */
  private static int partition(List<WhitespaceRule> rules, int p, int r) {
    final WhitespaceRule x = rules.get(p + r >>> 1);
    int i = p - 1, j = r + 1;
    while (true) {
      while (x.compareTo(rules.get(--j)) < 0) {
      }
      while (x.compareTo(rules.get(++i)) > 0) {
      }
      if (i < j) {
        final WhitespaceRule tmp = rules.get(i);
        rules.set(i, rules.get(j));
        rules.set(j, tmp);
      } else
        return j;
    }
  }

  /**
   * Type-check contents/attributes - nothing to do...
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    return Type.Void; // We don't return anything.
  }

  /**
   * This method should not produce any code
   */
  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
  }
}
