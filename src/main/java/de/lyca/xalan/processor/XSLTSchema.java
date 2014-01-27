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
package de.lyca.xalan.processor;

import java.util.HashMap;

import de.lyca.xalan.templates.Constants;
import de.lyca.xalan.templates.ElemApplyImport;
import de.lyca.xalan.templates.ElemApplyTemplates;
import de.lyca.xalan.templates.ElemAttribute;
import de.lyca.xalan.templates.ElemCallTemplate;
import de.lyca.xalan.templates.ElemChoose;
import de.lyca.xalan.templates.ElemComment;
import de.lyca.xalan.templates.ElemCopy;
import de.lyca.xalan.templates.ElemCopyOf;
import de.lyca.xalan.templates.ElemElement;
import de.lyca.xalan.templates.ElemExsltFuncResult;
import de.lyca.xalan.templates.ElemExsltFunction;
import de.lyca.xalan.templates.ElemExtensionDecl;
import de.lyca.xalan.templates.ElemExtensionScript;
import de.lyca.xalan.templates.ElemFallback;
import de.lyca.xalan.templates.ElemForEach;
import de.lyca.xalan.templates.ElemIf;
import de.lyca.xalan.templates.ElemLiteralResult;
import de.lyca.xalan.templates.ElemMessage;
import de.lyca.xalan.templates.ElemNumber;
import de.lyca.xalan.templates.ElemOtherwise;
import de.lyca.xalan.templates.ElemPI;
import de.lyca.xalan.templates.ElemParam;
import de.lyca.xalan.templates.ElemSort;
import de.lyca.xalan.templates.ElemTemplate;
import de.lyca.xalan.templates.ElemText;
import de.lyca.xalan.templates.ElemTextLiteral;
import de.lyca.xalan.templates.ElemUnknown;
import de.lyca.xalan.templates.ElemValueOf;
import de.lyca.xalan.templates.ElemVariable;
import de.lyca.xalan.templates.ElemWhen;
import de.lyca.xalan.templates.ElemWithParam;
import de.lyca.xml.utils.QName;

/**
 * This class defines the allowed structure for a stylesheet, and the mapping
 * between Xalan classes and the markup elements in the stylesheet.
 * 
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 */
public class XSLTSchema extends XSLTElementDef {

  /**
   * Construct a XSLTSchema which represents the XSLT "schema".
   */
  XSLTSchema() {
    build();
  }

