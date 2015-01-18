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
import static com.sun.codemodel.JExpr.lit;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.NodeSetType;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Morten Jorgensen
 * @author Santiago Pericas-Geertsen
 */
final class Key extends TopLevelElement {

  /**
   * The name of this key as defined in xsl:key.
   */
  private QName _name;

  /**
   * The pattern to match starting at the root node.
   */
  private Pattern _match;

  /**
   * The expression that generates the values for this key.
   */
  private Expression _use;

  /**
   * The type of the _use expression.
   */
  private Type _useType;

  /**
   * Parse the <xsl:key> element and attributes
   * 
   * @param parser
   *          A reference to the stylesheet parser
   */
  @Override
  public void parseContents(Parser parser) {

    // Get the required attributes and parser XPath expressions
    final String name = getAttribute("name");
    if (!XML11Char.isXML11ValidQName(name)) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name, this);
      parser.reportError(Constants.ERROR, err);
    }

    // Parse key name and add to symbol table
    _name = parser.getQNameIgnoreDefaultNs(name);
    getSymbolTable().addKey(_name, this);

    _match = parser.parsePattern(this, "match", null);
    _use = parser.parseExpression(this, "use", null);

    // Make sure required attribute(s) have been set
    if (_name == null) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
      return;
    }
    if (_match.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "match");
      return;
    }
    if (_use.isDummy()) {
      reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "use");
      return;
    }
  }

  /**
   * Returns a String-representation of this key's name
   * 
   * @return The key's name (from the <xsl:key> elements 'name' attribute).
   */
  public String getName() {
    return _name.toString();
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    // Type check match pattern
    _match.typeCheck(stable);

    // Cast node values to string values (except for nodesets)
    _useType = _use.typeCheck(stable);
    if (_useType instanceof StringType == false && _useType instanceof NodeSetType == false) {
      _use = new CastExpr(_use, Type.String);
    }

    return Type.Void;
  }

  /**
   * This method is called if the "use" attribute of the key contains a node
   * set. In this case we must traverse all nodes in the set and create one
   * entry in this key's index for each node in the set.
   */
  public void traverseNodeSet(JDefinedClass definedClass, JMethod method, int buildKeyIndex) {
 // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // DOM.getStringValueX(nodeIndex) => String
//    final int getNodeValue = cpg.addInterfaceMethodref(DOM_INTF, GET_NODE_VALUE, "(I)" + STRING_SIG);
//
//    cpg.addInterfaceMethodref(DOM_INTF, "getNodeIdent", "(I)" + NODE_SIG);
//
//    // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
//    final int keyDom = cpg.addMethodref(TRANSLET_CLASS, "setKeyIndexDom", "(" + STRING_SIG + DOM_INTF_SIG + ")V");
//
//    // This variable holds the id of the node we found with the "match"
//    // attribute of xsl:key. This is the id we store, with the value we
//    // get from the nodes we find here, in the index for this key.
//    final LocalVariableGen parentNode = methodGen.addLocalVariable("parentNode", Util.getJCRefType("I"), null, null);
//
//    // Get the 'parameter' from the stack and store it in a local var.
//    parentNode.setStart(il.append(new ISTORE(parentNode.getIndex())));
//
//    // Save current node and current iterator on the stack
//    il.append(methodGen.loadCurrentNode());
//    il.append(methodGen.loadIterator());
//
//    // Overwrite current iterator with one that gives us only what we want
//    _use.translate(classGen, methodGen);
//    _use.startIterator(classGen, methodGen);
//    il.append(methodGen.storeIterator());
//
//    final BranchHandle nextNode = il.append(new GOTO(null));
//    final InstructionHandle loop = il.append(NOP);
//
//    // Prepare to call buildKeyIndex(String name, int node, String value);
//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, _name.toString()));
//    parentNode.setEnd(il.append(new ILOAD(parentNode.getIndex())));
//
//    // Now get the node value and push it on the parameter stack
//    il.append(methodGen.loadDOM());
//    il.append(methodGen.loadCurrentNode());
//    il.append(new INVOKEINTERFACE(getNodeValue, 2));
//
//    // Finally do the call to add an entry in the index for this key.
//    il.append(new INVOKEVIRTUAL(buildKeyIndex));
//
//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, getName()));
//    il.append(methodGen.loadDOM());
//    il.append(new INVOKEVIRTUAL(keyDom));
//
//    nextNode.setTarget(il.append(methodGen.loadIterator()));
//    il.append(methodGen.nextNode());
//
//    il.append(DUP);
//    il.append(methodGen.storeCurrentNode());
//    il.append(new IFGE(loop)); // Go on to next matching node....
//
//    // Restore current node and current iterator from the stack
//    il.append(methodGen.storeIterator());
//    il.append(methodGen.storeCurrentNode());
  }

  public void traverseNodeSet(CompilerContext ctx) {
    // FIXME

    // DOM.getStringValueX(nodeIndex) => String
//    final int getNodeValue = cpg.addInterfaceMethodref(DOM_INTF, GET_NODE_VALUE, "(I)" + STRING_SIG);

//    cpg.addInterfaceMethodref(DOM_INTF, "getNodeIdent", "(I)" + NODE_SIG);

    // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
//    final int keyDom = cpg.addMethodref(TRANSLET_CLASS, "setKeyIndexDom", "(" + STRING_SIG + DOM_INTF_SIG + ")V");

    // This variable holds the id of the node we found with the "match"
    // attribute of xsl:key. This is the id we store, with the value we
    // get from the nodes we find here, in the index for this key.
//    final LocalVariableGen parentNode = methodGen.addLocalVariable("parentNode", Util.getJCRefType("I"), null, null);

    // Get the 'parameter' from the stack and store it in a local var.
//    parentNode.setStart(il.append(new ISTORE(parentNode.getIndex())));

    // Save current node and current iterator on the stack
//    il.append(methodGen.loadCurrentNode());
//    il.append(methodGen.loadIterator());

    // Overwrite current iterator with one that gives us only what we want
    JExpression use = _use.startIterator(ctx, _use.compile(ctx));
    
    JVar iterator = ctx.currentBlock().decl(ctx.ref(DTMAxisIterator.class), ctx.nextTmpIterator(), use);
    JBlock loop = ctx.currentBlock()._while(TRUE).body();
    JVar parentNode = loop.decl(ctx.owner().INT, "parentNode", iterator.invoke("next"));
    loop._if(parentNode.lt(lit(0)))._then()._break();

    // AbstractTranslet.buildKeyIndex(name,node_id,value) => void
    loop.invoke("buildKeyIndex").arg(_name.toString()).arg(ctx.currentNode()).arg(ctx.currentDom().invoke("getStringValueX").arg(parentNode));
    // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
    loop.invoke("setKeyIndexDom").arg(_name.toString()).arg(ctx.currentDom());

//    il.append(methodGen.storeIterator());

//    final BranchHandle nextNode = il.append(new GOTO(null));
//    final InstructionHandle loop = il.append(NOP);

    // Prepare to call buildKeyIndex(String name, int node, String value);
//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, _name.toString()));
//    parentNode.setEnd(il.append(new ILOAD(parentNode.getIndex())));

    // Now get the node value and push it on the parameter stack
//    il.append(methodGen.loadDOM());
//    il.append(methodGen.loadCurrentNode());
//    il.append(new INVOKEINTERFACE(getNodeValue, 2));

    // Finally do the call to add an entry in the index for this key.
//    il.append(new INVOKEVIRTUAL(buildKeyIndex));

//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, getName()));
//    il.append(methodGen.loadDOM());
//    il.append(new INVOKEVIRTUAL(keyDom));

//    nextNode.setTarget(il.append(methodGen.loadIterator()));
//    il.append(methodGen.nextNode());

//    il.append(DUP);
//    il.append(methodGen.storeCurrentNode());
//    il.append(new IFGE(loop)); // Go on to next matching node....

    // Restore current node and current iterator from the stack
//    il.append(methodGen.storeIterator());
//    il.append(methodGen.storeCurrentNode());
  }

  /**
   * Gather all nodes that match the expression in the attribute "match" and add
   * one (or more) entries in this key's index.
   */
  @Override
  public void translate(CompilerContext ctx) {
    // FIXME

//    cpg.addInterfaceMethodref(DOM_INTF, "getNodeIdent", "(I)" + NODE_SIG);

    // DOM.getAxisIterator(root) => NodeIterator
//    final int git = cpg.addInterfaceMethodref(DOM_INTF, "getAxisIterator", "(Lde/lyca/xml/dtm/Axis;)"
//        + NODE_ITERATOR_SIG);

//    il.append(methodGen.loadCurrentNode());
//    il.append(methodGen.loadIterator());

    // Get an iterator for all nodes in the DOM
    // and reset the iterator to start with the root node
//    il.append(methodGen.loadDOM());
//    il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", Axis.DESCENDANT.name(), Type.Axis.toJCType(),
//        org.apache.bcel.Constants.GETSTATIC));
//    il.append(new INVOKEINTERFACE(git, 2));
//    il.append(methodGen.loadCurrentNode());
//    il.append(methodGen.setStartNode());
//    il.append(methodGen.storeIterator());
    JVar axisIterator = ctx.currentBlock().decl(
        ctx.ref(DTMAxisIterator.class),
        ctx.nextTmpIterator(),
        ctx.currentDom().invoke("getAxisIterator").arg(ctx.ref(Axis.class).staticRef(Axis.DESCENDANT.name()))
            .invoke("setStartNode").arg(ctx.currentNode()));

    // Loop for traversing all nodes in the DOM
    final JBlock loop = ctx.currentBlock()._while(TRUE).body();
    JVar current = loop.decl(ctx.owner().INT, ctx.nextCurrent(), axisIterator.invoke("next"));
    loop._if(current.lte(lit(0)))._then()._break();
    ctx.pushBlock(loop);
    ctx.pushNode(current);

    // Check if the current node matches the pattern in "match"
    JBlock _if = loop._if(_match.compile(ctx))._then();
    ctx.pushBlock(_if);

    // If this is a node-set we must go through each node in the set
    if (_useType instanceof NodeSetType) {
      // Pass current node as parameter (we're indexing on that node)
      // il.append(methodGen.loadCurrentNode());
//       traverseNodeSet(classGen, methodGen, key);
       traverseNodeSet(ctx);
    } else {
      // AbstractTranslet.buildKeyIndex(name,node_id,value) => void
      _if.invoke("buildKeyIndex").arg(_name.toString()).arg(ctx.currentNode()).arg(_use.compile(ctx));
      // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
      _if.invoke("setKeyIndexDom").arg(_name.toString()).arg(ctx.currentDom());
    }
    
    ctx.popBlock();
    ctx.popNode();
    ctx.popBlock();

    // Get the next node from the iterator and do loop again...
//    final InstructionHandle skip = il.append(NOP);
//
//    il.append(methodGen.loadIterator());
//    il.append(methodGen.nextNode());
//    il.append(DUP);
//    il.append(methodGen.storeCurrentNode());
//    il.append(new IFGT(loop));
//
//    // Restore current node and current iterator from the stack
//    il.append(methodGen.storeIterator());
//    il.append(methodGen.storeCurrentNode());
//
//    nextNode.setTarget(skip);
//    skipNode.setTarget(skip);
  }

}
