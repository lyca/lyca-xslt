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
package de.lyca.xalan.xsltc.compiler;

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.DOM.GET_EXPANDED_TYPE_ID;
import static de.lyca.xalan.xsltc.DOM.GET_PARENT;
import static de.lyca.xalan.xsltc.DOM.IS_ATTRIBUTE;
import static de.lyca.xalan.xsltc.DOM.IS_ELEMENT;
import static de.lyca.xml.dtm.DTMAxisIterator.NEXT;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.MatchingIterator;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
class StepPattern extends RelativePathPattern {

  private static final int NO_CONTEXT = 0;
  private static final int SIMPLE_CONTEXT = 1;
  private static final int GENERAL_CONTEXT = 2;

  protected final Axis _axis;
  protected final int _nodeType;
  protected List<Predicate> _predicates;

  private Step _step = null;
  private boolean _isEpsilon = false;
  private int _contextCase;

  private double _priority = Double.MAX_VALUE;

  public StepPattern(Axis axis, int nodeType, List<Predicate> predicates) {
    _axis = axis;
    _nodeType = nodeType;
    _predicates = predicates;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_predicates != null) {
      for (final Predicate exp : _predicates) {
        exp.setParser(parser);
        exp.setParent(this);
      }
    }
  }

  public int getNodeType() {
    return _nodeType;
  }

  public void setPriority(double priority) {
    _priority = priority;
  }

  @Override
  public StepPattern getKernelPattern() {
    return this;
  }

  @Override
  public boolean isWildcard() {
    return _isEpsilon && hasPredicates() == false;
  }

  public StepPattern setPredicates(List<Predicate> predicates) {
    _predicates = predicates;
    return this;
  }

  protected boolean hasPredicates() {
    return _predicates != null && _predicates.size() > 0;
  }

  @Override
  public double getDefaultPriority() {
    if (_priority != Double.MAX_VALUE)
      return _priority;

    if (hasPredicates())
      return 0.5;
    else {
      switch (_nodeType) {
      case -1:
        return -0.5; // node()
      case 0:
        return 0.0;
      default:
        return _nodeType >= NodeTest.GTYPE ? 0.0 : -0.5;
      }
    }
  }

  @Override
  public Axis getAxis() {
    return _axis;
  }

  @Override
  public void reduceKernelPattern() {
    _isEpsilon = true;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder("stepPattern(\"");
    buffer.append(_axis.getName()).append("\", ")
        .append(_isEpsilon ? "epsilon{" + Integer.toString(_nodeType) + "}" : Integer.toString(_nodeType));
    if (_predicates != null) {
      buffer.append(", ").append(_predicates.toString());
    }
    return buffer.append(')').toString();
  }

  private int analyzeCases() {
    boolean noContext = true;
    final int n = _predicates.size();

    for (int i = 0; i < n && noContext; i++) {
      final Predicate pred = _predicates.get(i);
      if (pred.isNthPositionFilter() || pred.hasPositionCall() || pred.hasLastCall()) {
        noContext = false;
      }
    }

    if (noContext)
      return NO_CONTEXT;
    else if (n == 1)
      return SIMPLE_CONTEXT;
    return GENERAL_CONTEXT;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (hasPredicates()) {
      // Type check all the predicates (e -> position() = e)
      for (final Predicate pred : _predicates) {
        pred.typeCheck(stable);
      }

      // Analyze context cases
      _contextCase = analyzeCases();

      Step step = null;

      // Create an instance of Step to do the translation
      if (_contextCase == SIMPLE_CONTEXT) {
        final Predicate pred = _predicates.get(0);
        if (pred.isNthPositionFilter()) {
          _contextCase = GENERAL_CONTEXT;
          step = new Step(_axis, _nodeType, _predicates);
        } else {
          step = new Step(_axis, _nodeType, null);
        }
      } else if (_contextCase == GENERAL_CONTEXT) {
        for (final Predicate predicate : _predicates) {
          predicate.dontOptimize();
        }

        step = new Step(_axis, _nodeType, _predicates);
      }

      if (step != null) {
        step.setParser(getParser());
        step.typeCheck(stable);
        _step = step;
      }
    }
    return _axis == Axis.CHILD ? Type.Element : Type.Attribute;
  }

  private JExpression compileKernel(CompilerContext ctx) {
    if (_nodeType == DTM.ELEMENT_NODE) {
      return invoke(ctx.currentDom(), IS_ELEMENT).arg(ctx.currentNode());
    } else if (_nodeType == DTM.ATTRIBUTE_NODE) {
      return invoke(ctx.currentDom(), IS_ATTRIBUTE).arg(ctx.currentNode());
    } else {
      return invoke(ctx.currentDom(), GET_EXPANDED_TYPE_ID).arg(ctx.currentNode()).eq(lit(_nodeType));
    }
  }

  private JExpression compileNoContext(CompilerContext ctx) {
    // If pattern not reduced then check kernel
    JExpression result = null;
    if (!_isEpsilon) {
      result = compileKernel(ctx);
    }

    // Compile the expressions within the predicates
    for (final Predicate pred : _predicates) {
      final Expression exp = pred.getExpr();
      if (result == null) {
        result = exp.toJExpression(ctx);
      } else {
        result = result.cand(exp.toJExpression(ctx));
      }
    }
    return result;
  }

  private JExpression compileSimpleContext(CompilerContext ctx) {
    // If pattern not reduced then check kernel
    JExpression kernel = null;
    if (!_isEpsilon) {
      kernel = compileKernel(ctx);
    }

    JExpression step = _step.toJExpression(ctx);

    // Create a new matching iterator using the matching node
    JExpression match = JExpr._new(ctx.ref(MatchingIterator.class)).arg(ctx.currentNode()).arg(step);

    // Get the parent of the matching node
    JExpression parent = ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode());

    // Start the iterator with the parent
    match = match.invoke(SET_START_NODE).arg(parent);

    // Overwrite current iterator and current node
    ctx.currentBlock().decl(ctx.ref(DTMAxisIterator.class), ctx.nextTmpIterator(), match);

    // Translate the expression of the predicate
    final Predicate pred = _predicates.get(0);
    final Expression exp = pred.getExpr();
    return kernel == null ? exp.toJExpression(ctx) : kernel.cand(exp.toJExpression(ctx));
  }

  private JExpression compileGeneralContext(CompilerContext ctx) {
    // Add a new private field if this is the main class
    // if (!classGen.isExternal()) {
    // final Field iterator = new Field(ACC_PRIVATE, cpg.addUtf8(iteratorName),
    // cpg.addUtf8(NODE_ITERATOR_SIG), null,
    // cpg.getConstantPool());
    // classGen.addField(iterator);
    // iteratorIndex = cpg.addFieldref(classGen.getClassName(), iteratorName,
    // NODE_ITERATOR_SIG);
    //
    // il.append(classGen.loadTranslet());
    // il.append(new GETFIELD(iteratorIndex));
    // il.append(DUP);
    // iter.setStart(il.append(new ASTORE(iter.getIndex())));
    // ifBlock = il.append(new IFNONNULL(null));
    // il.append(classGen.loadTranslet());
    // }

    // Compile the step created at type checking time
    JExpression step = _step.toJExpression(ctx);

    // If in the main class update the field too
    // if (!classGen.isExternal()) {
    // il.append(new ALOAD(iter.getIndex()));
    // il.append(new PUTFIELD(iteratorIndex));
    // ifBlock.setTarget(il.append(NOP));
    // } else {
    // If class is not external, start of range for iter variable was
    // set above
    // iter.setStart(iterStore);
    // }

    // Get the parent of the node on the stack
    JExpression parent = ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode());

    // Initialize the iterator with the parent
    JVar iterator = ctx.currentBlock().decl(ctx.ref(DTMAxisIterator.class), ctx.nextTmpIterator(),
        step.invoke(SET_START_NODE).arg(parent));
    /*
     * Inline loop:
     * 
     * int node2; while ((node2 = iter.next()) != NodeIterator.END && node2 <
     * node); return node2 == node;
     */
    JVar current = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextCurrent(), iterator.invoke(NEXT));
    ctx.currentBlock()._while(current.gte(JExpr.lit(0)).cand(current.lt(ctx.currentNode()))).body()
        .assign(current, iterator.invoke(NEXT));
    return current.eq(ctx.currentNode());
  }

  @Override
  public void compilePattern(CompilerContext ctx, JStatement fail) {
    JConditional _if = ctx.currentBlock()._if(toJExpression(ctx));
    JBlock _then = _if._then();
    _then.add(getTemplate().compile(ctx));
    _then._break();
    if (fail != null)
      _if._else().add(fail);
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (hasPredicates()) {
      switch (_contextCase) {
      case NO_CONTEXT:
        return compileNoContext(ctx);
      case SIMPLE_CONTEXT:
        return compileSimpleContext(ctx);
      default:
        return compileGeneralContext(ctx);
      }
    } else if (isWildcard()) {
      return TRUE;
    } else {
      return compileKernel(ctx);
    }
  }

}
