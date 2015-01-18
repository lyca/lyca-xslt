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

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext.MethodContext;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;

/**
 * Mode gathers all the templates belonging to a given mode; it is responsible
 * for generating an appropriate applyTemplates + (mode name) method in the
 * translet.
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 * @author G. Todd Miller
 */
final class Mode implements Constants {

  /**
   * The name of this mode as defined in the stylesheet.
   */
  private final QName _name;

  /**
   * A reference to the stylesheet object that owns this mode.
   */
  private final Stylesheet _stylesheet;

  /**
   * The name of the method in which this mode is compiled.
   */
  private final String _methodName;

  /**
   * A list of all the templates in this mode.
   */
  private List<Template> _templates;

  /**
   * Group for patterns with node()-type kernel and child axis.
   */
  private List<LocationPathPattern> _childNodeGroup = null;

  /**
   * Test sequence for patterns with node()-type kernel and child axis.
   */
  private TestSeq _childNodeTestSeq = null;

  /**
   * Group for patterns with node()-type kernel and attribute axis.
   */
  private List<LocationPathPattern> _attribNodeGroup = null;

  /**
   * Group for patterns with id() or key()-type kernel.
   */
  private List<LocationPathPattern> _idxGroup = null;

  /**
   * Test sequence for patterns with id() or key()-type kernel.
   */
  private TestSeq _idxTestSeq = null;

  /**
   * Group for patterns with any other kernel type.
   */
  private List<LocationPathPattern>[] _patternGroups;

  /**
   * Test sequence for patterns with any other kernel type.
   */
  private TestSeq[] _testSeq;

  /**
   * A mapping between templates and test sequences.
   */
  private Map<Template, TestSeq> _neededTemplates = new HashMap<>();

  /**
   * A mapping between named templates and Mode objects.
   */
  private Map<Template, Mode> _namedTemplates = new HashMap<>();

  /**
   * A mapping between templates and statements.
   */
  private Map<Template, JStatement> _templateStatement = new HashMap<>();

  /**
   * A reference to the pattern matching the root node.
   */
  private LocationPathPattern _rootPattern = null;

  /**
   * Stores ranges of template precendences for the compilation of apply-imports
   * (a Map for historical reasons).
   */
  private Map<Integer, Integer> _importLevels = null;

  /**
   * Variable index for the current node used in code generation.
   */
  private int _currentIndex;

  /**
   * Creates a new Mode.
   * 
   * @param name
   *          A textual representation of the mode's QName
   * @param stylesheet
   *          The Stylesheet in which the mode occured
   * @param suffix
   *          A suffix to append to the method name for this mode (normally a
   *          sequence number - still in a String).
   */
  public Mode(QName name, Stylesheet stylesheet, String suffix) {
    _name = name;
    _stylesheet = stylesheet;
    _methodName = APPLY_TEMPLATES + suffix;
    _templates = new ArrayList<>();
    _patternGroups = new ArrayList[32];
  }

  /**
   * Returns the name of the method (_not_ function) that will be compiled for
   * this mode. Normally takes the form 'applyTemplates()' or *
   * 'applyTemplates2()'.
   * 
   * @return Method name for this mode
   */
  public String functionName() {
    return _methodName;
  }

  public String functionName(int min, int max) {
    if (_importLevels == null) {
      _importLevels = new HashMap<>();
    }
    _importLevels.put(max, min);
    return _methodName + '_' + max;
  }

  /**
   * Shortcut to get the class compiled for this mode (will be inlined).
   */
  private String getClassName() {
    return _stylesheet.getClassName();
  }

  public Stylesheet getStylesheet() {
    return _stylesheet;
  }

  public void addTemplate(Template template) {
    _templates.add(template);
  }

  private List<Template> quicksort(List<Template> templates, int p, int r) {
    if (p < r) {
      final int q = partition(templates, p, r);
      quicksort(templates, p, q);
      quicksort(templates, q + 1, r);
    }
    return templates;
  }

  private int partition(List<Template> templates, int p, int r) {
    final Template x = templates.get(p);
    int i = p - 1;
    int j = r + 1;
    while (true) {
      while (x.compareTo(templates.get(--j)) > 0) {
        ;
      }
      while (x.compareTo(templates.get(++i)) < 0) {
        ;
      }
      if (i < j) {
        templates.set(j, templates.set(i, templates.get(j)));
      } else
        return j;
    }
  }

  /**
   * Process all the test patterns in this mode
   */
  public void processPatterns() {
//      System.out.println("Before Sort " + _name); for (int i = 0; i <
//      _templates.size(); i++) { System.out.println("name = " +
//      ((Template)_templates.get(i)).getName());
//      System.out.println("pattern = " +
//      ((Template)_templates.get(i)).getPattern());
//      System.out.println("priority = " +
//      ((Template)_templates.get(i)).getPriority());
//      System.out.println("position = " +
//      ((Template)_templates.get(i)).getPosition()); }

    _templates = quicksort(_templates, 0, _templates.size() - 1);

//      System.out.println("\n After Sort " + _name); for (int i = 0; i <
//      _templates.size(); i++) { System.out.println("name = " +
//      ((Template)_templates.get(i)).getName());
//      System.out.println("pattern = " +
//      ((Template)_templates.get(i)).getPattern());
//      System.out.println("priority = " +
//      ((Template)_templates.get(i)).getPriority());
//      System.out.println("position = " +
//      ((Template)_templates.get(i)).getPosition()); }

    // Traverse all templates
    for (final Template template : _templates) {
      /*
       * Add this template to a table of named templates if it has a name. If
       * there are multiple templates with the same name, all but one (the one
       * with highest priority) will be disabled.
       */
      if (template.isNamed() && !template.disabled()) {
        _namedTemplates.put(template, this);
      }

      // Add this template to a test sequence if it has a pattern
      final Pattern pattern = template.getPattern();
      if (pattern != null) {
        flattenAlternative(pattern, template);
      }
    }
    prepareTestSequences();
  }

