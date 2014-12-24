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
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.InstructionList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JStatement;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;

/**
 * A test sequence is a sequence of patterns that
 * 
 * (1) occured in templates in the same mode (2) share the same kernel node type
 * (e.g. A/B and C/C/B) (3) may also contain patterns matching "*" and "node()"
 * (element sequence only) or matching "@*" (attribute sequence only).
 * 
 * A test sequence may have a default template, which will be instantiated if
 * none of the other patterns match.
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 * @author Morten Jorgensen <morten.jorgensen@sun.com>
 */
final class TestSeq {

  public static final TestSeq EMPTY = new TestSeq();
  
  /**
   * Integer code for the kernel type of this test sequence
   */
  private final int _kernelType;

  /**
   * List of all patterns in the test sequence. May include patterns with "*",
   * "@*" or "node()" kernel.
   */
  private List<LocationPathPattern> _patterns = null;

  /**
   * A reference to the Mode object.
   */
  private Mode _mode = null;

  /**
   * Default template for this test sequence
   */
  private Template _default = null;

  /**
   * Instruction list representing this test sequence.
   */
  private JStatement _instructionList;

  /**
   * Cached handle to avoid compiling more than once.
   */
  private JStatement _start = null;

  /**
   * Creates a new test sequence given a set of patterns and a mode.
   */
  public TestSeq(List<LocationPathPattern> patterns, Mode mode) {
    this(patterns, -2, mode);
  }

  public TestSeq(List<LocationPathPattern> patterns, int kernelType, Mode mode) {
    _patterns = patterns;
    _kernelType = kernelType;
    _mode = mode;
  }

  private TestSeq() {
    _kernelType = -1;
  }

  /**
   * Returns a string representation of this test sequence. Notice that test
   * sequences are mutable, so the value returned by this method is different
   * before and after calling reduce().
   */
  @Override
  public String toString() {
    final int count = _patterns.size();
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < count; i++) {
      final LocationPathPattern pattern = _patterns.get(i);

      if (i == 0) {
        result.append("Testseq for kernel " + _kernelType).append('\n');
      }
      result.append("   pattern " + i + ": ").append(pattern.toString()).append('\n');
    }
    return result.toString();
  }

  /**
   * Returns the instruction list for this test sequence
   */
  public JStatement getInstructionList() {
    return _instructionList;
  }

  /**
   * Return the highest priority for a pattern in this test sequence. This is
   * either the priority of the first or of the default pattern.
   */
  public double getPriority() {
    final Template template = _patterns.size() == 0 ? _default : _patterns.get(0).getTemplate();
    return template.getPriority();
  }

  /**
   * Returns the position of the highest priority pattern in this test sequence.
   */
  public int getPosition() {
    final Template template = _patterns.size() == 0 ? _default : _patterns.get(0).getTemplate();
    return template.getPosition();
  }

  /**
   * Reduce the patterns in this test sequence. Creates a new list of patterns
   * and sets the default pattern if it finds a patterns that is fully reduced.
   */
  public void reduce() {
    final List<LocationPathPattern> newPatterns = new ArrayList<>();

    final int count = _patterns.size();
    for (int i = 0; i < count; i++) {
      final LocationPathPattern pattern = _patterns.get(i);

      // Reduce this pattern
      pattern.reduceKernelPattern();

      // Is this pattern fully reduced?
      if (pattern.isWildcard()) {
        _default = pattern.getTemplate();
        break; // Ignore following patterns
      } else {
        newPatterns.add(pattern);
      }
    }
    _patterns = newPatterns;
  }

  /**
   * Returns, by reference, the templates that are included in this test
   * sequence. Note that a single template can occur in several test sequences
   * if its pattern is a union.
   */
  public void findTemplates(Map<Template, TestSeq> templates) {
    if (_default != null) {
      templates.put(_default, this);
    }
    for (int i = 0; i < _patterns.size(); i++) {
      final LocationPathPattern pattern = _patterns.get(i);
      templates.put(pattern.getTemplate(), this);
    }
  }

  /**
   * Get the instruction handle to a template's code. This is used when a single
   * template occurs in several test sequences; that is, if its pattern is a
   * union of patterns (e.g. match="A/B | A/C").
   */
  private JStatement getTemplateHandle(Template template) {
    return _mode.getTemplateInstructionHandle(template);
  }

  /**
   * Returns pattern n in this test sequence
   */
  private LocationPathPattern getPattern(int n) {
    return _patterns.get(n);
  }

  /**
   * Compile the code for this test sequence. Compile patterns from highest to
   * lowest priority. Note that since patterns can be share by multiple test
   * sequences, instruction lists must be copied before backpatching.
   * @param ctx TODO
   */
  public JStatement compile(CompilerContext ctx, JStatement defaultStatement) {
    // Returned cached value if already compiled
    if (_start != null)
      return _start;

    // If not patterns, then return handle for default template
    final int count = _patterns.size();
    if (count == 0)
      return _start = getTemplateHandle(_default);

    // Init handle to jump when all patterns failed
    JStatement fail = _default == null ? defaultStatement : getTemplateHandle(_default);

    // Compile all patterns in reverse order
    for (int n = count - 1; n >= 0; n--) {
      final LocationPathPattern pattern = getPattern(n);
      final Template template = pattern.getTemplate();
      JBlock block = new JBlock();

      // Patterns expect current node on top of stack
      // il.append(methodGen.loadCurrentNode());

      // Apply the test-code compiled for the pattern
      // TODO InstructionList ilist = methodGen.getInstructionList(pattern);
      // TODO if (ilist == null) {
        JInvocation invocation = (JInvocation) pattern.compile(ctx);
        block.add(invocation);
      // TODO methodGen.addInstructionList(pattern, ilist);
      // TODO }

//      // Make a copy of the instruction list for backpatching
//      final InstructionList copyOfilist = ilist.copy();
//
//      FlowList trueList = pattern.getTrueList();
//      if (trueList != null) {
//        trueList = trueList.copyAndRedirect(ilist, copyOfilist);
//      }
//      FlowList falseList = pattern.getFalseList();
//      if (falseList != null) {
//        falseList = falseList.copyAndRedirect(ilist, copyOfilist);
//      }
//
//      il.append(copyOfilist);
//
//      // On success branch to the template code
//      final JBlock gtmpl = getTemplateHandle(template);
//      final InstructionHandle success = il.append(new GOTO_W(gtmpl));
//
//      if (trueList != null) {
//        trueList.backPatch(success);
//      }
//      if (falseList != null) {
//        falseList.backPatch(fail);
//      }
//
//      // Next pattern's 'fail' target is this pattern's first instruction
//      fail = il.getStart();
//
//      // Append existing instruction list to the end of this one
      if (_instructionList != null) {
        block.add(_instructionList);
      }
//
//      // Set current instruction list to be this one
//      _instructionList = il;
      _instructionList = block;
    }
//    if(_instructionList != null) return _instructionList;
    return _start = fail;
  }
}
