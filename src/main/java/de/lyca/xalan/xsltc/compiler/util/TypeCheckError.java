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

import de.lyca.xalan.xsltc.compiler.SyntaxTreeNode;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public class TypeCheckError extends Exception {
  static final long serialVersionUID = 3246224233917854640L;
  ErrorMsg _error = null;
  SyntaxTreeNode _node = null;

  public TypeCheckError(SyntaxTreeNode node) {
    super();
    _node = node;
  }

  public TypeCheckError(ErrorMsg error) {
    super();
    _error = error;
  }

  public ErrorMsg getErrorMsg() {
    return _error;
  }

  @Override
  public String getMessage() {
    return toString();
  }

  @Override
  public String toString() {
    if (_error == null) {
      if (_node != null) {
        _error = new ErrorMsg(Messages.get().typeCheckErr(_node));
      } else {
        _error = new ErrorMsg(Messages.get().typeCheckUnkLocErr());
      }
    }
    return _error.toString();
  }
}