  /**
   * This method will break up alternative patterns (ie. unions of patterns,
   * such as match="A/B | C/B") and add the basic patterns to their respective
   * pattern groups.
   */
  private void flattenAlternative(Pattern pattern, Template template) {
    // Patterns on type id() and key() are special since they do not have
    // any kernel node type (it can be anything as long as the node is in
    // the id's or key's index).
    if (pattern instanceof IdKeyPattern) {
      final IdKeyPattern idkey = (IdKeyPattern) pattern;
      idkey.setTemplate(template);
      if (_idxGroup == null) {
        _idxGroup = new ArrayList<>();
      }
      _idxGroup.add(idkey);
    }
    // Alternative patterns are broken up and re-processed recursively
    else if (pattern instanceof AlternativePattern) {
      final AlternativePattern alt = (AlternativePattern) pattern;
      flattenAlternative(alt.getLeft(), template);
      flattenAlternative(alt.getRight(), template);
    }
    // Finally we have a pattern that can be added to a test sequence!
    else if (pattern instanceof LocationPathPattern) {
      final LocationPathPattern lpp = (LocationPathPattern) pattern;
      lpp.setTemplate(template);
      addPatternToGroup(lpp);
    }
  }

  /**
   * Group patterns by NodeTests of their last Step Keep them sorted by priority
   * within group
   */
  private void addPatternToGroup(final LocationPathPattern lpp) {
    // id() and key()-type patterns do not have a kernel type
    if (lpp instanceof IdKeyPattern) {
      addPattern(-1, lpp);
    }
    // Otherwise get the kernel pattern from the LPP
    else {
      // kernel pattern is the last (maybe only) Step
      final StepPattern kernel = lpp.getKernelPattern();
      if (kernel != null) {
        addPattern(kernel.getNodeType(), lpp);
      } else if (_rootPattern == null || lpp.noSmallerThan(_rootPattern)) {
        _rootPattern = lpp;
      }
    }
  }

  /**
   * Adds a pattern to a pattern group
   */
  private void addPattern(int kernelType, LocationPathPattern pattern) {
    // Make sure the array of pattern groups is long enough
    final int oldLength = _patternGroups.length;
    if (kernelType >= oldLength) {
      @SuppressWarnings("unchecked")
      final List<LocationPathPattern>[] newGroups = new ArrayList[kernelType * 2];
      System.arraycopy(_patternGroups, 0, newGroups, 0, oldLength);
      _patternGroups = newGroups;
    }

    // Find the list to put this pattern into
    List<LocationPathPattern> patterns;

    if (kernelType == DOM.NO_TYPE) {
      if (pattern.getAxis() == Axis.ATTRIBUTE) {
        patterns = _attribNodeGroup == null ? (_attribNodeGroup = new ArrayList<>(2)) : _attribNodeGroup;
      } else {
        patterns = _childNodeGroup == null ? (_childNodeGroup = new ArrayList<>(2)) : _childNodeGroup;
      }
    } else {
      patterns = _patternGroups[kernelType] == null ? (_patternGroups[kernelType] = new ArrayList<>(2))
              : _patternGroups[kernelType];
    }

    if (patterns.size() == 0) {
      patterns.add(pattern);
    } else {
      boolean inserted = false;
      for (int i = 0; i < patterns.size(); i++) {
        final LocationPathPattern lppToCompare = patterns.get(i);

        if (pattern.noSmallerThan(lppToCompare)) {
          inserted = true;
          patterns.add(i, pattern);
          break;
        }
      }
      if (inserted == false) {
        patterns.add(pattern);
      }
    }
  }

  /**
   * Complete test sequences of a given type by adding all patterns from a given
   * group.
   */
  private void completeTestSequences(int nodeType, List<LocationPathPattern> patterns) {
    if (patterns != null) {
      if (_patternGroups[nodeType] == null) {
        _patternGroups[nodeType] = patterns;
      } else {
        final int m = patterns.size();
        for (int j = 0; j < m; j++) {
          addPattern(nodeType, patterns.get(j));
        }
      }
    }
  }

