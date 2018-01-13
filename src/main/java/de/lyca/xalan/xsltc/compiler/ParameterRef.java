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

import static de.lyca.xml.dtm.DTMAxisIterator.CLONE_ITERATOR;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 */
final class ParameterRef extends VariableRefBase {

  /**
   * Name of param being referenced.
   */
  QName _name = null;

  public ParameterRef(Param param) {
    super(param);
    _name = param._name;
  }

  @Override
  public String toString() {
    return "parameter-ref(" + _variable.getName() + '/' + _variable.getType() + ')';
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    /*
     * To fix bug 24518 related to setting parameters of the form
     * {namespaceuri}localName, which will get mapped to an instance variable in
     * the class.
     */
    final String name = BasisLibrary.mapQNameToJavaName(_name.toString());
    JExpression variable;
    if (_variable.isLocal()) {
      variable = _variable.loadParam();
    } else {
      variable = ctx.field(name);
    }

    if (_variable.getType() instanceof NodeSetType) {
      // The method cloneIterator() also does resetting
      variable = variable.invoke(CLONE_ITERATOR);
    }
    return variable;
  }

}
