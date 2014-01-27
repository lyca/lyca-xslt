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

import javax.xml.transform.TransformerException;

import de.lyca.xalan.extensions.ExtensionNamespaceSupport;
import de.lyca.xalan.extensions.ExtensionNamespacesManager;
import de.lyca.xalan.res.XSLMessages;
import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.utils.StringVector;

/**
 * Implement the declaration of an extension element
 * 
 * @xsl.usage internal
 */
public class ElemExtensionDecl extends ElemTemplateElement {
  static final long serialVersionUID = -4692738885172766789L;

  /**
   * Constructor ElemExtensionDecl
   * 
   */
  public ElemExtensionDecl() {

    // System.out.println("ElemExtensionDecl ctor");
  }

  /**
   * Prefix string for this extension element.
   * 
   * @serial
   */
  private String m_prefix = null;

  /**
   * Set the prefix for this extension element
   * 
   * 
   * @param v
   *          Prefix to set for this extension element
   */
  @Override
  public void setPrefix(String v) {
    m_prefix = v;
  }

  /**
   * Get the prefix for this extension element
   * 
   * 
   * @return Prefix for this extension element
   */
  @Override
  public String getPrefix() {
    return m_prefix;
  }

  /**
   * StringVector holding the names of functions defined in this extension.
   * 
   * @serial
   */
  private StringVector m_functions = new StringVector();

  /**
   * Set the names of functions defined in this extension
   * 
   * 
   * @param v
   *          StringVector holding the names of functions defined in this
   *          extension
   */
  public void setFunctions(StringVector v) {
    m_functions = v;
  }

  /**
   * Get the names of functions defined in this extension
   * 
   * 
   * @return StringVector holding the names of functions defined in this
   *         extension
   */
  public StringVector getFunctions() {
    return m_functions;
  }

  /**
   * Get a function at a given index in this extension element
   * 
   * 
   * @param i
   *          Index of function to get
   * 
   * @return Name of Function at given index
   * 
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getFunction(int i) throws ArrayIndexOutOfBoundsException {

    if (null == m_functions)
      throw new ArrayIndexOutOfBoundsException();

    return m_functions.elementAt(i);
  }

  /**
   * Get count of functions defined in this extension element
   * 
   * 
   * @return count of functions defined in this extension element
   */
  public int getFunctionCount() {
    return null != m_functions ? m_functions.size() : 0;
  }

  /**
   * StringVector of elements defined in this extension.
   * 
   * @serial
   */
  private StringVector m_elements = null;

  /**
   * Set StringVector of elements for this extension
   * 
   * 
   * @param v
   *          StringVector of elements to set
   */
  public void setElements(StringVector v) {
    m_elements = v;
  }

  /**
   * Get StringVector of elements defined for this extension
   * 
   * 
   * @return StringVector of elements defined for this extension
   */
  public StringVector getElements() {
    return m_elements;
  }

  /**
   * Get the element at the given index
   * 
   * 
   * @param i
   *          Index of element to get
   * 
   * @return The element at the given index
   * 
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getElement(int i) throws ArrayIndexOutOfBoundsException {

    if (null == m_elements)
      throw new ArrayIndexOutOfBoundsException();

    return m_elements.elementAt(i);
  }

  /**
   * Return the count of elements defined for this extension element
   * 
   * 
   * @return the count of elements defined for this extension element
   */
  public int getElementCount() {
    return null != m_elements ? m_elements.size() : 0;
  }

  /**
   * Get an int constant identifying the type of element.
   * 
   * @see de.lyca.xalan.templates.Constants
   * 
   * @return The token ID for this element
   */
  @Override
  public int getXSLToken() {
    return Constants.ELEMNAME_EXTENSIONDECL;
  }

