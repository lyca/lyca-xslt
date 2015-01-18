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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUSH;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.util.ClassGenerator;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class DecimalFormatting extends TopLevelElement {

  private static final String DFS_CLASS = "java.text.DecimalFormatSymbols";
  private static final String DFS_SIG = "Ljava/text/DecimalFormatSymbols;";

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
      _name = parser.getQNameIgnoreDefaultNs(EMPTYSTRING);
    }

    // Check if a set of symbols has already been registered under this name
    final SymbolTable stable = parser.getSymbolTable();
    if (stable.getDecimalFormatting(_name) != null) {
      reportWarning(this, parser, ErrorMsg.SYMBOLS_REDEF_ERR, _name.toString());
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
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//
//    // DecimalFormatSymbols.<init>(Locale);
//    // xsl:decimal-format - except for the NaN and infinity attributes.
//    final int init = cpg.addMethodref(DFS_CLASS, "<init>", "(" + LOCALE_SIG + ")V");
//
//    // Push the format name on the stack for call to addDecimalFormat()
//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, _name.toString()));
//
//    // Manufacture a DecimalFormatSymbols on the stack
//    // for call to addDecimalFormat()
//    // Use the US Locale as the default, as most of its settings
//    // are equivalent to the default settings required of
//    il.append(new NEW(cpg.addClass(DFS_CLASS)));
//    il.append(DUP);
//    il.append(new GETSTATIC(cpg.addFieldref(LOCALE_CLASS, "US", LOCALE_SIG)));
//    il.append(new INVOKESPECIAL(init));
    JClass decimalFormatSymbols = ctx.ref(DecimalFormatSymbols.class);
    JClass locale = ctx.ref(Locale.class);
    JVar dfs = ctx.currentBlock().decl(decimalFormatSymbols, ctx.nextDecimalFormatting(), _new(decimalFormatSymbols).arg(locale.staticRef("US")));
    String tmp = getAttribute("NaN");
    if (tmp == null || tmp.equals(EMPTYSTRING)) {
      ctx.currentBlock().invoke(dfs, "setNaN").arg("NaN");
//      final int nan = cpg.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
//      il.append(DUP);
//      il.append(new PUSH(cpg, "NaN"));
//      il.append(new INVOKEVIRTUAL(nan));
    } else {
      ctx.currentBlock().invoke(dfs, "setNaN").arg(tmp);
    }

    tmp = getAttribute("infinity");
    if (tmp == null || tmp.equals(EMPTYSTRING)) {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg("Infinity");
//      final int inf = cpg.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
//      il.append(DUP);
//      il.append(new PUSH(cpg, "Infinity"));
//      il.append(new INVOKEVIRTUAL(inf));
    } else {
      ctx.currentBlock().invoke(dfs, "setInfinity").arg(tmp);
    }

    final int nAttributes = _attributes.getLength();
    for (int i = 0; i < nAttributes; i++) {
      final String name = _attributes.getQName(i);
      final String value = _attributes.getValue(i);

      boolean valid = true;
//      int method = 0;
      String method = null;
      if (name.equals("decimal-separator")) {
        // DecimalFormatSymbols.setDecimalSeparator();
        method = "setDecimalSeparator";
//        method = cpg.addMethodref(DFS_CLASS, "setDecimalSeparator", "(C)V");
      } else if (name.equals("grouping-separator")) {
        method = "setGroupingSeparator";
//        method = cpg.addMethodref(DFS_CLASS, "setGroupingSeparator", "(C)V");
      } else if (name.equals("minus-sign")) {
        method = "setMinusSign";
//        method = cpg.addMethodref(DFS_CLASS, "setMinusSign", "(C)V");
      } else if (name.equals("percent")) {
        method = "setPercent";
//        method = cpg.addMethodref(DFS_CLASS, "setPercent", "(C)V");
      } else if (name.equals("per-mille")) {
        method = "setPerMill";
//        method = cpg.addMethodref(DFS_CLASS, "setPerMill", "(C)V");
      } else if (name.equals("zero-digit")) {
        method = "setZeroDigit";
//        method = cpg.addMethodref(DFS_CLASS, "setZeroDigit", "(C)V");
      } else if (name.equals("digit")) {
        method = "setDigit";
//        method = cpg.addMethodref(DFS_CLASS, "setDigit", "(C)V");
      } else if (name.equals("pattern-separator")) {
        method = "setPatternSeparator";
//        method = cpg.addMethodref(DFS_CLASS, "setPatternSeparator", "(C)V");
//      } else if (name.equals("NaN")) {
//        method = cpg.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
//        il.append(DUP);
//        il.append(new PUSH(cpg, value));
//        il.append(new INVOKEVIRTUAL(method));
//        valid = false;
//      } else if (name.equals("infinity")) {
//        method = cpg.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
//        il.append(DUP);
//        il.append(new PUSH(cpg, value));
//        il.append(new INVOKEVIRTUAL(method));
//        valid = false;
      } else {
        valid = false;
      }

      if (valid) {
        ctx.currentBlock().invoke(dfs, method).arg(JExpr.lit(value.charAt(0)));
//        il.append(DUP);
//        il.append(new PUSH(cpg, value.charAt(0)));
//        il.append(new INVOKEVIRTUAL(method));
      }

    }
//
//    final int put = cpg.addMethodref(TRANSLET_CLASS, "addDecimalFormat", "(" + STRING_SIG + DFS_SIG + ")V");
//    il.append(new INVOKEVIRTUAL(put));
    ctx.currentBlock().invoke("addDecimalFormat").arg(_name.toString()).arg(dfs);
  }

  /**
   * Creates the default, nameless, DecimalFormat object in AbstractTranslet's
   * format_symbols Map. This should be called for every stylesheet, and the
   * entry may be overridden by later nameless xsl:decimal-format instructions.
   */
  public static void translateDefaultDFS(CompilerContext ctx) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final int init = cpg.addMethodref(DFS_CLASS, "<init>", "(" + LOCALE_SIG + ")V");
