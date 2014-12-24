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
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._this;

import java.util.List;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.CurrentNodeListIterator;
import de.lyca.xalan.xsltc.dom.NthIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
class FilterExpr extends Expression {

  /**
   * Primary expression of this filter. I.e., 'e' in '(e)[p1]...[pn]'.
   */
  private Expression _primary;

  /**
   * Array of predicates in '(e)[p1]...[pn]'.
   */
  private final List<Expression> _predicates;

  public FilterExpr(Expression primary, List<Expression> predicates) {
    _primary = primary;
    _predicates = predicates;
    primary.setParent(this);
  }

  protected Expression getExpr() {
    if (_primary instanceof CastExpr)
      return ((CastExpr) _primary).getExpr();
    else
      return _primary;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _primary.setParser(parser);
    if (_predicates != null) {
      for (Expression exp : _predicates) {
        exp.setParser(parser);
        exp.setParent(this);
      }
    }
  }

  @Override
  public String toString() {
    return "filter-expr(" + _primary + ", " + _predicates + ")";
  }

  /**
   * Type check a FilterParentPath. If the filter is not a node-set add a cast
   * to node-set only if it is of reference type. This type coercion is needed
   * for expressions like $x where $x is a parameter reference. All
   * optimizations are turned off before type checking underlying predicates.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type ptype = _primary.typeCheck(stable);
    final boolean canOptimize = _primary instanceof KeyCall;

    if (ptype instanceof NodeSetType == false) {
      if (ptype instanceof ReferenceType) {
        _primary = new CastExpr(_primary, Type.NodeSet);
      } else
        throw new TypeCheckError(this);
    }

    // Type check predicates and turn all optimizations off if appropriate
    for (Expression exp : _predicates) {
      final Predicate pred = (Predicate) exp;
      if (!canOptimize) {
        pred.dontOptimize();
      }
      pred.typeCheck(stable);
    }
    return _type = Type.NodeSet;
  }

  @Override
  public JExpression compile(CompilerContext ctx) {
    if (_predicates.size() > 0) {
      return compilePredicates(ctx);
    } else {
      return _primary.compile(ctx);
    }
  }

  /**
   * Translate a filter expression by pushing the appropriate iterator onto the
   * stack.
   */
  @Override
  public void translate(CompilerContext ctx) {
    if (_predicates.size() > 0) {
      translatePredicates(ctx.clazz(), ctx.currentMethod());
    } else {
      _primary.translate(ctx);
    }
  }

  public JExpression compilePredicates(CompilerContext ctx) {
    // If not predicates left, translate primary expression
    if (_predicates.size() == 0) {
      return compile(ctx);
    } else {
      // Remove the next predicate to be translated
      final Predicate predicate = (Predicate) _predicates.get(_predicates.size() - 1);
      _predicates.remove(predicate);

      // Translate the rest of the predicates from right to left
      JExpression predicateExpressions = compilePredicates(ctx);
      JExpression predicateExpression = predicate.compile(ctx);

      if (predicate.isNthPositionFilter()) {
        // public NthIterator(DTMAxisIterator, int)
        return _new(ctx.ref(NthIterator.class)).arg(predicateExpressions).arg(predicateExpression);
      } else {
        // public CurrentNodeListIterator(DTMAxisIterator, boolean,
        // CurrentNodeListFilter, int, AbstractTranslet)
        return _new(ctx.ref(CurrentNodeListIterator.class)).arg(predicateExpressions).arg(TRUE)
            .arg(predicateExpression).arg(ctx.currentNode()).arg(_this());
      }
    }
  }

