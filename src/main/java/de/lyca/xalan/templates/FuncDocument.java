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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMIterator;
import de.lyca.xml.utils.XMLString;
import de.lyca.xpath.Expression;
import de.lyca.xpath.NodeSetDTM;
import de.lyca.xpath.SourceTreeManager;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.functions.Function2Args;
import de.lyca.xpath.functions.WrongNumberArgsException;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;

/**
 * Execute the Doc() function.
 * 
 * When the document function has exactly one argument and the argument is a
 * node-set, then the result is the union, for each node in the argument
 * node-set, of the result of calling the document function with the first
 * argument being the string-value of the node, and the second argument being a
 * node-set with the node as its only member. When the document function has two
 * arguments and the first argument is a node-set, then the result is the union,
 * for each node in the argument node-set, of the result of calling the document
 * function with the first argument being the string-value of the node, and with
 * the second argument being the second argument passed to the document
 * function.
 * 
 * @xsl.usage advanced
 */
public class FuncDocument extends Function2Args {
  static final long serialVersionUID = 2483304325971281424L;

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
    final int context = xctxt.getCurrentNode();
    final DTM dtm = xctxt.getDTM(context);

    final int docContext = dtm.getDocumentRoot(context);
    final XObject arg = this.getArg0().execute(xctxt);

    String base = "";
    final Expression arg1Expr = this.getArg1();

    if (null != arg1Expr) {

      // The URI reference may be relative. The base URI (see [3.2 Base URI])
      // of the node in the second argument node-set that is first in document
      // order is used as the base URI for resolving the
      // relative URI into an absolute URI.
      final XObject arg2 = arg1Expr.execute(xctxt);

      if (XObject.CLASS_NODESET == arg2.getType()) {
        final int baseNode = arg2.iter().nextNode();

        if (baseNode == DTM.NULL) {
          // See http://www.w3.org/1999/11/REC-xslt-19991116-errata#E14.
          // If the second argument is an empty nodeset, this is an error.
          // The processor can recover by returning an empty nodeset.
          warn(xctxt, XSLTErrorResources.WG_EMPTY_SECOND_ARG, null);
          final XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
          return nodes;
        } else {
          final DTM baseDTM = xctxt.getDTM(baseNode);
          base = baseDTM.getDocumentBaseURI();
        }
        // %REVIEW% This doesn't seem to be a problem with the conformance
        // suite, but maybe it's just not doing a good test?
        // int baseDoc = baseDTM.getDocument();
        //
        // if (baseDoc == DTM.NULL /* || baseDoc instanceof Stylesheet -->What
        // to do?? */)
        // {
        //
        // // base = ((Stylesheet)baseDoc).getBaseIdentifier();
        // base = xctxt.getNamespaceContext().getBaseIdentifier();
        // }
        // else
        // base = xctxt.getSourceTreeManager().findURIFromDoc(baseDoc);
      } else {
        // Can not convert other type to a node-set!;
        arg2.iter();
      }
    } else {

      // If the second argument is omitted, then it defaults to
      // the node in the stylesheet that contains the expression that
      // includes the call to the document function. Note that a
      // zero-length URI reference is a reference to the document
      // relative to which the URI reference is being resolved; thus
      // document("") refers to the root node of the stylesheet;
      // the tree representation of the stylesheet is exactly
      // the same as if the XML document containing the stylesheet
      // was the initial source document.
      assertion(null != xctxt.getNamespaceContext(), "Namespace context can not be null!");
      base = xctxt.getNamespaceContext().getBaseIdentifier();
    }

    final XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
    final NodeSetDTM mnl = nodes.mutableNodeset();
    final DTMIterator iterator = XObject.CLASS_NODESET == arg.getType() ? arg.iter() : null;
    int pos = DTM.NULL;

