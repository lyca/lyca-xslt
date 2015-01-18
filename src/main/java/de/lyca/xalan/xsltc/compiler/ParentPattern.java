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
import static de.lyca.xalan.xsltc.compiler.Constants.GET_PARENT;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.dtm.DTM;

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
  public void compilePattern(CompilerContext ctx, JStatement fail) {
//    final LocalVariableGen local = methodGen.addLocalVariable2("ppt", Util.getJCRefType(NODE_SIG), null);
    JVar parent = ctx.currentParent() == null ? ctx.currentNode() : ctx.currentParent();
    JInvocation getParent = invoke(ctx.currentDom(), GET_PARENT).arg(parent);
    JVar node = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(), getParent);
    ctx.addParent(node);
    JExpression right;
    if (_right.isWildcard()) {
//      il.append(methodGen.loadDOM());
//      il.append(SWAP);
      System.out.println("wildcard");
//      right = ctx.currentDom();
      right = TRUE;

    } else if (_right instanceof StepPattern) {
      System.out.println("steppattern");
//      il.append(DUP);
//      local.setStart(il.append(storeLocal));
//
//      _right.translate(classGen, methodGen);
//
//      il.append(methodGen.loadDOM());
//      local.setEnd(il.append(loadLocal));
      right = _right.compile(ctx);
    } else {
      right = _right.compile(ctx);
      System.out.println("somepattern");
//      _right.translate(classGen, methodGen);

      if (_right instanceof AncestorPattern) {
        System.out.println("ancestorpattern");
//        il.append(methodGen.loadDOM());
//        il.append(SWAP);
      }
    }

//    final int getParent = cpg.addInterfaceMethodref(DOM_INTF, GET_PARENT, GET_PARENT_SIG);
//    il.append(new INVOKEINTERFACE(getParent, 2));
//    JInvocation getParent = invoke(right, GET_PARENT).arg(ctx.currentNode());
//    JVar node = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(), getParent);

    JVar currentParent =ctx.pollParent();
    final SyntaxTreeNode p = getParent();
    if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
      System.out.println("_lefttranslate");
      ctx.pushNode(currentParent);
      JExpression left = _left.compile(ctx);
      ctx.popNode();
      JConditional _if = ctx.currentBlock()._if(right.cand(left));
      JBlock _then = _if._then();
      _then.add(getTemplate().compile(ctx));
      _then._break();
      if (fail != null) {
        JBlock _else = _if._else();
        _else.add(fail);
//        _else._break();
      }
      //      _left.translate(classGen, methodGen);
    } else {
      System.out.println("sometranslate");
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
    }
//    methodGen.removeLocalVariable(local);

    /*
     * If _right is an ancestor pattern, backpatch _left false list to the loop
     * that searches for more ancestors.
     */
    if (_right instanceof AncestorPattern) {
      final AncestorPattern ancestor = (AncestorPattern) _right;
//      _left.backPatchFalseList(ancestor.getLoopHandle()); // clears list
    }

//    _trueList.append(_right._trueList.append(_left._trueList));
//    _falseList.append(_right._falseList.append(_left._falseList));
  }

  @Override
  public JExpression compile(CompilerContext ctx) {
    JVar parent = ctx.currentParent() == null ? ctx.currentNode() : ctx.currentParent();
    JInvocation getParent = invoke(ctx.currentDom(), GET_PARENT).arg(parent);
    JVar node = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(), getParent);
    ctx.addParent(node);
    JExpression right;
    if (_right.isWildcard()) {
      right = ctx.currentDom().invoke("getExpandedTypeID")
          .arg(ctx.currentDom().invoke(GET_PARENT).arg(node)).eq(lit(DTM.DOCUMENT_NODE));
      right = JExpr.TRUE;// ctx.currentDom();
    } else if (_right instanceof StepPattern) {
      right = _right.compile(ctx);
    } else {
      right = _right.compile(ctx);
      System.out.println("somepattern");
      if (_right instanceof AncestorPattern) {
        System.out.println("ancestorpattern");
      }
    }

    ctx.pushNode(ctx.pollParent());
    final SyntaxTreeNode p = getParent();
    if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
      System.out.println("_lefttranslate");
//      ctx.pushNode(node);
      JExpression left = _left.compile(ctx);
      ctx.popNode();
      if (_right.isWildcard()) {
        return left.cand(right);
      } else {
        return right.cand(left);
      }
    } else {
      System.out.println("sometranslate");
      JExpression left = _left.compile(ctx);
      if (p instanceof AbsolutePathPattern) {
        left = left.cand(ctx.currentDom().invoke("getExpandedTypeID")
            .arg(ctx.currentDom().invoke(GET_PARENT).arg(ctx.popNode())).eq(lit(DTM.DOCUMENT_NODE)));
      } else {
        ctx.popNode();
      }
      if (_right.isWildcard()) {
        return left.cand(right);
      } else {
        return right.cand(left);
      }
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
    }
//    return right;
  }
  
  @Override
  public void translate(CompilerContext ctx) {
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
