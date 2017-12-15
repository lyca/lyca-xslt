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

import static de.lyca.xalan.xsltc.compiler.Constants.ITERATOR_PNAME;
import static de.lyca.xml.dtm.DTMAxisIterator.GET_LAST;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.dom.CurrentNodeListFilter;
import de.lyca.xalan.xsltc.dom.NodeSortRecord;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class LastCall extends FunctionCall {

  public LastCall(QName fname) {
    super(fname);
  }

  @Override
  public boolean hasPositionCall() {
    return true;
  }

  @Override
  public boolean hasLastCall() {
    return true;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    if (ctx.ref(NodeSortRecord.class).isAssignableFrom(ctx.clazz())) {
      return ctx.param("last");
    }
    if (ctx.ref(CurrentNodeListFilter.class).isAssignableFrom(ctx.clazz())) {
      return ctx.param("last");
    }
    // FIXME
    if ("iterator0".equals(ctx.currentTmpIterator())) {
      return ctx.param(ITERATOR_PNAME).invoke(GET_LAST);
    }
    return JExpr.direct(ctx.currentTmpIterator()).invoke(GET_LAST);
  }

}
