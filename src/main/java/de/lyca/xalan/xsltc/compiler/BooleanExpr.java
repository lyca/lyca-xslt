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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * This class implements inlined calls to the XSLT standard functions true() and
 * false().
 * 
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class BooleanExpr extends Expression {
  private final boolean _value;

  public BooleanExpr(boolean value) {
    _value = value;
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _type = Type.Boolean;
    return _type;
  }

  @Override
  public String toString() {
    return _value ? "true()" : "false()";
  }

  public boolean getValue() {
    return _value;
  }

  @Override
  public boolean contextDependent() {
    return false;
  }

  @Override
  public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
    final ConstantPoolGen cpg = classGen.getConstantPool();
    final InstructionList il = methodGen.getInstructionList();
    il.append(new PUSH(cpg, _value));
  }

  @Override
  public void translateDesynthesized(ClassGenerator classGen, MethodGenerator methodGen) {
    final InstructionList il = methodGen.getInstructionList();
    if (_value) {
      il.append(NOP); // true list falls through
    } else {
      _falseList.add(il.append(new GOTO(null)));
    }
  }
}
