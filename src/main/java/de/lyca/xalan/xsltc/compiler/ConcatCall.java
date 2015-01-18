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

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.compiler.Constants.EMPTYSTRING;

import java.util.List;
import java.util.ListIterator;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ConcatCall extends FunctionCall {
  public ConcatCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    ListIterator<Expression> expressions = getArguments().listIterator();
    while (expressions.hasNext()) {
      Expression exp = expressions.next();
      if (!exp.typeCheck(stable).identicalTo(Type.String)) {
        expressions.set(new CastExpr(exp, Type.String));
      }
    }
    return _type = Type.String;
  }

  @Override
  public JExpression compile(CompilerContext ctx) {
    final int nArgs = argumentCount();

    switch (nArgs) {
    case 0:
      return lit(EMPTYSTRING);
    case 1:
      return argument().compile(ctx);
    default:
      JExpression stringBuilder = _new(ctx.ref(StringBuilder.class));
      for (int i = 0; i < nArgs; i++) {
        stringBuilder = stringBuilder.invoke("append").arg(argument(i).compile(ctx));
      }
      return stringBuilder.invoke("toString");
    }
  }
  
  /** translate leaves a String on the stack */
  @Override
  public void translate(CompilerContext ctx) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final int nArgs = argumentCount();
//
//    switch (nArgs) {
//      case 0:
//        il.append(new PUSH(cpg, EMPTYSTRING));
//        break;
//
//      case 1:
//        argument().translate(classGen, methodGen);
//        break;
//
//      default:
//        final int initBuffer = cpg.addMethodref(STRING_BUILDER_CLASS, "<init>", "()V");
//        final Instruction append = new INVOKEVIRTUAL(cpg.addMethodref(STRING_BUILDER_CLASS, "append", "(" + STRING_SIG
//                + ")" + STRING_BUILDER_SIG));
//
//        final int toString = cpg.addMethodref(STRING_BUILDER_CLASS, "toString", "()" + STRING_SIG);
//
//        il.append(new NEW(cpg.addClass(STRING_BUILDER_CLASS)));
//        il.append(DUP);
//        il.append(new INVOKESPECIAL(initBuffer));
//        for (int i = 0; i < nArgs; i++) {
//          argument(i).translate(classGen, methodGen);
//          il.append(append);
//        }
//        il.append(new INVOKEVIRTUAL(toString));
//    }
  }
}
