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

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.compiler.Constants.ADD_PARAMETER;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author John Howard <JohnH@schemasoft.com>
 */
final class WithParam extends Instruction {

  /**
   * Parameter's name.
   */
  private QName _name;

  /**
   * The escaped qname of the with-param.
   */
  protected String _escapedName;

  /**
   * Parameter's default value.
   */
  private Expression _select;

  /**
   * %OPT% This is set to true when the WithParam is used in a CallTemplate for
   * a simple named template. If this is true, the parameters are passed to the
   * named template through method arguments rather than using the expensive
   * Translet.addParameter() call.
   */
  private boolean _doParameterOptimization = false;

  /**
   * Displays the contents of this element
   */
  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("with-param " + _name);
    if (_select != null) {
      indent(indent + IndentIncrement);
      Util.println("select " + _select.toString());
    }
    displayContents(indent + IndentIncrement);
  }

  /**
   * Returns the escaped qname of the parameter
   */
  public String getEscapedName() {
    return _escapedName;
  }

  /**
   * Return the name of this WithParam.
   */
  public QName getName() {
    return _name;
  }

  /**
   * Set the name of the variable or paremeter. Escape all special chars.
   */
  public void setName(QName name) {
    _name = name;
    _escapedName = Util.escape(name.getStringRep());
  }

  /**
   * Set the do parameter optimization flag
   */
  public void setDoParameterOptimization(boolean flag) {
    _doParameterOptimization = flag;
  }

  /**
   * The contents of a <xsl:with-param> elements are either in the element's
   * 'select' attribute (this has precedence) or in the element body.
   */
  @Override
  public void parseContents(Parser parser) {
    final String name = getAttribute("name");
    if (name.length() > 0) {
      if (!XML11Char.isXML11ValidQName(name)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name, this);
        parser.reportError(Constants.ERROR, err);
      }
      setName(parser.getQNameIgnoreDefaultNs(name));
    } else {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
    }

    final String select = getAttribute("select");
    if (select.length() > 0) {
      _select = parser.parseExpression(this, "select", null);
    }

    parseChildren(parser);
  }

  /**
   * Type-check either the select attribute or the element body, depending on
   * which is in use.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_select != null) {
      final Type tselect = _select.typeCheck(stable);
      if (tselect instanceof ReferenceType == false) {
        _select = new CastExpr(_select, Type.Reference);
      }
    } else {
      typeCheckContents(stable);
    }
    return Type.Void;
  }

  /**
   * Compile the value of the parameter, which is either in an expression in a
   * 'select' attribute, or in the with-param element's body
   */
  public JExpression translateValue(CompilerContext ctx) {
    // FIXME

    // Compile expression is 'select' attribute if present
    if (_select != null) {
      JExpression select = _select.toJExpression(ctx);
      return _select.startIterator(ctx, select);
    }
    // If not, compile result tree from parameter body if present.
    else if (hasContents()) {
      return compileResultTree(ctx);
    }
    // If neither are present then store empty string in parameter slot
    else {
      return lit("");
    }
  }

  /**
   * This code generates a sequence of bytecodes that call the addParameter()
   * method in AbstractTranslet. The method call will add (or update) the
   * parameter frame with the new parameter value.
   */
  @Override
  public void translate(CompilerContext ctx) {
//    FIXME
    // Translate the value and put it on the stack
    if (_doParameterOptimization) {
//      translateValue(classGen, methodGen);
      return;
    }

    // Make name acceptable for use as field name in class
    final String name = getEscapedName();
    // Generate the value of the parameter (use value in 'select' by def.)
    JExpression translateValue = translateValue(ctx);
    // Pass the parameter to the template
    // Load the name of the parameter
    // Mark this parameter value is not being the default value
    ctx.currentBlock().invoke(ADD_PARAMETER).arg(name).arg(translateValue).arg(FALSE);
  }

}
