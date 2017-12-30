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
package de.lyca.xalan.xsltc.compiler.util;

import de.lyca.xalan.xsltc.compiler.Stylesheet;
import de.lyca.xalan.xsltc.compiler.SyntaxTreeNode;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller
 * @author Erwin Bolwidt <ejb@klomp.org>
 * @author Morten Jorgensen
 */
public final class ErrorMsg {

  private final int _line;
  private String _message = null;
  private String _url = null;

  public ErrorMsg(String message) {
    _message = message;
    _line = 0;
  }

  public ErrorMsg(Throwable e) {
    _message = e.getMessage();
    _line = 0;
  }

  public ErrorMsg(String message, int line) {
    _message = message;
    _line = line;
  }

  public ErrorMsg(SyntaxTreeNode node, String message) {
    _message = message;
    _url = getFileName(node);
    _line = node.getLineNumber();
  }

  private String getFileName(SyntaxTreeNode node) {
    final Stylesheet stylesheet = node.getStylesheet();
    if (stylesheet != null)
      return stylesheet.getSystemId();
    else
      return null;
  }

  private String formatLine() {
    final StringBuilder result = new StringBuilder();
    if (_url != null) {
      result.append(_url);
      result.append(": ");
    }
    if (_line > 0) {
      result.append("line ");
      result.append(Integer.toString(_line));
      result.append(": ");
    }
    return result.toString();
  }

  /**
   * This version of toString() uses the _params instance variable to format the
   * message. If the <code>_code</code> is negative the use _message as the
   * error string.
   */
  @Override
  public String toString() {
    return formatLine() + _message;
  }

}
