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

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class AlternativePattern extends Pattern {
  private final Pattern _left;
  private final Pattern _right;

  /**
   * Construct an alternative pattern. The method <code>setParent</code> should
   * not be called in this case.
   */
  public AlternativePattern(Pattern left, Pattern right) {
    _left = left;
    _right = right;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _left.setParser(parser);
    _right.setParser(parser);
  }

  public Pattern getLeft() {
    return _left;
  }

  public Pattern getRight() {
    return _right;
  }

  /**
   * The type of an '|' is not really defined, hence null is returned.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _left.typeCheck(stable);
    _right.typeCheck(stable);
    return null;
  }

  @Override
  public double getPriority() {
    final double left = _left.getPriority();
    final double right = _right.getPriority();

    if (left < right)
      return left;
    else
      return right;
  }

  @Override
  public String toString() {
    return "alternative(" + _left + ", " + _right + ')';
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    return _left.toJExpression(ctx).cor(_right.toJExpression(ctx));
  }

  @Override
  public void translate(CompilerContext ctx) {
  }

}
