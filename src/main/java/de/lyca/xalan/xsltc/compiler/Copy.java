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
import static com.sun.codemodel.JExpr.lit;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class Copy extends Instruction {
  private UseAttributeSets _useSets;

  @Override
  public void parseContents(Parser parser) {
    final String useSets = getAttribute("use-attribute-sets");
    if (useSets.length() > 0) {
      if (!Util.isValidQNames(useSets)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, useSets, this);
        parser.reportError(Constants.ERROR, err);
      }
      _useSets = new UseAttributeSets(useSets, parser);
    }
    parseChildren(parser);
  }

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("Copy");
    indent(indent + IndentIncrement);
    displayContents(indent + IndentIncrement);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_useSets != null) {
      _useSets.typeCheck(stable);
    }
    typeCheckContents(stable);
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    // FIXME
    JBlock body = ctx.currentBlock();

    // Get the name of the node to copy and save for later
    JVar name = body.decl(ctx.ref(String.class), "name", ctx.currentDom().invoke("shallowCopy").arg(ctx.currentNode()).arg(ctx.currentHandler()));

    // final BranchHandle ifBlock1 = il.append(new IFNULL(null));
    JBlock _if1 = body._if(name.ne(_null()))._then();

    // Get the length of the node name and save for later
    JVar length = _if1.decl(ctx.owner().INT, "length", name.invoke("length"));

    ctx.pushBlock(_if1);
    // Copy in attribute sets if specified
    if (_useSets != null) {
      // If the parent of this element will result in an element being
      // output then we know that it is safe to copy out the attributes
      final SyntaxTreeNode parent = getParent();
      if (parent instanceof LiteralElement || parent instanceof LiteralElement) {
        _useSets.translate(ctx);
      }
      // If not we have to check to see if the copy will result in an
      // element being output.
      else {
        // check if element; if not skip to translate body
//        il.append(new ILOAD(length.getIndex()));
//        final BranchHandle ifBlock2 = il.append(new IFEQ(null));
        // length != 0 -> element -> do attribute sets
        _useSets.translate(ctx);
        // not an element; root
//        ifBlock2.setTarget(il.append(NOP));
      }
    }

    // Instantiate body of xsl:copy
    translateContents(ctx);
    ctx.popBlock();
    // Call the output handler's endElement() if we copied an element
    // (The DOM.shallowCopy() method calls startElement().)
//    length.setEnd(il.append(new ILOAD(length.getIndex())));
//    final BranchHandle ifBlock3 = il.append(new IFEQ(null));
//    il.append(methodGen.loadHandler());
//    name.setEnd(il.append(new ALOAD(name.getIndex())));
//    il.append(methodGen.endElement());
    
    JBlock _if3 = _if1._if(length.ne(lit(0)))._then();
    _if3.invoke(ctx.currentHandler(), "endElement").arg(name);
//    final InstructionHandle end = il.append(NOP);
//    ifBlock1.setTarget(end);
//    ifBlock3.setTarget(end);
//    methodGen.removeLocalVariable(name);
//    methodGen.removeLocalVariable(length);
  }
}
