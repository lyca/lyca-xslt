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

import static com.sun.codemodel.JExpr._new;
import static de.lyca.xalan.xsltc.DOM.ORDER_NODES;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.StepIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class FilterParentPath extends Expression {

  private Expression _filterExpr;
  private Expression _path;
  private boolean _hasDescendantAxis = false;

  public FilterParentPath(Expression filterExpr, Expression path) {
    (_path = path).setParent(this);
    (_filterExpr = filterExpr).setParent(this);
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _filterExpr.setParser(parser);
    _path.setParser(parser);
  }

  @Override
  public String toString() {
    return "FilterParentPath(" + _filterExpr + ", " + _path + ')';
  }

  public void setDescendantAxis() {
    _hasDescendantAxis = true;
  }

  /**
   * Type check a FilterParentPath. If the filter is not a node-set add a cast
   * to node-set only if it is of reference type. This type coercion is needed
   * for expressions like $x/LINE where $x is a parameter reference.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type ftype = _filterExpr.typeCheck(stable);
    if (ftype instanceof NodeSetType == false) {
      if (ftype instanceof ReferenceType) {
        _filterExpr = new CastExpr(_filterExpr, Type.NodeSet);
      }
      /*
       * else if (ftype instanceof ResultTreeType) { _filterExpr = new
       * CastExpr(_filterExpr, Type.NodeSet); }
       */
      else if (ftype instanceof NodeType) {
        _filterExpr = new CastExpr(_filterExpr, Type.NodeSet);
      } else
        throw new TypeCheckError(this);
    }

    // Wrap single node path in a node set
    final Type ptype = _path.typeCheck(stable);
    if (!(ptype instanceof NodeSetType)) {
      _path = new CastExpr(_path, Type.NodeSet);
    }

    return _type = Type.NodeSet;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    // Recursively compile 2 iterators
    JExpression filter = _filterExpr.toJExpression(ctx);
    JExpression path = _path.toJExpression(ctx);

    // Create new StepIterator
    // Initialize StepIterator with iterators from the stack
    JExpression stepIterator = _new(ctx.ref(StepIterator.class)).arg(filter).arg(path);

    // This is a special case for the //* path with or without predicates
    if (_hasDescendantAxis) {
      stepIterator = stepIterator.invoke("includeSelf");
    }

    final SyntaxTreeNode parent = getParent();

    final boolean parentAlreadyOrdered = parent instanceof RelativeLocationPath || parent instanceof FilterParentPath
        || parent instanceof KeyCall || parent instanceof CurrentCall || parent instanceof DocumentCall;

    if (!parentAlreadyOrdered) {
      return JExpr.invoke(ctx.currentDom(), ORDER_NODES).arg(stepIterator).arg(ctx.currentNode());
    }
    return stepIterator;
  }

}
