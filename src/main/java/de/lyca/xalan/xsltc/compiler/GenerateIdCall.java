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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class GenerateIdCall extends FunctionCall {
  public GenerateIdCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method) {
 // FIXME
//    final InstructionList il = methodGen.getInstructionList();
//    if (argumentCount() == 0) {
//      il.append(methodGen.loadContextNode());
//    } else { // one argument
//      argument().translate(definedClass, method);
//    }
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    il.append(new INVOKESTATIC(cpg.addMethodref(BASIS_LIBRARY_CLASS, "generate_idF",
//    // reuse signature
//            GET_NODE_NAME_SIG)));
  }
}
