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

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;

import org.apache.bcel.generic.InstructionHandle;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class AncestorPattern extends RelativePathPattern {

  private final Pattern _left; // may be null
  private final RelativePathPattern _right;
  private InstructionHandle _loop;

  public AncestorPattern(RelativePathPattern right) {
    this(null, right);
  }

  public AncestorPattern(Pattern left, RelativePathPattern right) {
    _left = left;
    (_right = right).setParent(this);
    if (left != null) {
      left.setParent(this);
    }
  }

  public InstructionHandle getLoopHandle() {
    return _loop;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_left != null) {
      _left.setParser(parser);
    }
    _right.setParser(parser);
  }

  @Override
  public boolean isWildcard() {
    // !!! can be wildcard
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
    if (_left != null) {
      _left.typeCheck(stable);
    }
    return _right.typeCheck(stable);
  }

//  @Override
//  public JExpression compile(CompilerContext ctx) {
//    // FIXME
//    JInvocation parent;
//
//    /*
//     * The scope of this local var must be the entire method since a another
//     * pattern may decide to jump back into the loop
//     */
////    final LocalVariableGen local = methodGen.addLocalVariable2("app", Util.getJCRefType(NODE_SIG), il.getEnd());
//
////    final org.apache.bcel.generic.Instruction loadLocal = new ILOAD(local.getIndex());
////    final org.apache.bcel.generic.Instruction storeLocal = new ISTORE(local.getIndex());
//
//    JExpression right = _right.compile(ctx);
//
//    if (_left != null) {
//      final JBlock loop = new JBlock()._while(TRUE).body();
//
//      // Create a local variable to hold the current node
//      // Create an instruction list that contains the default next-node
//      // iteration
//      parent = invoke(ctx.param(DOCUMENT_PNAME), GET_PARENT).arg(ctx.currentNode());
//      JVar current = loop.decl(ctx.owner().INT, "app", parent);
//      ctx.pushNode(current);
//      // The body of this code can get very large - large than can be handled
//      // by a single IFNE(body.getStart()) instruction - need workaround:
//      loop._if(current.lt(lit(0)))._then()._return(); // applyTemplates() ends here!
//
//      
//      
//      final int getParent = cpg.addInterfaceMethodref(DOM_INTF, GET_PARENT, GET_PARENT_SIG);
//
//      il.append(DUP);
//      il.append(storeLocal);
//      _falseList.add(il.append(new IFLT(null)));
//      il.append(loadLocal);
//
//      _left.translate(classGen, methodGen);
//
//      final SyntaxTreeNode p = getParent();
//      if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
//        // do nothing
//      } else {
//        il.append(loadLocal);
//      }
//
//      final BranchHandle exit = il.append(new GOTO(null));
//      _loop = il.append(methodGen.loadDOM());
//      il.append(loadLocal);
//      local.setEnd(_loop);
//      il.append(new GOTO(parent));
//      exit.setTarget(il.append(NOP));
//      _left.backPatchFalseList(_loop);
//
//      _trueList.append(_left._trueList);
//    } else {
//      il.append(POP2);
//    }
//
//    /*
//     * If _right is an ancestor pattern, backpatch this pattern's false list to
//     * the loop that searches for more ancestors.
//     */
//    if (_right instanceof AncestorPattern) {
//      final AncestorPattern ancestor = (AncestorPattern) _right;
//      _falseList.backPatch(ancestor.getLoopHandle()); // clears list
//    }
//
//    _trueList.append(_right._trueList);
//    _falseList.append(_right._falseList);
//    return super.compile(ctx);
//  }

  @Override
  public void translate(CompilerContext ctx) {
//    FIXME
//    InstructionHandle parent;
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    /*
//     * The scope of this local var must be the entire method since a another
//     * pattern may decide to jump back into the loop
//     */
//    final LocalVariableGen local = methodGen.addLocalVariable2("app", Util.getJCRefType(NODE_SIG), il.getEnd());
//
//    final org.apache.bcel.generic.Instruction loadLocal = new ILOAD(local.getIndex());
//    final org.apache.bcel.generic.Instruction storeLocal = new ISTORE(local.getIndex());
//
//    if (_right instanceof StepPattern) {
//      il.append(DUP);
//      il.append(storeLocal);
//      _right.translate(classGen, methodGen);
//      il.append(methodGen.loadDOM());
//      il.append(loadLocal);
//    } else {
//      _right.translate(classGen, methodGen);
//
//      if (_right instanceof AncestorPattern) {
//        il.append(methodGen.loadDOM());
//        il.append(SWAP);
//      }
//    }
//
//    if (_left != null) {
//      final int getParent = cpg.addInterfaceMethodref(DOM_INTF, GET_PARENT, GET_PARENT_SIG);
//      parent = il.append(new INVOKEINTERFACE(getParent, 2));
//
//      il.append(DUP);
//      il.append(storeLocal);
//      _falseList.add(il.append(new IFLT(null)));
//      il.append(loadLocal);
//
//      _left.translate(classGen, methodGen);
//
//      final SyntaxTreeNode p = getParent();
//      if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
//        // do nothing
//      } else {
//        il.append(loadLocal);
//      }
//
//      final BranchHandle exit = il.append(new GOTO(null));
//      _loop = il.append(methodGen.loadDOM());
//      il.append(loadLocal);
//      local.setEnd(_loop);
//      il.append(new GOTO(parent));
//      exit.setTarget(il.append(NOP));
//      _left.backPatchFalseList(_loop);
//
//      _trueList.append(_left._trueList);
//    } else {
//      il.append(POP2);
//    }
//
//    /*
//     * If _right is an ancestor pattern, backpatch this pattern's false list to
//     * the loop that searches for more ancestors.
//     */
//    if (_right instanceof AncestorPattern) {
//      final AncestorPattern ancestor = (AncestorPattern) _right;
//      _falseList.backPatch(ancestor.getLoopHandle()); // clears list
//    }
//
//    _trueList.append(_right._trueList);
//    _falseList.append(_right._falseList);
  }

  @Override
  public String toString() {
    return "AncestorPattern(" + _left + ", " + _right + ')';
  }
}
