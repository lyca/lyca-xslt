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
package de.lyca.xalan.extensions;

import de.lyca.xalan.templates.StylesheetRoot;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.functions.FuncExtFunction;
import de.lyca.xpath.functions.FuncExtFunctionAvailable;
import de.lyca.xpath.functions.Function;

/**
 * When {@link de.lyca.xalan.processor.StylesheetHandler} creates an
 * {@link de.lyca.xpath.XPath}, the ExpressionVisitor visits the XPath
 * expression. For any extension functions it encounters, it instructs
 * StylesheetRoot to register the extension namespace.
 * 
 * This mechanism is required to locate extension functions that may be embedded
 * within an expression.
 */
public class ExpressionVisitor extends XPathVisitor {
  private final StylesheetRoot m_sroot;

  /**
   * The constructor sets the StylesheetRoot variable which is used to register
   * extension namespaces.
   * 
   * @param sroot
   *          the StylesheetRoot that is being constructed.
   */
  public ExpressionVisitor(StylesheetRoot sroot) {
    m_sroot = sroot;
  }

  /**
   * If the function is an extension function, register the namespace.
   * 
   * @param owner
   *          The current XPath object that owns the expression.
   * @param func
   *          The function currently being visited.
   * 
   * @return true to continue the visit in the subtree, if any.
   */
  @Override
  public boolean visitFunction(ExpressionOwner owner, Function func) {
    if (func instanceof FuncExtFunction) {
      final String namespace = ((FuncExtFunction) func).getNamespace();
      m_sroot.getExtensionNamespacesManager().registerExtension(namespace);
    } else if (func instanceof FuncExtFunctionAvailable) {
      final String arg = ((FuncExtFunctionAvailable) func).getArg0().toString();
      if (arg.indexOf(":") > 0) {
        final String prefix = arg.substring(0, arg.indexOf(":"));
        final String namespace = m_sroot.getNamespaceForPrefix(prefix);
        m_sroot.getExtensionNamespacesManager().registerExtension(namespace);
      }
    }
    return true;
  }

}