  @Override
  public void compose(StylesheetRoot sroot) throws TransformerException {
    super.compose(sroot);
    final String prefix = getPrefix();
    final String declNamespace = getNamespaceForPrefix(prefix);
    String lang = null;
    String srcURL = null;
    String scriptSrc = null;
    if (null == declNamespace)
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_NAMESPACE_DECL,
              new Object[] { prefix }));
    // "Prefix " + prefix does not have a corresponding namespace declaration");
    for (ElemTemplateElement child = getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
      if (Constants.ELEMNAME_EXTENSIONSCRIPT == child.getXSLToken()) {
        final ElemExtensionScript sdecl = (ElemExtensionScript) child;
        lang = sdecl.getLang();
        srcURL = sdecl.getSrc();
        final ElemTemplateElement childOfSDecl = sdecl.getFirstChildElem();
        if (null != childOfSDecl) {
          if (Constants.ELEMNAME_TEXTLITERALRESULT == childOfSDecl.getXSLToken()) {
            final ElemTextLiteral tl = (ElemTextLiteral) childOfSDecl;
            final char[] chars = tl.getChars();
            scriptSrc = new String(chars);
            if (scriptSrc.trim().length() == 0) {
              scriptSrc = null;
            }
          }
        }
      }
    }
    if (null == lang) {
      lang = "javaclass";
    }
    if (lang.equals("javaclass") && scriptSrc != null)
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEM_CONTENT_NOT_ALLOWED,
              new Object[] { scriptSrc }));
    // "Element content not allowed for lang=javaclass " + scriptSrc);

    // Register the extension namespace if it has not already been registered.
    ExtensionNamespaceSupport extNsSpt = null;
    final ExtensionNamespacesManager extNsMgr = sroot.getExtensionNamespacesManager();
    if (extNsMgr.namespaceIndex(declNamespace, extNsMgr.getExtensions()) == -1) {
      if (lang.equals("javaclass")) {
        if (null == srcURL) {
          extNsSpt = extNsMgr.defineJavaNamespace(declNamespace);
        } else if (extNsMgr.namespaceIndex(srcURL, extNsMgr.getExtensions()) == -1) {
          extNsSpt = extNsMgr.defineJavaNamespace(declNamespace, srcURL);
        }
      } else // not java
      {
        final String handler = "de.lyca.xalan.extensions.ExtensionHandlerGeneral";
        final Object[] args = { declNamespace, m_elements, m_functions, lang, srcURL, scriptSrc, getSystemId() };
        extNsSpt = new ExtensionNamespaceSupport(declNamespace, handler, args);
      }
    }
    if (extNsSpt != null) {
      extNsMgr.registerExtension(extNsSpt);
    }
  }

  /**
   * This function will be called on top-level elements only, just before the
   * transform begins.
   * 
   * @param transformer
   *          The XSLT TransformerFactory.
   * 
   * @throws TransformerException
   */
  @Override
  public void runtimeInit(TransformerImpl transformer) throws TransformerException {
    /*
     * //System.out.println("ElemExtensionDecl.runtimeInit()"); String lang =
     * null; String srcURL = null; String scriptSrc = null; String prefix =
     * getPrefix(); String declNamespace = getNamespaceForPrefix(prefix);
     * 
     * if (null == declNamespace) throw new
     * TransformerException(XSLMessages.createMessage
     * (XSLTErrorResources.ER_NO_NAMESPACE_DECL, new Object[]{prefix}));
     * //"Prefix " + prefix does not have a corresponding namespace
     * declaration");
     * 
     * for (ElemTemplateElement child = getFirstChildElem(); child != null;
     * child = child.getNextSiblingElem()) { if
     * (Constants.ELEMNAME_EXTENSIONSCRIPT == child.getXSLToken()) {
     * ElemExtensionScript sdecl = (ElemExtensionScript) child;
     * 
     * lang = sdecl.getLang(); srcURL = sdecl.getSrc();
     * 
     * ElemTemplateElement childOfSDecl = sdecl.getFirstChildElem();
     * 
     * if (null != childOfSDecl) { if (Constants.ELEMNAME_TEXTLITERALRESULT ==
     * childOfSDecl.getXSLToken()) { ElemTextLiteral tl = (ElemTextLiteral)
     * childOfSDecl; char[] chars = tl.getChars();
     * 
     * scriptSrc = new String(chars);
     * 
     * if (scriptSrc.trim().length() == 0) scriptSrc = null; } } } }
     * 
     * if (null == lang) lang = "javaclass";
     * 
     * if (lang.equals("javaclass") && (scriptSrc != null)) throw new
     * TransformerException
     * (XSLMessages.createMessage(XSLTErrorResources.ER_ELEM_CONTENT_NOT_ALLOWED
     * , new Object[]{scriptSrc}));
     * //"Element content not allowed for lang=javaclass " + scriptSrc);
     * 
     * // Instantiate a handler for this extension namespace. ExtensionsTable
     * etable = transformer.getExtensionsTable(); ExtensionHandler nsh =
     * etable.get(declNamespace);
     * 
     * // If we have no prior ExtensionHandler for this namespace, we need to //
     * create one. // If the script element is for javaclass, this is our
     * special compiled java. // Element content is not supported for this so we
     * throw an exception if // it is provided. Otherwise, we look up the srcURL
     * to see if we already have // an ExtensionHandler. if (null == nsh) { if
     * (lang.equals("javaclass")) { if (null == srcURL) { nsh =
     * etable.makeJavaNamespace(declNamespace); } else { nsh =
     * etable.get(srcURL);
     * 
     * if (null == nsh) { nsh = etable.makeJavaNamespace(srcURL); } } } else //
     * not java { nsh = new ExtensionHandlerGeneral(declNamespace,
     * this.m_elements, this.m_functions, lang, srcURL, scriptSrc,
     * getSystemId());
     * 
     * // System.out.println("Adding NS Handler: declNamespace = "+ //
     * declNamespace+", lang = "+lang+", srcURL = "+ //
     * srcURL+", scriptSrc="+scriptSrc); }
     * 
     * etable.addExtensionNamespace(declNamespace, nsh); }
     */
  }
}