//
//    // Push the format name, which is empty, on the stack
//    // for call to addDecimalFormat()
//    il.append(classGen.loadTranslet());
//    il.append(new PUSH(cpg, EMPTYSTRING));
//
//    // Manufacture a DecimalFormatSymbols on the stack for
//    // call to addDecimalFormat(). Use the US Locale as the
//    // default, as most of its settings are equivalent to
//    // the default settings required of xsl:decimal-format -
//    // except for the NaN and infinity attributes.
//    il.append(new NEW(cpg.addClass(DFS_CLASS)));
//    il.append(DUP);
//    il.append(new GETSTATIC(cpg.addFieldref(LOCALE_CLASS, "US", LOCALE_SIG)));
//    il.append(new INVOKESPECIAL(init));
    JClass decimalFormatSymbols = ctx.ref(DecimalFormatSymbols.class);
    JClass locale = ctx.ref(Locale.class);
    JVar dfs = ctx.currentBlock().decl(decimalFormatSymbols, "__$dfs$__", _new(decimalFormatSymbols).arg(locale.staticRef("US")));
//    final int nan = cpg.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
//    il.append(DUP);
//    il.append(new PUSH(cpg, "NaN"));
//    il.append(new INVOKEVIRTUAL(nan));
    ctx.currentBlock().invoke(dfs, "setNaN").arg("NaN");
//    final int inf = cpg.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
//    il.append(DUP);
//    il.append(new PUSH(cpg, "Infinity"));
//    il.append(new INVOKEVIRTUAL(inf));
    ctx.currentBlock().invoke(dfs, "setInfinity").arg("Infinity");
//    final int put = cpg.addMethodref(TRANSLET_CLASS, "addDecimalFormat", "(" + STRING_SIG + DFS_SIG + ")V");
//    il.append(new INVOKEVIRTUAL(put));
    ctx.currentBlock().invoke("addDecimalFormat").arg("").arg(dfs);
  }
}
