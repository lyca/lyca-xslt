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

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

import java.io.StringWriter;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.serializer.ToXMLStream;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Message extends Instruction {
  private boolean _terminate = false;

  @Override
  public void parseContents(Parser parser) {
    final String termstr = getAttribute("terminate");
    if (!("".equals(termstr) || "yes".equals(termstr) || "no".equals(termstr))) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.INTERNAL_ERR, "Value for terminate should equal yes or no", this);
      parser.reportError(Constants.ERROR, err);
    }
    _terminate = termstr.equals("yes");
    parseChildren(parser);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    typeCheckContents(stable);
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    // FIXME

    // Load the translet (for call to displayMessage() function)
    // il.append(classGen.loadTranslet());

    JExpression expr = null;
    switch (elementCount()) {
    case 0:
      expr = lit("");
      // il.append(new PUSH(cpg, ""));
      break;
    case 1:
      final SyntaxTreeNode child = elementAt(0);
      if (child instanceof Text) {
        expr = lit(((Text) child).getText());
        // il.append(new PUSH(cpg, ((Text) child).getText()));
        break;
      }
      // falls through
    default:
      // Push current output handler onto the stack
      // il.append(methodGen.loadHandler());

      // Replace the current output handler by a ToXMLStream
      // il.append(new NEW(cpg.addClass(STREAM_XML_OUTPUT)));
      // il.append(methodGen.storeHandler());
      JClass classToXMLStream = ctx.ref(ToXMLStream.class);
      JExpression streamXmlOutput = ctx.currentBlock().decl(classToXMLStream, "messageStream", _new(classToXMLStream));
      ctx.pushHandler(streamXmlOutput);

      // Push a reference to a StringWriter
      // il.append(new NEW(cpg.addClass(STRING_WRITER)));
      // il.append(DUP);
      // il.append(DUP);
      // il.append(new INVOKESPECIAL(cpg.addMethodref(STRING_WRITER, "<init>",
      // "()V")));
      JClass classStringWriter = ctx.ref(StringWriter.class);
      JExpression stringWriter = ctx.currentBlock().decl(classStringWriter, "stringWriter", _new(classStringWriter));

      // Load ToXMLStream
      // il.append(methodGen.loadHandler());
      // il.append(new INVOKESPECIAL(cpg.addMethodref(STREAM_XML_OUTPUT,
      // "<init>", "()V")));

      // Invoke output.setWriter(STRING_WRITER)
      // il.append(methodGen.loadHandler());
      // il.append(SWAP);
      // il.append(new
      // INVOKEINTERFACE(cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "setWriter", "(" + WRITER_SIG
      // + ")V"), 2));
      ctx.currentBlock().invoke(streamXmlOutput, "setWriter").arg(stringWriter);

      // Invoke output.setEncoding("UTF-8")
      // il.append(methodGen.loadHandler());
      // il.append(new PUSH(cpg, "UTF-8")); // other encodings?
      // il.append(new
      // INVOKEINTERFACE(cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "setEncoding", "("
      // + STRING_SIG + ")V"), 2));
      ctx.currentBlock().invoke(streamXmlOutput, "setEncoding").arg(lit("UTF-8"));

      // Invoke output.setOmitXMLDeclaration(true)
      // il.append(methodGen.loadHandler());
      // il.append(ICONST_1);
      // il.append(new
      // INVOKEINTERFACE(cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "setOmitXMLDeclaration",
      // "(Z)V"), 2));
      ctx.currentBlock().invoke(streamXmlOutput, "setOmitXMLDeclaration").arg(TRUE);

      // il.append(methodGen.loadHandler());
      // il.append(new
      // INVOKEINTERFACE(cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "startDocument", "()V"), 1));
      ctx.currentBlock().invoke(streamXmlOutput, "startDocument");

      // Inline translation of contents
      translateContents(ctx);

      // il.append(methodGen.loadHandler());
      // il.append(new
      // INVOKEINTERFACE(cpg.addInterfaceMethodref(TRANSLET_OUTPUT_INTERFACE,
      // "endDocument", "()V"), 1));
      ctx.currentBlock().invoke(streamXmlOutput, "endDocument");

      // Call toString() on StringWriter
      // il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_WRITER, "toString",
      // "()" + STRING_SIG)));
      expr = stringWriter.invoke("toString");

      // Restore old output handler
      ctx.popHandler();
      // il.append(SWAP);
      // il.append(methodGen.storeHandler());
      break;
    }

    ctx.currentBlock().invoke("displayMessage").arg(expr);
    // Send the resulting string to the message handling method
    // il.append(new INVOKEVIRTUAL(cpg.addMethodref(TRANSLET_CLASS,
    // "displayMessage", "(" + STRING_SIG + ")V")));

    // If 'terminate' attribute is set to 'yes': Instanciate a
    // RunTimeException, but it on the stack and throw an exception
    if (_terminate) {
      // Create a new instance of RunTimeException
      ctx.currentBlock()
          ._throw(_new(ctx.ref(RuntimeException.class)).arg(lit("Termination forced by an xsl:message instruction")));
      // final int einit = cpg.addMethodref("java.lang.RuntimeException",
      // "<init>", "(Ljava/lang/String;)V");
      // il.append(new NEW(cpg.addClass("java.lang.RuntimeException")));
      // il.append(DUP);
      // il.append(new PUSH(cpg, "Termination forced by an xsl:message
      // instruction"));
      // il.append(new INVOKESPECIAL(einit));
      // il.append(ATHROW);
    }
  }

}
