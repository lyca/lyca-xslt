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

import static de.lyca.xalan.xsltc.DOM.GET_NAMESPACE_NAME;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;

/**
 * @author Morten Jorgensen
 */
final class NamespaceUriCall extends NameBase {

  /**
   * Handles calls with no parameter (current node is implicit parameter).
   */
  public NamespaceUriCall(QName fname) {
    super(fname);
  }

  /**
   * Handles calls with one parameter (either node or node-set).
   */
  public NamespaceUriCall(QName fname, List<Expression> arguments) {
    super(fname, arguments);
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    // Returns the string value for a node in the DOM
    return ctx.currentDom().invoke(GET_NAMESPACE_NAME).arg(super.toJExpression(ctx));
  }

}