  /**
   * Translate a sequence of predicates. Each predicate is translated by
   * constructing an instance of <code>CurrentNodeListIterator</code> which is
   * initialized from another iterator (recursive call), a filter and a closure
   * (call to translate on the predicate) and "this".
   */
  public void translatePredicates(JDefinedClass definedClass, JMethod method) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // If not predicates left, translate primary expression
//    if (_predicates.size() == 0) {
//      translate(definedClass, method);
//    } else {
//      // Remove the next predicate to be translated
//      final Predicate predicate = (Predicate) _predicates.get(_predicates.size() - 1);
//      _predicates.remove(predicate);
//
//      // Translate the rest of the predicates from right to left
//      translatePredicates(definedClass, method);
//
//      if (predicate.isNthPositionFilter()) {
//        final int nthIteratorIdx = cpg.addMethodref(NTH_ITERATOR_CLASS, "<init>", "(" + NODE_ITERATOR_SIG + "I)V");
//
//        // Backwards branches are prohibited if an uninitialized object
//        // is on the stack by section 4.9.4 of the JVM Specification,
//        // 2nd Ed. We don't know whether this code might contain
//        // backwards branches, so we mustn't create the new object unti
//
//        // after we've created the suspect arguments to its constructor
//
//        // Instead we calculate the values of the arguments to the
//        // constructor first, store them in temporary variables, create
//        // the object and reload the arguments from the temporaries to
//        // avoid the problem.
//        final LocalVariableGen iteratorTemp = methodGen.addLocalVariable("filter_expr_tmp1",
//                Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
//        iteratorTemp.setStart(il.append(new ASTORE(iteratorTemp.getIndex())));
//
//        predicate.translate(definedClass, method);
//        final LocalVariableGen predicateValueTemp = methodGen.addLocalVariable("filter_expr_tmp2",
//                Util.getJCRefType("I"), null, null);
//        predicateValueTemp.setStart(il.append(new ISTORE(predicateValueTemp.getIndex())));
//
//        il.append(new NEW(cpg.addClass(NTH_ITERATOR_CLASS)));
//        il.append(DUP);
//        iteratorTemp.setEnd(il.append(new ALOAD(iteratorTemp.getIndex())));
//        predicateValueTemp.setEnd(il.append(new ILOAD(predicateValueTemp.getIndex())));
//        il.append(new INVOKESPECIAL(nthIteratorIdx));
//      } else {
//        // Translate predicates from right to left
//        final int initCNLI = cpg.addMethodref(CURRENT_NODE_LIST_ITERATOR, "<init>", "(" + NODE_ITERATOR_SIG + "Z"
//                + CURRENT_NODE_LIST_FILTER_SIG + NODE_SIG + TRANSLET_SIG + ")V");
//
//        // Backwards branches are prohibited if an uninitialized object
//        // is on the stack by section 4.9.4 of the JVM Specification,
//        // 2nd Ed. We don't know whether this code might contain
//        // backwards branches, so we mustn't create the new object
//        // until after we've created the suspect arguments to its
//        // constructor. Instead we calculate the values of the
//        // arguments to the constructor first, store them in temporary
//        // variables, create the object and reload the arguments from
//        // the temporaries to avoid the problem.
//
//        final LocalVariableGen nodeIteratorTemp = methodGen.addLocalVariable("filter_expr_tmp1",
//                Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
//        nodeIteratorTemp.setStart(il.append(new ASTORE(nodeIteratorTemp.getIndex())));
//
//        predicate.translate(classGen, methodGen);
//        final LocalVariableGen filterTemp = methodGen.addLocalVariable("filter_expr_tmp2",
//                Util.getJCRefType(CURRENT_NODE_LIST_FILTER_SIG), null, null);
//        filterTemp.setStart(il.append(new ASTORE(filterTemp.getIndex())));
//
//        // Create a CurrentNodeListIterator
//        il.append(new NEW(cpg.addClass(CURRENT_NODE_LIST_ITERATOR)));
//        il.append(DUP);
//
//        // Initialize CurrentNodeListIterator
//        nodeIteratorTemp.setEnd(il.append(new ALOAD(nodeIteratorTemp.getIndex())));
//        il.append(ICONST_1);
//        filterTemp.setEnd(il.append(new ALOAD(filterTemp.getIndex())));
//        il.append(methodGen.loadCurrentNode());
//        il.append(classGen.loadTranslet());
//        il.append(new INVOKESPECIAL(initCNLI));
//      }
//    }
  }
}
