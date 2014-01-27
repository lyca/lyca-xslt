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

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.NEW;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author G. Todd Miller
 */
final class FilteredAbsoluteLocationPath extends Expression {
  private Expression _path; // may be null

  public FilteredAbsoluteLocationPath() {
    _path = null;
  }

  public FilteredAbsoluteLocationPath(Expression path) {
    _path = path;
    if (path != null) {
      _path.setParent(this);
    }
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_path != null) {
      _path.setParser(parser);
    }
  }

  public Expression getPath() {
    return _path;
  }

  @Override
  public String toString() {
    return "FilteredAbsoluteLocationPath(" + (_path != null ? _path.toString() : "null") + ')';
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_path != null) {
      final Type ptype = _path.typeCheck(stable);
      if (ptype instanceof NodeType) { // promote to node-set
        _path = new CastExpr(_path, Type.NodeSet);
      }
    }
    return _type = Type.NodeSet;
  }

  @Override
  public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
    final ConstantPoolGen cpg = classGen.getConstantPool();
    final InstructionList il = methodGen.getInstructionList();
    if (_path != null) {
      final int initDFI = cpg.addMethodref(DUP_FILTERED_ITERATOR, "<init>", "(" + NODE_ITERATOR_SIG + ")V");

      // Backwards branches are prohibited if an uninitialized object is
      // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
      // We don't know whether this code might contain backwards branches,
      // so we mustn't create the new object until after we've created
      // the suspect arguments to its constructor. Instead we calculate
      // the values of the arguments to the constructor first, store them
      // in temporary variables, create the object and reload the
      // arguments from the temporaries to avoid the problem.

      // Compile relative path iterator(s)
      final LocalVariableGen pathTemp = methodGen.addLocalVariable("filtered_absolute_location_path_tmp",
              Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
      _path.translate(classGen, methodGen);
      pathTemp.setStart(il.append(new ASTORE(pathTemp.getIndex())));

      // Create new Dup Filter Iterator
      il.append(new NEW(cpg.addClass(DUP_FILTERED_ITERATOR)));
      il.append(DUP);
      pathTemp.setEnd(il.append(new ALOAD(pathTemp.getIndex())));

      // Initialize Dup Filter Iterator with iterator from the stack
      il.append(new INVOKESPECIAL(initDFI));
    } else {
      final int git = cpg.addInterfaceMethodref(DOM_INTF, "getIterator", "()" + NODE_ITERATOR_SIG);
      il.append(methodGen.loadDOM());
      il.append(new INVOKEINTERFACE(git, 1));
    }
  }
}
