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

import static com.sun.codemodel.JExpr.invoke;
import static de.lyca.xalan.xsltc.compiler.Constants.ITERATOR_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_OUTPUT_PNAME;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JStatement;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 */
public final class Template extends TopLevelElement implements Comparable<Template> {

  private QName _name; // The name of the template (if any)
  private QName _mode; // Mode in which this template is instantiated.
  private Pattern _pattern; // Matching pattern defined for this template.
  private double _priority; // Matching priority of this template.
  private int _position; // Position within stylesheet (prio. resolution)
  private boolean _disabled = false;
  private boolean _compiled = false;// make sure it is compiled only once
  private boolean _simplified = false;

  // True if this is a simple named template. A simple named
  // template is a template which only has a name but no match pattern.
  private boolean _isSimpleNamedTemplate = false;

  // The list of parameters in this template. This is only used
  // for simple named templates.
  private final List<Param> _parameters = new ArrayList<>();

  public boolean hasParams() {
    return _parameters.size() > 0;
  }

  public boolean isSimplified() {
    return _simplified;
  }

  public void setSimplified() {
    _simplified = true;
  }

  public boolean isSimpleNamedTemplate() {
    return _isSimpleNamedTemplate;
  }

  public void addParameter(Param param) {
    _parameters.add(param);
  }

  public List<Param> getParameters() {
    return _parameters;
  }

  public void disable() {
    _disabled = true;
  }

  public boolean disabled() {
    return _disabled;
  }

  public double getPriority() {
    return _priority;
  }

  public int getPosition() {
    return _position;
  }

  public boolean isNamed() {
    return _name != null;
  }

  public Pattern getPattern() {
    return _pattern;
  }

  public QName getName() {
    return _name;
  }

  public void setName(QName qname) {
    if (_name == null) {
      _name = qname;
    }
  }

  public QName getModeName() {
    return _mode;
  }

  /**
   * Compare this template to another. First checks priority, then position.
   */
  @Override
  public int compareTo(Template other) {
    if (_priority > other._priority)
      return 1;
    else if (_priority < other._priority)
      return -1;
    else if (_position > other._position)
      return 1;
    else if (_position < other._position)
      return -1;
    else
      return 0;
  }

  private boolean resolveNamedTemplates(Template other, Parser parser) {

    if (other == null)
      return true;

    final SymbolTable stable = parser.getSymbolTable();

    final int us = this.getImportPrecedence();
    final int them = other.getImportPrecedence();

    if (us > them) {
      other.disable();
      return true;
    } else if (us < them) {
      stable.addTemplate(other);
      this.disable();
      return true;
    } else
      return false;
  }

  private Stylesheet _stylesheet = null;

  @Override
  public Stylesheet getStylesheet() {
    return _stylesheet;
  }

  @Override
  public void parseContents(Parser parser) {

    if (!(getParent() instanceof Stylesheet)) {
      // TODO better error reporting ILLEGAL_PARENT
      final ErrorMsg err = new ErrorMsg(this, Messages.get().internalErr("Parent is not Stylesheet"));
      parser.reportError(Constants.ERROR, err);
    }

    final String name = getAttribute("name");
    final String mode = getAttribute("mode");
    final String match = getAttribute("match");
    final String priority = getAttribute("priority");

    // TODO Perhaps revisit matcherr08
    if (name.isEmpty() && match.isEmpty()) {
      // TODO better error reporting ER_NEED_NAME_OR_MATCH_ATTRIB
      final ErrorMsg err = new ErrorMsg(this, Messages.get().internalErr("Name and match are empty"));
      parser.reportError(Constants.ERROR, err);
    }

    _stylesheet = super.getStylesheet();

    if (name.length() > 0) {
      if (!XML11Char.isXML11ValidQName(name)) {
        final ErrorMsg err = new ErrorMsg(this, Messages.get().invalidQnameErr(name));
        parser.reportError(Constants.ERROR, err);
      }
      _name = parser.getQNameIgnoreDefaultNs(name);
    }

    if (mode.length() > 0) {
      if (!XML11Char.isXML11ValidQName(mode)) {
        final ErrorMsg err = new ErrorMsg(this, Messages.get().invalidQnameErr(mode));
        parser.reportError(Constants.ERROR, err);
      }
      _mode = parser.getQNameIgnoreDefaultNs(mode);
    }

    if (match.length() > 0) {
      _pattern = parser.parsePattern(this, "match", null);
    }

    if (priority.length() > 0) {
      _priority = Double.parseDouble(priority);
    } else {
      if (_pattern != null) {
        _priority = _pattern.getPriority();
      } else {
        _priority = Double.NaN;
      }
    }

    _position = parser.getTemplateIndex();

    // Add the (named) template to the symbol table
    if (_name != null) {
      final Template other = parser.getSymbolTable().addTemplate(this);
      if (!resolveNamedTemplates(other, parser)) {
        final ErrorMsg err = new ErrorMsg(this, Messages.get().templateRedefErr(_name));
        parser.reportError(Constants.ERROR, err);
      }
      // Is this a simple named template?
      if (_pattern == null && _mode == null) {
        _isSimpleNamedTemplate = true;
      }
    }

    if (_parent instanceof Stylesheet) {
      ((Stylesheet) _parent).addTemplate(this);
    }

    parser.setTemplate(this); // set current template
    parseChildren(parser);
    parser.setTemplate(null); // clear template
  }

