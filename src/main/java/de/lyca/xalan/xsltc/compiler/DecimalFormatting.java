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

import static com.sun.codemodel.JExpr._new;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.SYMBOLS_REDEF_ERR;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class DecimalFormatting extends TopLevelElement {

  private QName _name = null;

  /**
   * No type check needed for the <xsl:decimal-formatting/> element
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    return Type.Void;
  }

  /**
   * Parse the name of the <xsl:decimal-formatting/> element
   */
  @Override
  public void parseContents(Parser parser) {
    // Get the name of these decimal formatting symbols
    final String name = getAttribute("name");
    if (name.length() > 0) {
      if (!XML11Char.isXML11ValidQName(name)) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR, name, this);
        parser.reportError(Constants.ERROR, err);
      }
    }
    _name = parser.getQNameIgnoreDefaultNs(name);
    if (_name == null) {
      _name = parser.getQNameIgnoreDefaultNs("");
    }

    // Check if a set of symbols has already been registered under this name
    final SymbolTable stable = parser.getSymbolTable();
    if (stable.getDecimalFormatting(_name) != null) {
      reportWarning(this, parser, SYMBOLS_REDEF_ERR, _name.toString());
    } else {
      stable.addDecimalFormatting(_name, this);
    }
  }

  /**
   * This method is called when the constructor is compiled in
   * Stylesheet.compileConstructor() and not as the syntax tree is traversed.
   */
  @Override
  public void translate(CompilerContext ctx) {
    // Manufacture a DecimalFormatSymbols on the stack
    // for call to addDecimalFormat()
    // Use the US Locale as the default, as most of its settings
    // are equivalent to the default settings required of
    JClass decimalFormatSymbols = ctx.ref(DecimalFormatSymbols.class);
    JClass locale = ctx.ref(Locale.class);
    JVar dfs = ctx.currentBlock().decl(decimalFormatSymbols, ctx.nextDecimalFormatting(),
        _new(decimalFormatSymbols).arg(locale.staticRef("US")));
    String tmp = getAttribute("NaN");
    if (tmp == null || tmp.isEmpty()) {
      ctx.currentBlock().invoke(dfs, "setNaN").arg("NaN");
    } else {
      ctx.currentBlock().invoke(dfs, "setNaN").arg(tmp);
    }

    tmp = getAttribute("infinity");
    if (tmp == null || tmp.isEmpty()) {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg("Infinity");
    } else {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg(tmp);
    }

    final int nAttributes = _attributes.getLength();
    for (int i = 0; i < nAttributes; i++) {
      final String name = _attributes.getQName(i);

      String method = null;
      if (name.equals("decimal-separator")) {
        method = "setDecimalSeparator";
      } else if (name.equals("grouping-separator")) {
        method = "setGroupingSeparator";
      } else if (name.equals("minus-sign")) {
        method = "setMinusSign";
      } else if (name.equals("percent")) {
        method = "setPercent";
      } else if (name.equals("per-mille")) {
        method = "setPerMill";
      } else if (name.equals("zero-digit")) {
        method = "setZeroDigit";
      } else if (name.equals("digit")) {
        method = "setDigit";
      } else if (name.equals("pattern-separator")) {
        method = "setPatternSeparator";
      } else {
        continue;
      }
      final String value = _attributes.getValue(i);
      ctx.currentBlock().invoke(dfs, method).arg(JExpr.lit(value.charAt(0)));
    }

    ctx.currentBlock().invoke("addDecimalFormat").arg(_name.toString()).arg(dfs);
  }

  /**
   * Creates the default, nameless, DecimalFormat object in AbstractTranslet's
   * format_symbols Map. This should be called for every stylesheet, and the
   * entry may be overridden by later nameless xsl:decimal-format instructions.
   */
  public static void translateDefaultDFS(CompilerContext ctx) {
    // Manufacture a DecimalFormatSymbols on the stack for
    // call to addDecimalFormat(). Use the US Locale as the
    // default, as most of its settings are equivalent to
    // the default settings required of xsl:decimal-format -
    // except for the NaN and infinity attributes.
    JClass decimalFormatSymbols = ctx.ref(DecimalFormatSymbols.class);
    JClass locale = ctx.ref(Locale.class);
    JVar dfs = ctx.currentBlock().decl(decimalFormatSymbols, "__$dfs$__",
        _new(decimalFormatSymbols).arg(locale.staticRef("US")));
    ctx.currentBlock().invoke(dfs, "setNaN").arg("NaN");
    ctx.currentBlock().invoke(dfs, "setInfinity").arg("Infinity");
    ctx.currentBlock().invoke("addDecimalFormat").arg("").arg(dfs);
  }

}