    while (null == iterator || DTM.NULL != (pos = iterator.nextNode())) {
      final XMLString ref = null != iterator ? xctxt.getDTM(pos).getStringValue(pos) : arg.xstr();

      // The first and only argument was a nodeset, the base in that
      // case is the base URI of the node from the first argument nodeset.
      // Remember, when the document function has exactly one argument and
      // the argument is a node-set, then the result is the union, for each
      // node in the argument node-set, of the result of calling the document
      // function with the first argument being the string-value of the node,
      // and the second argument being a node-set with the node as its only
      // member.
      if (null == arg1Expr && DTM.NULL != pos) {
        final DTM baseDTM = xctxt.getDTM(pos);
        base = baseDTM.getDocumentBaseURI();
      }

      if (null == ref) {
        continue;
      }

      if (DTM.NULL == docContext) {
        error(xctxt, XSLTErrorResources.ER_NO_CONTEXT_OWNERDOC, null); // "context does not have an owner document!");
      }

      // From http://www.ics.uci.edu/pub/ietf/uri/rfc1630.txt
      // A partial form can be distinguished from an absolute form in that the
      // latter must have a colon and that colon must occur before any slash
      // characters. Systems not requiring partial forms should not use any
      // unencoded slashes in their naming schemes. If they do, absolute URIs
      // will still work, but confusion may result.
      final int indexOfColon = ref.indexOf(':');
      final int indexOfSlash = ref.indexOf('/');

      if (indexOfColon != -1 && indexOfSlash != -1 && indexOfColon < indexOfSlash) {

        // The url (or filename, for that matter) is absolute.
        base = null;
      }

      final int newDoc = getDoc(xctxt, context, ref.toString(), base);

      // nodes.mutableNodeset().addNode(newDoc);
      if (DTM.NULL != newDoc) {
        // TODO: mnl.addNodeInDocOrder(newDoc, true, xctxt); ??
        if (!mnl.contains(newDoc)) {
          mnl.addElement(newDoc);
        }
      }

      if (null == iterator || newDoc == DTM.NULL) {
        break;
      }
    }