  /**
   * Build test sequences. The first step is to complete the test sequences by
   * including patterns of "*" and "node()" kernel to all element test
   * sequences, and of "@*" to all attribute test sequences.
   */
  private void prepareTestSequences() {
    final List<LocationPathPattern> starGroup = _patternGroups[DTM.ELEMENT_NODE];
    final List<LocationPathPattern> atStarGroup = _patternGroups[DTM.ATTRIBUTE_NODE];

    // Complete test sequence for "text()" with "child::node()"
    completeTestSequences(DTM.TEXT_NODE, _childNodeGroup);

    // Complete test sequence for "*" with "child::node()"
    completeTestSequences(DTM.ELEMENT_NODE, _childNodeGroup);

    // Complete test sequence for "pi()" with "child::node()"
    completeTestSequences(DTM.PROCESSING_INSTRUCTION_NODE, _childNodeGroup);

    // Complete test sequence for "comment()" with "child::node()"
    completeTestSequences(DTM.COMMENT_NODE, _childNodeGroup);

    // Complete test sequence for "@*" with "attribute::node()"
    completeTestSequences(DTM.ATTRIBUTE_NODE, _attribNodeGroup);

    final List<String> names = _stylesheet.getXSLTC().getNamesIndex();
    if (starGroup != null || atStarGroup != null || _childNodeGroup != null || _attribNodeGroup != null) {
      final int n = _patternGroups.length;

      // Complete test sequence for user-defined types
      for (int i = DTM.NTYPES; i < n; i++) {
        if (_patternGroups[i] == null) {
          continue;
        }

        final String name = names.get(i - DTM.NTYPES);

        if (isAttributeName(name)) {
          // If an attribute then copy "@*" to its test sequence
          completeTestSequences(i, atStarGroup);

          // And also copy "attribute::node()" to its test sequence
          completeTestSequences(i, _attribNodeGroup);
        } else {
          // If an element then copy "*" to its test sequence
          completeTestSequences(i, starGroup);

          // And also copy "child::node()" to its test sequence
          completeTestSequences(i, _childNodeGroup);
        }
      }
    }

    _testSeq = new TestSeq[DTM.NTYPES + names.size()];

    final int n = _patternGroups.length;
    for (int i = 0; i < n; i++) {
      final List<LocationPathPattern> patterns = _patternGroups[i];
      if (patterns != null) {
        final TestSeq testSeq = new TestSeq(patterns, i, this);
        // System.out.println("testSeq[" + i + "] = " + testSeq);
        testSeq.reduce();
        _testSeq[i] = testSeq;
        testSeq.findTemplates(_neededTemplates);
      }
    }

    if (_childNodeGroup != null && _childNodeGroup.size() > 0) {
      _childNodeTestSeq = new TestSeq(_childNodeGroup, -1, this);
      _childNodeTestSeq.reduce();
      _childNodeTestSeq.findTemplates(_neededTemplates);
    }

    /*
     * if (_attribNodeGroup != null && _attribNodeGroup.size() > 0) {
     * _attribNodeTestSeq = new TestSeq(_attribNodeGroup, -1, this);
     * _attribNodeTestSeq.reduce();
     * _attribNodeTestSeq.findTemplates(_neededTemplates); }
     */

    if (_idxGroup != null && _idxGroup.size() > 0) {
      _idxTestSeq = new TestSeq(_idxGroup, this);
      _idxTestSeq.reduce();
      _idxTestSeq.findTemplates(_neededTemplates);
    }

    if (_rootPattern != null) {
      // doesn't matter what is 'put', only key matters
      _neededTemplates.put(_rootPattern.getTemplate(), TestSeq.EMPTY);
    }
  }

  private void compileNamedTemplate(Template template, CompilerContext ctx) {
    final String methodName = Util.escape(template.getName().toString());
    JMethod method = ctx.method(JMod.PUBLIC, void.class, methodName)._throws(SAXException.class);
    // Set the parameters.
    ctx.param(DOM.class, DOCUMENT_PNAME);
    ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar handler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);
    JVar current = ctx.param(int.class, NODE_PNAME);
    // For simple named templates, the signature of the generated method
    // is not fixed. It depends on the number of parameters declared in the
    // template.
    if (template.isSimpleNamedTemplate()) {
      final List<Param> parameters = template.getParameters();
      for (int i = 0, numParams = parameters.size(); i < numParams; i++) {
        Param param = parameters.get(i);
        ctx.param(param._type.toJCType(), param._escapedName);
      }
    }

