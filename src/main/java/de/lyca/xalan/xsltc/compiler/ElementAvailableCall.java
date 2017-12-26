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

import static com.sun.codemodel.JExpr.lit;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ElementAvailableCall extends FunctionCall {

  public ElementAvailableCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  /**
   * Force the argument to this function to be a literal string.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (argumentCount() != 1) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(err);
    }
    if (argument() instanceof LiteralExpr)
      return _type = Type.Boolean;
    final ErrorMsg err = new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR, "element-available", this);
    throw new TypeCheckError(err);
  }

  /**
   * Returns an object representing the compile-time evaluation of an
   * expression. We are only using this for function-available and
   * element-available at this time.
   */
  @Override
  public Object evaluateAtCompileTime() {
    return getResult() ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Returns the result that this function will return
   */
  public boolean getResult() {
    try {
      final LiteralExpr arg = (LiteralExpr) argument();
      final String qname = arg.getValue();
      final int index = qname.indexOf(':');
      final String localName = index > 0 ? qname.substring(index + 1) : qname;
      return getParser().elementSupported(arg.getNamespace(), localName);
    } catch (final ClassCastException e) {
      return false;
    }
  }

  /**
   * Calls to 'element-available' are resolved at compile time since the
   * namespaces declared in the stylsheet are not available at run time.
   * Consequently, arguments to this function must be literals.
   */
  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    return lit(getResult());
  }

}
