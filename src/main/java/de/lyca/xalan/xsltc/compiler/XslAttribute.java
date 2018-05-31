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
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JExpr.ref;
import static de.lyca.xalan.xsltc.compiler.Constants.XMLNS_PREFIX;

import java.util.List;

import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xml.serializer.ElemDesc;
import de.lyca.xml.serializer.ExtendedContentHandler;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 * @author Gunnlaugur Briem <gthb@dimon.is>
 */
final class XslAttribute extends Instruction {

  private String _prefix;
  private AttributeValue _name; // name treated as AVT (7.1.3)
  private AttributeValueTemplate _namespace = null;
  private boolean _ignore = false;
  private boolean _isLiteral = false; // specified name is not AVT

  /**
   * Returns the name of the attribute
   */
  public AttributeValue getName() {
    return _name;
  }

  /**
   * Parses the attribute's contents. Special care taken for namespaces.
   */
  @Override
  public void parseContents(Parser parser) {
    boolean generated = false;
    final SymbolTable stable = parser.getSymbolTable();

    String name = getAttribute("name");
    String namespace = getAttribute("namespace");
    final QName qname = parser.getQName(name, false);
    final String prefix = qname.getPrefix();

    if (prefix != null && prefix.equals(XMLNS_PREFIX) || name.equals(XMLNS_PREFIX)) {
      reportError(this, parser, Messages.get().illegalAttrNameErr(name));
      return;
    }

    _isLiteral = Util.isLiteral(name);
    if (_isLiteral) {
      if (!XML11Char.isXML11ValidQName(name)) {
        reportError(this, parser, Messages.get().illegalAttrNameErr(name));
        return;
      }
    }

    // Ignore attribute if preceeded by some other type of element
    final SyntaxTreeNode parent = getParent();
    final List<SyntaxTreeNode> siblings = parent.getContents();
    for (final SyntaxTreeNode item : siblings) {
      if (item == this) {
        break;
      }

      // These three objects result in one or more attribute output
      if (item instanceof XslAttribute) {
        continue;
      }
      if (item instanceof UseAttributeSets) {
        continue;
      }
      if (item instanceof LiteralAttribute) {
        continue;
      }
      if (item instanceof Text) {
        continue;
      }

      // These objects _can_ result in one or more attribute
      // The output handler will generate an error if not (at runtime)
      if (item instanceof If) {
        continue;
      }
      if (item instanceof Choose) {
        continue;
      }
      if (item instanceof CopyOf) {
        continue;
      }
      if (item instanceof VariableBase) {
        continue;
      }

      // Report warning but do not ignore attribute
      reportWarning(this, parser, Messages.get().strayAttributeErr(name));
    }

    // Get namespace from namespace attribute?
    if (namespace != null && namespace != "") {
      _prefix = lookupPrefix(namespace);
      _namespace = new AttributeValueTemplate(namespace, parser, this);
    }
    // Get namespace from prefix in name attribute?
    else if (prefix != null && prefix != "") {
      _prefix = prefix;
      namespace = lookupNamespace(prefix);
      if (namespace != null) {
        _namespace = new AttributeValueTemplate(namespace, parser, this);
      }
    }

    // Common handling for namespaces:
    if (_namespace != null) {
      // Generate prefix if we have none
      if (_prefix == null || _prefix == "") {
        if (prefix != null) {
          _prefix = prefix;
        } else {
          _prefix = stable.generateNamespacePrefix();
          generated = true;
        }
      } else if (prefix != null && !prefix.equals(_prefix)) {
        _prefix = prefix;
      }

      name = _prefix + ":" + qname.getLocalPart();

      /*
       * TODO: The namespace URI must be passed to the parent element but we
       * don't yet know what the actual URI is (as we only know it as an
       * attribute value template).
       */
      if (parent instanceof LiteralElement && !generated) {
        ((LiteralElement) parent).registerNamespace(_prefix, namespace, stable, false);
      }
    }

    if (parent instanceof LiteralElement) {
      ((LiteralElement) parent).addAttribute(this);
    }

    _name = AttributeValue.create(this, name, parser);
    parseChildren(parser);
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (getParent() instanceof XslAttribute) {
      // TODO better error reporting ILLEGAL_PARENT
      final ErrorMsg err = new ErrorMsg(this,
          Messages.get().internalErr("xsl:attribute cannot be child of xsl:attribute"));
      throw new TypeCheckError(err);
    }
    if (!_ignore) {
      _name.typeCheck(stable);
      if (_namespace != null) {
        _namespace.typeCheck(stable);
      }
      typeCheckContents(stable);
    }
    return Type.Void;
  }

  @Override
  public void translate(CompilerContext ctx) {
    if (_ignore)
      return;
    _ignore = true;

    // Compile code that emits any needed namespace declaration
    if (_namespace != null) {
      ctx.currentBlock().add(
          ctx.currentHandler().invoke("namespaceAfterStartElement").arg(_prefix).arg(_namespace.toJExpression(ctx)));
    }

    JExpression secondArg = null;
    if (!_isLiteral) {
      // if the qname is an AVT, then the qname has to be checked at runtime if
      // it is a valid qname
      // store the name into a variable first so _name.translate only needs to
      // be called once
      secondArg = ctx.currentBlock().decl(ctx.ref(String.class), ctx.nextNameValue(), _name.toJExpression(ctx));

      // call checkQName if the name is an AVT
      ctx.currentBlock().staticInvoke(ctx.ref(BasisLibrary.class), "checkAttribQName").arg(secondArg);
    } else {
      // Push attribute name
      secondArg = _name.toJExpression(ctx);// 2nd arg
    }

    // Push attribute value - shortcut for literal strings
    JExpression text = null;
    if (elementCount() == 1 && elementAt(0) instanceof Text) {
      text = lit(((Text) elementAt(0)).getText());
    } else {
      ctx.pushHandler(ref("stringValueHandler"));
      // translate contents with substituted handler
      translateContents(ctx);
      // get String out of the handler
      text = invoke(ctx.popHandler(), "getValue");
    }

    final SyntaxTreeNode parent = getParent();
    if (parent instanceof LiteralElement && ((LiteralElement) parent).allAttributesUnique()) {
      int flags = 0;
      final ElemDesc elemDesc = ((LiteralElement) parent).getElemDesc();

      // Set the HTML flags
      if (elemDesc != null && _name instanceof SimpleAttributeValue) {
        final String attrName = ((SimpleAttributeValue) _name).toString();
        if (elemDesc.isAttrFlagSet(attrName, ElemDesc.ATTREMPTY)) {
          flags = flags | ExtendedContentHandler.HTML_ATTREMPTY;
        } else if (elemDesc.isAttrFlagSet(attrName, ElemDesc.ATTRURL)) {
          flags = flags | ExtendedContentHandler.HTML_ATTRURL;
        }
      }
      ctx.currentBlock().invoke(ctx.currentHandler(), "addUniqueAttribute").arg(secondArg).arg(text).arg(lit(flags));
    } else {
      // call "attribute"
      ctx.currentBlock().invoke(ctx.currentHandler(), "addAttribute").arg(secondArg).arg(text);
    }
  }

}
