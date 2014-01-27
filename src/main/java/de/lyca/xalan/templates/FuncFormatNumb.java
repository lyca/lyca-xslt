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
package de.lyca.xalan.templates;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.functions.Function3Args;
import de.lyca.xpath.functions.WrongNumberArgsException;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.objects.XString;

/**
 * Execute the FormatNumber() function.
 * 
 * @xsl.usage advanced
 */
public class FuncFormatNumb extends Function3Args {
  static final long serialVersionUID = -8869935264870858636L;

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {

    // A bit of an ugly hack to get our context.
    final ElemTemplateElement templElem = (ElemTemplateElement) xctxt.getNamespaceContext();
    final StylesheetRoot ss = templElem.getStylesheetRoot();
    java.text.DecimalFormat formatter = null;
    java.text.DecimalFormatSymbols dfs = null;
    final double num = getArg0().execute(xctxt).num();
    final String patternStr = getArg1().execute(xctxt).str();

    // TODO: what should be the behavior here??
    if (patternStr.indexOf(0x00A4) > 0) {
      ss.error(XSLTErrorResources.ER_CURRENCY_SIGN_ILLEGAL); // currency sign
                                                             // not allowed
    }

    // this third argument is not a locale name. It is the name of a
    // decimal-format declared in the stylesheet!(xsl:decimal-format
    try {
      final Expression arg2Expr = getArg2();

      if (null != arg2Expr) {
        final String dfName = arg2Expr.execute(xctxt).str();
        final QName qname = new QName(dfName, xctxt.getNamespaceContext());

        dfs = ss.getDecimalFormatComposed(qname);

        if (null == dfs) {
          warn(xctxt, XSLTErrorResources.WG_NO_DECIMALFORMAT_DECLARATION, new Object[] { dfName }); // "not
                                                                                                    // found!!!

          // formatter = new java.text.DecimalFormat(patternStr);
        } else {

          // formatter = new java.text.DecimalFormat(patternStr, dfs);
          formatter = new java.text.DecimalFormat();

          formatter.setDecimalFormatSymbols(dfs);
          formatter.applyLocalizedPattern(patternStr);
        }
      }

      // else
      if (null == formatter) {

        // look for a possible default decimal-format
        dfs = ss.getDecimalFormatComposed(new QName(""));

        if (dfs != null) {
          formatter = new java.text.DecimalFormat();

          formatter.setDecimalFormatSymbols(dfs);
          formatter.applyLocalizedPattern(patternStr);
        } else {
          dfs = new java.text.DecimalFormatSymbols(java.util.Locale.US);

          dfs.setInfinity(Constants.ATTRVAL_INFINITY);
          dfs.setNaN(Constants.ATTRVAL_NAN);

          formatter = new java.text.DecimalFormat();

          formatter.setDecimalFormatSymbols(dfs);

          if (null != patternStr) {
            formatter.applyLocalizedPattern(patternStr);
          }
        }
      }

      return new XString(formatter.format(num));
    } catch (final Exception iae) {
      templElem.error(XSLTErrorResources.ER_MALFORMED_FORMAT_STRING, new Object[] { patternStr });

      return XString.EMPTYSTRING;

      // throw new XSLProcessorException(iae);
    }
  }

  /**
   * Warn the user of a problem.
   * 
   * @param xctxt
   *          The XPath runtime state.
   * @param msg
   *          Warning message key
   * @param args
   *          Arguments to be used in warning message
   * @throws XSLProcessorException
   *           thrown if the active ProblemListener and XPathContext decide the
   *           error condition is severe enough to halt processing.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public void warn(XPathContext xctxt, String msg, Object args[]) throws javax.xml.transform.TransformerException {

    final String formattedMsg = XSLMessages.createWarning(msg, args);
    final ErrorListener errHandler = xctxt.getErrorListener();

    errHandler.warning(new TransformerException(formattedMsg, xctxt.getSAXLocator()));
  }

  /**
   * Overide the superclass method to allow one or two arguments.
   * 
   * 
   * @param argNum
   *          Number of arguments passed in
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
    if (argNum > 3 || argNum < 2) {
      reportWrongNumberArgs();
    }
  }

  /**
   * Constructs and throws a WrongNumberArgException with the appropriate
   * message for this function object.
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
    throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_TWO_OR_THREE, null)); // "2 or 3");
  }
}
