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

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static de.lyca.xalan.xsltc.compiler.Constants.CHARACTERS;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class ValueOf extends Instruction {
  private Expression _select;
  private boolean _escaping = true;
  private boolean _isString = false;

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("ValueOf");
    indent(indent + IndentIncrement);
    Util.println("select " + _select.toString());
  }

  @Override
  public void parseContents(Parser parser) {
    _select = parser.parseExpression(this, "select", null);

    // make sure required attribute(s) have been set
    if (_select.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "select");
      return;
    }
    final String str = getAttribute("disable-output-escaping");
    if (str != null && str.equals("yes")) {
      _escaping = false;
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type type = _select.typeCheck(stable);

    // Prefer to handle the value as a node; fall back to String, otherwise
    if (type != null && !type.identicalTo(Type.Node)) {
      /***
       *** %HZ% Would like to treat result-tree fragments in the same %HZ% way as
       * node sets for value-of, but that's running into %HZ% some snags.
       * Instead, they'll be converted to String if
       * (type.identicalTo(Type.ResultTree)) { _select = new CastExpr(new
       * CastExpr(_select, Type.NodeSet), Type.Node); } else
       ***/
      if (type.identicalTo(Type.NodeSet)) {
        _select = new CastExpr(_select, Type.Node);
      } else {
        _isString = true;
        if (!type.identicalTo(Type.String)) {
          _select = new CastExpr(_select, Type.String);
        }
        _isString = true;
      }
    }
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    JExpression document =  ctx.currentDom();
    JExpression handler = ctx.currentHandler();
//    JVar handler = method.listParams()[2];
    
//    JFieldRef handler = ref("stringValueHandler");

    // Turn off character escaping if so is wanted.
    if (!_escaping) {
      ctx.currentBlock().add(handler.invoke("setEscaping").arg(FALSE));
    }

    // Translate the contents. If the value is a string, use the
    // translet.characters(String, TranslatOutputHandler) method.
    // Otherwise, the value is a node, and the
    // dom.characters(int node, TransletOutputHandler) method can dispatch
    // the string value of the node to the output handler more efficiently.
    JExpression select = _select.compile(ctx);
    if (_isString) {
      ctx.currentBlock().invoke(CHARACTERS).arg(select).arg(handler);
    } else {
      ctx.currentBlock().invoke(document, CHARACTERS).arg(select).arg(handler);
//      il.append(methodGen.loadDOM());
//      _select.translate(classGen, methodGen);
//      il.append(methodGen.loadHandler());
//      il.append(new INVOKEINTERFACE(characters, 3));
    }

    // Restore character escaping setting to whatever it was.
    if (!_escaping) {
      ctx.currentBlock().add(handler.invoke("setEscaping").arg(TRUE));
    }
  }
}
