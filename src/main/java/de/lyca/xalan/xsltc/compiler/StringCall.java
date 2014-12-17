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

import org.apache.bcel.generic.InstructionList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class StringCall extends FunctionCall {
  public StringCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final int argc = argumentCount();
    if (argc > 1) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, this);
      throw new TypeCheckError(err);
    }

    if (argc > 0) {
      argument().typeCheck(stable);
    }
    return _type = Type.String;
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
//    FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    Type targ;
//
//    if (argumentCount() == 0) {
//      il.append(methodGen.loadContextNode());
//      targ = Type.Node;
//    } else {
//      final Expression arg = argument();
//      arg.translate(classGen, methodGen);
//      arg.startIterator(classGen, methodGen);
//      targ = arg.getType();
//    }
//
//    if (!targ.identicalTo(Type.String)) {
//      targ.translateTo(classGen, methodGen, Type.String);
//    }
  }
}
