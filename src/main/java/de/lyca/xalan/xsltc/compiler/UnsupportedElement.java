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

import static com.sun.codemodel.JExpr.lit;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JClass;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Morten Jorgensen
 */
final class UnsupportedElement extends SyntaxTreeNode {

  private List<Fallback> _fallbacks = null;
  private ErrorMsg _message = null;
  private boolean _isExtension = false;

  /**
   * Basic constructor - stores element uri/prefix/localname
   */
  public UnsupportedElement(String uri, String prefix, String local, boolean isExtension) {
    super(uri, prefix, local);
    _isExtension = isExtension;
  }

  /**
   * There are different categories of unsupported elements (believe it or not):
   * there are elements within the XSLT namespace (these would be elements that
   * are not yet implemented), there are extensions of other XSLT processors and
   * there are unrecognised extension elements of this XSLT processor. The error
   * message passed to this method should describe the unsupported element
   * itself and what category the element belongs in.
   */
  public void setErrorMessage(ErrorMsg message) {
    _message = message;
  }

  /**
   * Scan and process all fallback children of the unsupported element.
   */
  private void processFallbacks(Parser parser) {

    final List<SyntaxTreeNode> children = getContents();
    if (children != null) {
      final int count = children.size();
      for (int i = 0; i < count; i++) {
        final SyntaxTreeNode child = children.get(i);
        if (child instanceof Fallback) {
          final Fallback fallback = (Fallback) child;
          fallback.activate();
          fallback.parseContents(parser);
          if (_fallbacks == null) {
            _fallbacks = new ArrayList<>();
          }
          _fallbacks.add(fallback);
        }
      }
    }
  }

  /**
   * Find any fallback in the descendant nodes; then activate & parse it
   */
  @Override
  public void parseContents(Parser parser) {
    processFallbacks(parser);
  }

  /**
   * Run type check on the fallback element (if any).
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_fallbacks != null) {
      final int count = _fallbacks.size();
      for (int i = 0; i < count; i++) {
        final Fallback fallback = _fallbacks.get(i);
        fallback.typeCheck(stable);
      }
    }
    return Type.Void;
  }

  /**
   * Translate the fallback element (if any).
   */
  @Override
  public void translate(CompilerContext ctx) {
    if (_fallbacks != null) {
      final int count = _fallbacks.size();
      for (int i = 0; i < count; i++) {
        final Fallback fallback = _fallbacks.get(i);
        fallback.translate(ctx);
      }
    }
    // We only go into the else block in forward-compatibility mode, when
    // the unsupported element has no fallback.
    else {
      // If the unsupported element does not have any fallback child, then
      // at runtime, a runtime error should be raised when the unsupported
      // element is instantiated. Otherwise, no error is thrown.
      JClass basisLibrary = ctx.ref(BasisLibrary.class);
      ctx.currentBlock().add(basisLibrary.staticInvoke("unsupported_ElementF").arg(getQName().toString()).arg(lit(_isExtension)));
    }
  }

}
