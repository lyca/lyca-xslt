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

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class StringLengthCall extends FunctionCall {
  public StringLengthCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression expr;
    if (argumentCount() > 0) {
      expr = argument().toJExpression(ctx);
    } else {
      expr = Type.Node.compileTo(ctx, ctx.currentNode(), Type.String);
    }
    return expr.invoke("length");
  }

  @Override
  public void translate(CompilerContext ctx) {
//    FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    if (argumentCount() > 0) {
//      argument().translate(classGen, methodGen);
//    } else {
//      il.append(methodGen.loadContextNode());
//      Type.Node.translateTo(classGen, methodGen, Type.String);
//    }
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_CLASS, "length", "()I")));
  }
}
