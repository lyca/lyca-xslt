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

import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class NamespaceAlias extends TopLevelElement {

  private String stylesheetPrefix;
  private String resultPrefix;

  /*
   * The namespace alias definitions given here have an impact only on literal
   * elements and literal attributes.
   */
  @Override
  public void parseContents(Parser parser) {
    stylesheetPrefix = getAttribute("stylesheet-prefix");
    if (stylesheetPrefix.isEmpty()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "stylesheet-prefix");
    }
    resultPrefix = getAttribute("result-prefix");
    if (resultPrefix.isEmpty()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "result-prefix");
    }
    parser.getSymbolTable().addPrefixAlias(stylesheetPrefix, resultPrefix);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (!(getParent() instanceof Stylesheet)) {
      // TODO better error reporting
      final ErrorMsg err = new ErrorMsg(ErrorMsg.INTERNAL_ERR, "Parent is not Stylesheet", this);
      throw new TypeCheckError(err);
    }
    return Type.Void;
  }

}