    ctx.pushBlock(method.body());
    ctx.pushHandler(handler);
    ctx.pushNode(current);
    template.translate(ctx);
    ctx.popNode();
    ctx.popHandler();
    ctx.popBlock();
    ctx.popMethodContext();
  }

  private void compileTemplates(CompilerContext ctx) {
    for (final Template template : _namedTemplates.keySet()) {
      compileNamedTemplate(template, ctx);
    }

    for (final Template template : _neededTemplates.keySet()) {
      if (template.hasContents()) {
        // !!! TODO templates both named and matched
        final JStatement statement = template.compile(ctx);
        _templateStatement.put(template, statement);
      } else {
        // empty template
        _templateStatement.put(template, null);
      }
    }
  }

  private void appendTemplateCode(JSwitch test) {
    for (final Template template : _neededTemplates.keySet()) {
      final JStatement invocation = _templateStatement.get(template);
      if (invocation == null) {
      } else {
        // FIXME
        // test._case(block);
      }
    }
  }

  private void appendTestSequences(JSwitch test) {
    final int n = _testSeq.length;
    for (int i = 0; i < n; i++) {
      final TestSeq testSeq = _testSeq[i];
      if (testSeq != null) {
        final JStatement il = testSeq.getInstructionList();
        if (il != null) {
          test._case((JExpression) il);
          // else trivial TestSeq
        }
      }
    }
  }

  public static JInvocation compileGetChildren(CompilerContext ctx) {
    return ctx.currentDom().invoke(GET_CHILDREN).arg(ctx.currentNode());
  }

  /**
   * Compiles the default handling for DOM elements: traverse all children
   */
  private JInvocation compileDefaultRecursion(CompilerContext ctx) {
    JExpression document = ctx.currentDom();
    JVar handler = ctx.param(TRANSLET_OUTPUT_PNAME);
    JInvocation getChildren = document.invoke(GET_CHILDREN).arg(ctx.currentNode());
    return _this().invoke(ctx.currentMethod()).arg(document).arg(getChildren).arg(handler);
  }

  /**
   * Compiles the default action for DOM text nodes and attribute nodes: output
   * the node's text value
   */
  private JInvocation compileDefaultText(CompilerContext ctx) {
    JExpression document = ctx.currentDom();
    JVar handler = ctx.param(TRANSLET_OUTPUT_PNAME);
    return document.invoke(CHARACTERS).arg(ctx.currentNode()).arg(handler);
  }

  private JStatement compileNamespaces(CompilerContext ctx, boolean[] isNamespace, boolean[] isAttribute,
      boolean attrFlag, JStatement defaultTarget) {
    final XSLTC xsltc = ctx.xsltc();

    // Append switch() statement - namespace test dispatch loop
    final List<String> namespaces = xsltc.getNamespaceIndex();
    final List<String> names = xsltc.getNamesIndex();
    final int namespaceCount = namespaces.size() + 1;
    final int namesCount = names.size();

    final JBlock body = new JBlock();
    final int[] types = new int[namespaceCount];
    final JStatement[] targets = new JStatement[types.length];

    if (namespaceCount > 0) {
      boolean compiled = false;

      // Initialize targets for namespace() switch statement
      for (int i = 0; i < namespaceCount; i++) {
        targets[i] = defaultTarget;
        types[i] = i;
      }

      // Add test sequences for known namespace types
      for (int i = DTM.NTYPES; i < DTM.NTYPES + namesCount; i++) {
        if (isNamespace[i] && isAttribute[i] == attrFlag) {
          final String name = names.get(i - DTM.NTYPES);
          final String namespace = name.substring(0, name.lastIndexOf(':'));
          final int type = xsltc.registerNamespace(namespace);

          if (i < _testSeq.length && _testSeq[i] != null) {
            targets[type] = _testSeq[i].compile(ctx, defaultTarget);
            compiled = true;
          }
        }
      }

      // Return "null" if no test sequences were compiled
      if (!compiled)
        return null;

      // Append first code in applyTemplates() - get type of current node
      
      body.invoke("getNamespaceType").arg(ctx.currentNode());
//      final int getNS = cpg.addInterfaceMethodref(DOM_INTF, "getNamespaceType", "(I)I");
//      body.append(methodGen.loadDOM());
//      body.append(new ILOAD(_currentIndex));
//      body.append(new INVOKEINTERFACE(getNS, 2));
//      body.append(new SWITCH(types, targets, defaultTarget));
      return body;
    } else
      return null;
  }

  /**
   * Compiles the applyTemplates() method and adds it to the translet. This is
   * the main dispatch method.
   */
  public void compileApplyTemplates(CompilerContext ctx, XSLTC xsltc) {
    // Create the applyTemplates() method
    JMethod applyTemplates = ctx.method(JMod.PUBLIC | JMod.FINAL, void.class, functionName())._throws(SAXException.class);

    JVar document = ctx.param(DOM.class, DOCUMENT_PNAME);
    JVar iterator = ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar handler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);

    final JBlock loop = applyTemplates.body()._while(TRUE).body();

    // Create a local variable to hold the current node
    // Create an instruction list that contains the default next-node
    // iteration
    JVar current = loop.decl(ctx.owner().INT, "current", invoke(iterator, "next"));
    ctx.pushNode(current);
    // The body of this code can get very large - large than can be handled
    // by a single IFNE(body.getStart()) instruction - need workaround:
    loop._if(current.lt(lit(0)))._then()._return(); // applyTemplates() ends here!

    ctx.pushBlock(loop);
    // Compile default handling of elements (traverse children)
    final JInvocation defaultRecursion = compileDefaultRecursion(ctx);

    // Compile default handling of text/attribute nodes (output text)
    JStatement defaultText = compileDefaultText(ctx);

    // Distinguish attribute/element/namespace tests for further processing
    List<String> names = xsltc.getNamesIndex();
    final int[] types = new int[DTM.NTYPES + names.size()];
    for (int i = 0; i < types.length; i++) {
      types[i] = i;
    }

    // Initialize isAttribute[] and isNamespace[] arrays
    final boolean[] isAttribute = new boolean[types.length];
    final boolean[] isNamespace = new boolean[types.length];
    for (int i = 0; i < names.size(); i++) {
      final String name = names.get(i);
      isAttribute[i + DTM.NTYPES] = isAttributeName(name);
      isNamespace[i + DTM.NTYPES] = isNamespaceName(name);
    }

    // Compile all templates - regardless of pattern type
    compileTemplates(ctx);

    // Handle template with explicit "*" pattern
    final TestSeq elementTest = _testSeq[DTM.ELEMENT_NODE];
    JStatement elementInvocation = defaultRecursion;
    if (elementTest != null) {
      elementInvocation = elementTest.compile(ctx, defaultRecursion);
    }

    // Handle template with explicit "@*" pattern
    final TestSeq attrTest = _testSeq[DTM.ATTRIBUTE_NODE];
    JStatement attrInvocation = defaultText;
    if (attrTest != null) {
      attrInvocation = attrTest.compile(ctx, defaultText);
    }

    // Do tests for id() and key() patterns first
