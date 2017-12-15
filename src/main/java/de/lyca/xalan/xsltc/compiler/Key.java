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

import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.DOM.GET_AXIS_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.GET_STRING_VALUE_X;
import static de.lyca.xml.dtm.DTMAxisIterator.NEXT;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
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
  public void traverseNodeSet(CompilerContext ctx) {
    // Overwrite current iterator with one that gives us only what we want
    JExpression use = _use.startIterator(ctx, _use.toJExpression(ctx));

    JVar iterator = ctx.currentBlock().decl(ctx.ref(DTMAxisIterator.class), ctx.nextTmpIterator(), use);
    JBlock loop = ctx.currentBlock()._while(TRUE).body();

    // This variable holds the id of the node we found with the "match"
    // attribute of xsl:key. This is the id we store, with the value we
    // get from the nodes we find here, in the index for this key.
    JVar parentNode = loop.decl(ctx.owner().INT, "parentNode", iterator.invoke(NEXT));
    loop._if(parentNode.lt(lit(0)))._then()._break();

    // Prepare to call buildKeyIndex(String name, int node, String value);
    // AbstractTranslet.buildKeyIndex(name,node_id,value) => void
    // Now get the node value and push it on the parameter stack
    // DOM.getStringValueX(nodeIndex) => String
    loop.invoke("buildKeyIndex").arg(_name.toString()).arg(ctx.currentNode())
        .arg(ctx.currentDom().invoke(GET_STRING_VALUE_X).arg(parentNode));
    // Finally do the call to add an entry in the index for this key.
    // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
    loop.invoke("setKeyIndexDom").arg(_name.toString()).arg(ctx.currentDom());
  }

  /**
   * Gather all nodes that match the expression in the attribute "match" and add
   * one (or more) entries in this key's index.
   */
  @Override
  public void translate(CompilerContext ctx) {
    // Get an iterator for all nodes in the DOM and reset the iterator to start
    // with the root node
    JVar axisIterator = ctx.currentBlock().decl(
        ctx.ref(DTMAxisIterator.class),
        ctx.nextTmpIterator(),
        ctx.currentDom().invoke(GET_AXIS_ITERATOR).arg(ctx.ref(Axis.class).staticRef(Axis.DESCENDANT.name()))
            .invoke(SET_START_NODE).arg(ctx.currentNode()));

    // Loop for traversing all nodes in the DOM
    final JBlock loop = ctx.currentBlock()._while(TRUE).body();
    // Get the next node from the iterator and do loop again...
    JVar current = loop.decl(ctx.owner().INT, ctx.nextCurrent(), axisIterator.invoke(NEXT));
    loop._if(current.lte(lit(0)))._then()._break();
    ctx.pushBlock(loop);
    ctx.pushNode(current);

    // Check if the current node matches the pattern in "match"
    JBlock _if = loop._if(_match.toJExpression(ctx))._then();
    ctx.pushBlock(_if);

    // If this is a node-set we must go through each node in the set
    if (_useType instanceof NodeSetType) {
      // Pass current node as parameter (we're indexing on that node)
      traverseNodeSet(ctx);
    } else {
      // AbstractTranslet.buildKeyIndex(name,node_id,value) => void
      JExpression use = _use.toJExpression(ctx);
      _if.invoke("buildKeyIndex").arg(_name.toString()).arg(ctx.currentNode()).arg(use);
      // AbstractTranslet.SetKeyIndexDom(name, Dom) => void
      _if.invoke("setKeyIndexDom").arg(_name.toString()).arg(ctx.currentDom());
    }

    ctx.popBlock();
    ctx.popNode();
    ctx.popBlock();
  }

}
