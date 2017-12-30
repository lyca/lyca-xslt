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

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class When extends Instruction {

  private Expression _test;
  private boolean _ignore = false;

  public Expression getTest() {
    return _test;
  }

  public boolean ignore() {
    return _ignore;
  }

  @Override
  public void parseContents(Parser parser) {
    _test = parser.parseExpression(this, "test", null);

    // Ignore xsl:if when test is false (function-available() and
    // element-available())
    final Object result = _test.evaluateAtCompileTime();
    if (result instanceof Boolean) {
      _ignore = !(Boolean) result;
    }

    parseChildren(parser);

    // Make sure required attribute(s) have been set
    if (_test.isDummy()) {
      reportError(this, parser, Messages.get().requiredAttrErr("test"));
    }
  }

  /**
   * Type-check this when element. The test should always be type checked, while
   * we do not bother with the contents if we know the test fails. This is
   * important in cases where the "test" expression tests for the support of a
   * non-available element, and the <xsl:when> body contains this non-available
   * element.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // Type-check the test expression
    if (_test.typeCheck(stable) instanceof BooleanType == false) {
      _test = new CastExpr(_test, Type.Boolean);
    }
    // Type-check the contents (if necessary)
    if (!_ignore) {
      typeCheckContents(stable);
    }

    return Type.Void;
  }

  /**
   * This method should never be called. An Otherwise object will explicitly
   * translate the "test" expression and and contents of this element.
   */
  @Override
  public void translate(CompilerContext ctx) {
    final ErrorMsg msg = new ErrorMsg(this, Messages.get().strayWhenErr());
    getParser().reportError(Constants.ERROR, msg);
  }
}