    return nodes;
  }

  /**
   * Get the document from the given URI and base
   * 
   * @param xctxt
   *          The XPath runtime state.
   * @param context
   *          The current context node
   * @param uri
   *          Relative(?) URI of the document
   * @param base
   *          Base to resolve relative URI from.
   * 
   * @return The document Node pointing to the document at the given URI or null
   * 
   * @throws javax.xml.transform.TransformerException
   */
  int getDoc(XPathContext xctxt, int context, String uri, String base) throws javax.xml.transform.TransformerException {

    // System.out.println("base: "+base+", uri: "+uri);
    final SourceTreeManager treeMgr = xctxt.getSourceTreeManager();
    Source source;

    int newDoc;
    try {
      source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
      newDoc = treeMgr.getNode(source);
    } catch (final IOException ioe) {
      throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
    } catch (final TransformerException te) {
      throw new TransformerException(te);
    }

    if (DTM.NULL != newDoc)
      return newDoc;

    // If the uri length is zero, get the uri of the stylesheet.
    if (uri.length() == 0) {
      // Hmmm... this seems pretty bogus to me... -sb
      uri = xctxt.getNamespaceContext().getBaseIdentifier();
      try {
        source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
      } catch (final IOException ioe) {
        throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
      }
    }

    String diagnosticsString = null;

    try {
      if (null != uri && uri.length() > 0) {
        newDoc = treeMgr.getSourceTree(source, xctxt.getSAXLocator(), xctxt);

        // System.out.println("newDoc: "+((Document)newDoc).getDocumentElement().getNodeName());
      } else {
        warn(xctxt, XSLTErrorResources.WG_CANNOT_MAKE_URL_FROM, new Object[] { (base == null ? "" : base) + uri }); // "Can not make URL from: "+((base
                                                                                                                    // ==
                                                                                                                    // null)
                                                                                                                    // ?
                                                                                                                    // ""
                                                                                                                    // :
                                                                                                                    // base
                                                                                                                    // )+uri);
      }
    } catch (Throwable throwable) {

      // throwable.printStackTrace();
      newDoc = DTM.NULL;

      // path.warn(XSLTErrorResources.WG_ENCODING_NOT_SUPPORTED_USING_JAVA, new
      // Object[]{((base == null) ? "" : base )+uri});
      // //"Can not load requested doc: "+((base == null) ? "" : base )+uri);
      while (throwable instanceof de.lyca.xml.utils.WrappedRuntimeException) {
        throwable = ((de.lyca.xml.utils.WrappedRuntimeException) throwable).getException();
      }

      if (throwable instanceof NullPointerException || throwable instanceof ClassCastException)
        throw new de.lyca.xml.utils.WrappedRuntimeException((Exception) throwable);

      final StringWriter sw = new StringWriter();
      final PrintWriter diagnosticsWriter = new PrintWriter(sw);

      if (throwable instanceof TransformerException) {
        final TransformerException spe = (TransformerException) throwable;

        {
          Throwable e = spe;

          while (null != e) {
            if (null != e.getMessage()) {
              diagnosticsWriter.println(" (" + e.getClass().getName() + "): " + e.getMessage());
            }

            if (e instanceof TransformerException) {
              final TransformerException spe2 = (TransformerException) e;

              final SourceLocator locator = spe2.getLocator();
              if (null != locator && null != locator.getSystemId()) {
                diagnosticsWriter.println("   ID: " + locator.getSystemId() + " Line #" + locator.getLineNumber()
                        + " Column #" + locator.getColumnNumber());
              }

              e = spe2.getException();

              if (e instanceof de.lyca.xml.utils.WrappedRuntimeException) {
                e = ((de.lyca.xml.utils.WrappedRuntimeException) e).getException();
              }
            } else {
              e = null;
            }
          }
        }
      } else {
        diagnosticsWriter.println(" (" + throwable.getClass().getName() + "): " + throwable.getMessage());
      }

      diagnosticsString = throwable.getMessage(); // sw.toString();
    }

    if (DTM.NULL == newDoc) {

      // System.out.println("what?: "+base+", uri: "+uri);
      if (null != diagnosticsString) {
        warn(xctxt, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC, new Object[] { diagnosticsString }); // "Can not load requested doc: "+((base
                                                                                                          // ==
                                                                                                          // null)
                                                                                                          // ?
                                                                                                          // ""
                                                                                                          // :
                                                                                                          // base
                                                                                                          // )+uri);
      } else {
        warn(xctxt, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC, new Object[] { uri == null ? (base == null ? ""
                : base) + uri : uri.toString() }); // "Can not load requested doc: "+((base
                                                   // == null) ? "" : base
                                                   // )+uri);
      }
    } else {
      // %REVIEW%
      // TBD: What to do about XLocator?
      // xctxt.getSourceTreeManager().associateXLocatorToNode(newDoc, url,
      // null);
    }

    return newDoc;
  }

  /**
   * Tell the user of an error, and probably throw an exception.
   * 
   * @param xctxt
   *          The XPath runtime state.
   * @param msg
   *          The error message key
   * @param args
   *          Arguments to be used in the error message
   * @throws XSLProcessorException
   *           thrown if the active ProblemListener and XPathContext decide the
   *           error condition is severe enough to halt processing.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public void error(XPathContext xctxt, String msg, Object args[]) throws javax.xml.transform.TransformerException {

    final String formattedMsg = XSLMessages.createMessage(msg, args);
    final ErrorListener errHandler = xctxt.getErrorListener();
    final TransformerException spe = new TransformerException(formattedMsg, xctxt.getSAXLocator());

    if (null != errHandler) {
      errHandler.error(spe);
    } else {
      System.out.println(formattedMsg);
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
   *          Arguments to be used in the warning message
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
    final TransformerException spe = new TransformerException(formattedMsg, xctxt.getSAXLocator());

    if (null != errHandler) {
      errHandler.warning(spe);
    } else {
      System.out.println(formattedMsg);
    }
  }

  /**
   * Overide the superclass method to allow one or two arguments.
   * 
   * 
   * @param argNum
   *          Number of arguments passed in to this function
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
    if (argNum < 1 || argNum > 2) {
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
    throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_ONE_OR_TWO, null)); // "1 or 2");
  }

  /**
   * Tell if the expression is a nodeset expression.
   * 
   * @return true if the expression can be represented as a nodeset.
   */
  @Override
  public boolean isNodesetExpr() {
    return true;
  }

}
