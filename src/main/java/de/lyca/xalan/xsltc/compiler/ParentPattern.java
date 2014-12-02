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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class ParentPattern extends RelativePathPattern {
  private final Pattern _left;
  private final RelativePathPattern _right;

  public ParentPattern(Pattern left, RelativePathPattern right) {
    (_left = left).setParent(this);
    (_right = right).setParent(this);
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _left.setParser(parser);
    _right.setParser(parser);
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  public StepPattern getKernelPattern() {
    return _right.getKernelPattern();
  }

  @Override
  public void reduceKernelPattern() {
    _right.reduceKernelPattern();
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _left.typeCheck(stable);
    return _right.typeCheck(stable);
  }

  @Override
  public void translate(JDefinedClass definedClass, JMethod method) {
 // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final LocalVariableGen local = methodGen.addLocalVariable2("ppt", Util.getJCRefType(NODE_SIG), null);
//
//    final org.apache.bcel.generic.Instruction loadLocal = new ILOAD(local.getIndex());
//    final org.apache.bcel.generic.Instruction storeLocal = new ISTORE(local.getIndex());
//
//    if (_right.isWildcard()) {
//      il.append(methodGen.loadDOM());
//      il.append(SWAP);
//    } else if (_right instanceof StepPattern) {
//      il.append(DUP);
//      local.setStart(il.append(storeLocal));
//
//      _right.translate(classGen, methodGen);
//
//      il.append(methodGen.loadDOM());
//      local.setEnd(il.append(loadLocal));
//    } else {
//      _right.translate(classGen, methodGen);
//
//      if (_right instanceof AncestorPattern) {
//        il.append(methodGen.loadDOM());
//        il.append(SWAP);
//      }
//    }
//
//    final int getParent = cpg.addInterfaceMethodref(DOM_INTF, GET_PARENT, GET_PARENT_SIG);
//    il.append(new INVOKEINTERFACE(getParent, 2));
//
//    final SyntaxTreeNode p = getParent();
//    if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
//      _left.translate(classGen, methodGen);
//    } else {
//      il.append(DUP);
//      final InstructionHandle storeInst = il.append(storeLocal);
//
//      if (local.getStart() == null) {
//        local.setStart(storeInst);
//      }
//      _left.translate(classGen, methodGen);
//
//      il.append(methodGen.loadDOM());
//      local.setEnd(il.append(loadLocal));
//    }
//
//    methodGen.removeLocalVariable(local);
//
//    /*
//     * If _right is an ancestor pattern, backpatch _left false list to the loop
//     * that searches for more ancestors.
//     */
//    if (_right instanceof AncestorPattern) {
//      final AncestorPattern ancestor = (AncestorPattern) _right;
//      _left.backPatchFalseList(ancestor.getLoopHandle()); // clears list
//    }
//
//    _trueList.append(_right._trueList.append(_left._trueList));
//    _falseList.append(_right._falseList.append(_left._falseList));
  }

  @Override
  public String toString() {
    return "Parent(" + _left + ", " + _right + ')';
  }
}
