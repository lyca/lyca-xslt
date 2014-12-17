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

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.NEW;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
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
      reportError(parent, parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, value);
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
                reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, text);
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
                  reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, text);
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
      reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, text);
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
  public JExpression compile(JDefinedClass definedClass, JMethod method) {
    if (elementCount() == 1) {
      final Expression exp = (Expression) elementAt(0);
      return exp.compile(definedClass, method);
    } else {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final InstructionList il = methodGen.getInstructionList();
//      final int initBuffer = cpg.addMethodref(STRING_BUILDER_CLASS, "<init>", "()V");
//      final Instruction append = new INVOKEVIRTUAL(cpg.addMethodref(STRING_BUILDER_CLASS, "append", "(" + STRING_SIG
//          + ")" + STRING_BUILDER_SIG));
//
//      final int toString = cpg.addMethodref(STRING_BUILDER_CLASS, "toString", "()" + STRING_SIG);
//      il.append(new NEW(cpg.addClass(STRING_BUILDER_CLASS)));
//      il.append(DUP);
//      il.append(new INVOKESPECIAL(initBuffer));
//      // StringBuilder is on the stack
//      final ListIterator<SyntaxTreeNode> elements = elements();
//      while (elements.hasNext()) {
//        final Expression exp = (Expression) elements.next();
//        exp.translate(classGen, methodGen);
//        il.append(append);
//      }
//      il.append(new INVOKEVIRTUAL(toString));
      return null;
    }
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
// FIXME
//    if (elementCount() == 1) {
//      final Expression exp = (Expression) elementAt(0);
//      exp.translate(classGen, methodGen);
//    } else {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final InstructionList il = methodGen.getInstructionList();
//      final int initBuffer = cpg.addMethodref(STRING_BUILDER_CLASS, "<init>", "()V");
//      final Instruction append = new INVOKEVIRTUAL(cpg.addMethodref(STRING_BUILDER_CLASS, "append", "(" + STRING_SIG
//              + ")" + STRING_BUILDER_SIG));
//
//      final int toString = cpg.addMethodref(STRING_BUILDER_CLASS, "toString", "()" + STRING_SIG);
//      il.append(new NEW(cpg.addClass(STRING_BUILDER_CLASS)));
//      il.append(DUP);
//      il.append(new INVOKESPECIAL(initBuffer));
//      // StringBuilder is on the stack
//      final ListIterator<SyntaxTreeNode> elements = elements();
//      while (elements.hasNext()) {
//        final Expression exp = (Expression) elements.next();
//        exp.translate(classGen, methodGen);
//        il.append(append);
//      }
//      il.append(new INVOKEVIRTUAL(toString));
//    }
  }
}
