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
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Morten Jorgensen
 */
final class LocalNameCall extends NameBase {

  /**
   * Handles calls with no parameter (current node is implicit parameter).
   */
  public LocalNameCall(QName fname) {
    super(fname);
  }

  /**
   * Handles calls with one parameter (either node or node-set).
   */
  public LocalNameCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public JExpression compile(CompilerContext ctx) {
    // Returns the local name of a node in the DOM
    return ctx.ref(BasisLibrary.class).staticInvoke("getLocalName")
        .arg(ctx.currentDom().invoke("getNodeName").arg(super.compile(ctx)));
  }

  /**
   * This method is called when the constructor is compiled in
   * Stylesheet.compileConstructor() and not as the syntax tree is traversed.
   */
  @Override
  public void translate(CompilerContext ctx) {
 // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // Returns the name of a node in the DOM
//    final int getNodeName = cpg.addInterfaceMethodref(DOM_INTF, "getNodeName", "(I)" + STRING_SIG);
//
//    final int getLocalName = cpg.addMethodref(BASIS_LIBRARY_CLASS, "getLocalName", "(Ljava/lang/String;)"
//            + "Ljava/lang/String;");
//    super.translate(classGen, methodGen);
//    il.append(new INVOKEINTERFACE(getNodeName, 2));
//    il.append(new INVOKESTATIC(getLocalName));
  }
}
