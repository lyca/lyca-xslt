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

import static de.lyca.xalan.xsltc.DOM.GET_ITERATOR;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.AbsoluteIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class AbsoluteLocationPath extends Expression {
  private Expression _path; // may be null

  public AbsoluteLocationPath() {
    _path = null;
  }

  public AbsoluteLocationPath(Expression path) {
    _path = path;
    if (path != null) {
      _path.setParent(this);
    }
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_path != null) {
      _path.setParser(parser);
    }
  }

  public Expression getPath() {
    return _path;
  }

  @Override
  public String toString() {
    return "AbsoluteLocationPath(" + (_path != null ? _path.toString() : "null") + ')';
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_path != null) {
      final Type ptype = _path.typeCheck(stable);
      if (ptype instanceof NodeType) { // promote to node-set
        _path = new CastExpr(_path, Type.NodeSet);
      }
    }
    return _type = Type.NodeSet;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (_path != null) {
      // Compile relative path iterator(s)
      JExpression path = _path.toJExpression(ctx);
      // Create and initialize AbsoluteIterator with the path iterator
      return JExpr._new(ctx.ref(AbsoluteIterator.class)).arg(path);
    } else {
      return ctx.currentDom().invoke(GET_ITERATOR);
    }
  }

}
