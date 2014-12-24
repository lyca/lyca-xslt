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

import java.util.Iterator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.dom.CurrentNodeListFilter;

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
  public JExpression compile(CompilerContext ctx) {
    for (Iterator<JClass> classes = ctx.clazz()._implements(); classes.hasNext();) {
      if (classes.next().isAssignableFrom(ctx.ref(CurrentNodeListFilter.class))) {
        return ctx.param("last");
      }
    }
    JExpression currentNode = ctx.currentNode();
    if (currentNode == null) {
      currentNode = ctx.param(ITERATOR_PNAME).invoke("getLast");
    }
    return currentNode;
  }

  @Override
  public void translate(CompilerContext ctx) {
 // FIXME
//    final InstructionList il = methodGen.getInstructionList();
//
//    if (methodGen instanceof CompareGenerator) {
//      il.append(((CompareGenerator) methodGen).loadLastNode());
//    } else if (methodGen instanceof TestGenerator) {
//      il.append(new ILOAD(LAST_INDEX));
//    } else {
//      final ConstantPoolGen cpg = classGen.getConstantPool();
//      final int getLast = cpg.addInterfaceMethodref(NODE_ITERATOR, "getLast", "()I");
//      il.append(methodGen.loadIterator());
//      il.append(new INVOKEINTERFACE(getLast, 1));
//    }
  }
}