  /**
   * This method builds an XSLT "schema" according to
   * http://www.w3.org/TR/xslt#dtd. This schema provides instructions for
   * building the Xalan Stylesheet (Templates) structure.
   */
  void build() {
    // xsl:import, xsl:include
    final XSLTAttributeDef hrefAttr = new XSLTAttributeDef(null, "href", XSLTAttributeDef.T_URL, true, false,
            XSLTAttributeDef.ERROR);

    // xsl:preserve-space, xsl:strip-space
    final XSLTAttributeDef elementsAttr = new XSLTAttributeDef(null, "elements", XSLTAttributeDef.T_SIMPLEPATTERNLIST,
            true, false, XSLTAttributeDef.ERROR);

    // XSLTAttributeDef anyNamespacedAttr = new XSLTAttributeDef("*", "*",
    // XSLTAttributeDef.T_CDATA, false);

    // xsl:output
    final XSLTAttributeDef methodAttr = new XSLTAttributeDef(null, "method", XSLTAttributeDef.T_QNAME, false, false,
            XSLTAttributeDef.ERROR);
    final XSLTAttributeDef versionAttr = new XSLTAttributeDef(null, "version", XSLTAttributeDef.T_NMTOKEN, false,
            false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef encodingAttr = new XSLTAttributeDef(null, "encoding", XSLTAttributeDef.T_CDATA, false,
            false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef omitXmlDeclarationAttr = new XSLTAttributeDef(null, "omit-xml-declaration",
            XSLTAttributeDef.T_YESNO, false, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef standaloneAttr = new XSLTAttributeDef(null, "standalone", XSLTAttributeDef.T_YESNO, false,
            false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef doctypePublicAttr = new XSLTAttributeDef(null, "doctype-public", XSLTAttributeDef.T_CDATA,
            false, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef doctypeSystemAttr = new XSLTAttributeDef(null, "doctype-system", XSLTAttributeDef.T_CDATA,
            false, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef cdataSectionElementsAttr = new XSLTAttributeDef(null, "cdata-section-elements",
            XSLTAttributeDef.T_QNAMES_RESOLVE_NULL, false, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef indentAttr = new XSLTAttributeDef(null, "indent", XSLTAttributeDef.T_YESNO, false, false,
            XSLTAttributeDef.ERROR);
    final XSLTAttributeDef mediaTypeAttr = new XSLTAttributeDef(null, "media-type", XSLTAttributeDef.T_CDATA, false,
            false, XSLTAttributeDef.ERROR);

    // Required.
    // It is an error if the name attribute is invalid on any of these elements
    // xsl:key, xsl:attribute-set, xsl:call-template, xsl:with-param,
    // xsl:variable, xsl:param
    final XSLTAttributeDef nameAttrRequired = new XSLTAttributeDef(null, "name", XSLTAttributeDef.T_QNAME, true, false,
            XSLTAttributeDef.ERROR);
    // Required.
    // Support AVT
    // xsl:element, xsl:attribute
    final XSLTAttributeDef nameAVTRequired = new XSLTAttributeDef(null, "name", XSLTAttributeDef.T_AVT_QNAME, true,
            true, XSLTAttributeDef.WARNING);

    // Required.
    // Support AVT
    // xsl:processing-instruction
    final XSLTAttributeDef nameAVT_NCNAMERequired = new XSLTAttributeDef(null, "name", XSLTAttributeDef.T_NCNAME, true,
            true, XSLTAttributeDef.WARNING);

    // Optional.
    // Static error if invalid
    // xsl:template, xsl:decimal-format
    final XSLTAttributeDef nameAttrOpt_ERROR = new XSLTAttributeDef(null, "name", XSLTAttributeDef.T_QNAME, false,
            false, XSLTAttributeDef.ERROR);

    // xsl:key
    final XSLTAttributeDef useAttr = new XSLTAttributeDef(null, "use", XSLTAttributeDef.T_EXPR, true, false,
            XSLTAttributeDef.ERROR);

    // xsl:element, xsl:attribute
    final XSLTAttributeDef namespaceAVTOpt = new XSLTAttributeDef(null, "namespace", XSLTAttributeDef.T_URL, false,
            true, XSLTAttributeDef.WARNING);
    // xsl:decimal-format
    final XSLTAttributeDef decimalSeparatorAttr = new XSLTAttributeDef(null, "decimal-separator",
            XSLTAttributeDef.T_CHAR, false, XSLTAttributeDef.ERROR, ".");
    final XSLTAttributeDef infinityAttr = new XSLTAttributeDef(null, "infinity", XSLTAttributeDef.T_CDATA, false,
            XSLTAttributeDef.ERROR, "Infinity");
    final XSLTAttributeDef minusSignAttr = new XSLTAttributeDef(null, "minus-sign", XSLTAttributeDef.T_CHAR, false,
            XSLTAttributeDef.ERROR, "-");
    final XSLTAttributeDef NaNAttr = new XSLTAttributeDef(null, "NaN", XSLTAttributeDef.T_CDATA, false,
            XSLTAttributeDef.ERROR, "NaN");
    final XSLTAttributeDef percentAttr = new XSLTAttributeDef(null, "percent", XSLTAttributeDef.T_CHAR, false,
            XSLTAttributeDef.ERROR, "%");
    final XSLTAttributeDef perMilleAttr = new XSLTAttributeDef(null, "per-mille", XSLTAttributeDef.T_CHAR, false,
            false, XSLTAttributeDef.ERROR /* ,"&#x2030;" */);
    final XSLTAttributeDef zeroDigitAttr = new XSLTAttributeDef(null, "zero-digit", XSLTAttributeDef.T_CHAR, false,
            XSLTAttributeDef.ERROR, "0");
    final XSLTAttributeDef digitAttr = new XSLTAttributeDef(null, "digit", XSLTAttributeDef.T_CHAR, false,
            XSLTAttributeDef.ERROR, "#");
    final XSLTAttributeDef patternSeparatorAttr = new XSLTAttributeDef(null, "pattern-separator",
            XSLTAttributeDef.T_CHAR, false, XSLTAttributeDef.ERROR, ";");
    // xsl:decimal-format
    final XSLTAttributeDef groupingSeparatorAttr = new XSLTAttributeDef(null, "grouping-separator",
            XSLTAttributeDef.T_CHAR, false, XSLTAttributeDef.ERROR, ",");

    // xsl:element, xsl:attribute-set, xsl:copy
    final XSLTAttributeDef useAttributeSetsAttr = new XSLTAttributeDef(null, "use-attribute-sets",
            XSLTAttributeDef.T_QNAMES, false, false, XSLTAttributeDef.ERROR);

    // xsl:if, xsl:when
    final XSLTAttributeDef testAttrRequired = new XSLTAttributeDef(null, "test", XSLTAttributeDef.T_EXPR, true, false,
            XSLTAttributeDef.ERROR);

    // Required.
    // xsl:value-of, xsl:for-each, xsl:copy-of
    final XSLTAttributeDef selectAttrRequired = new XSLTAttributeDef(null, "select", XSLTAttributeDef.T_EXPR, true,
            false, XSLTAttributeDef.ERROR);

    // Optional.
    // xsl:variable, xsl:param, xsl:with-param
    final XSLTAttributeDef selectAttrOpt = new XSLTAttributeDef(null, "select", XSLTAttributeDef.T_EXPR, false, false,
            XSLTAttributeDef.ERROR);

    // Optional.
    // Default: "node()"
    // xsl:apply-templates
    final XSLTAttributeDef selectAttrDefNode = new XSLTAttributeDef(null, "select", XSLTAttributeDef.T_EXPR, false,
            XSLTAttributeDef.ERROR, "node()");
    // Optional.
    // Default: "."
    // xsl:sort
    final XSLTAttributeDef selectAttrDefDot = new XSLTAttributeDef(null, "select", XSLTAttributeDef.T_EXPR, false,
            XSLTAttributeDef.ERROR, ".");
    // xsl:key
    final XSLTAttributeDef matchAttrRequired = new XSLTAttributeDef(null, "match", XSLTAttributeDef.T_PATTERN, true,
            false, XSLTAttributeDef.ERROR);
    // xsl:template
    final XSLTAttributeDef matchAttrOpt = new XSLTAttributeDef(null, "match", XSLTAttributeDef.T_PATTERN, false, false,
            XSLTAttributeDef.ERROR);
    // xsl:template
    final XSLTAttributeDef priorityAttr = new XSLTAttributeDef(null, "priority", XSLTAttributeDef.T_NUMBER, false,
            false, XSLTAttributeDef.ERROR);

    // xsl:template, xsl:apply-templates
    final XSLTAttributeDef modeAttr = new XSLTAttributeDef(null, "mode", XSLTAttributeDef.T_QNAME, false, false,
            XSLTAttributeDef.ERROR);

    final XSLTAttributeDef spaceAttr = new XSLTAttributeDef(de.lyca.xml.utils.Constants.S_XMLNAMESPACEURI, "space",
            false, false, false, XSLTAttributeDef.WARNING, "default", Constants.ATTRVAL_STRIP, "preserve",
            Constants.ATTRVAL_PRESERVE);

    final XSLTAttributeDef spaceAttrLiteral = new XSLTAttributeDef(de.lyca.xml.utils.Constants.S_XMLNAMESPACEURI,
            "space", XSLTAttributeDef.T_URL, false, true, XSLTAttributeDef.ERROR);
    // xsl:namespace-alias
    final XSLTAttributeDef stylesheetPrefixAttr = new XSLTAttributeDef(null, "stylesheet-prefix",
            XSLTAttributeDef.T_CDATA, true, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef resultPrefixAttr = new XSLTAttributeDef(null, "result-prefix", XSLTAttributeDef.T_CDATA,
            true, false, XSLTAttributeDef.ERROR);

    // xsl:text, xsl:value-of
    final XSLTAttributeDef disableOutputEscapingAttr = new XSLTAttributeDef(null, "disable-output-escaping",
            XSLTAttributeDef.T_YESNO, false, false, XSLTAttributeDef.ERROR);

    // xsl:number
    final XSLTAttributeDef levelAttr = new XSLTAttributeDef(null, "level", false, false, false, XSLTAttributeDef.ERROR,
            "single", Constants.NUMBERLEVEL_SINGLE, "multiple", Constants.NUMBERLEVEL_MULTI, "any",
            Constants.NUMBERLEVEL_ANY);
    levelAttr.setDefault("single");
    final XSLTAttributeDef countAttr = new XSLTAttributeDef(null, "count", XSLTAttributeDef.T_PATTERN, false, false,
            XSLTAttributeDef.ERROR);
    final XSLTAttributeDef fromAttr = new XSLTAttributeDef(null, "from", XSLTAttributeDef.T_PATTERN, false, false,
            XSLTAttributeDef.ERROR);
    final XSLTAttributeDef valueAttr = new XSLTAttributeDef(null, "value", XSLTAttributeDef.T_EXPR, false, false,
            XSLTAttributeDef.ERROR);
    final XSLTAttributeDef formatAttr = new XSLTAttributeDef(null, "format", XSLTAttributeDef.T_CDATA, false, true,
            XSLTAttributeDef.ERROR);
    formatAttr.setDefault("1");

    // xsl:number, xsl:sort
    final XSLTAttributeDef langAttr = new XSLTAttributeDef(null, "lang", XSLTAttributeDef.T_NMTOKEN, false, true,
            XSLTAttributeDef.ERROR);

    // xsl:number
    final XSLTAttributeDef letterValueAttr = new XSLTAttributeDef(null, "letter-value", false, true, false,
            XSLTAttributeDef.ERROR, "alphabetic", Constants.NUMBERLETTER_ALPHABETIC, "traditional",
            Constants.NUMBERLETTER_TRADITIONAL);
    // xsl:number
    final XSLTAttributeDef groupingSeparatorAVT = new XSLTAttributeDef(null, "grouping-separator",
            XSLTAttributeDef.T_CHAR, false, true, XSLTAttributeDef.ERROR);
    // xsl:number
    final XSLTAttributeDef groupingSizeAttr = new XSLTAttributeDef(null, "grouping-size", XSLTAttributeDef.T_NUMBER,
            false, true, XSLTAttributeDef.ERROR);

    // xsl:sort
    final XSLTAttributeDef dataTypeAttr = new XSLTAttributeDef(null, "data-type", false, true, true,
            XSLTAttributeDef.ERROR, "text", Constants.SORTDATATYPE_TEXT, "number", Constants.SORTDATATYPE_TEXT);
    dataTypeAttr.setDefault("text");

    // xsl:sort
    final XSLTAttributeDef orderAttr = new XSLTAttributeDef(null, "order", false, true, false, XSLTAttributeDef.ERROR,
            "ascending", Constants.SORTORDER_ASCENDING, "descending", Constants.SORTORDER_DESCENDING);
    orderAttr.setDefault("ascending");

    // xsl:sort
    final XSLTAttributeDef caseOrderAttr = new XSLTAttributeDef(null, "case-order", false, true, false,
            XSLTAttributeDef.ERROR, "upper-first", Constants.SORTCASEORDER_UPPERFIRST, "lower-first",
            Constants.SORTCASEORDER_LOWERFIRST);

    // xsl:message
    final XSLTAttributeDef terminateAttr = new XSLTAttributeDef(null, "terminate", XSLTAttributeDef.T_YESNO, false,
            false, XSLTAttributeDef.ERROR);
    terminateAttr.setDefault("no");

    // top level attributes
    final XSLTAttributeDef xslExcludeResultPrefixesAttr = new XSLTAttributeDef(
            de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "exclude-result-prefixes", XSLTAttributeDef.T_PREFIXLIST,
            false, false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef xslExtensionElementPrefixesAttr = new XSLTAttributeDef(
            de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "extension-element-prefixes",
            XSLTAttributeDef.T_PREFIX_URLLIST, false, false, XSLTAttributeDef.ERROR);
    // result-element-atts
    final XSLTAttributeDef xslUseAttributeSetsAttr = new XSLTAttributeDef(
            de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "use-attribute-sets", XSLTAttributeDef.T_QNAMES, false,
            false, XSLTAttributeDef.ERROR);
    final XSLTAttributeDef xslVersionAttr = new XSLTAttributeDef(de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "version", XSLTAttributeDef.T_NMTOKEN, false, false, XSLTAttributeDef.ERROR);

    final XSLTElementDef charData = new XSLTElementDef(this, null, "text()", null /* alias */, null /* elements */,
            null, /* attributes */
            new ProcessorCharacters(), ElemTextLiteral.class /* class object */);

    charData.setType(XSLTElementDef.T_PCDATA);

    final XSLTElementDef whiteSpaceOnly = new XSLTElementDef(this, null, "text()", null /* alias */,
            null /* elements */, null, /* attributes */
            null, ElemTextLiteral.class /* should be null? -sb */);

    charData.setType(XSLTElementDef.T_PCDATA);

    final XSLTAttributeDef resultAttr = new XSLTAttributeDef(null, "*", XSLTAttributeDef.T_AVT, false, true,
            XSLTAttributeDef.WARNING);
    final XSLTAttributeDef xslResultAttr = new XSLTAttributeDef(de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "*",
            XSLTAttributeDef.T_CDATA, false, false, XSLTAttributeDef.WARNING);

    final XSLTElementDef[] templateElements = new XSLTElementDef[23];
    final XSLTElementDef[] templateElementsAndParams = new XSLTElementDef[24];
    final XSLTElementDef[] templateElementsAndSort = new XSLTElementDef[24];
    // exslt
    final XSLTElementDef[] exsltFunctionElements = new XSLTElementDef[24];

    final XSLTElementDef[] charTemplateElements = new XSLTElementDef[15];
    final XSLTElementDef resultElement = new XSLTElementDef(this, null, "*", null /* alias */,
            templateElements /* elements */, new XSLTAttributeDef[] {
                    spaceAttrLiteral, // special
                    xslExcludeResultPrefixesAttr, xslExtensionElementPrefixesAttr, xslUseAttributeSetsAttr,
                    xslVersionAttr, xslResultAttr, resultAttr }, new ProcessorLRE(), ElemLiteralResult.class /*
                                                                                                              * class
                                                                                                              * object
                                                                                                              */, 20,
            true);
    final XSLTElementDef unknownElement = new XSLTElementDef(this, "*", "unknown", null /* alias */,
            templateElementsAndParams /* elements */,
            new XSLTAttributeDef[] { xslExcludeResultPrefixesAttr, xslExtensionElementPrefixesAttr,
                    xslUseAttributeSetsAttr, xslVersionAttr, xslResultAttr, resultAttr }, new ProcessorUnknown(),
            ElemUnknown.class /* class object */, 20, true);
    final XSLTElementDef xslValueOf = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "value-of", null /* alias */, null /* elements */, new XSLTAttributeDef[] { selectAttrRequired,
                    disableOutputEscapingAttr }, new ProcessorTemplateElem(), ElemValueOf.class /*
                                                                                                 * class
                                                                                                 * object
                                                                                                 */, 20, true);
    final XSLTElementDef xslCopyOf = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "copy-of",
            null /* alias */, null /* elements */, new XSLTAttributeDef[] { selectAttrRequired },
            new ProcessorTemplateElem(), ElemCopyOf.class /* class object */, 20, true);
    final XSLTElementDef xslNumber = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "number",
            null /* alias */, null /* elements */, new XSLTAttributeDef[] { levelAttr, countAttr, fromAttr, valueAttr,
                    formatAttr, langAttr, letterValueAttr, groupingSeparatorAVT, groupingSizeAttr },
            new ProcessorTemplateElem(), ElemNumber.class /* class object */, 20, true);

    // <!-- xsl:sort cannot occur after any other elements or
    // any non-whitespace character -->
    final XSLTElementDef xslSort = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "sort",
            null /* alias */, null /* elements */, new XSLTAttributeDef[] { selectAttrDefDot, langAttr, dataTypeAttr,
                    orderAttr, caseOrderAttr }, new ProcessorTemplateElem(), ElemSort.class/*
                                                                                            * class
                                                                                            * object
                                                                                            */, 19, true);
    final XSLTElementDef xslWithParam = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "with-param", null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { nameAttrRequired, selectAttrOpt }, new ProcessorTemplateElem(),
            ElemWithParam.class /* class object */, 19, true);
    final XSLTElementDef xslApplyTemplates = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "apply-templates", null /* alias */, new XSLTElementDef[] { xslSort, xslWithParam } /* elements */,
            new XSLTAttributeDef[] { selectAttrDefNode, modeAttr }, new ProcessorTemplateElem(),
            ElemApplyTemplates.class /* class object */, 20, true);
    final XSLTElementDef xslApplyImports = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "apply-imports", null /* alias */, null /* elements */, new XSLTAttributeDef[] {},
            new ProcessorTemplateElem(), ElemApplyImport.class /* class object */);
    final XSLTElementDef xslForEach = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "for-each", null /* alias */,
            templateElementsAndSort, // (#PCDATA %instructions;
                                     // %result-elements; | xsl:sort)*
            new XSLTAttributeDef[] { selectAttrRequired, spaceAttr }, new ProcessorTemplateElem(),
            ElemForEach.class /* class object */, true, false, true, 20, true);
    final XSLTElementDef xslIf = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "if",
            null /* alias */,
            templateElements /* elements */, // %template;
            new XSLTAttributeDef[] { testAttrRequired, spaceAttr }, new ProcessorTemplateElem(), ElemIf.class /*
                                                                                                               * class
                                                                                                               * object
                                                                                                               */, 20,
            true);
    final XSLTElementDef xslWhen = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "when",
            null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { testAttrRequired, spaceAttr }, new ProcessorTemplateElem(), ElemWhen.class /*
                                                                                                                 * class
                                                                                                                 * object
                                                                                                                 */,
            false, true, 1, true);
    final XSLTElementDef xslOtherwise = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "otherwise", null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { spaceAttr }, new ProcessorTemplateElem(), ElemOtherwise.class /*
                                                                                                    * class
                                                                                                    * object
                                                                                                    */, false, false,
            2, false);
    final XSLTElementDef xslChoose = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "choose",
            null /* alias */, new XSLTElementDef[] { xslWhen, xslOtherwise } /* elements */,
            new XSLTAttributeDef[] { spaceAttr }, new ProcessorTemplateElem(), ElemChoose.class /*
                                                                                                 * class
                                                                                                 * object
                                                                                                 */, true, false, true,
            20, true);
    final XSLTElementDef xslAttribute = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "attribute", null /* alias */,
            charTemplateElements /* elements */, // %char-template;>
            new XSLTAttributeDef[] { nameAVTRequired, namespaceAVTOpt, spaceAttr }, new ProcessorTemplateElem(),
            ElemAttribute.class /* class object */, 20, true);
    final XSLTElementDef xslCallTemplate = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "call-template", null /* alias */, new XSLTElementDef[] { xslWithParam } /* elements */,
            new XSLTAttributeDef[] { nameAttrRequired }, new ProcessorTemplateElem(), ElemCallTemplate.class /*
                                                                                                              * class
                                                                                                              * object
                                                                                                              */, 20,
            true);
    final XSLTElementDef xslVariable = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "variable", null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { nameAttrRequired, selectAttrOpt }, new ProcessorTemplateElem(),
            ElemVariable.class /* class object */, 20, true);
    final XSLTElementDef xslParam = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "param",
            null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { nameAttrRequired, selectAttrOpt }, new ProcessorTemplateElem(),
            ElemParam.class /* class object */, 19, true);
    final XSLTElementDef xslText = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "text",
            null /* alias */, new XSLTElementDef[] { charData } /* elements */,
            new XSLTAttributeDef[] { disableOutputEscapingAttr }, new ProcessorText(), ElemText.class /*
                                                                                                       * class
                                                                                                       * object
                                                                                                       */, 20, true);
    final XSLTElementDef xslProcessingInstruction = new XSLTElementDef(this,
            de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "processing-instruction",
            null /* alias */,
            charTemplateElements /* elements */, // %char-template;>
            new XSLTAttributeDef[] { nameAVT_NCNAMERequired, spaceAttr }, new ProcessorTemplateElem(),
            ElemPI.class /* class object */, 20, true);
    final XSLTElementDef xslElement = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "element", null /* alias */,
            templateElements /* elements */, // %template;
            new XSLTAttributeDef[] { nameAVTRequired, namespaceAVTOpt, useAttributeSetsAttr, spaceAttr },
            new ProcessorTemplateElem(), ElemElement.class /* class object */, 20, true);
    final XSLTElementDef xslComment = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "comment", null /* alias */, charTemplateElements /* elements */, // %char-template;>
            new XSLTAttributeDef[] { spaceAttr }, new ProcessorTemplateElem(), ElemComment.class /*
                                                                                                  * class
                                                                                                  * object
                                                                                                  */, 20, true);
    final XSLTElementDef xslCopy = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "copy",
            null /* alias */,
            templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { spaceAttr, useAttributeSetsAttr }, new ProcessorTemplateElem(),
            ElemCopy.class /* class object */, 20, true);
    final XSLTElementDef xslMessage = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "message", null /* alias */, templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { terminateAttr }, new ProcessorTemplateElem(), ElemMessage.class /*
                                                                                                      * class
                                                                                                      * object
                                                                                                      */, 20, true);
    final XSLTElementDef xslFallback = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "fallback", null /* alias */, templateElements /* elements */, // %template;>
            new XSLTAttributeDef[] { spaceAttr }, new ProcessorTemplateElem(), ElemFallback.class /*
                                                                                                   * class
                                                                                                   * object
                                                                                                   */, 20, true);
    // exslt
    final XSLTElementDef exsltFunction = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_EXSLT_FUNCTIONS_URL,
            "function", null /* alias */, exsltFunctionElements /* elements */,
            new XSLTAttributeDef[] { nameAttrRequired }, new ProcessorExsltFunction(), ElemExsltFunction.class /*
                                                                                                                * class
                                                                                                                * object
                                                                                                                */);
    final XSLTElementDef exsltResult = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_EXSLT_FUNCTIONS_URL,
            "result", null /* alias */, templateElements /* elements */, new XSLTAttributeDef[] { selectAttrOpt },
            new ProcessorExsltFuncResult(), ElemExsltFuncResult.class /*
                                                                       * class
                                                                       * object
                                                                       */);

    int i = 0;

    templateElements[i++] = charData; // #PCDATA

    // char-instructions
    templateElements[i++] = xslApplyTemplates;
    templateElements[i++] = xslCallTemplate;
    templateElements[i++] = xslApplyImports;
    templateElements[i++] = xslForEach;
    templateElements[i++] = xslValueOf;
    templateElements[i++] = xslCopyOf;
    templateElements[i++] = xslNumber;
    templateElements[i++] = xslChoose;
    templateElements[i++] = xslIf;
    templateElements[i++] = xslText;
    templateElements[i++] = xslCopy;
    templateElements[i++] = xslVariable;
    templateElements[i++] = xslMessage;
    templateElements[i++] = xslFallback;

    // instructions
    templateElements[i++] = xslProcessingInstruction;
    templateElements[i++] = xslComment;
    templateElements[i++] = xslElement;
    templateElements[i++] = xslAttribute;
    templateElements[i++] = resultElement;
    templateElements[i++] = unknownElement;
    templateElements[i++] = exsltFunction;
    templateElements[i++] = exsltResult;

    System.arraycopy(templateElements, 0, templateElementsAndParams, 0, i);
    System.arraycopy(templateElements, 0, templateElementsAndSort, 0, i);
    System.arraycopy(templateElements, 0, exsltFunctionElements, 0, i);

    templateElementsAndParams[i] = xslParam;
    templateElementsAndSort[i] = xslSort;
    exsltFunctionElements[i] = xslParam;

    i = 0;
    charTemplateElements[i++] = charData; // #PCDATA

    // char-instructions
    charTemplateElements[i++] = xslApplyTemplates;
    charTemplateElements[i++] = xslCallTemplate;
    charTemplateElements[i++] = xslApplyImports;
    charTemplateElements[i++] = xslForEach;
    charTemplateElements[i++] = xslValueOf;
    charTemplateElements[i++] = xslCopyOf;
    charTemplateElements[i++] = xslNumber;
    charTemplateElements[i++] = xslChoose;
    charTemplateElements[i++] = xslIf;
    charTemplateElements[i++] = xslText;
    charTemplateElements[i++] = xslCopy;
    charTemplateElements[i++] = xslVariable;
    charTemplateElements[i++] = xslMessage;
    charTemplateElements[i++] = xslFallback;

    final XSLTElementDef importDef = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "import",
            null /* alias */, null /* elements */, new XSLTAttributeDef[] { hrefAttr }, // EMPTY
            new ProcessorImport(), null /* class object */, 1, true);
    final XSLTElementDef includeDef = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "include", null /* alias */, null /* elements */, // EMPTY
            new XSLTAttributeDef[] { hrefAttr }, new ProcessorInclude(), null /*
                                                                               * class
                                                                               * object
                                                                               */, 20, true);

    final XSLTAttributeDef[] scriptAttrs = new XSLTAttributeDef[] {
            new XSLTAttributeDef(null, "lang", XSLTAttributeDef.T_NMTOKEN, true, false, XSLTAttributeDef.WARNING),
            new XSLTAttributeDef(null, "src", XSLTAttributeDef.T_URL, false, false, XSLTAttributeDef.WARNING) };

    final XSLTAttributeDef[] componentAttrs = new XSLTAttributeDef[] {
            new XSLTAttributeDef(null, "prefix", XSLTAttributeDef.T_NMTOKEN, true, false, XSLTAttributeDef.WARNING),
            new XSLTAttributeDef(null, "elements", XSLTAttributeDef.T_STRINGLIST, false, false,
                    XSLTAttributeDef.WARNING),
            new XSLTAttributeDef(null, "functions", XSLTAttributeDef.T_STRINGLIST, false, false,
                    XSLTAttributeDef.WARNING) };

    final XSLTElementDef[] topLevelElements = new XSLTElementDef[] {
            includeDef,
            importDef,
            // resultElement,
            whiteSpaceOnly,
            unknownElement,
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "strip-space", null /* alias */,
                    null /* elements */, new XSLTAttributeDef[] { elementsAttr }, new ProcessorStripSpace(), null /*
                                                                                                                   * class
                                                                                                                   * object
                                                                                                                   */,
                    20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "preserve-space", null /* alias */,
                    null /* elements */, new XSLTAttributeDef[] { elementsAttr }, new ProcessorPreserveSpace(),
                    null /* class object */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "output", null /* alias */,
                    null /* elements */, new XSLTAttributeDef[] { methodAttr, versionAttr, encodingAttr,
                            omitXmlDeclarationAttr, standaloneAttr, doctypePublicAttr, doctypeSystemAttr,
                            cdataSectionElementsAttr, indentAttr, mediaTypeAttr, XSLTAttributeDef.m_foreignAttr },
                    new ProcessorOutputElem(), null /* class object */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "key",
                    null /* alias */,
                    null /* elements */, // EMPTY
                    new XSLTAttributeDef[] { nameAttrRequired, matchAttrRequired, useAttr }, new ProcessorKey(),
                    null /* class object */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "decimal-format", null /* alias */,
                    null /* elements */, // EMPTY
                    new XSLTAttributeDef[] { nameAttrOpt_ERROR, decimalSeparatorAttr, groupingSeparatorAttr,
                            infinityAttr, minusSignAttr, NaNAttr, percentAttr, perMilleAttr, zeroDigitAttr, digitAttr,
                            patternSeparatorAttr }, new ProcessorDecimalFormat(), null /*
                                                                                        * class
                                                                                        * object
                                                                                        */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "attribute-set", null /* alias */,
                    new XSLTElementDef[] { xslAttribute } /* elements */, new XSLTAttributeDef[] { nameAttrRequired,
                            useAttributeSetsAttr }, new ProcessorAttributeSet(), null /*
                                                                                       * class
                                                                                       * object
                                                                                       */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "variable", null /* alias */,
                    templateElements /* elements */, new XSLTAttributeDef[] { nameAttrRequired, selectAttrOpt },
                    new ProcessorGlobalVariableDecl(), ElemVariable.class /*
                                                                           * class
                                                                           * object
                                                                           */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "param", null /* alias */,
                    templateElements /* elements */, new XSLTAttributeDef[] { nameAttrRequired, selectAttrOpt },
                    new ProcessorGlobalParamDecl(), ElemParam.class /*
                                                                     * class
                                                                     * object
                                                                     */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "template", null /* alias */,
                    templateElementsAndParams /* elements */, new XSLTAttributeDef[] { matchAttrOpt, nameAttrOpt_ERROR,
                            priorityAttr, modeAttr, spaceAttr }, new ProcessorTemplate(), ElemTemplate.class /*
                                                                                                              * class
                                                                                                              * object
                                                                                                              */, true,
                    20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL, "namespace-alias",
                    null /* alias */,
                    null /* elements */, // EMPTY
                    new XSLTAttributeDef[] { stylesheetPrefixAttr, resultPrefixAttr }, new ProcessorNamespaceAlias(),
                    null /* class object */, 20, true),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_BUILTIN_EXTENSIONS_URL, "component",
                    null /* alias */, new XSLTElementDef[] { new XSLTElementDef(this,
                            de.lyca.xml.utils.Constants.S_BUILTIN_EXTENSIONS_URL, "script", null /* alias */,
                            new XSLTElementDef[] { charData } /* elements */, scriptAttrs, new ProcessorLRE(),
                            ElemExtensionScript.class /* class object */, 20, true) }, // EMPTY
                    componentAttrs, new ProcessorLRE(), ElemExtensionDecl.class /*
                                                                                 * class
                                                                                 * object
                                                                                 */),
            new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL, "component",
                    null /* alias */, new XSLTElementDef[] { new XSLTElementDef(this,
                            de.lyca.xml.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL, "script", null /* alias */,
                            new XSLTElementDef[] { charData } /* elements */, scriptAttrs, new ProcessorLRE(),
                            ElemExtensionScript.class /* class object */, 20, true) }, // EMPTY
                    componentAttrs, new ProcessorLRE(), ElemExtensionDecl.class /*
                                                                                 * class
                                                                                 * object
                                                                                 */), exsltFunction }/* exslt */; // end
                                                                                                                  // of
                                                                                                                  // topevelElements

    final XSLTAttributeDef excludeResultPrefixesAttr = new XSLTAttributeDef(null, "exclude-result-prefixes",
            XSLTAttributeDef.T_PREFIXLIST, false, false, XSLTAttributeDef.WARNING);
    final XSLTAttributeDef extensionElementPrefixesAttr = new XSLTAttributeDef(null, "extension-element-prefixes",
            XSLTAttributeDef.T_PREFIX_URLLIST, false, false, XSLTAttributeDef.WARNING);
    final XSLTAttributeDef idAttr = new XSLTAttributeDef(null, "id", XSLTAttributeDef.T_CDATA, false, false,
            XSLTAttributeDef.WARNING);
    final XSLTAttributeDef versionAttrRequired = new XSLTAttributeDef(null, "version", XSLTAttributeDef.T_NMTOKEN,
            true, false, XSLTAttributeDef.WARNING);
    final XSLTElementDef stylesheetElemDef = new XSLTElementDef(this, de.lyca.xml.utils.Constants.S_XSLNAMESPACEURL,
            "stylesheet", "transform", topLevelElements, new XSLTAttributeDef[] { extensionElementPrefixesAttr,
                    excludeResultPrefixesAttr, idAttr, versionAttrRequired, spaceAttr },
            new ProcessorStylesheetElement(), /* ContentHandler */
            null /* class object */, true, -1, false);

    importDef.setElements(new XSLTElementDef[] { stylesheetElemDef, resultElement, unknownElement });
    includeDef.setElements(new XSLTElementDef[] { stylesheetElemDef, resultElement, unknownElement });
    build(null, null, null, new XSLTElementDef[] { stylesheetElemDef, whiteSpaceOnly, resultElement, unknownElement },
            null, new ProcessorStylesheetDoc(), /* ContentHandler */
            null /* class object */
    );
  }

  /**
   * A hashtable of all available built-in elements for use by the
   * element-available function. TODO: When we convert to Java2, this should be
   * a Set.
   */
  private final HashMap m_availElems = new HashMap();

  /**
   * Get the table of available elements.
   * 
   * @return table of available elements, keyed by qualified names, and with
   *         values of the same qualified names.
   */
  public HashMap getElemsAvailable() {
    return m_availElems;
  }

  /**
   * Adds a new element name to the Hashtable of available elements.
   * 
   * @param elemName
   *          The name of the element to add to the Hashtable of available
   *          elements.
   */
  void addAvailableElement(QName elemName) {
    m_availElems.put(elemName, elemName);
  }

  /**
   * Determines whether the passed element name is present in the list of
   * available elements.
   * 
   * @param elemName
   *          The name of the element to look up.
   * 
   * @return true if an element corresponding to elemName is available.
   */
  public boolean elementAvailable(QName elemName) {
    return m_availElems.containsKey(elemName);
  }
}