//    InstructionList ilKey = null;
    if (_idxTestSeq != null) {
      ctx.currentBlock().add(_idxTestSeq.compile(ctx, null));
//      loop.setTarget(_idxTestSeq.compile(definedClass, applyTemplates, body.getStart()));
//      ilKey = _idxTestSeq.getInstructionList();
//    } else {
//      loop.setTarget(body.getStart());
    }

    // If there is a match on node() we need to replace ihElem
    // and ihText if the priority of node() is higher
    if (_childNodeTestSeq != null) {
      // Compare priorities of node() and "*"
      final double nodePrio = _childNodeTestSeq.getPriority();
      final int nodePos = _childNodeTestSeq.getPosition();
      double elemPrio = 0 - Double.MAX_VALUE;
      int elemPos = Integer.MIN_VALUE;

      if (elementTest != null) {
        elemPrio = elementTest.getPriority();
        elemPos = elementTest.getPosition();
      }
      if (elemPrio == Double.NaN || elemPrio < nodePrio || elemPrio == nodePrio && elemPos < nodePos) {
        elementInvocation = _childNodeTestSeq.compile(ctx, null);
      }

      // Compare priorities of node() and text()
      final TestSeq textTest = _testSeq[DTM.TEXT_NODE];
      double textPrio = 0 - Double.MAX_VALUE;
      int textPos = Integer.MIN_VALUE;

      if (textTest != null) {
        textPrio = textTest.getPriority();
        textPos = textTest.getPosition();
      }
      if (Double.isNaN(textPrio) || textPrio < nodePrio || textPrio == nodePrio && textPos < nodePos) {
        defaultText = _childNodeTestSeq.compile(ctx, null);
        _testSeq[DTM.TEXT_NODE] = _childNodeTestSeq;
      }
    }

    // Handle templates with "ns:*" pattern
    final JStatement nsElem = compileNamespaces(ctx, isNamespace, isAttribute, false,
        elementInvocation);
    JStatement elemNamespaceHandle = nsElem == null ? elementInvocation : nsElem;

    // Handle templates with "ns:@*" pattern
    final JStatement nsAttr = compileNamespaces(ctx, isNamespace, isAttribute, true,
        attrInvocation);
    JStatement attrNamespaceHandle = nsAttr == null ? attrInvocation : nsAttr;

    // Handle templates with "ns:elem" or "ns:@attr" pattern
    final JStatement[] targets = new JStatement[types.length];
//    for (int i = DTM.NTYPES; i < targets.length; i++) {
//      final TestSeq testSeq = _testSeq[i];
//      // Jump straight to namespace tests ?
//      if (isNamespace[i]) {
//        if (isAttribute[i]) {
//          targets[i] = attrNamespaceHandle;
//        } else {
//          targets[i] = elemNamespaceHandle;
//        }
//      }
//      // Test first, then jump to namespace tests
//      else if (testSeq != null) {
//        if (isAttribute[i]) {
//          targets[i] = testSeq.compile(ctx, attrNamespaceHandle);
//        } else {
//          targets[i] = testSeq.compile(ctx, elemNamespaceHandle);
//        }
//      } else {
//        targets[i] = null;
//      }
//    }

    // Handle pattern with match on root node - default: traverse children
    targets[DTM.ROOT_NODE] = _rootPattern != null ? getTemplateInstructionHandle(_rootPattern.getTemplate())
        : defaultRecursion;

    // Handle pattern with match on root node - default: traverse children
    targets[DTM.DOCUMENT_NODE] = _rootPattern != null ? getTemplateInstructionHandle(_rootPattern.getTemplate())
        : defaultRecursion;

    // Handle any pattern with match on text nodes - default: output text
    targets[DTM.TEXT_NODE] = _testSeq[DTM.TEXT_NODE] != null ? _testSeq[DTM.TEXT_NODE].compile(ctx,
        defaultText) : defaultText;

    // This DOM-type is not in use - default: process next node
    targets[DTM.NAMESPACE_NODE] = null;

    // Match unknown element in DOM - default: check for namespace match
    targets[DTM.ELEMENT_NODE] = elemNamespaceHandle;

    // Match unknown attribute in DOM - default: check for namespace match
    targets[DTM.ATTRIBUTE_NODE] = attrNamespaceHandle;

    // Match on processing instruction - default: process next node
    JStatement ihPI = null;
    if (_childNodeTestSeq != null) {
      ihPI = elementInvocation;
    }
    if (_testSeq[DTM.PROCESSING_INSTRUCTION_NODE] != null) {
      targets[DTM.PROCESSING_INSTRUCTION_NODE] = _testSeq[DTM.PROCESSING_INSTRUCTION_NODE].compile(ctx, ihPI);
    } else {
      targets[DTM.PROCESSING_INSTRUCTION_NODE] = ihPI;
    }

    // Match on comments - default: process next node
    JStatement ihComment = null;
    if (_childNodeTestSeq != null) {
      ihComment = elementInvocation;
    }
    targets[DTM.COMMENT_NODE] = _testSeq[DTM.COMMENT_NODE] != null ? _testSeq[DTM.COMMENT_NODE].compile(ctx,
        ihComment) : ihComment;

    // This DOM-type is not in use - default: process next node
    targets[DTM.CDATA_SECTION_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.DOCUMENT_FRAGMENT_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.DOCUMENT_TYPE_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.ENTITY_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.ENTITY_REFERENCE_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.NOTATION_NODE] = null;

    // Now compile test sequences for various match patterns:
    for (int i = DTM.NTYPES; i < targets.length; i++) {
      final TestSeq testSeq = _testSeq[i];
      // Jump straight to namespace tests ?
      if (testSeq == null || isNamespace[i]) {
        if (isAttribute[i]) {
          targets[i] = attrNamespaceHandle;
        } else {
          targets[i] = elemNamespaceHandle;
        }
      }
      // Match on node type
      else {
        if (isAttribute[i]) {
          targets[i] = testSeq.compile(ctx, attrNamespaceHandle);
        } else {
          targets[i] = testSeq.compile(ctx, elemNamespaceHandle);
        }
      }
    }

//    if (ilKey != null) {
//      body.insert(ilKey);
//    }

    // Append first code in applyTemplates() - get type of current node
    JInvocation getExpandedTypeID = document.invoke("getExpandedTypeID").arg(current);
    
    // Append switch() statement - main dispatch loop in applyTemplates()
    JSwitch test = loop._switch(getExpandedTypeID);

    // Append all the "case:" statements
//    for (int i = 0; i < targets.length; i++) {
    for (int i = targets.length-1;i>=0; i--) {
      JStatement jStatement = targets[i];
      if (jStatement != null) test._case(lit(i)).body().add(jStatement)._break();
    }
    // appendTestSequences(test);
    // Append the actual template code
    // appendTemplateCode(test);

    // Append NS:* node tests (if any)
