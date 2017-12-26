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

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.ref;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xalan.xsltc.runtime.StringValueHandler;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ProcessingInstruction extends Instruction {

  private AttributeValue _name; // name treated as AVT (7.1.3)
  private boolean _isLiteral = false; // specified name is not AVT

  @Override
  public void parseContents(Parser parser) {
    final String name = getAttribute("name");

    if (name.length() > 0) {
      _isLiteral = Util.isLiteral(name);
      if (_isLiteral) {
        if (!XML11Char.isXML11ValidNCName(name)) {
          final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_NCNAME_ERR, name, this);
          parser.reportError(Constants.ERROR, err);
        }
      }
      _name = AttributeValue.create(this, name, parser);
    } else {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
    }

    if (name.equals("xml")) {
      reportError(this, parser, ErrorMsg.ILLEGAL_PI_ERR, "xml");
    }
    parseChildren(parser);

    for (SyntaxTreeNode child : getContents()) {
      if (child instanceof XslElement) {
        // TODO
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INTERNAL_ERR, "xsl:processing-instruction cannot contain xsl:element", this);
        parser.reportError(Constants.ERROR, err);
      }
    }

  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _name.typeCheck(stable);
    typeCheckContents(stable);
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    JExpression nameValue;
    if (!_isLiteral) {
      // if the ncname is an AVT, then the ncname has to be checked at runtime
      // if it is a valid ncname
      nameValue = _name.toJExpression(ctx);

      // store the name into a variable first so _name.translate only needs to
      // be called once
      // nameValue.setStart(il.append(new ASTORE(nameValue.getIndex())));
      // il.append(new ALOAD(nameValue.getIndex()));

      // call checkNCName if the name is an AVT
      // final int check = cpg.addMethodref(BASIS_LIBRARY_CLASS, "checkNCName",
      // "(" + STRING_SIG + ")V");
      // il.append(new INVOKESTATIC(check));

      // Save the current handler base on the stack
      // il.append(methodGen.loadHandler());
      // il.append(DUP); // first arg to "attributes" call

      // load name value again
      // nameValue.setEnd(il.append(new ALOAD(nameValue.getIndex())));
    } else {
      // Save the current handler base on the stack
      // il.append(methodGen.loadHandler());
      // il.append(DUP); // first arg to "attributes" call

      // Push attribute name
      nameValue = _name.toJExpression(ctx);// 2nd arg

    }

    JExpression handler = ctx.currentHandler();

    ctx.pushHandler(ref("stringValueHandler"));
    // translate contents with substituted handler
    translateContents(ctx);

    // get String out of the handler
    JClass stringValueHandler = ctx.ref(StringValueHandler.class);
    JExpression valOfPI = ((JExpression) cast(stringValueHandler, ctx.popHandler())).invoke("getValueOfPI");
    // call "processingInstruction"
    ctx.currentBlock().invoke(handler, "processingInstruction").arg(nameValue).arg(valOfPI);
  }
}
