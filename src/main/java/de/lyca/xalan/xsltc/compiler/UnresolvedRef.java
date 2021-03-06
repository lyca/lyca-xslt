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

import static com.sun.codemodel.JExpr._null;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Morten Jorgensen
 */
final class UnresolvedRef extends VariableRefBase {

  private QName _variableName = null;
  private VariableRefBase _ref = null;

  public UnresolvedRef(QName name) {
    super();
    _variableName = name;
  }

  public QName getName() {
    return _variableName;
  }

  private ErrorMsg reportError() {
    final ErrorMsg err = new ErrorMsg(this, Messages.get().variableUndefErr(_variableName));
    getParser().reportError(Constants.ERROR, err);
    return err;
  }

  private VariableRefBase resolve(Parser parser, SymbolTable stable) {
    // At this point the AST is already built and we should be able to
    // find any declared global variable or parameter
    VariableBase ref = parser.lookupVariable(_variableName);
    if (ref == null) {
      ref = (VariableBase) stable.lookupName(_variableName);
    }
    if (ref == null) {
      reportError();
      return null;
    }

    // If in a top-level element, create dependency to the referenced var
    _variable = ref;
    addParentDependency();

    if (ref instanceof Variable)
      return new VariableRef((Variable) ref);
    else if (ref instanceof Param)
      return new ParameterRef((Param) ref);
    return null;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_ref != null) {
      final String name = _variableName.toString();
      final ErrorMsg err = new ErrorMsg(this, Messages.get().circularVariableErr(name));
    }
    if ((_ref = resolve(getParser(), stable)) != null)
      return _type = _ref.typeCheck(stable);
    throw new TypeCheckError(reportError());
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (_ref != null) {
      return _ref.toJExpression(ctx);
    } else {
      reportError();
      return _null();
    }
  }

  @Override
  public void translate(CompilerContext ctx) {
    if (_ref != null) {
      _ref.translate(ctx);
    } else {
      reportError();
    }
  }

  @Override
  public String toString() {
    return "unresolved-ref()";
  }

}
