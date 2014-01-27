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

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class When extends Instruction {

  private Expression _test;
  private boolean _ignore = false;

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("When");
    indent(indent + IndentIncrement);
    System.out.print("test ");
    Util.println(_test.toString());
    displayContents(indent + IndentIncrement);
  }

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
    if (result != null && result instanceof Boolean) {
      _ignore = !((Boolean) result).booleanValue();
    }

    parseChildren(parser);

    // Make sure required attribute(s) have been set
    if (_test.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "test");
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
  public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
    final ErrorMsg msg = new ErrorMsg(ErrorMsg.STRAY_WHEN_ERR, this);
    getParser().reportError(Constants.ERROR, msg);
  }
}
