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
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.DOM.GET_EXPANDED_TYPE_ID;
import static de.lyca.xalan.xsltc.DOM.GET_PARENT;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
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
    JVar parent = ctx.currentParent() == null ? ctx.currentNode() : ctx.currentParent();
    JInvocation getParent = invoke(ctx.currentDom(), GET_PARENT).arg(parent);
    JVar node = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(), getParent);
    ctx.addParent(node);

    JExpression right;
    if (_right.isWildcard()) {
      right = TRUE;
    } else if (_right instanceof StepPattern) {
      right = _right.toJExpression(ctx);
    } else {
      right = _right.toJExpression(ctx);
    }

    JVar currentParent = ctx.pollParent();
    final SyntaxTreeNode p = getParent();
    if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
      ctx.pushNode(currentParent);
      JExpression left = _left.toJExpression(ctx);
      ctx.popNode();
      JConditional _if = ctx.currentBlock()._if(right.cand(left));
      JBlock _then = _if._then();
      _then.add(getTemplate().compile(ctx));
      _then._break();
      if (fail != null) {
        JBlock _else = _if._else();
        _else.add(fail);
      }
    }
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JVar parent = ctx.currentParent() == null ? ctx.currentNode() : ctx.currentParent();
    JInvocation getParent = invoke(ctx.currentDom(), GET_PARENT).arg(parent);
    JVar node = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(), getParent);
    ctx.addParent(node);
    JExpression right;
    if (_right.isWildcard()) {
      right = TRUE;
    } else if (_right instanceof StepPattern) {
      right = _right.toJExpression(ctx);
    } else {
      right = _right.toJExpression(ctx);
    }

    ctx.pushNode(ctx.pollParent());
    final SyntaxTreeNode p = getParent();
    if (p == null || p instanceof Instruction || p instanceof TopLevelElement) {
      JExpression left = _left.toJExpression(ctx);
      ctx.popNode();
      if (_right.isWildcard()) {
        return left.cand(right);
      } else {
        return right.cand(left);
      }
    } else {
      JExpression left = _left.toJExpression(ctx);
      if (p instanceof AbsolutePathPattern) {
        left = left.cand(ctx.currentDom().invoke(GET_EXPANDED_TYPE_ID)
            .arg(ctx.currentDom().invoke(GET_PARENT).arg(ctx.popNode())).eq(lit(DTM.DOCUMENT_NODE)));
      } else {
        ctx.popNode();
      }
      if (_right.isWildcard()) {
        return left.cand(right);
      } else {
        return right.cand(left);
      }
    }
  }

  @Override
  public String toString() {
    return "Parent(" + _left + ", " + _right + ')';
  }
}
