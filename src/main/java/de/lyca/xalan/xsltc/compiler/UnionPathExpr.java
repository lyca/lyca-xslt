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

import static de.lyca.xalan.xsltc.compiler.Constants.ADD_ITERATOR;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.UnionIterator;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class UnionPathExpr extends Expression {

  private final Expression _pathExpr;
  private final Expression _rest;
  private boolean _reverse = false;

  // linearization for top level UnionPathExprs
  private Expression[] _components;

  public UnionPathExpr(Expression pathExpr, Expression rest) {
    _pathExpr = pathExpr;
    _rest = rest;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    // find all expressions in this Union
    final List<Expression> components = new ArrayList<>();
    flatten(components);
    final int size = components.size();
    _components = components.toArray(new Expression[size]);
    for (int i = 0; i < size; i++) {
      _components[i].setParser(parser);
      _components[i].setParent(this);
      if (_components[i] instanceof Step) {
        final Step step = (Step) _components[i];
        final Axis axis = step.getAxis();
        final int type = step.getNodeType();
        // Put attribute iterators first
        if (axis == Axis.ATTRIBUTE || type == DTM.ATTRIBUTE_NODE) {
          _components[i] = _components[0];
          _components[0] = step;
        }
        // Check if the union contains a reverse iterator
        if (axis.isReverse()) {
          _reverse = true;
        }
      }
    }
    // No need to reverse anything if another expression lies on top of this
    if (getParent() instanceof Expression) {
      _reverse = false;
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final int length = _components.length;
    for (int i = 0; i < length; i++) {
      if (_components[i].typeCheck(stable) != Type.NodeSet) {
        _components[i] = new CastExpr(_components[i], Type.NodeSet);
      }
    }
    return _type = Type.NodeSet;
  }

  @Override
  public String toString() {
    return "union(" + _pathExpr + ", " + _rest + ')';
  }

  private void flatten(List<Expression> components) {
    components.add(_pathExpr);
    if (_rest != null) {
      if (_rest instanceof UnionPathExpr) {
        ((UnionPathExpr) _rest).flatten(components);
      } else {
        components.add(_rest);
      }
    }
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    // Create the UnionIterator and leave it on the stack
    JExpression unionIterator = JExpr._new(ctx.ref(UnionIterator.class)).arg(ctx.currentDom());

    // Add the various iterators to the UnionIterator
    for (final Expression expression : _components) {
      unionIterator = unionIterator.invoke(ADD_ITERATOR).arg(expression.toJExpression(ctx));
    }

    // Order the iterator only if strictly needed
    if (_reverse) {
      // FIXME
//      final int order = cpg.addInterfaceMethodref(DOM_INTF, ORDER_ITERATOR, ORDER_ITERATOR_SIG);
//      il.append(methodGen.loadDOM());
//      il.append(SWAP);
//      il.append(methodGen.loadContextNode());
//      il.append(new INVOKEINTERFACE(order, 3));
    }
    return unionIterator;
  }

  @Override
  public void translate(CompilerContext ctx) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    final int init = cpg.addMethodref(UNION_ITERATOR_CLASS, "<init>", "(" + DOM_INTF_SIG + ")V");
//    final int iter = cpg.addMethodref(UNION_ITERATOR_CLASS, ADD_ITERATOR, ADD_ITERATOR_SIG);
//
//    // Create the UnionIterator and leave it on the stack
//    il.append(new NEW(cpg.addClass(UNION_ITERATOR_CLASS)));
//    il.append(DUP);
//    il.append(methodGen.loadDOM());
//    il.append(new INVOKESPECIAL(init));
//
//    // Add the various iterators to the UnionIterator
//    for (final Expression expression : _components) {
//      expression.translate(classGen, methodGen);
//      il.append(new INVOKEVIRTUAL(iter));
//    }
//
//    // Order the iterator only if strictly needed
//    if (_reverse) {
//      final int order = cpg.addInterfaceMethodref(DOM_INTF, ORDER_ITERATOR, ORDER_ITERATOR_SIG);
//      il.append(methodGen.loadDOM());
//      il.append(SWAP);
//      il.append(methodGen.loadContextNode());
//      il.append(new INVOKEINTERFACE(order, 3));
//
//    }
  }
}
