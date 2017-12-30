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

import static com.sun.codemodel.JExpr._new;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.ILLEGAL_CHILD_ERR;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.SYMBOLS_REDEF_ERR;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
 * @see <a href= "https://www.w3.org/TR/xslt-10/#format-number">
 *      https://www.w3.org/TR/xslt-10/#format-number</a>
 */
final class DecimalFormatting extends TopLevelElement {

  private static final Map<String, String> ATTR_TO_METHOD = new HashMap<>();
  private static final Map<String, String> ATTR_TO_DEFAULT = new HashMap<>();
  static {
    ATTR_TO_METHOD.put("decimal-separator", "setDecimalSeparator");
    ATTR_TO_METHOD.put("grouping-separator", "setGroupingSeparator");
    ATTR_TO_METHOD.put("minus-sign", "setMinusSign");
    ATTR_TO_METHOD.put("percent", "setPercent");
    ATTR_TO_METHOD.put("per-mille", "setPerMill");
    ATTR_TO_METHOD.put("zero-digit", "setZeroDigit");
    ATTR_TO_METHOD.put("digit", "setDigit");
    ATTR_TO_METHOD.put("pattern-separator", "setPatternSeparator");

    ATTR_TO_DEFAULT.put("decimal-separator", ".");
    ATTR_TO_DEFAULT.put("grouping-separator", ",");
    ATTR_TO_DEFAULT.put("percent", "%");
    ATTR_TO_DEFAULT.put("per-mille", "\u2030");
    ATTR_TO_DEFAULT.put("zero-digit", "0");
    ATTR_TO_DEFAULT.put("digit", "#");
    ATTR_TO_DEFAULT.put("pattern-separator", ";");
    ATTR_TO_DEFAULT.put("infinity", "Infinity");
    ATTR_TO_DEFAULT.put("NaN", "NaN");
    ATTR_TO_DEFAULT.put("minus-sign", "-");
  }

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
    DecimalFormatting decimalFormatting = stable.getDecimalFormatting(_name);
    if (decimalFormatting == null) {
      stable.addDecimalFormatting(_name, this);
    } else if (!equivalent(decimalFormatting)) {
      reportError(this, parser, SYMBOLS_REDEF_ERR, _name.toString());
    }
    if (elementCount() > 0) {
      reportError(this, parser, ILLEGAL_CHILD_ERR, _name.toString());
    }
    for (String attr : ATTR_TO_METHOD.keySet()) {
      String val = getAttribute(attr);
      if (val.length() > 1) {
        // TODO better Error reporting
        reportError(this, parser, ErrorMsg.ILLEGAL_CHAR_ERR, val);
      }
    }
  }

  private boolean equivalent(DecimalFormatting other) {
    for (String attr : ATTR_TO_DEFAULT.keySet()) {
      String val = getAttribute(attr);
      String otherVal = other.getAttribute(attr);
      String defVal = ATTR_TO_DEFAULT.get(attr);
      if (val.equals(otherVal) //
          || defVal != null //
              && (defVal.equals(val) && otherVal.isEmpty() //
                  || defVal.equals(otherVal) && val.isEmpty()))
        continue;
      return false;
    }
    return true;

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
    if (tmp.isEmpty()) {
      ctx.currentBlock().invoke(dfs, "setNaN").arg("NaN");
    } else {
      ctx.currentBlock().invoke(dfs, "setNaN").arg(tmp);
    }

    tmp = getAttribute("infinity");
    if (tmp.isEmpty()) {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg("Infinity");
    } else {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg(tmp);
    }

    final int nAttributes = _attributes.getLength();
    for (int i = 0; i < nAttributes; i++) {
      final String name = _attributes.getQName(i);

      String method = ATTR_TO_METHOD.get(name);
      if (method == null) {
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
