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
import static de.lyca.xalan.xsltc.DOM.GET_EXPANDED_TYPE_ID;
import static de.lyca.xalan.xsltc.DOM.GET_PARENT;
import static de.lyca.xml.dtm.DTM.DOCUMENT_NODE;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStatement;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class AbsolutePathPattern extends LocationPathPattern {
  private final RelativePathPattern _left; // may be null

  public AbsolutePathPattern(RelativePathPattern left) {
    _left = left;
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
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    return _left == null ? Type.Root : _left.typeCheck(stable);
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  public StepPattern getKernelPattern() {
    return _left != null ? _left.getKernelPattern() : null;
  }

  @Override
  public void reduceKernelPattern() {
    _left.reduceKernelPattern();
  }

  public void compilePattern(CompilerContext ctx, JStatement fail) {
    if (_left == null) {
      ctx.currentBlock().invoke(ctx.currentDom(), GET_EXPANDED_TYPE_ID)
          .arg(ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode())).eq(lit(DOCUMENT_NODE));
    } else {
      if (getTemplate() != null) {
        JExpression absPath = TRUE;
        if (_left instanceof StepPattern) {
          absPath = ctx.currentDom().invoke(GET_EXPANDED_TYPE_ID)
              .arg(ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode())).eq(lit(DOCUMENT_NODE));
        }
        JExpression left = _left.toJExpression(ctx);
        JConditional _if = ctx.currentBlock()._if(left.cand(absPath));
        _if._then().add(getTemplate().compile(ctx));
        if (fail == null) {
          _if._then()._break();
        } else {
          _if._else().add(fail);
        }
      }
    }
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (_left == null) {
      return invoke(ctx.currentDom(), GET_EXPANDED_TYPE_ID).arg(
          ctx.currentDom().invoke(GET_PARENT).arg(ctx.currentNode())).eq(lit(DTM.DOCUMENT_NODE));
    } else {
      return _left.toJExpression(ctx);
    }
  }

  @Override
  public String toString() {
    return "absolutePathPattern(" + (_left != null ? _left.toString() : ")");
  }
}
