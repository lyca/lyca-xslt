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

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.invoke;
import static de.lyca.xalan.xsltc.compiler.Constants.ADD_PARAMETER;

import java.util.List;

import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.ObjectType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 * @author John Howard {@literal <JohnH@schemasoft.com>}
 */
final class Param extends VariableBase {

  /**
   * True if this Param is declared in a simple named template. This is used to
   * optimize codegen for parameter passing in named templates.
   */
  private boolean _isInSimpleNamedTemplate = false;

  /**
   * Display variable as single string
   */
  @Override
  public String toString() {
    return "param(" + _name + ")";
  }

  /**
   * Parse the contents of the <xsl:param> element. This method must read the
   * 'name' (required) and 'select' (optional) attributes.
   */
  @Override
  public void parseContents(Parser parser) {

    // Parse 'name' and 'select' attributes plus parameter contents
    super.parseContents(parser);

    // Add a ref to this param to its enclosing construct
    final SyntaxTreeNode parent = getParent();
    if (parent instanceof Stylesheet) {
      // Mark this as a global parameter
      _isLocal = false;
      // Check if a global variable with this name already exists...
      final Param param = parser.getSymbolTable().lookupParam(_name);
      // ...and if it does we need to check import precedence
      if (param != null) {
        final int us = this.getImportPrecedence();
        final int them = param.getImportPrecedence();
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
          param.disable();
        }
      }
      // Add this variable if we have higher precedence
      ((Stylesheet) parent).addParam(this);
      parser.getSymbolTable().addParam(this);
    } else if (parent instanceof Template) {
      final Template template = (Template) parent;
      _isLocal = true;
      template.addParameter(this);
      if (template.isSimpleNamedTemplate()) {
        _isInSimpleNamedTemplate = true;
      }
    }
  }

  /**
   * Type-checks the parameter. The parameter type is determined by the 'select'
   * expression (if present) or is a result tree if the parameter element has a
   * body and no 'select' expression.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_select != null && hasContents()) {
      List<SyntaxTreeNode> contents = getContents();
      int size = contents.size();
      for (int i = 0; i < size; i++) {
        SyntaxTreeNode child = contents.get(i);
        if (child instanceof Text && ((Text) child).isIgnore())
          continue;
        // TODO better error reporting
        final ErrorMsg err = new ErrorMsg(this,
            Messages.get().internalErr("xsl:param element must not have both content and a select attribute"));
        throw new TypeCheckError(err);
      }
    }
    if (!(getParent() instanceof Stylesheet || getParent() instanceof Template)) {
      // TODO better error reporting
      final ErrorMsg err = new ErrorMsg(this, Messages.get().internalErr("Parent is not Stylesheet or Template"));
      throw new TypeCheckError(err);
    }

    if (_select != null) {
      _type = _select.typeCheck(stable);
      if (_type instanceof ReferenceType == false && !(_type instanceof ObjectType)) {
        _select = new CastExpr(_select, Type.Reference);
      }
    } else if (hasContents()) {
      typeCheckContents(stable);
    }
    _type = Type.Reference;

    // This element has no type (the parameter does, but the parameter
    // element itself does not).
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    // FIXME

    if (_ignore)
      return;
    _ignore = true;

    /*
     * To fix bug 24518 related to setting parameters of the form
     * {namespaceuri}localName which will get mapped to an instance variable in
     * the class.
     */
    final String name = _escapedName;// BasisLibrary.mapQNameToJavaName(_name.toString());

    if (isLocal()) {
      /*
       * If simple named template then generate a conditional init of the param
       * using its default value: if (param == null) param = <default-value>
       */
      if (_isInSimpleNamedTemplate) {
        _param = ctx.param(name);
        JConditional _if = ctx.currentBlock()._if(_param.eq(_null()));
        ctx.pushBlock(_if._then());
        ctx.currentBlock().assign(_param, compileValue(ctx));
        ctx.popBlock();
        return;
      }

      if (_refs.isEmpty()) { // nobody uses the value
        _param = null;
      } else { // normal case
        // Call addParameter() from this class
        JExpression addParameter = invoke(ADD_PARAMETER).arg(name).arg(compileValue(ctx)).arg(JExpr.TRUE);
        // Cache the result of addParameter() in a local variable
        _param = ctx.currentBlock().decl(_type.toJCType(), name, addParameter);
      }
    } else {
      if (ctx.field(name) == null) {
        JVar field = ctx.addPublicField(_type.toJCType(), name);
        // Call addParameter() from this class
        JExpression addParameter = invoke(ADD_PARAMETER).arg(name).arg(compileValue(ctx)).arg(JExpr.TRUE);
        // Cache the result of addParameter() in a field
        ctx.currentBlock().assign(field, addParameter);
      }
    }
  }

}
