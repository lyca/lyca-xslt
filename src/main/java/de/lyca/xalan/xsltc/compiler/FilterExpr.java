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

import com.sun.codemodel.JExpression;

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

  /**
   * Translate a filter expression by pushing the appropriate iterator onto the
   * stack.
   */
  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (_predicates.size() > 0) {
      return compilePredicates(ctx);
    } else {
      return _primary.toJExpression(ctx);
    }
  }

  /**
   * Translate a sequence of predicates. Each predicate is translated by
   * constructing an instance of <code>CurrentNodeListIterator</code> which is
   * initialized from another iterator (recursive call), a filter and a closure
   * (call to translate on the predicate) and "this".
   */
  public JExpression compilePredicates(CompilerContext ctx) {
    // If not predicates left, translate primary expression
    if (_predicates.size() == 0) {
      return toJExpression(ctx);
    } else {
      // Remove the next predicate to be translated
      final Predicate predicate = (Predicate) _predicates.get(_predicates.size() - 1);
      _predicates.remove(predicate);

      // Translate the rest of the predicates from right to left
      JExpression predicateExpressions = compilePredicates(ctx);
      JExpression predicateExpression = predicate.toJExpression(ctx);

      if (predicate.isNthPositionFilter()) {
        return _new(ctx.ref(NthIterator.class)).arg(predicateExpressions).arg(predicateExpression);
      } else {
        return _new(ctx.ref(CurrentNodeListIterator.class)).arg(predicateExpressions).arg(TRUE)
            .arg(predicateExpression).arg(ctx.currentNode()).arg(_this());
      }
    }
  }

}
