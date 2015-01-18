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

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.invoke;
import static de.lyca.xalan.xsltc.compiler.Constants.ITERATOR_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.POP_PARAM_FRAME;
import static de.lyca.xalan.xsltc.compiler.Constants.PUSH_PARAM_FRAME;

import java.util.List;

import com.sun.codemodel.JInvocation;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
final class CallTemplate extends Instruction {

  /**
   * Name of template to call.
   */
  private QName _name;

  /**
   * The array of effective parameters in this CallTemplate. An object in this
   * array can be either a WithParam or a Param if no WithParam exists for a
   * particular parameter.
   */
  private SyntaxTreeNode[] _parameters = null;

  /**
   * The corresponding template which this CallTemplate calls.
   */
  private Template _calleeTemplate = null;

  @Override
  public void display(int indent) {
    indent(indent);
    System.out.print("CallTemplate");
    Util.println(" name " + _name);
    displayContents(indent + IndentIncrement);
  }

  public boolean hasWithParams() {
    return elementCount() > 0;
  }

  @Override
  public void parseContents(Parser parser) {
    final String name = getAttribute("name");
    if (name.length() > 0) {
      if (!XML11Char.isXML11ValidQName(name)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name, this);
        parser.reportError(Constants.ERROR, err);
      }
      _name = parser.getQNameIgnoreDefaultNs(name);
    } else {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
    }
    parseChildren(parser);
  }

  /**
   * Verify that a template with this name exists.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Template template = stable.lookupTemplate(_name);
    if (template != null) {
      typeCheckContents(stable);
    } else {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.TEMPLATE_UNDEF_ERR, _name, this);
      throw new TypeCheckError(err);
    }
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    final Stylesheet stylesheet = getStylesheet();

    // If there are Params in the stylesheet or WithParams in this call?
    if (stylesheet.hasLocalParams() || hasContents()) {
      _calleeTemplate = getCalleeTemplate();

      // Build the parameter list if the called template is simple named
      if (_calleeTemplate != null) {
        buildParameterList();
      }
      // This is only needed when the called template is not
      // a simple named template.
      else {
        // Push parameter frame
        ctx.currentBlock().invoke(PUSH_PARAM_FRAME);
        translateContents(ctx);
      }
    }

    // Generate a valid Java method name
//    final String className = stylesheet.getClassName();
    final String methodName = Util.escape(_name.toString());

    // Load standard arguments
//    il.append(classGen.loadTranslet());
//    il.append(methodGen.loadDOM());
//    il.append(methodGen.loadIterator());
//    il.append(methodGen.loadHandler());
//    il.append(methodGen.loadCurrentNode());

    // Initialize prefix of method signature
//    final StringBuilder methodSig = new StringBuilder("(" + DOM_INTF_SIG + NODE_ITERATOR_SIG + TRANSLET_OUTPUT_SIG
//            + NODE_SIG);

    JInvocation callTemplate = invoke(methodName).arg(ctx.currentDom()).arg(ctx.param(ITERATOR_PNAME)).arg(ctx.currentHandler()).arg(ctx.currentNode());
    // If calling a simply named template, push actual arguments
    if (_calleeTemplate != null) {
//       List<Param> calleeParams = _calleeTemplate.getParameters();
      for (final SyntaxTreeNode node : _parameters) {
//        methodSig.append(OBJECT_SIG); // append Object to signature
        // Push 'null' if Param to indicate no actual parameter specified
        if (node instanceof Param) {
          callTemplate = callTemplate.arg(_null());
        } else {
          // translate WithParam
//          callTemplate = callTemplate.arg(_null());
          callTemplate = callTemplate.arg(((WithParam)node).translateValue(ctx));
        }
      }
    }

    // Complete signature and generate invokevirtual call
//    methodSig.append(")V");
//    il.append(new INVOKEVIRTUAL(cpg.addMethodref(className, methodName, methodSig.toString())));

    ctx.currentBlock().add(callTemplate);

    // Do not need to call Translet.popParamFrame() if we are
    // calling a simple named template.
    if (_calleeTemplate == null && (stylesheet.hasLocalParams() || hasContents())) {
      // Pop parameter frame
      ctx.currentBlock().invoke(POP_PARAM_FRAME);
    }
  }

  /**
   * Return the simple named template which this CallTemplate calls. Return
   * false if there is no matched template or the matched template is not a
   * simple named template.
   */
  public Template getCalleeTemplate() {
    final Template foundTemplate = getXSLTC().getParser().getSymbolTable().lookupTemplate(_name);

    return foundTemplate.isSimpleNamedTemplate() ? foundTemplate : null;
  }

  /**
   * Build the list of effective parameters in this CallTemplate. The parameters
   * of the called template are put into the array first. Then we visit the
   * WithParam children of this CallTemplate and replace the Param with a
   * corresponding WithParam having the same name.
   */
  private void buildParameterList() {
    // Put the parameters from the called template into the array first.
    // This is to ensure the order of the parameters.
    final List<Param> defaultParams = _calleeTemplate.getParameters();
    final int numParams = defaultParams.size();
    _parameters = defaultParams.toArray(new SyntaxTreeNode[numParams]);

    // Replace a Param with a WithParam if they have the same name.
    for (final SyntaxTreeNode node : getContents()) {
      // Ignore if not WithParam
      if (node instanceof WithParam) {
        final WithParam withParam = (WithParam) node;
        final QName name = withParam.getName();

        // Search for a Param with the same name
        for (int k = 0; k < numParams; k++) {
          final SyntaxTreeNode object = _parameters[k];
          if (object instanceof Param && ((Param) object).getName().equals(name)) {
            withParam.setDoParameterOptimization(true);
            _parameters[k] = withParam;
            break;
          } else if (object instanceof WithParam && ((WithParam) object).getName().equals(name)) {
            withParam.setDoParameterOptimization(true);
            _parameters[k] = withParam;
            break;
          }
        }
      }
    }
  }
}
