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
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.StringValueHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Comment extends Instruction {

  @Override
  public void parseContents(Parser parser) {
    parseChildren(parser);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    typeCheckContents(stable);
    return Type.String;
  }

  @Override
  public void translate(CompilerContext ctx) {
    // Shortcut for literal strings
    Text rawText = null;
    if (elementCount() == 1) {
      final SyntaxTreeNode content = elementAt(0);
      if (content instanceof Text) {
        rawText = (Text) content;
      }
    }

    // If the content is literal text, call comment(char[],int,int) or
    // comment(String), as appropriate. Otherwise, use a
    // StringValueHandler to gather the textual content of the xsl:comment
    // and call comment(String) with the result.
    JExpression handler = ctx.currentHandler();
    if (rawText != null) {
      if (rawText.canLoadAsArrayOffsetLength()) {
        rawText.loadAsArrayOffsetLength(ctx, "comment");
      } else {
        ctx.currentBlock().invoke(ctx.currentHandler(), "comment").arg(rawText.getText());
      }
    } else {
      ctx.pushHandler(ref("stringValueHandler"));
      translateContents(ctx);
      JClass stringValueHandler = ctx.ref(StringValueHandler.class);
      ctx.currentBlock().invoke(handler, "comment").arg(((JExpression)cast(stringValueHandler, ctx.popHandler())).invoke("getValue"));
    }
  }
}