//    if (nsElem != null) {
//      body.append(nsElem);
//    }
    // Append NS:@* node tests (if any)
//    if (nsAttr != null) {
//      body.append(nsAttr);
//    }

    // Append default action for element and root nodes
//    body.append(ilRecurse);
    // Append default action for text and attribute nodes
//    body.append(ilText);

    // putting together constituent instruction lists
//    mainIL.append(body);
    // fall through to ilLoop
//    mainIL.append(ilLoop);

    // Compile method(s) for <xsl:apply-imports/> for this mode
    if (_importLevels != null) {
      for (final Map.Entry<Integer, Integer> entry : _importLevels.entrySet()) {
        compileApplyImports(ctx, entry.getValue().intValue(), entry.getKey().intValue(), xsltc);
      }
    }
    ctx.popNode();
  }

  private void compileTemplateCalls(CompilerContext ctx, JInvocation next, int min, int max) {
    for (final Template template : _neededTemplates.keySet()) {
      final int prec = template.getImportPrecedence();
      if (prec >= min && prec < max) {
        if (template.hasContents()) {
          final JStatement til = template.compile(ctx);
          _templateStatement.put(template, til);
        } else {
          // empty template
          _templateStatement.put(template, next);
        }
      }
    }
  }

  public void compileApplyImports(CompilerContext ctx, int min, int max, XSLTC xsltc) {
    // Clear some datastructures
    _namedTemplates = new HashMap<>();
    _neededTemplates = new HashMap<>();
    _templateStatement = new HashMap<>();
    _patternGroups = new ArrayList[32];
    _rootPattern = null;

    // IMPORTANT: Save orignal & complete set of templates!!!!
    final List<Template> oldTemplates = _templates;

    // Gather templates that are within the scope of this import
    _templates = new ArrayList<>();
    for (final Template template : oldTemplates) {
      final int prec = template.getImportPrecedence();
      if (prec >= min && prec < max) {
        addTemplate(template);
      }
    }

    // Process all patterns from those templates
    processPatterns();

    // Create the applyTemplates() method
    JMethod applyTemplates = ctx.method(JMod.PUBLIC | JMod.FINAL, void.class, functionName() + '_' + max)._throws(SAXException.class);
    JVar document = ctx.param(DOM.class, DOCUMENT_PNAME);
    JVar iterator = ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar handler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);
    // Create the local variable to hold the current node
    JVar current = ctx.param(int.class, NODE_PNAME);
    ctx.pushNode(current);

    JBlock body = applyTemplates.body();
    ctx.pushBlock(body);

//    final LocalVariableGen current;
//    current = methodGen.addLocalVariable2("current", org.apache.bcel.generic.Type.INT, null);
//    _currentIndex = current.getIndex();
//
//    mainIL.append(new ILOAD(methodGen.getLocalIndex(NODE_PNAME)));
//    current.setStart(mainIL.append(new ISTORE(_currentIndex)));
//
//    // Create the "body" instruction list that will eventually hold the
//    // code for the entire method (other ILs will be appended).
//    final InstructionList body = new InstructionList();
//    body.append(NOP);
//
//    // Create an instruction list that contains the default next-node
//    // iteration
//    final InstructionList ilLoop = new InstructionList();
//    ilLoop.append(RETURN);
//    final InstructionHandle ihLoop = ilLoop.getStart();
    // Compile default handling of elements (traverse children)
    MethodContext methodContext = ctx.popMethodContext();
    ctx.pushNode(current);
    final JInvocation defaultRecursion = compileDefaultRecursion(ctx);
    ctx.popNode();
    ctx.pushMethodContext(methodContext);
    
    // Compile default handling of text/attribute nodes (output text)
    JStatement defaultText = compileDefaultText(ctx);

    List<String> names = xsltc.getNamesIndex();
    final int[] types = new int[DTM.NTYPES + names.size()];
    for (int i = 0; i < types.length; i++) {
      types[i] = i;
    }

    // Initialize isAttribute[] and isNamespace[] arrays
    final boolean[] isAttribute = new boolean[types.length];
    final boolean[] isNamespace = new boolean[types.length];
    for (int i = 0; i < names.size(); i++) {
      final String name = names.get(i);
      isAttribute[i + DTM.NTYPES] = isAttributeName(name);
      isNamespace[i + DTM.NTYPES] = isNamespaceName(name);
    }

// FIXME    // Compile all templates - regardless of pattern type
    compileTemplateCalls(ctx, null, min, max);

    // Handle template with explicit "*" pattern
    final TestSeq elementTest = _testSeq[DTM.ELEMENT_NODE];
    JStatement elementInvocation = defaultRecursion;
    if (elementTest != null) {
      elementInvocation = elementTest.compile(ctx, defaultRecursion);
    }

    // Handle template with explicit "@*" pattern
    final TestSeq attrTest = _testSeq[DTM.ATTRIBUTE_NODE];
    JStatement attrInvocation = defaultText;
    if (attrTest != null) {
      attrInvocation = attrTest.compile(ctx, defaultText);
    }

    // Do tests for id() and key() patterns first
    if (_idxTestSeq != null) {
      ctx.currentBlock().add(_idxTestSeq.compile(ctx, null));
    }
