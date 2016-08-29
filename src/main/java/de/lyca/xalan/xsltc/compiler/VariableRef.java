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

import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_PNAME;
import static de.lyca.xml.dtm.DTMAxisIterator.CLONE_ITERATOR;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class VariableRef extends VariableRefBase {

  public VariableRef(Variable variable) {
    super(variable);
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    // Fall-through for variables that are implemented as methods
    if (_type.implementedAsMethod())
      return null;

    final String name = _variable.getEscapedName();
    JExpression exp = null;
    if (_variable.isLocal()) {
      exp = _variable.loadParam();
    } else {
      JExpression classCtx = _this();
      if (ctx.isInnerClass()) {
        classCtx = ((JExpression) cast(ctx.clazz().outer(), ctx.param(TRANSLET_PNAME)));
      }
      exp = classCtx.ref(name);
    }

    if (_variable.getType() instanceof NodeSetType) {
      // The method cloneIterator() also does resetting
      exp = exp.invoke(CLONE_ITERATOR);
    }
    return exp;
  }

}
