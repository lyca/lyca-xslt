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

import static com.sun.codemodel.JExpr.TRUE;
import static de.lyca.xalan.xsltc.compiler.Constants.WARNING;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.NodeType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.ResultTreeType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.dtm.DTMAxisIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class ForEach extends Instruction {

  private Expression _select;
  private Type _type;

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("ForEach");
    indent(indent + IndentIncrement);
    Util.println("select " + _select.toString());
    displayContents(indent + IndentIncrement);
  }

  @Override
  public void parseContents(Parser parser) {
    _select = parser.parseExpression(this, "select", null);

    parseChildren(parser);

    // make sure required attribute(s) have been set
    if (_select.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "select");
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _type = _select.typeCheck(stable);

    if (_type instanceof ReferenceType || _type instanceof NodeType) {
      _select = new CastExpr(_select, Type.NodeSet);
      typeCheckContents(stable);
      return Type.Void;
    }
    if (_type instanceof NodeSetType || _type instanceof ResultTreeType) {
      typeCheckContents(stable);
      return Type.Void;
    }
    throw new TypeCheckError(this);
  }

  @Override
  public void translate(CompilerContext ctx) {
 // FIXME
//    final InstructionList il = method.getInstructionList();

    // Save current node and current iterator on the stack
//    il.append(method.loadCurrentNode());
//    il.append(method.loadIterator());

    // Collect sort objects associated with this instruction
    final List<Sort> sortObjects = new ArrayList<>();
    for (SyntaxTreeNode child : getContents()) {
      if (child instanceof Sort) {
        sortObjects.add((Sort) child);
      }
    }

    JExpression iterator;
    if (_type != null && _type instanceof ResultTreeType) {
      // Store existing DOM on stack - must be restored when loop is done
//      il.append(method.loadDOM());

      // <xsl:sort> cannot be applied to a result tree - issue warning
      if (sortObjects.size() > 0) {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.RESULT_TREE_SORT_ERR, this);
        getParser().reportError(WARNING, msg);
      }
      iterator = _select.compile(ctx);//.invoke("setStartNode").arg(params[3]);

      // Put the result tree on the stack (DOM)
//      _select.translate(definedClass, method);
      // Get an iterator for the whole DOM - excluding the root node
      _type.translateTo(ctx.clazz(), ctx.currentMethod(), Type.NodeSet);
      // Store the result tree as the default DOM
//      il.append(SWAP);
//      il.append(method.storeDOM());
    } else {
      // Compile node iterator
      if (sortObjects.size() > 0) {
        iterator = Sort.translateSortIterator(ctx, _select, sortObjects);
      } else {
        JExpression select;
        if(_type instanceof ReferenceType){
          select = _select.compile(ctx);
        }else{
          select = _select.compile(ctx).invoke("setStartNode").arg(ctx.currentNode());
        }
        iterator = ctx.currentBlock().decl(ctx.ref(DTMAxisIterator.class), ctx.nextTmpIterator(), select);
//        _select.translate(definedClass, method);
      }

      if (_type instanceof ReferenceType == false) {
//        iterator = iterator.invoke("setStartNode").arg(ctx.currentNode());
//        il.append(method.loadContextNode());
//        il.append(method.setStartNode());
      }
    }

    // Overwrite current iterator
//    il.append(method.storeIterator());

    // Give local variables (if any) default values before starting loop
    initializeVariables(ctx);

//    final BranchHandle nextNode = il.append(new GOTO(null));
//    final InstructionHandle loop = il.append(NOP);
    final JBlock loop = ctx.currentBlock()._while(TRUE).body();
    JVar current = loop.decl(ctx.owner().INT, ctx.nextCurrent(), iterator.invoke("next"));
    final JConditional _if = loop._if(current.gt(JExpr.lit(0)));

    ctx.pushNode(current);
    ctx.pushBlock(_if._then());
    translateContents(ctx);
    ctx.popBlock();
    ctx.popNode();

    _if._else()._break();
//    nextNode.setTarget(il.append(method.loadIterator()));
//    il.append(method.nextNode());
//    il.append(DUP);
//    il.append(method.storeCurrentNode());
//    il.append(new IFGT(loop));

    // Restore current DOM (if result tree was used instead for this loop)
    if (_type != null && _type instanceof ResultTreeType) {
//      il.append(method.storeDOM());
    }

    // Restore current node and current iterator from the stack
//    il.append(method.storeIterator());
//    il.append(method.storeCurrentNode());
  }

  /**
   * The code that is generated by nested for-each loops can appear to some JVMs
   * as if it is accessing un-initialized variables. We must add some code that
   * pushes the default variable value on the stack and pops it into the
   * variable slot. This is done by the Variable.initialize() method. The code
   * that we compile for this loop looks like this:
   * 
   * initialize iterator initialize variables <-- HERE!!! goto Iterate Loop: : :
   * (code for <xsl:for-each> contents) : Iterate: node = iterator.next(); if
   * (node != END) goto Loop
   */
  public void initializeVariables(CompilerContext ctx) {
    for (SyntaxTreeNode child : getContents()) {
      if (child instanceof Variable) {
        final Variable var = (Variable) child;
        var.initialize(ctx);
      }
    }
  }

}