//    InstructionList ilKey = null;
//    if (_idxTestSeq != null) {
//      ilKey = _idxTestSeq.getInstructionList();
//    }

    // If there is a match on node() we need to replace ihElem
    // and ihText if the priority of node() is higher
    if (_childNodeTestSeq != null) {
      // Compare priorities of node() and "*"
      final double nodePrio = _childNodeTestSeq.getPriority();
      final int nodePos = _childNodeTestSeq.getPosition();
      double elemPrio = 0 - Double.MAX_VALUE;
      int elemPos = Integer.MIN_VALUE;

      if (elementTest != null) {
        elemPrio = elementTest.getPriority();
        elemPos = elementTest.getPosition();
      }
      if (elemPrio == Double.NaN || elemPrio < nodePrio || elemPrio == nodePrio && elemPos < nodePos) {
        elementInvocation = _childNodeTestSeq.compile(ctx, null);
      }

      // Compare priorities of node() and text()
      final TestSeq textTest = _testSeq[DTM.TEXT_NODE];
      double textPrio = 0 - Double.MAX_VALUE;
      int textPos = Integer.MIN_VALUE;

      if (textTest != null) {
        textPrio = textTest.getPriority();
        textPos = textTest.getPosition();
      }
      if (Double.isNaN(textPrio) || textPrio < nodePrio || textPrio == nodePrio && textPos < nodePos) {
        defaultText = _childNodeTestSeq.compile(ctx, null);
        _testSeq[DTM.TEXT_NODE] = _childNodeTestSeq;
      }
    }

    // Handle templates with "ns:*" pattern
    final JStatement nsElem = compileNamespaces(ctx, isNamespace, isAttribute, false,
        elementInvocation);
    JStatement elemNamespaceHandle = nsElem == null ? elementInvocation : nsElem;

    // Handle templates with "ns:@*" pattern
    final JStatement nsAttr = compileNamespaces(ctx, isNamespace, isAttribute, true,
        attrInvocation);
    JStatement attrNamespaceHandle = nsAttr == null ? attrInvocation : nsAttr;

    // Handle templates with "ns:elem" or "ns:@attr" pattern
    final JStatement[] targets = new JStatement[types.length];
//    final InstructionHandle[] targets = new InstructionHandle[types.length];
//    for (int i = DTM.NTYPES; i < targets.length; i++) {
//      final TestSeq testSeq = _testSeq[i];
//      // Jump straight to namespace tests ?
//      if (isNamespace[i]) {
//        if (isAttribute[i]) {
//          targets[i] = attrNamespaceHandle;
//        } else {
//          targets[i] = elemNamespaceHandle;
//        }
//      }
//      // Test first, then jump to namespace tests
//      else if (testSeq != null) {
//        if (isAttribute[i]) {
//          targets[i] = testSeq.compile(definedClass, methodGen, attrNamespaceHandle);
//        } else {
//          targets[i] = testSeq.compile(definedClass, methodGen, elemNamespaceHandle);
//        }
//      } else {
//        targets[i] = ihLoop;
//      }
//    }

    // Handle pattern with match on root node - default: traverse children
    targets[DTM.ROOT_NODE] = _rootPattern != null ? getTemplateInstructionHandle(_rootPattern.getTemplate())
        : defaultRecursion;

    // Handle pattern with match on root node - default: traverse children
    targets[DTM.DOCUMENT_NODE] = _rootPattern != null ? getTemplateInstructionHandle(_rootPattern.getTemplate())
        : defaultRecursion;

    // Handle any pattern with match on text nodes - default: loop
    targets[DTM.TEXT_NODE] = _testSeq[DTM.TEXT_NODE] != null ? _testSeq[DTM.TEXT_NODE].compile(ctx,
        defaultText) : defaultText;

    // This DOM-type is not in use - default: process next node
    targets[DTM.NAMESPACE_NODE] = null;

    // Match unknown element in DOM - default: check for namespace match
    targets[DTM.ELEMENT_NODE] = elemNamespaceHandle;

    // Match unknown attribute in DOM - default: check for namespace match
    targets[DTM.ATTRIBUTE_NODE] = attrNamespaceHandle;

    // Match on processing instruction - default: process next node
    JStatement ihPI = null;
    if (_childNodeTestSeq != null) {
      ihPI = elementInvocation;
    }
    if (_testSeq[DTM.PROCESSING_INSTRUCTION_NODE] != null) {
      targets[DTM.PROCESSING_INSTRUCTION_NODE] = _testSeq[DTM.PROCESSING_INSTRUCTION_NODE].compile(ctx, ihPI);
    } else {
      targets[DTM.PROCESSING_INSTRUCTION_NODE] = ihPI;
    }

    // Match on comments - default: process next node
    JStatement ihComment = null;
    if (_childNodeTestSeq != null) {
      ihComment = elementInvocation;
    }
    targets[DTM.COMMENT_NODE] = _testSeq[DTM.COMMENT_NODE] != null ? _testSeq[DTM.COMMENT_NODE].compile(ctx,
        ihComment) : ihComment;

    // This DOM-type is not in use - default: process next node
    targets[DTM.CDATA_SECTION_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.DOCUMENT_FRAGMENT_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.DOCUMENT_TYPE_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.ENTITY_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.ENTITY_REFERENCE_NODE] = null;

    // This DOM-type is not in use - default: process next node
    targets[DTM.NOTATION_NODE] = null;

    // Now compile test sequences for various match patterns:
    for (int i = DTM.NTYPES; i < targets.length; i++) {
      final TestSeq testSeq = _testSeq[i];
      // Jump straight to namespace tests ?
      if (testSeq == null || isNamespace[i]) {
        if (isAttribute[i]) {
          targets[i] = attrNamespaceHandle;
        } else {
          targets[i] = elemNamespaceHandle;
        }
      }
      // Match on node type
      else {
        if (isAttribute[i]) {
          targets[i] = testSeq.compile(ctx, attrNamespaceHandle);
        } else {
          targets[i] = testSeq.compile(ctx, elemNamespaceHandle);
        }
      }
    }

