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

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.ref;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;

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
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
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
    JVar handler = method.listParams()[2];
    if (rawText != null) {
      if (rawText.canLoadAsArrayOffsetLength()) {
        rawText.loadAsArrayOffsetLength(definedClass, method, body, "comment");
//        final int comment = cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE, "comment", "([CII)V");
//        il.append(new INVOKEINTERFACE(comment, 4));
      } else {
        body.invoke(handler, "comment").arg(rawText.getText());
      }
    } else {
      body.invoke("pushHandler").arg(ref("stringValueHandler"));
      translateContents(definedClass, method, body);
      JClass stringValueHandler = definedClass.owner().ref(StringValueHandler.class);
      body.invoke(handler, "comment").arg(((JExpression)cast(stringValueHandler, invoke("popHandler"))).invoke("getValue"));
      // translate contents with substituted handler
      // FIXME
    }
  }
}
