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
 * @author Morten Jorgensen
 */
final class ContainsCall extends FunctionCall {

  private Expression _base = null;
  private Expression _token = null;

  /**
   * Create a contains() call - two arguments, both strings
   */
  public ContainsCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  /**
   * This XPath function returns true/false values
   */
  public boolean isBoolean() {
    return true;
  }

  /**
   * Type check the two parameters for this function
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {

    // Check that the function was passed exactly two arguments
    if (argumentCount() != 2)
      throw new TypeCheckError(ErrorMsg.ILLEGAL_ARG_ERR, getName(), this);

    // The first argument must be a String, or cast to a String
    _base = argument(0);
    final Type baseType = _base.typeCheck(stable);
    if (baseType != Type.String) {
      _base = new CastExpr(_base, Type.String);
    }

    // The second argument must also be a String, or cast to a String
    _token = argument(1);
    final Type tokenType = _token.typeCheck(stable);
    if (tokenType != Type.String) {
      _token = new CastExpr(_token, Type.String);
    }

    return _type = Type.Boolean;
  }

  @Override
  public JExpression compile(CompilerContext ctx) {
    JExpression base = _base.compile(ctx);
    JExpression token = _token.compile(ctx);
    return base.invoke("indexOf").arg(token).gte(lit(0));
  }

  /**
   * Compile the expression - leave boolean expression on stack
   */
  @Override
  public void translate(CompilerContext ctx) {
    // FIXME
//    translateDesynthesized(classGen, methodGen);
//    synthesize(classGen, methodGen);
  }

  /**
   * Compile expression and update true/false-lists
   */
  @Override
  public void translateDesynthesized(CompilerContext ctx) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    _base.translate(classGen, methodGen);
//    _token.translate(classGen, methodGen);
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_CLASS, "indexOf", "(" + STRING_SIG + ")I")));
//    _falseList.add(il.append(new IFLT(null)));
  }
}
