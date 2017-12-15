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

import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.not;
import static de.lyca.xalan.xsltc.DOM.GET_EXPANDED_TYPE_ID;
import static de.lyca.xalan.xsltc.DOM.GET_PARENT;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class AncestorPattern extends RelativePathPattern {

  private final Pattern _left; // may be null
  private final RelativePathPattern _right;

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

  @Override
  public void compilePattern(CompilerContext ctx, JStatement fail) {
    JExpression right = _right.toJExpression(ctx);

    if (_left != null) {
      JVar parent = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(),
          ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode()));
      ctx.pushNode(parent);
      JExpression result = _left.toJExpression(ctx).cand(right);
      ctx.popNode();
      JExpression pgte0 = parent.gte(lit(0));
      ctx.currentBlock()._while(pgte0.cand(not(result))).body()
          .assign(parent, ctx.currentDom().invoke(GET_PARENT).arg(parent));

      Template nodeTemplate = getTemplate();
      SyntaxTreeNode parentNode = getParent();
      while (nodeTemplate == null && parentNode != null) {
        nodeTemplate = parentNode.getTemplate();
        parentNode = parentNode.getParent();
      }
      if (nodeTemplate != null) {
        JStatement template = nodeTemplate.compile(ctx);
        JConditional _if = ctx.currentBlock()._if(pgte0.cand(result));
        JBlock _then = _if._then();
        _then.add(template);
        _then._break();
        if (fail != null)
          _if._else().add(fail);
      }
    } else {
      Template nodeTemplate = getTemplate();
      SyntaxTreeNode parentNode = getParent();
      while (nodeTemplate == null && parentNode != null) {
        nodeTemplate = parentNode.getTemplate();
        parentNode = parentNode.getParent();
      }
      if (nodeTemplate != null) {
        JStatement template = nodeTemplate.compile(ctx);
        if (right == JExpr.TRUE) {
          ctx.currentBlock().add(template);
        } else {
          JConditional _if = ctx.currentBlock()._if(right);
          JBlock _then2 = _if._then();
          _then2.add(template);
          _then2._break();
          _if._else().add(fail);
        }
      }
    }
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    JExpression right = _right.toJExpression(ctx);

    if (_left != null) {
      JVar parent = ctx.currentBlock().decl(ctx.owner().INT, ctx.nextParent(),
          ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode()));
      ctx.pushNode(parent);
      JExpression result = _left.toJExpression(ctx).cand(right);
      if (getParent() instanceof AbsolutePathPattern) {
        result = result.cand(ctx.currentDom().invoke(GET_EXPANDED_TYPE_ID)
            .arg(ctx.currentDom().invoke(GET_PARENT).arg(parent)).eq(lit(DTM.DOCUMENT_NODE)));
      }
      ctx.popNode();
      JExpression pgte0 = parent.gte(lit(0));
      ctx.currentBlock()._while(pgte0.cand(not(result))).body()
          .assign(parent, ctx.currentDom().invoke(GET_PARENT).arg(parent));
      if (getParent() instanceof ParentPattern) {
        ctx.currentBlock().assign(ctx.currentParent(), ctx.currentDom().invoke(GET_PARENT).arg(parent));
      }
      return pgte0.cand(result);
    }
    return JExpr._null();
  }

  @Override
  public void translate(CompilerContext ctx) {
  }

  @Override
  public String toString() {
    return "AncestorPattern(" + _left + ", " + _right + ')';
  }

}
