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

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;

import de.lyca.xalan.xsltc.compiler.util.BooleanType;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.RealType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

final class Variable extends VariableBase {

  /**
   * Parse the contents of the variable
   */
  @Override
  public void parseContents(Parser parser) {
    // Parse 'name' and 'select' attributes plus parameter contents
    super.parseContents(parser);

    // Add a ref to this var to its enclosing construct
    final SyntaxTreeNode parent = getParent();
    if (parent instanceof Stylesheet) {
      // Mark this as a global variable
      _isLocal = false;
      // Check if a global variable with this name already exists...
      final Variable var = parser.getSymbolTable().lookupVariable(_name);
      // ...and if it does we need to check import precedence
      if (var != null) {
        final int us = this.getImportPrecedence();
        final int them = var.getImportPrecedence();
        // It is an error if the two have the same import precedence
        if (us == them) {
          final String name = _name.toString();
          reportError(this, parser, Messages.get().variableRedefErr(name));
        }
        // Ignore this if previous definition has higher precedence
        else if (them > us) {
          _ignore = true;
          return;
        } else {
          var.disable();
        }
        // Add this variable if we have higher precedence
      }
      ((Stylesheet) parent).addVariable(this);
      parser.getSymbolTable().addVariable(this);
    } else {
      _isLocal = true;
    }
  }

  /**
   * Runs a type check on either the variable element body or the expression in
   * the 'select' attribute
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_select != null && hasContents()) {
      // TODO better error reporting
      final ErrorMsg err = new ErrorMsg(this,
          Messages.get().internalErr("xsl:variable element must not have both content and a select attribute"));
      throw new TypeCheckError(err);
    }

    // Type check the 'select' expression if present
    if (_select != null) {
      _type = _select.typeCheck(stable);
    }
    // Type check the element contents otherwise
    else if (hasContents()) {
      typeCheckContents(stable);
      _type = Type.ResultTree;
    } else {
      _type = Type.Reference;
    }
    // The return type is void as the variable element does not leave
    // anything on the JVM's stack. The '_type' global will be returned
    // by the references to this variable, and not by the variable itself.
    return Type.Void;
  }

  /**
   * This method is part of a little trick that is needed to use local variables
   * inside nested for-each loops. See the initializeVariables() method in the
   * ForEach class for an explanation
   */
  public void initialize(CompilerContext ctx) {
    // This is only done for local variables that are actually used
    if (isLocal() && !_refs.isEmpty()) {
      // Create the default value
      JExpression defaultValue;
      if (_type instanceof IntType || _type instanceof NodeType) {
        defaultValue = lit(0); // 0 for node-id, integer
      } else if (_type instanceof BooleanType) {
        defaultValue = FALSE; // false for boolean
      } else if (_type instanceof RealType) {
        defaultValue = lit(0.0); // 0.0 for floating point numbers
      } else {
        defaultValue = _null(); // and 'null' for anything else
      }

      // Create a variable slot if none is allocated
      if (_param == null) {
        _param = ctx.currentBlock().decl(_type.toJCType(), getEscapedName(), defaultValue);
      } else {
        ctx.currentBlock().assign(_param, defaultValue);
      }
    }
  }

  @Override
  public void translate(CompilerContext ctx) {
    // Don't generate code for unreferenced variables
    if (_refs.isEmpty()) {
      _ignore = true;
    }

    // Make sure that a variable instance is only compiled once
    if (_ignore)
      return;
    _ignore = true;

    final String name = getEscapedName();

    if (isLocal()) {
      // Add a new local variable and store value
      if (_param == null) {
        mapRegister(ctx);
      }
      // Compile variable value computation
      ctx.currentBlock().assign(_param, compileValue(ctx));
    } else {
      // Global variables are store in class fields
      if (ctx.field(name) == null) {
        JFieldVar field = ctx.field(JMod.PUBLIC, _type.toJCType(), name);
        // Compile variable value computation
        // Store the variable in the allocated field
        ctx.currentBlock().assign(field, compileValue(ctx));
      }
    }
  }
}
