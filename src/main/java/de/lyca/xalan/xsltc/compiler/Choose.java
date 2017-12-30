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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sun.codemodel.JConditional;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Choose extends Instruction {

  /**
   * Translate this Choose element. Generate a test-chain for the various
   * <xsl:when> elements and default to the <xsl:otherwise> if present.
   */
  @Override
  public void translate(CompilerContext ctx) {
    final List<When> whenElements = new ArrayList<>();
    Otherwise otherwise = null;
    final ListIterator<SyntaxTreeNode> elements = elements();

    // This is for reporting errors only
    ErrorMsg error = null;

    // Traverse all child nodes - must be either When or Otherwise
    while (elements.hasNext()) {
      final Object element = elements.next();
      // Add a When child element when no otherwise exists
      if (element instanceof When && otherwise == null) {
        whenElements.add((When) element);
      }
      // Add a When child element when no otherwise exists
      else if (element instanceof When && otherwise != null) {
        // TODO
        error = new ErrorMsg(this, Messages.get().internalErr("when after otherwise"));
        getParser().reportError(Constants.ERROR, error);
      }
      // Add an Otherwise child element
      else if (element instanceof Otherwise) {
        if (otherwise == null) {
          otherwise = (Otherwise) element;
        } else {
          error = new ErrorMsg(this, Messages.get().multipleOtherwiseErr());
          getParser().reportError(Constants.ERROR, error);
        }
      } else if (element instanceof Text) {
        ((Text) element).ignore();
      }
      // It is an error if we find some other element here
      else {
        error = new ErrorMsg(this, Messages.get().whenElementErr());
        getParser().reportError(Constants.ERROR, error);
      }
    }

    // Make sure that there is at least one <xsl:when> element
    if (whenElements.size() == 0) {
      error = new ErrorMsg(this, Messages.get().missingWhenErr());
      getParser().reportError(Constants.ERROR, error);
      return;
    }

    When first = whenElements.remove(0);
    JConditional currentIf = ctx.currentBlock()._if(first.getTest().toJExpression(ctx));
    if (!first.ignore()) {
      ctx.pushBlock(currentIf._then());
      first.translateContents(ctx);
      ctx.popBlock();
    }

    final Iterator<When> whens = whenElements.iterator();
    while (whens.hasNext()) {
      final When when = whens.next();

      currentIf = currentIf._elseif(when.getTest().toJExpression(ctx));

      // The When object should be ignored completely in case it tests
      // for the support of a non-available element
      if (!when.ignore()) {
        ctx.pushBlock(currentIf._then());
        when.translateContents(ctx);
        ctx.popBlock();
      }
    }

    // Translate any <xsl:otherwise> element
    if (otherwise != null) {
      ctx.pushBlock(currentIf._else());
      otherwise.translateContents(ctx);
      ctx.popBlock();
    }
  }

}
