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

import static com.sun.codemodel.JExpr.lit;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.serializer.ElemDesc;
import de.lyca.xml.serializer.ExtendedContentHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class LiteralAttribute extends Instruction {

  private final String _name; // Attribute name (incl. prefix)
  private final AttributeValue _value; // Attribute value

  /**
   * Creates a new literal attribute (but does not insert it into the AST).
   * 
   * @param name
   *          the attribute name (incl. prefix) as a String.
   * @param value
   *          the attribute value.
   * @param parser
   *          the XSLT parser (wraps XPath parser).
   */
  public LiteralAttribute(String name, String value, Parser parser, SyntaxTreeNode parent) {
    _name = name;
    setParent(parent);
    _value = AttributeValue.create(this, value, parser);
  }

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("LiteralAttribute name=" + _name + " value=" + _value);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    _value.typeCheck(stable);
    typeCheckContents(stable);
    return Type.Void;
  }

  @Override
  protected boolean contextDependent() {
    return _value.contextDependent();
  }

  @Override
  public void translate(CompilerContext ctx) {
    // Generate code that calls SerializationHandler.addUniqueAttribute()
    // if all attributes are unique.
    final SyntaxTreeNode parent = getParent();
    JExpression value = _value.toJExpression(ctx);
    if (parent instanceof LiteralElement && ((LiteralElement) parent).allAttributesUnique()) {

      int flags = 0;
      boolean isHTMLAttrEmpty = false;
      final ElemDesc elemDesc = ((LiteralElement) parent).getElemDesc();

      // Set the HTML flags
      if (elemDesc != null) {
        if (elemDesc.isAttrFlagSet(_name, ElemDesc.ATTREMPTY)) {
          flags = flags | ExtendedContentHandler.HTML_ATTREMPTY;
          isHTMLAttrEmpty = true;
        } else if (elemDesc.isAttrFlagSet(_name, ElemDesc.ATTRURL)) {
          flags = flags | ExtendedContentHandler.HTML_ATTRURL;
        }
      }

      if (_value instanceof SimpleAttributeValue) {
        final String attrValue = ((SimpleAttributeValue) _value).toString();

        if (!hasBadChars(attrValue) && !isHTMLAttrEmpty) {
          flags = flags | ExtendedContentHandler.NO_BAD_CHARS;
        }
      }
      ctx.currentBlock().invoke(ctx.currentHandler(), "addUniqueAttribute").arg(_name).arg(value).arg(lit(flags));

    } else {
      // call attribute
      ctx.currentBlock().invoke(ctx.currentHandler() ,"addAttribute").arg(_name).arg(value);
    }
  }

    /**
     * Return true if at least one character in the String is considered to
     * be a "bad" character. A bad character is one whose code is:
     * less than 32 (a space),
     * or greater than 126,
     * or it is one of '<', '>', '&' or '\"'. 
     * This helps the serializer to decide whether the String needs to be escaped.
     */
  private boolean hasBadChars(String value) {
    final char[] chars = value.toCharArray();
    final int size = chars.length;
    for (int i = 0; i < size; i++) {
      final char ch = chars[i];
      if (ch < 32 || 126 < ch || ch == '<' || ch == '>' || ch == '&' || ch == '\"')
        return true;
    }
    return false;
  }

  /**
   * Return the name of the attribute
   */
  public String getName() {
    return _name;
  }

  /**
   * Return the value of the attribute
   */
  public AttributeValue getValue() {
    return _value;
  }

}
