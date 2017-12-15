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

import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.NOT_IMPLEMENTED_ERR;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
abstract class Expression extends SyntaxTreeNode {
  /**
   * The type of this expression. It is set after calling
   * <code>typeCheck()</code>.
   */
  protected Type _type;

  public Type getType() {
    return _type;
  }

  @Override
  public abstract String toString();

  public boolean hasPositionCall() {
    return false; // default should be 'false' for StepPattern
  }

  public boolean hasLastCall() {
    return false;
  }

  /**
   * Returns an object representing the compile-time evaluation of an
   * expression. We are only using this for function-available and
   * element-available at this time.
   */
  public Object evaluateAtCompileTime() {
    return null;
  }

  /**
   * Type check all the children of this node.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    return typeCheckContents(stable);
  }

  /**
   * Translate this node into JVM bytecodes.
   */
  @Override
  public void translate(CompilerContext ctx) {
    final ErrorMsg msg = new ErrorMsg(NOT_IMPLEMENTED_ERR, getClass(), this);
    getParser().reportError(FATAL, msg);
  }

  /**
   * Translate this node into a fresh instruction list. The original instruction
   * list is saved and restored.
   * @param ctx TODO
   */
  public JExpression toJExpression(CompilerContext ctx) {
    return null;
    // FIXME
//    final InstructionList result, save = methodGen.getInstructionList();
//    methodGen.setInstructionList(result = new InstructionList());
//    translate(classGen, methodGen);
//    methodGen.setInstructionList(save);
//    return result;
  }

  /**
   * If this expression is of type node-set and it is not a variable reference,
   * then call setStartNode() passing the context node.
   * @param ctx TODO
   * @return TODO
   */
  public JExpression startIterator(CompilerContext ctx, JExpression iter) {
    // Ignore if type is not node-set
    if (!(_type instanceof NodeSetType))
      return iter;

    // setStartNode() should not be called if expr is a variable ref
    Expression expr = this;
    if (expr instanceof CastExpr) {
      expr = ((CastExpr) expr).getExpr();
    }
    if (!(expr instanceof VariableRefBase)) {
      return iter.invoke(SET_START_NODE).arg(ctx.currentNode());
    }
    return iter;
  }

  /**
   * Search for a primop in the symbol table that matches the method type
   * <code>ctype</code>. Two methods match if they have the same arity. If a
   * primop is overloaded then the "closest match" is returned. The first entry
   * in the list of primops that has the right arity is considered to be the
   * default one.
   */
  public MethodType lookupPrimop(SymbolTable stable, String op, MethodType ctype) {
    MethodType result = null;
    final List<MethodType> primop = stable.lookupPrimop(op);
    if (primop != null) {
      final int n = primop.size();
      int minDistance = Integer.MAX_VALUE;
      for (int i = 0; i < n; i++) {
        final MethodType ptype = primop.get(i);
        // Skip if different arity
        if (ptype.argsCount() != ctype.argsCount()) {
          continue;
        }

        // The first method with the right arity is the default
        if (result == null) {
          result = ptype; // default method
        }

        // Check if better than last one found
        final int distance = ctype.distanceTo(ptype);
        if (distance < minDistance) {
          minDistance = distance;
          result = ptype;
        }
      }
    }
    return result;
  }
}
