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

import static de.lyca.xalan.xsltc.DOM.GET_NODE_NAME;

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
  public JExpression toJExpression(CompilerContext ctx) {
    // Returns the local name of a node in the DOM
    return ctx.ref(BasisLibrary.class).staticInvoke("getLocalName")
        .arg(ctx.currentDom().invoke(GET_NODE_NAME).arg(super.toJExpression(ctx)));
  }

}
