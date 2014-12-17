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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Choose extends Instruction {

  /**
   * Display the element contents (a lot of when's and an otherwise)
   */
  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("Choose");
    indent(indent + IndentIncrement);
    displayContents(indent + IndentIncrement);
  }

  /**
   * Translate this Choose element. Generate a test-chain for the various
   * <xsl:when> elements and default to the <xsl:otherwise> if present.
   */
  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
    // FIXME
//    final List<When> whenElements = new ArrayList<>();
//    Otherwise otherwise = null;
//    final ListIterator<SyntaxTreeNode> elements = elements();
//
//    // This is for reporting errors only
//    ErrorMsg error = null;
//
//    // Traverse all child nodes - must be either When or Otherwise
//    while (elements.hasNext()) {
//      final Object element = elements.next();
//      // Add a When child element
//      if (element instanceof When) {
//        whenElements.add((When) element);
//      }
//      // Add an Otherwise child element
//      else if (element instanceof Otherwise) {
//        if (otherwise == null) {
//          otherwise = (Otherwise) element;
//        } else {
//          error = new ErrorMsg(ErrorMsg.MULTIPLE_OTHERWISE_ERR, this);
//          getParser().reportError(Constants.ERROR, error);
//        }
//      } else if (element instanceof Text) {
//        ((Text) element).ignore();
//      }
//      // It is an error if we find some other element here
//      else {
//        error = new ErrorMsg(ErrorMsg.WHEN_ELEMENT_ERR, this);
//        getParser().reportError(Constants.ERROR, error);
//      }
//    }
//
//    // Make sure that there is at least one <xsl:when> element
//    if (whenElements.size() == 0) {
//      error = new ErrorMsg(ErrorMsg.MISSING_WHEN_ERR, this);
//      getParser().reportError(Constants.ERROR, error);
//      return;
//    }
//
//    final InstructionList il = methodGen.getInstructionList();
//
//    // next element will hold a handle to the beginning of next
//    // When/Otherwise if test on current When fails
//    BranchHandle nextElement = null;
//    final List<BranchHandle> exitHandles = new ArrayList<>();
//    InstructionHandle exit = null;
//
//    final Iterator<When> whens = whenElements.iterator();
//    while (whens.hasNext()) {
//      final When when = whens.next();
//      final Expression test = when.getTest();
//
//      InstructionHandle truec = il.getEnd();
//
//      if (nextElement != null) {
//        nextElement.setTarget(il.append(NOP));
//      }
//      test.translateDesynthesized(classGen, methodGen);
//
//      if (test instanceof FunctionCall) {
//        final FunctionCall call = (FunctionCall) test;
//        try {
//          final Type type = call.typeCheck(getParser().getSymbolTable());
//          if (type != Type.Boolean) {
//            test._falseList.add(il.append(new IFEQ(null)));
//          }
//        } catch (final TypeCheckError e) {
//          // handled later!
//        }
//      }
//      // remember end of condition
//      truec = il.getEnd();
//
//      // The When object should be ignored completely in case it tests
//      // for the support of a non-available element
//      if (!when.ignore()) {
//        when.translateContents(classGen, methodGen);
//      }
//
//      // goto exit after executing the body of when
//      exitHandles.add(il.append(new GOTO(null)));
//      if (whens.hasNext() || otherwise != null) {
//        nextElement = il.append(new GOTO(null));
//        test.backPatchFalseList(nextElement);
//      } else {
//        test.backPatchFalseList(exit = il.append(NOP));
//      }
//      test.backPatchTrueList(truec.getNext());
//    }
//
//    // Translate any <xsl:otherwise> element
//    if (otherwise != null) {
//      nextElement.setTarget(il.append(NOP));
//      otherwise.translateContents(classGen, methodGen);
//      exit = il.append(NOP);
//    }
//
//    // now that end is known set targets of exit gotos
//    final Iterator<BranchHandle> exitGotos = exitHandles.iterator();
//    while (exitGotos.hasNext()) {
//      final BranchHandle gotoExit = exitGotos.next();
//      gotoExit.setTarget(exit);
//    }
  }
}