//    if (ilKey != null) {
//      body.insert(ilKey);
//    }

    // Append first code in applyTemplates() - get type of current node
    JInvocation getExpandedTypeID = document.invoke("getExpandedTypeID").arg(current);
    
    // Append switch() statement - main dispatch loop in applyTemplates()
    JSwitch test = body._switch(getExpandedTypeID);

    // Append all the "case:" statements
    for (int i = targets.length-1;i>=0; i--) {
      JStatement jStatement = targets[i];
      if (jStatement != null) test._case(lit(i)).body().add(jStatement)._break();
    }

//    appendTestSequences(body);
//    // Append the actual template code
//    appendTemplateCode(body);
//
//    // Append NS:* node tests (if any)
//    if (nsElem != null) {
//      body.append(nsElem);
//    }
//    // Append NS:@* node tests (if any)
//    if (nsAttr != null) {
//      body.append(nsAttr);
//    }
//
//    // Append default action for element and root nodes
//    body.append(ilRecurse);
//    // Append default action for text and attribute nodes
//    body.append(ilText);
//
//    // putting together constituent instruction lists
//    mainIL.append(body);
//
//    // Mark the end of the live range for the "current" variable
//    current.setEnd(body.getEnd());
//
//    // fall through to ilLoop
//    mainIL.append(ilLoop);
//
//    //peepHoleOptimization(methodGen);
//
//    definedClass.addMethod(methodGen);

    // Restore original (complete) set of templates for this transformation
    _templates = oldTemplates;
    ctx.popMethodContext();
  }

  /**
   * Peephole optimization.
   */
  private void peepHoleOptimization(MethodGenerator methodGen) {
    final InstructionList il = methodGen.getInstructionList();
    final InstructionFinder find = new InstructionFinder(il);
    String pattern;

    // LoadInstruction, POP => (removed)
    pattern = "LoadInstruction POP";
    for (@SuppressWarnings("unchecked")
    final Iterator<InstructionHandle[]> iter = find.search(pattern); iter.hasNext();) {
      final InstructionHandle[] match = iter.next();
      try {
        if (!match[0].hasTargeters() && !match[1].hasTargeters()) {
          il.delete(match[0], match[1]);
        }
      } catch (final TargetLostException e) {
        // TODO: move target down into the list
      }
    }

    // ILOAD_N, ILOAD_N, SWAP, ISTORE_N => ILOAD_N
    pattern = "ILOAD ILOAD SWAP ISTORE";
    for (@SuppressWarnings("unchecked")
    final Iterator<InstructionHandle[]> iter = find.search(pattern); iter.hasNext();) {
      final InstructionHandle[] match = iter.next();
      try {
        final org.apache.bcel.generic.ILOAD iload1 = (org.apache.bcel.generic.ILOAD) match[0].getInstruction();
        final org.apache.bcel.generic.ILOAD iload2 = (org.apache.bcel.generic.ILOAD) match[1].getInstruction();
        final org.apache.bcel.generic.ISTORE istore = (org.apache.bcel.generic.ISTORE) match[3].getInstruction();

        if (!match[1].hasTargeters() && !match[2].hasTargeters() && !match[3].hasTargeters()
                && iload1.getIndex() == iload2.getIndex() && iload2.getIndex() == istore.getIndex()) {
          il.delete(match[1], match[3]);
        }
      } catch (final TargetLostException e) {
        // TODO: move target down into the list
      }
    }

    // LoadInstruction_N, LoadInstruction_M, SWAP => LoadInstruction_M,
    // LoadInstruction_N
    pattern = "LoadInstruction LoadInstruction SWAP";
    for (@SuppressWarnings("unchecked")
    final Iterator<InstructionHandle[]> iter = find.search(pattern); iter.hasNext();) {
      final InstructionHandle[] match = iter.next();
      try {
        if (!match[0].hasTargeters() && !match[1].hasTargeters() && !match[2].hasTargeters()) {
          final Instruction load_m = match[1].getInstruction();
          il.insert(match[0], load_m);
          il.delete(match[1], match[2]);
        }
      } catch (final TargetLostException e) {
        // TODO: move target down into the list
      }
    }

    // ALOAD_N ALOAD_N => ALOAD_N DUP
    pattern = "ALOAD ALOAD";
    for (@SuppressWarnings("unchecked")
    final Iterator<InstructionHandle[]> iter = find.search(pattern); iter.hasNext();) {
      final InstructionHandle[] match = iter.next();
      try {
        if (!match[1].hasTargeters()) {
          final org.apache.bcel.generic.ALOAD aload1 = (org.apache.bcel.generic.ALOAD) match[0].getInstruction();
          final org.apache.bcel.generic.ALOAD aload2 = (org.apache.bcel.generic.ALOAD) match[1].getInstruction();

          if (aload1.getIndex() == aload2.getIndex()) {
            il.insert(match[1], new DUP());
            il.delete(match[1]);
          }
        }
      } catch (final TargetLostException e) {
        // TODO: move target down into the list
      }
    }
  }

  public JStatement getTemplateInstructionHandle(Template template) {
    return _templateStatement.get(template);
  }

  /**
   * Auxiliary method to determine if a qname is an attribute.
   */
  private static boolean isAttributeName(String qname) {
    final int col = qname.lastIndexOf(':') + 1;
    return qname.charAt(col) == '@';
  }

  /**
   * Auxiliary method to determine if a qname is a namespace qualified "*".
   */
  private static boolean isNamespaceName(String qname) {
    final int col = qname.lastIndexOf(':');
    return col > -1 && qname.charAt(qname.length() - 1) == '*';
  }
}
