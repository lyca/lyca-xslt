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

import static com.sun.codemodel.JExpr._new;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class AttributeValueTemplate extends AttributeValue {

  final static int OUT_EXPR = 0;
  final static int IN_EXPR = 1;
  final static int IN_EXPR_SQUOTES = 2;
  final static int IN_EXPR_DQUOTES = 3;
  final static String DELIMITER = "\uFFFE"; // A Unicode nonchar

  public AttributeValueTemplate(String value, Parser parser, SyntaxTreeNode parent) {
    setParent(parent);
    setParser(parser);

    try {
      parseAVTemplate(value, parser);
    } catch (final NoSuchElementException e) {
      reportError(parent, parser, Messages.get().attrValTemplateErr(value));
    }
  }

  /**
   * Two-pass parsing of ATVs. In the first pass, double curly braces are
   * replaced by one, and expressions are delimited using DELIMITER. The second
   * pass splits up the resulting buffer into literal and non-literal
   * expressions. Errors are reported during the first pass.
   */
  private void parseAVTemplate(String text, Parser parser) {
    StringTokenizer tokenizer = new StringTokenizer(text, "{}\"\'", true);

    /*
     * First pass: replace double curly braces and delimit expressions Simple
     * automaton to parse ATVs, delimit expressions and report errors.
     */
    String t = null;
    String lookahead = null;
    final StringBuilder buffer = new StringBuilder();
    int state = OUT_EXPR;

    while (tokenizer.hasMoreTokens()) {
      // Use lookahead if available
      if (lookahead != null) {
        t = lookahead;
        lookahead = null;
      } else {
        t = tokenizer.nextToken();
      }

      if (t.length() == 1) {
        switch (t.charAt(0)) {
        case '{':
          switch (state) {
          case OUT_EXPR:
            lookahead = tokenizer.nextToken();
            if (lookahead.equals("{")) {
              buffer.append(lookahead); // replace {{ by {
              lookahead = null;
            } else {
              buffer.append(DELIMITER);
              state = IN_EXPR;
            }
            break;
          case IN_EXPR:
          case IN_EXPR_SQUOTES:
          case IN_EXPR_DQUOTES:
            reportError(getParent(), parser, Messages.get().attrValTemplateErr(text));
            break;
          }
          break;
        case '}':
          switch (state) {
          case OUT_EXPR:
            lookahead = tokenizer.nextToken();
            if (lookahead.equals("}")) {
              buffer.append(lookahead); // replace }} by }
              lookahead = null;
            } else {
              reportError(getParent(), parser,Messages.get().attrValTemplateErr(text));
            }
            break;
          case IN_EXPR:
            buffer.append(DELIMITER);
            state = OUT_EXPR;
            break;
          case IN_EXPR_SQUOTES:
          case IN_EXPR_DQUOTES:
            buffer.append(t);
            break;
          }
          break;
        case '\'':
          switch (state) {
          case IN_EXPR:
            state = IN_EXPR_SQUOTES;
            break;
          case IN_EXPR_SQUOTES:
            state = IN_EXPR;
            break;
          case OUT_EXPR:
          case IN_EXPR_DQUOTES:
            break;
          }
          buffer.append(t);
          break;
        case '\"':
          switch (state) {
          case IN_EXPR:
            state = IN_EXPR_DQUOTES;
            break;
          case IN_EXPR_DQUOTES:
            state = IN_EXPR;
            break;
          case OUT_EXPR:
          case IN_EXPR_SQUOTES:
            break;
          }
          buffer.append(t);
          break;
        default:
          buffer.append(t);
          break;
        }
      } else {
        buffer.append(t);
      }
    }

    // Must be in OUT_EXPR at the end of parsing
    if (state != OUT_EXPR) {
      reportError(getParent(), parser, Messages.get().attrValTemplateErr(text));
    }

    /*
     * Second pass: split up buffer into literal and non-literal expressions.
     */
    tokenizer = new StringTokenizer(buffer.toString(), DELIMITER, true);

    while (tokenizer.hasMoreTokens()) {
      t = tokenizer.nextToken();

      if (t.equals(DELIMITER)) {
        addElement(parser.parseExpression(this, tokenizer.nextToken()));
        tokenizer.nextToken(); // consume other delimiter
      } else {
        addElement(new LiteralExpr(t));
      }
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final ListIterator<SyntaxTreeNode> li = elements();
    while (li.hasNext()) {
      final Expression exp = (Expression) li.next();
      if (!exp.typeCheck(stable).identicalTo(Type.String)) {
        li.set(new CastExpr(exp, Type.String));
      }
    }
    return _type = Type.String;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder("AVT:[");
    final int count = elementCount();
    for (int i = 0; i < count; i++) {
      buffer.append(elementAt(i).toString());
      if (i < count - 1) {
        buffer.append(' ');
      }
    }
    return buffer.append(']').toString();
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    switch (elementCount()) {
    case 0:
      return JExpr.lit("");
    case 1:
      return ((Expression) elementAt(0)).toJExpression(ctx);
    default:
      JExpression stringBuilder = _new(ctx.ref(StringBuilder.class));
      final ListIterator<SyntaxTreeNode> elements = elements();
      while (elements.hasNext()) {
        final Expression exp = (Expression) elements.next();
        JExpression string = exp.toJExpression(ctx);
        stringBuilder = stringBuilder.invoke("append").arg(string);
      }
      return stringBuilder.invoke("toString");
    }
  }

  @Override
  public void translate(CompilerContext ctx) {
  }

}
