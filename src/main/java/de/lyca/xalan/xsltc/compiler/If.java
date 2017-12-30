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

import static com.sun.codemodel.JExpr.FALSE;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class If extends Instruction {

  private Expression _test;
  private boolean _ignore = false;

  /**
   * Parse the "test" expression and contents of this element.
   */
  @Override
  public void parseContents(Parser parser) {
    // Parse the "test" expression
    _test = parser.parseExpression(this, "test", null);

    // Make sure required attribute(s) have been set
    if (_test.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "test");
      return;
    }

    // Ignore xsl:if when test is false (function-available() and
    // element-available())
    final Object result = _test.evaluateAtCompileTime();
    if (result instanceof Boolean) {
      _ignore = !(Boolean) result;
    }

    parseChildren(parser);
  }

  /**
   * Type-check the "test" expression and contents of this element. The contents
   * will be ignored if we know the test will always fail.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // Type-check the "test" expression
    if (_test.typeCheck(stable) instanceof BooleanType == false) {
      _test = new CastExpr(_test, Type.Boolean);
    }
    // Type check the element contents
    if (!_ignore) {
      typeCheckContents(stable);
    }
    return Type.Void;
  }

  /**
   * Translate the "test" expression and contents of this element. The contents
   * will be ignored if we know the test will always fail.
   */
  @Override
  public void translate(CompilerContext ctx) {
    JExpression testExp = _test.toJExpression(ctx);
    if (!_ignore && testExp != FALSE) {
      ctx.pushBlock(ctx.currentBlock()._if(testExp)._then());
      translateContents(ctx);
      ctx.popBlock();
    }
  }

}
