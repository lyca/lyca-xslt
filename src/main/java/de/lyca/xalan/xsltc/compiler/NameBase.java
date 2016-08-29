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

import static de.lyca.xml.dtm.DTMAxisIterator.NEXT;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
class NameBase extends FunctionCall {

  private Expression _param = null;
  private Type _paramType = Type.Node;

  /**
   * Handles calls with no parameter (current node is implicit parameter).
   */
  public NameBase(QName fname) {
    super(fname);
  }

  /**
   * Handles calls with one parameter (either node or node-set).
   */
  public NameBase(QName fname, List<Expression> arguments) {
    super(fname, arguments);
    _param = argument(0);
  }

  /**
   * Check that we either have no parameters or one parameter that is either a
   * node or a node-set.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {

    // Check the argument type (if any)
    switch (argumentCount()) {
      case 0:
        _paramType = Type.Node;
        break;
      case 1:
        _paramType = _param.typeCheck(stable);
        break;
      default:
        throw new TypeCheckError(this);
    }

    // The argument has to be a node, a node-set or a node reference
    if (_paramType != Type.NodeSet && _paramType != Type.Node && _paramType != Type.Reference)
      throw new TypeCheckError(this);

    return _type = Type.String;
  }

  @Override
  public Type getType() {
    return _type;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    // Function was called with no parameters
    JExpression expr;
    if (argumentCount() == 0) {
      expr = ctx.currentNode();
    }
    // Function was called with node parameter
    else if (_paramType == Type.Node) {
      expr = _param.toJExpression(ctx);
    } else if (_paramType == Type.Reference) {
      expr = ctx.ref(BasisLibrary.class).staticInvoke("referenceToNodeSet").arg(_param.toJExpression(ctx)).invoke(NEXT);
    }
    // Function was called with node-set parameter
    else {
      expr = _param.startIterator(ctx, _param.toJExpression(ctx)).invoke(NEXT);
    }
    return expr;
  }

}