  /**
   * When the parser realises that it is dealign with a simplified stylesheet it
   * will create an empty Stylesheet object with the root element of the
   * stylesheet (a LiteralElement object) as its only child. The Stylesheet
   * object will then create this Template object and invoke this method to
   * force some specific behaviour. What we need to do is: o) create a pattern
   * matching on the root node o) add the LRE root node (the only child of the
   * Stylesheet) as our only child node o) set the empty Stylesheet as our
   * parent o) set this template as the Stylesheet's only child
   * @param stylesheet TODO
   * @param parser TODO
   */
  public void parseSimplified(Stylesheet stylesheet, Parser parser) {

    _stylesheet = stylesheet;
    setParent(stylesheet);

    _name = null;
    _mode = null;
    _priority = Double.NaN;
    _pattern = parser.parsePattern(this, "/");

    final List<SyntaxTreeNode> contents = _stylesheet.getContents();
    final SyntaxTreeNode root = contents.get(0);

    if (root instanceof LiteralElement) {
      addElement(root);
      root.setParent(this);
      contents.set(0, this);
      parser.setTemplate(this);
      root.parseContents(parser);
      parser.setTemplate(null);
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    List<SyntaxTreeNode> contents = getContents();
    int size = contents.size();
    boolean inParams = true;
    for (int i = 0; i < size; i++) {
      SyntaxTreeNode child = contents.get(i);
      if (!(child instanceof Param || child instanceof Text && ((Text) child).isIgnore())) {
        inParams = false;
      } else if (!inParams && child instanceof Param) {
        // TODO better error reporting ER_NOT_ALLOWED_IN_POSITION
        final ErrorMsg err = new ErrorMsg(this, Messages.get().internalErr("Params must be first in template"));
        throw new TypeCheckError(err);
      }
    }
    if (_pattern != null) {
      _pattern.typeCheck(stable);
    }

    return typeCheckContents(stable);
  }

  /**
   * Translate this node into a fresh instruction list. The original instruction
   * list is saved and restored.
   */
  @Override
  public JStatement compile(CompilerContext ctx) {
    if (!isNamed()) {
      CompilerContext newCtx = new CompilerContext(ctx.owner(), ctx.clazz(), ctx.stylesheet(), ctx.xsltc());
      newCtx.pushMethodContext(ctx.currentMethodContext());
      newCtx.pushNode(ctx.currentNode());
      newCtx.pushHandler(ctx.param(TRANSLET_OUTPUT_PNAME));
      newCtx.pushBlock(new JBlock());
      translate(newCtx);
      return newCtx.popBlock();
    }
    final String methodName = Util.escape(_name.toString());
    return invoke(methodName).arg(ctx.currentDom()).arg(ctx.param(ITERATOR_PNAME)).arg(ctx.param(TRANSLET_OUTPUT_PNAME))
        .arg(ctx.currentNode());
  }

  @Override
  public void translate(CompilerContext ctx) {

    if (_disabled)
      return;
    // bug fix #4433133, add a call to named template from applyTemplates
    // XXX final String className = classGen.getClassName();

    if (_compiled && isNamed()) {
      final String methodName = Util.escape(_name.toString());
      ctx.currentBlock().invoke(methodName).arg(ctx.currentDom()).arg(ctx.param(ITERATOR_PNAME))
          .arg(ctx.param(TRANSLET_OUTPUT_PNAME)).arg(ctx.currentNode());
      return;
    }

    if (_compiled)
      return;
    _compiled = true;

    // %OPT% Special handling for simple named templates.
    if (_isSimpleNamedTemplate) {
      final int numParams = _parameters.size();

      // Update load/store instructions to access Params from the stack
      for (int i = 0; i < numParams; i++) {
        // FIXME
        final Param param = _parameters.get(i);
        param.storeParam(ctx.param(param.getEscapedName()));
      }
    }
    translateContents(ctx);
  }

}
