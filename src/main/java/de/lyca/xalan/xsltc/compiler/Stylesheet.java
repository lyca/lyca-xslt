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
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JExpr.newArray;
import static com.sun.codemodel.JExpr.refthis;
import static com.sun.codemodel.JMod.PUBLIC;
import static de.lyca.xalan.xsltc.DOM.GET_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.SET_FILTER;
import static de.lyca.xalan.xsltc.compiler.Constants.DOCUMENT_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.DOM_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.ERROR;
import static de.lyca.xalan.xsltc.compiler.Constants.HASIDCALL_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.ITERATOR_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.NAMESPACE_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.NAMES_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_CHAR_DATA_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_NAMESPACE_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_NAMES_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_NS_ANCESTORS_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_PREFIX_URIS_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_PREFIX_URIS_IDX_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_TYPES_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STATIC_URIS_ARRAY_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.STRIP_SPACE;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_OUTPUT_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_VERSION_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.TYPES_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.URIS_INDEX;
import static de.lyca.xalan.xsltc.compiler.Constants.XHTML_URI;
import static de.lyca.xalan.xsltc.compiler.Constants.XSLT_URI;
import static de.lyca.xalan.xsltc.compiler.util.ErrorMsg.MULTIPLE_STYLESHEET_ERR;
import static de.lyca.xml.dtm.DTMAxisIterator.NEXT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.compiler.Whitespace.WhitespaceRule;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xalan.xsltc.dom.MultiDOM;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.utils.SystemIDResolver;

public final class Stylesheet extends SyntaxTreeNode {

  /**
   * XSLT version defined in the stylesheet.
   */
  private String _version;

  /**
   * Internal name of this stylesheet used as a key into the symbol table.
   */
  private QName _name;

  /**
   * A URI that represents the system ID for this stylesheet.
   */
  private String _systemId;

  /**
   * A reference to the parent stylesheet or null if topmost.
   */
  private Stylesheet _parentStylesheet;

  /**
   * Contains global variables and parameters defined in the stylesheet.
   */
  private final List<VariableBase> _globals = new ArrayList<>();

  /**
   * Used to cache the result returned by <code>hasLocalParams()</code>.
   */
  private Boolean _hasLocalParams = null;

  /**
   * The name of the class being generated.
   */
  private String _className;

  /**
   * Contains all templates defined in this stylesheet
   */
  private final List<Template> _templates = new ArrayList<>();

  /**
   * Used to cache result of <code>getAllValidTemplates()</code>. Only set in
   * top-level stylesheets that include/import other stylesheets.
   */
  private List<Template> _allValidTemplates = null;

  /**
   * Counter to generate unique mode suffixes.
   */
  private int _nextModeSerial = 1;

  /**
   * Mapping between mode names and Mode instances.
   */
  private final Map<QName, Mode> _modes = new HashMap<>();

  /**
   * A reference to the default Mode object.
   */
  private Mode _defaultMode;

  /**
   * Mapping between extension URIs and their prefixes.
   */
  private final Map<String, String> _extensions = new HashMap<>();

  /**
   * Reference to the stylesheet from which this stylesheet was imported (if
   * any).
   */
  public Stylesheet _importedFrom = null;

  /**
   * Reference to the stylesheet from which this stylesheet was included (if
   * any).
   */
  public Stylesheet _includedFrom = null;

  /**
   * Array of all the stylesheets imported or included from this one.
   */
  private List<Stylesheet> _includedStylesheets = null;

  /**
   * Import precendence for this stylesheet.
   */
  private int _importPrecedence = 1;

  /**
   * Minimum precendence of any descendant stylesheet by inclusion or
   * importation.
   */
  private int _minimumDescendantPrecedence = -1;

  /**
   * Mapping between key names and Key objects (needed by Key/IdPattern).
   */
  private final Map<String, Key> _keys = new HashMap<>();

  /**
   * A reference to the SourceLoader set by the user (a URIResolver if the JAXP
   * API is being used).
   */
  private SourceLoader _loader = null;

  /**
   * Flag indicating if format-number() is called.
   */
  private boolean _numberFormattingUsed = false;

  /**
   * Flag indicating if this is a simplified stylesheets. A template matching on
   * "/" must be added in this case.
   */
  private boolean _simplified = false;

  /**
   * Flag indicating if multi-document support is needed.
   */
  private boolean _multiDocument = false;

  /**
   * Flag indicating if nodset() is called.
   */
  private boolean _callsNodeset = false;

  /**
   * Flag indicating if id() is called.
   */
  private boolean _hasIdCall = false;

  /**
   * Set to true to enable template inlining optimization.
   * 
   * @see XSLTC#_templateInlining
   */
  private boolean _templateInlining = false;

  /**
   * A reference to the last xsl:output object found in the styleshet.
   */
  private Output _lastOutputElement = null;

  /**
   * Output properties for this stylesheet.
   */
  private Properties _outputProperties = null;

  /**
   * Output method for this stylesheet (must be set to one of the constants
   * defined below).
   */
  private OutputMethod _outputMethod = OutputMethod.UNKNOWN;

  // Output method constants
  public enum OutputMethod {
    UNKNOWN, XML, HTML, TEXT, XHTML
  }

  /**
   * Return the output method
   */
  public OutputMethod getOutputMethod() {
    return _outputMethod;
  }

  /**
   * Check and set the output method
   */
  private void checkOutputMethod() {
    if (_lastOutputElement != null) {
      final String method = _lastOutputElement.getOutputMethod();
      if (method != null) {
        if (method.equals("xml")) {
          _outputMethod = OutputMethod.XML;
        } else if (method.equals("html")) {
          _outputMethod = OutputMethod.HTML;
        } else if (method.equals("text")) {
          _outputMethod = OutputMethod.TEXT;
        } else if (method.equals("xhtml")) {
          _outputMethod = OutputMethod.XHTML;
        }
      }
    }
  }

  public boolean getTemplateInlining() {
    return _templateInlining;
  }

  public void setTemplateInlining(boolean flag) {
    _templateInlining = flag;
  }

  public boolean isSimplified() {
    return _simplified;
  }

  public void setSimplified() {
    _simplified = true;
  }

  public void setHasIdCall(boolean flag) {
    _hasIdCall = flag;
  }

  public void setOutputProperty(String key, String value) {
    if (_outputProperties == null) {
      _outputProperties = new Properties();
    }
    _outputProperties.setProperty(key, value);
  }

  public void setOutputProperties(Properties props) {
    _outputProperties = props;
  }

  public Properties getOutputProperties() {
    return _outputProperties;
  }

  public Output getLastOutputElement() {
    return _lastOutputElement;
  }

  public void setMultiDocument(boolean flag) {
    _multiDocument = flag;
  }

  public boolean isMultiDocument() {
    return _multiDocument;
  }

  public void setCallsNodeset(boolean flag) {
    if (flag) {
      setMultiDocument(flag);
    }
    _callsNodeset = flag;
  }

  public boolean callsNodeset() {
    return _callsNodeset;
  }

  public void numberFormattingUsed() {
    _numberFormattingUsed = true;
    /*
     * Fix for bug 23046, if the stylesheet is included, set the
     * numberFormattingUsed flag to the parent stylesheet too.
     * AbstractTranslet.addDecimalFormat() will be inlined once for the outer
     * most stylesheet.
     */
    final Stylesheet parent = getParentStylesheet();
    if (null != parent) {
      parent.numberFormattingUsed();
    }
  }

  public void setImportPrecedence(final int precedence) {
    // Set import precedence for this stylesheet
    _importPrecedence = precedence;

    // Set import precedence for all included stylesheets
    for (SyntaxTreeNode child : getContents()) {
      if (child instanceof Include) {
        final Stylesheet included = ((Include) child).getIncludedStylesheet();
        if (included != null && included._includedFrom == this) {
          included.setImportPrecedence(precedence);
        }
      }
    }

    // Set import precedence for the stylesheet that imported this one
    if (_importedFrom != null) {
      if (_importedFrom.getImportPrecedence() < precedence) {
        final Parser parser = getParser();
        final int nextPrecedence = parser.getNextImportPrecedence();
        _importedFrom.setImportPrecedence(nextPrecedence);
      }
    }
    // Set import precedence for the stylesheet that included this one
    else if (_includedFrom != null) {
      if (_includedFrom.getImportPrecedence() != precedence) {
        _includedFrom.setImportPrecedence(precedence);
      }
    }
  }

  @Override
  public int getImportPrecedence() {
    return _importPrecedence;
  }

  /**
   * Get the minimum of the precedence of this stylesheet, any stylesheet
   * imported by this stylesheet and any include/import descendant of this
   * stylesheet.
   */
  public int getMinimumDescendantPrecedence() {
    if (_minimumDescendantPrecedence == -1) {
      // Start with precedence of current stylesheet as a basis.
      int min = getImportPrecedence();

      // Recursively examine all imported/included stylesheets.
      final int inclImpCount = _includedStylesheets != null ? _includedStylesheets.size() : 0;

      for (int i = 0; i < inclImpCount; i++) {
        final int prec = _includedStylesheets.get(i).getMinimumDescendantPrecedence();

        if (prec < min) {
          min = prec;
        }
      }

      _minimumDescendantPrecedence = min;
    }
    return _minimumDescendantPrecedence;
  }

  public boolean checkForLoop(String systemId) {
    // Return true if this stylesheet includes/imports itself
    if (_systemId != null && _systemId.equals(systemId))
      return true;
    // Then check with any stylesheets that included/imported this one
    if (_parentStylesheet != null)
      return _parentStylesheet.checkForLoop(systemId);
    // Otherwise OK
    return false;
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    _name = makeStylesheetName("__stylesheet_");
  }

  public void setParentStylesheet(Stylesheet parent) {
    _parentStylesheet = parent;
  }

  public Stylesheet getParentStylesheet() {
    return _parentStylesheet;
  }

  public void setImportingStylesheet(Stylesheet parent) {
    _importedFrom = parent;
    parent.addIncludedStylesheet(this);
  }

  public void setIncludingStylesheet(Stylesheet parent) {
    _includedFrom = parent;
    parent.addIncludedStylesheet(this);
  }

  public void addIncludedStylesheet(Stylesheet child) {
    if (_includedStylesheets == null) {
      _includedStylesheets = new ArrayList<>();
    }
    _includedStylesheets.add(child);
  }

  public void setSystemId(String systemId) {
    if (systemId != null) {
      _systemId = SystemIDResolver.getAbsoluteURI(systemId);
    }
  }

  public String getSystemId() {
    return _systemId;
  }

  public void setSourceLoader(SourceLoader loader) {
    _loader = loader;
  }

  public SourceLoader getSourceLoader() {
    return _loader;
  }

  private QName makeStylesheetName(String prefix) {
    return getParser().getQName(prefix + getXSLTC().nextStylesheetSerial());
  }

  /**
   * Returns true if this stylesheet has global vars or params.
   */
  public boolean hasGlobals() {
    return _globals.size() > 0;
  }

  /**
   * Returns true if at least one template in the stylesheet has params defined.
   * Uses the variable <code>_hasLocalParams</code> to cache the result.
   */
  public boolean hasLocalParams() {
    if (_hasLocalParams == null) {
      final List<Template> templates = getAllValidTemplates();
      final int n = templates.size();
      for (int i = 0; i < n; i++) {
        final Template template = templates.get(i);
        if (template.hasParams()) {
          _hasLocalParams = Boolean.TRUE;
          return true;
        }
      }
      _hasLocalParams = Boolean.FALSE;
      return false;
    } else
      return _hasLocalParams.booleanValue();
  }

  /**
   * Adds a single prefix mapping to this syntax tree node.
   * 
   * @param prefix
   *          Namespace prefix.
   * @param uri
   *          Namespace URI.
   */
  @Override
  protected void addPrefixMapping(String prefix, String uri) {
    if (prefix.isEmpty() && uri.equals(XHTML_URI))
      return;
    super.addPrefixMapping(prefix, uri);
  }

  /**
   * Store extension URIs
   */
  private void extensionURI(String prefixes, SymbolTable stable) {
    if (prefixes != null) {
      final StringTokenizer tokens = new StringTokenizer(prefixes);
      while (tokens.hasMoreTokens()) {
        final String prefix = tokens.nextToken();
        final String uri = lookupNamespace(prefix);
        if (uri != null) {
          _extensions.put(uri, prefix);
        }
      }
    }
  }

  public boolean isExtension(String uri) {
    return _extensions.get(uri) != null;
  }

  public void declareExtensionPrefixes(Parser parser) {
    final SymbolTable stable = parser.getSymbolTable();
    final String extensionPrefixes = getAttribute("extension-element-prefixes");
    extensionURI(extensionPrefixes, stable);
  }

  /**
   * Parse the version and uri fields of the stylesheet and add an entry to the
   * symbol table mapping the name <tt>__stylesheet_</tt> to an instance of this
   * class.
   */
  @Override
  public void parseContents(Parser parser) {
    final SymbolTable stable = parser.getSymbolTable();

    /*
     * // Make sure the XSL version set in this stylesheet if ((_version ==
     * null) || (_version.isEmpty())) { reportError(this, parser,
     * ErrorMsg.REQUIRED_ATTR_ERR,"version"); } // Verify that the version is
     * 1.0 and nothing else else if (!_version.equals("1.0")) {
     * reportError(this, parser, ErrorMsg.XSL_VERSION_ERR, _version); }
     */

    // Add the implicit mapping of 'xml' to the XML namespace URI
    addPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");

    // Report and error if more than one stylesheet defined
    final Stylesheet sheet = stable.addStylesheet(_name, this);
    if (sheet != null) {
      // Error: more that one stylesheet defined
      final ErrorMsg err = new ErrorMsg(MULTIPLE_STYLESHEET_ERR, this);
      parser.reportError(ERROR, err);
    }

    // If this is a simplified stylesheet we must create a template that
    // grabs the root node of the input doc ( <xsl:template match="/"/> ).
    // This template needs the current element (the one passed to this
    // method) as its only child, so the Template class has a special
    // method that handles this (parseSimplified()).
    if (_simplified) {
      stable.excludeURI(XSLT_URI);
      final Template template = new Template();
      template.parseSimplified(this, parser);
    }
    // Parse the children of this node
    else {
      parseOwnChildren(parser);
    }
  }

  /**
   * Parse all direct children of the <xsl:stylesheet/> element.
   */
  public final void parseOwnChildren(Parser parser) {
    final SymbolTable stable = parser.getSymbolTable();
    final String excludePrefixes = getAttribute("exclude-result-prefixes");
    final String extensionPrefixes = getAttribute("extension-element-prefixes");

    // Exclude XSLT uri
    stable.pushExcludedNamespacesContext();
    stable.excludeURI(Constants.XSLT_URI);
    stable.excludeNamespaces(excludePrefixes);
    stable.excludeNamespaces(extensionPrefixes);

    final List<SyntaxTreeNode> contents = getContents();
    final int count = contents.size();

    // We have to scan the stylesheet element's top-level elements for
    // variables and/or parameters before we parse the other elements
    for (int i = 0; i < count; i++) {
      final SyntaxTreeNode child = contents.get(i);
      if (child instanceof VariableBase || child instanceof NamespaceAlias) {
        parser.getSymbolTable().setCurrentNode(child);
        child.parseContents(parser);
      }
    }

    // Now go through all the other top-level elements...
    for (int i = 0; i < count; i++) {
      final SyntaxTreeNode child = contents.get(i);
      if (!(child instanceof VariableBase) && !(child instanceof NamespaceAlias)) {
        parser.getSymbolTable().setCurrentNode(child);
        child.parseContents(parser);
      }

      // All template code should be compiled as methods if the
      // <xsl:apply-imports/> element was ever used in this stylesheet
      if (!_templateInlining && child instanceof Template) {
        final Template template = (Template) child;
        final String name = "template$dot$" + template.getPosition();
        template.setName(parser.getQName(name));
      }
    }

    stable.popExcludedNamespacesContext();
  }

  public void processModes() {
    if (_defaultMode == null) {
      _defaultMode = new Mode(null, this, "");
    }
    _defaultMode.processPatterns();
    for (final Mode mode : _modes.values()) {
      mode.processPatterns();
    }
  }

  private void compileModes(CompilerContext ctx) {
    _defaultMode.compileApplyTemplates(ctx, getXSLTC());
    for (final Mode mode : _modes.values()) {
      mode.compileApplyTemplates(ctx, getXSLTC());
    }
  }

  public Mode getMode(QName modeName) {
    if (modeName == null) {
      if (_defaultMode == null) {
        _defaultMode = new Mode(null, this, "");
      }
      return _defaultMode;
    } else {
      Mode mode = _modes.get(modeName);
      if (mode == null) {
        final String suffix = Integer.toString(_nextModeSerial++);
        _modes.put(modeName, mode = new Mode(modeName, this, suffix));
      }
      return mode;
    }
  }

  /**
   * Type check all the children of this node.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final int count = _globals.size();
    for (int i = 0; i < count; i++) {
      final VariableBase var = _globals.get(i);
      var.typeCheck(stable);
    }
    return typeCheckContents(stable);
  }

  /**
   * Translate the stylesheet into JVM bytecodes.
   */
  @Override
  public void translate(CompilerContext ctx) {
    generate();
  }

  /**
   * Generates the stylesheet java class.
   */
  public void generate() {
    _className = getXSLTC().getClassName();
    JCodeModel jCodeModel = new JCodeModel();
    try {
      // Define a new class by extending TRANSLET_CLASS
      JDefinedClass definedClass = jCodeModel._class(_className)._extends(AbstractTranslet.class);

      CompilerContext ctx = new CompilerContext(jCodeModel, definedClass, this, getXSLTC());
      ctx.addPublicField(DOM.class, DOM_FIELD);

      // Compile transform() to initialize parameters, globals & output
      // and run the transformation
      createTransformMethod(ctx);

      // Translate all non-template elements and filter out all templates
      for (SyntaxTreeNode element : getContents()) {
        // xsl:template
        if (element instanceof Template) {
          // Separate templates by modes
          final Template template = (Template) element;
          // _templates.addElement(template);
          getMode(template.getModeName()).addTemplate(template);
        }
        // xsl:attribute-set
        else if (element instanceof AttributeSet) {
          ((AttributeSet) element).translate(ctx);
        } else if (element instanceof Output) {
          // save the element for later to pass to compileConstructor
          final Output output = (Output) element;
          if (output.enabled()) {
            _lastOutputElement = output;
          }
        } else {
          // Global variables and parameters are handled elsewhere.
          // Other top-level non-template elements are ignored. Literal
          // elements outside of templates will never be output.
        }
      }
      checkOutputMethod();
      processModes();
      compileModes(ctx);
      compileStaticInitializer(ctx);
      compileConstructor(ctx, _lastOutputElement);

      if (!getParser().errorsFound()) {
        getXSLTC().dumpClass(jCodeModel, definedClass);
      }

    } catch (JClassAlreadyExistsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Compile the namesArray, urisArray, typesArray, namespaceArray,
   * namespaceAncestorsArray, prefixURIsIdxArray and prefixURIPairsArray into
   * the static initializer. They are read-only from the translet. All translet
   * instances can share a single copy of this informtion.
   * </p>
   * <p>
   * The <code>namespaceAncestorsArray</code>, <code>prefixURIsIdxArray</code>
   * and <code>prefixURIPairsArray</code> contain namespace information
   * accessible from the stylesheet:
   * <dl>
   * <dt><code>namespaceAncestorsArray</code></dt>
   * <dd>Array indexed by integer stylesheet node IDs containing node IDs of the
   * nearest ancestor node in the stylesheet with namespace declarations or
   * <code>-1</code> if there is no such ancestor. There can be more than one
   * disjoint tree of nodes - one for each stylesheet module</dd>
   * <dt><code>prefixURIsIdxArray</code></dt>
   * <dd>Array indexed by integer stylesheet node IDs containing the index into
   * <code>prefixURIPairsArray</code> of the first namespace prefix declared for
   * the node. The values are stored in ascending order, so the next value in
   * this array (if any) can be used to find the last such prefix-URI pair</dd>
   * <dt>prefixURIPairsArray</dt>
   * <dd>Array of pairs of namespace prefixes and URIs. A zero-length string
   * represents the default namespace if it appears as a prefix and a namespace
   * undeclaration if it appears as a URI.</dd>
   * </dl>
   * </p>
   * <p>
   * For this stylesheet
   * 
   * <pre>
   * <code>
   * &lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"&gt;
   *   &lt;xsl:template match="/"&gt;
   *     &lt;xsl:for-each select="*" xmlns:foo="foouri"&gt;
   *       &lt;xsl:element name="{n}" xmlns:foo="baruri"&gt;
   *     &lt;/xsl:for-each&gt;
   *     &lt;out xmlns="lumpit"/&gt;
   *     &lt;xsl:element name="{n}" xmlns="foouri"/&gt;
   *     &lt;xsl:element name="{n}" namespace="{ns}" xmlns="limpit"/gt;
   *   &lt;/xsl:template&gt;
   * &lt;/xsl:stylesheet&gt;
   * </code>
   * </pre>
   * 
   * there will be four stylesheet nodes whose namespace information is needed,
   * and
   * <ul>
   * <li><code>namespaceAncestorsArray</code> will have the value
   * <code>[-1,0,1,0]</code>;</li>
   * <li><code>prefixURIsIdxArray</code> will have the value
   * <code>[0,4,6,8]</code>; and</li>
   * <li><code>prefixURIPairsArray</code> will have the value
   * <code>["xml","http://www.w3.org/XML/1998/namespace",
   *        "xsl","http://www.w3.org/1999/XSL/Transform"
   *        "foo","foouri","foo","baruri","","foouri"].</code></li>
   * </ul>
   * </p>
   */
  private void compileStaticInitializer(CompilerContext ctx) {
    JFieldVar _sNamesArray = ctx.addProtectedStaticField(String[].class, STATIC_NAMES_ARRAY_FIELD);
    JFieldVar _sUrisArray = ctx.addProtectedStaticField(String[].class, STATIC_URIS_ARRAY_FIELD);
    JFieldVar _sTypesArray = ctx.addProtectedStaticField(int[].class, STATIC_TYPES_ARRAY_FIELD);
    JFieldVar _sNamespaceArray = ctx.addProtectedStaticField(String[].class, STATIC_NAMESPACE_ARRAY_FIELD);
    // Create fields of type char[] that will contain literal text from
    // the stylesheet.
    // TODO is this really needed
    // final int charDataFieldCount = getXSLTC().getCharacterDataCount();
    // for (int i = 0; i < charDataFieldCount; i++) {
    // addStaticField(definedClass, char[].class, STATIC_CHAR_DATA_FIELD + i);
    // }

    // Put the names array into the translet - used for dom/translet mapping
    final List<String> namesIndex = getXSLTC().getNamesIndex();
    final int size = namesIndex.size();
    final String[] namesArray = new String[size];
    final String[] urisArray = new String[size];
    final int[] typesArray = new int[size];

    int index;
    for (int i = 0; i < size; i++) {
      final String encodedName = namesIndex.get(i);
      if ((index = encodedName.lastIndexOf(':')) > -1) {
        urisArray[i] = encodedName.substring(0, index);
      }

      index = index + 1;
      if (encodedName.charAt(index) == '@') {
        typesArray[i] = DTM.ATTRIBUTE_NODE;
        index++;
      } else if (encodedName.charAt(index) == '?') {
        typesArray[i] = DTM.NAMESPACE_NODE;
        index++;
      } else {
        typesArray[i] = DTM.ELEMENT_NODE;
      }

      if (index == 0) {
        namesArray[i] = encodedName;
      } else {
        namesArray[i] = encodedName.substring(index);
      }
    }

    JBlock staticInit = ctx.clazz().init();

//    initArray(namesArray, _sNamesArray, staticInit);
    JArray namesArrayRef = newArray(_sNamesArray.type().elementType());
    for (int i = 0; i < size; i++) {
      final String name = namesArray[i];
      namesArrayRef.add(lit(name));
    }
    staticInit.assign(_sNamesArray, namesArrayRef);

//    initArray(urisArray, _sUrisArray, staticInit);
    JArray urisArrayRef = newArray(_sUrisArray.type().elementType());
    for (int i = 0; i < size; i++) {
      final String uri = urisArray[i];
      urisArrayRef.add(uri == null ? _null() : lit(uri));
    }
    staticInit.assign(_sUrisArray, urisArrayRef);

//    initArray(typesArray, _sTypesArray, staticInit);
    JArray typesArrayRef = newArray(_sTypesArray.type().elementType());
    for (int i = 0; i < size; i++) {
      final int nodeType = typesArray[i];
      typesArrayRef.add(lit(nodeType));
    }
    staticInit.assign(_sTypesArray, typesArrayRef);

    // Put the namespace names array into the translet
    final List<String> namespaces = getXSLTC().getNamespaceIndex();
    JArray namespaceArrayRef = newArray(_sNamespaceArray.type().elementType());
    for (int i = 0; i < namespaces.size(); i++) {
      final String ns = namespaces.get(i);
      namespaceArrayRef.add(lit(ns));
    }
    staticInit.assign(_sNamespaceArray, namespaceArrayRef);

    // Put the tree of stylesheet namespace declarations into the translet
    final List<Integer> namespaceAncestors = getXSLTC().getNSAncestorPointers();
    if (namespaceAncestors != null && namespaceAncestors.size() != 0) {
      JFieldVar _sNamespaceAncestorsArray = ctx.addProtectedStaticField(int[].class, STATIC_NS_ANCESTORS_ARRAY_FIELD);
      JArray namespaceAncestorsArrayRef = newArray(_sNamespaceAncestorsArray.type().elementType());
      for (int i = 0; i < namespaceAncestors.size(); i++) {
        final int ancestor = namespaceAncestors.get(i).intValue();
        namespaceAncestorsArrayRef.add(lit(ancestor));
      }
      staticInit.assign(_sNamespaceAncestorsArray, namespaceAncestorsArrayRef);
    }

    // Put the array of indices into the namespace prefix/URI pairs array
    // into the translet
    final List<Integer> prefixURIPairsIdx = getXSLTC().getPrefixURIPairsIdx();
    if (prefixURIPairsIdx != null && prefixURIPairsIdx.size() != 0) {
      JFieldVar _sPrefixURIsIdxArray = ctx.addProtectedStaticField(int[].class, STATIC_PREFIX_URIS_IDX_ARRAY_FIELD);
      JArray prefixURIPairsIdxArrayRef = newArray(_sPrefixURIsIdxArray.type().elementType());
      for (int i = 0; i < prefixURIPairsIdx.size(); i++) {
        final int idx = prefixURIPairsIdx.get(i).intValue();
        prefixURIPairsIdxArrayRef.add(lit(idx));
      }
      staticInit.assign(_sPrefixURIsIdxArray, prefixURIPairsIdxArrayRef);
    }

    // Put the array of pairs of namespace prefixes and URIs into the
    // translet
    final List<String> prefixURIPairs = getXSLTC().getPrefixURIPairs();
    if (prefixURIPairs != null && prefixURIPairs.size() != 0) {
      JFieldVar _sPrefixURIPairsArray = ctx.addProtectedStaticField(String[].class, STATIC_PREFIX_URIS_ARRAY_FIELD);
      JArray prefixURIPairsRef = newArray(_sPrefixURIPairsArray.type().elementType());
      for (int i = 0; i < prefixURIPairs.size(); i++) {
        final String prefixOrURI = prefixURIPairs.get(i);
        prefixURIPairsRef.add(lit(prefixOrURI));
      }
      staticInit.assign(_sPrefixURIPairsArray, prefixURIPairsRef);
    }

    // Grab all the literal text in the stylesheet and put it in a char[]
    final int charDataCount = getXSLTC().getCharacterDataCount();
    for (int i = 0; i < charDataCount; i++) {
      String characterData = getXSLTC().getCharacterData(i);
      JFieldVar _scharData = ctx.addProtectedStaticField(char[].class, STATIC_CHAR_DATA_FIELD + i);
      staticInit.assign(_scharData, lit(characterData).invoke("toCharArray"));
    }
  }

  private void initArray(String[] values, JFieldVar arrayVar, JBlock body) {
    JArray jArray = newArray(arrayVar.type().elementType());
    for (int i = 0, size = values.length; i < size; i++) {
      final String value = values[i];
      jArray.add(lit(value));
    }
    body.assign(arrayVar, jArray);
  }

  private void initArray(int[] values, JFieldVar arrayVar, JBlock body) {
    JArray jArray = newArray(arrayVar.type().elementType());
    for (int i = 0, size = values.length; i < size; i++) {
      final int value = values[i];
      jArray.add(lit(value));
    }
    body.assign(arrayVar, jArray);
  }

  /**
   * Compile the translet's constructor
   */
  private void compileConstructor(CompilerContext ctx, Output output) {

    JMethod constructor = ctx.clazz().constructor(JMod.PUBLIC);
    JBlock body = constructor.body();
    // Call the constructor in the AbstractTranslet superclass
    body.invoke("super");
    body.assign(refthis(NAMES_INDEX), ctx.clazz().staticRef(STATIC_NAMES_ARRAY_FIELD));
    body.assign(refthis(URIS_INDEX), ctx.clazz().staticRef(STATIC_URIS_ARRAY_FIELD));
    body.assign(refthis(TYPES_INDEX), ctx.clazz().staticRef(STATIC_TYPES_ARRAY_FIELD));
    body.assign(refthis(NAMESPACE_INDEX), ctx.clazz().staticRef(STATIC_NAMESPACE_ARRAY_FIELD));
    body.assign(refthis(TRANSLET_VERSION_INDEX), lit(AbstractTranslet.CURRENT_TRANSLET_VERSION));

    if (_hasIdCall) {
      body.assign(refthis(HASIDCALL_INDEX), lit(true));
    }

    ctx.pushBlock(body);
    // Compile in code to set the output configuration from <xsl:output>
    if (output != null) {
      // Set all the output settings files in the translet
      output.translate(ctx);
    }

    // Compile default decimal formatting symbols.
    // This is an implicit, nameless xsl:decimal-format top-level element.
    if (_numberFormattingUsed) {
      DecimalFormatting.translateDefaultDFS(ctx);
    }
    ctx.popBlock();
  }

  /**
   * Compile a topLevel() method into the output class. This method is called
   * from transform() to handle all non-template top-level elements. Returns the
   * signature of the topLevel() method.
   * 
   * Global variables/params and keys are first sorted to resolve dependencies
   * between them. The XSLT 1.0 spec does not allow a key to depend on a
   * variable. However, for compatibility with Xalan interpretive, that type of
   * dependency is allowed. Note also that the buildKeys() method is still
   * generated as it is used by the LoadDocument class, but it no longer called
   * from transform().
   */
  private JMethod compileTopLevel(CompilerContext ctx) {
    JMethod topLevel = ctx.method(PUBLIC, void.class, "topLevel")._throws(TransletException.class)._throws(SAXException.class);
    JVar document = ctx.param(DOM.class, DOCUMENT_PNAME);
    JVar iterator = ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar handler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);

    JBlock body = topLevel.body();
    ctx.pushBlock(body);
    // Define and initialize 'current' variable with the root node
    JVar current = body.decl(ctx.owner().INT, "current", invoke(document, GET_ITERATOR).invoke(NEXT));
    ctx.pushNode(current);

    // Create a new list containing variables/params + keys
    List<TopLevelElement> varDepElements = new ArrayList<TopLevelElement>(_globals);
    for (SyntaxTreeNode element : getContents()) {
      if (element instanceof Key) {
        varDepElements.add((Key) element);
      }
    }

    // Determine a partial order for the variables/params and keys
    varDepElements = resolveDependencies(varDepElements);

    // Translate vars/params and keys in the right order
    final int count = varDepElements.size();
    for (int i = 0; i < count; i++) {
      final TopLevelElement tle = varDepElements.get(i);
      tle.translate(ctx);
      if (tle instanceof Key) {
        final Key key = (Key) tle;
        _keys.put(key.getName(), key);
      }
    }

    // Compile code for other top-level elements
    final List<WhitespaceRule> whitespaceRules = new ArrayList<>();
    for (SyntaxTreeNode element : getContents()) {
      // xsl:decimal-format
      if (element instanceof DecimalFormatting) {
        ((DecimalFormatting) element).translate(ctx);
      }
      // xsl:strip/preserve-space
      else if (element instanceof Whitespace) {
        whitespaceRules.addAll(((Whitespace) element).getRules());
      }
    }

    // Translate all whitespace strip/preserve rules
    if (whitespaceRules.size() > 0) {
      Whitespace.translateRules(whitespaceRules, ctx.clazz(), getXSLTC());
    }

    JMethod stripSpace = ctx.clazz().getMethod(STRIP_SPACE,
        new JType[] { ctx.ref(DOM.class), ctx.owner().INT, ctx.owner().INT });
    if (stripSpace != null && stripSpace.type() == ctx.owner().BOOLEAN) {
//      JFieldVar _dom = definedClass.fields().get(DOM_FIELD);
      body.invoke(document, SET_FILTER).arg(_this());
    }
    ctx.popNode();
    ctx.popBlock();
    return topLevel;
  }

  /**
   * This method returns a list with variables/params and keys in the order in
   * which they are to be compiled for initialization. The order is determined
   * by analyzing the dependencies between them. The XSLT 1.0 spec does not
   * allow a key to depend on a variable. However, for compatibility with Xalan
   * interpretive, that type of dependency is allowed and, therefore, consider
   * to determine the partial order.
   */
  private List<TopLevelElement> resolveDependencies(List<TopLevelElement> input) {
    /*
     * DEBUG CODE - INGORE for (int i = 0; i < input.size(); i++) { final
     * TopLevelElement e = (TopLevelElement) input.elementAt(i);
     * System.out.println("e = " + e + " depends on:"); List<TopLevelElement>
     * dep = e.getDependencies(); for (int j = 0; j < (dep != null ? dep.size()
     * : 0); j++) { System.out.println("\t" + dep.get(j)); } }
     * System.out.println("=================================");
     */

    final List<TopLevelElement> result = new ArrayList<>();
    while (input.size() > 0) {
      boolean changed = false;
      for (int i = 0; i < input.size();) {
        final TopLevelElement vde = input.get(i);
        final List<TopLevelElement> dep = vde.getDependencies();
        if (dep == null || result.containsAll(dep)) {
          result.add(vde);
          input.remove(i);
          changed = true;
        } else {
          i++;
        }
      }

      // If nothing was changed in this pass then we have a circular ref
      if (!changed) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.CIRCULAR_VARIABLE_ERR, input.toString(), this);
        getParser().reportError(Constants.ERROR, err);
        return result;
      }
    }

    /*
     * DEBUG CODE - INGORE
     * System.out.println("================================="); for (int i = 0;
     * i < result.size(); i++) { final TopLevelElement e = (TopLevelElement)
     * result.elementAt(i); System.out.println("e = " + e); }
     */

    return result;
  }

  /**
   * Compile a buildKeys() method into the output class. Note that keys for the
   * input document are created in topLevel(), not in this method. However, we
   * still need this method to create keys for documents loaded via the XPath
   * document() function.
   */
  private void compileBuildKeys(CompilerContext ctx) {
    JMethod buildKeys = ctx.method(JMod.PUBLIC, void.class, "buildKeys")._throws(TransletException.class);
    ctx.param(DOM.class, DOCUMENT_PNAME);
    ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);
    JVar current = ctx.param(int.class, "current");
    ctx.pushBlock(buildKeys.body());
    ctx.pushNode(current);
    for (SyntaxTreeNode element : getContents()) {
      // xsl:key
      if (element instanceof Key) {
        final Key key = (Key) element;
        key.translate(ctx);
        _keys.put(key.getName(), key);
      }
    }
    ctx.popNode();
    ctx.popBlock();
    ctx.popMethodContext();
  }

  /**
   * Compile transform() into the output class. This method is used to
   * initialize global variables and global parameters. The current node is set
   * to be the document's root node.
   */
  private void createTransformMethod(CompilerContext ctx) {
    /*
     * Define the the method transform with the following signature: void
     * transform(DOM, NodeIterator, HandlerBase)
     */
    JMethod method = ctx.method(JMod.PUBLIC, void.class, "transform")._throws(TransletException.class);
    JVar document = ctx.param(DOM.class, DOCUMENT_PNAME);
    JVar dtmAxisIterator = ctx.param(DTMAxisIterator.class, ITERATOR_PNAME);
    JVar serializationHandler = ctx.param(SerializationHandler.class, TRANSLET_OUTPUT_PNAME);

    // Start with try block
    JTryBlock _try = method.body()._try();
    JBlock block = _try.body();

    JInvocation makeDomAdapter = invoke("makeDOMAdapter").arg(document);
    // Define and initialize current with the root node
    JFieldVar _dom = ctx.field(DOM_FIELD);
    // Prepare the appropriate DOM implementation
    if (isMultiDocument()) {
      // MultiDOM with DomAdapter
      JClass multiDOM = ctx.ref(MultiDOM.class);
      block.assign(_dom, _new(multiDOM).arg(makeDomAdapter));
    } else {
      // DOMAdapter only
      block.assign(_dom, makeDomAdapter);
    }

    // continue with globals initialization
    JVar current = block.decl(ctx.owner().INT, "current", invoke(document, GET_ITERATOR).invoke(NEXT));

    // Transfer the output settings to the output post-processor
    block.invoke("transferOutputSettings").arg(serializationHandler);

    /*
     * Compile buildKeys() method. Note that this method is not invoked here as
     * keys for the input document are now created in topLevel(). However, this
     * method is still needed by the LoadDocument class.
     */
    compileBuildKeys(ctx);

    // Look for top-level elements that need handling
    final ListIterator<SyntaxTreeNode> toplevel = elements();
    if (_globals.size() > 0 || toplevel.hasNext()) {
      // Compile method for handling top-level elements
      final JMethod topLevel = compileTopLevel(ctx);
      // Call topLevel()
      block.invoke(topLevel).arg(_dom).arg(dtmAxisIterator).arg(serializationHandler);
    }

    // start document
    block.invoke(serializationHandler, "startDocument");

    // call applyTemplates
    block.invoke("applyTemplates").arg(_dom).arg(dtmAxisIterator).arg(serializationHandler);

    // endDocument
    block.invoke(serializationHandler, "endDocument");

    // End try-block with catch
    JClass saxException = ctx.ref(SAXException.class);
    JClass transletException = ctx.ref(TransletException.class);
    JCatchBlock _catch = _try._catch(saxException);
    JVar e = _catch.param("e");
    _catch.body()._throw(_new(transletException).arg(e));
  }

  public int addParam(Param param) {
    _globals.add(param);
    return _globals.size() - 1;
  }

  public int addVariable(Variable global) {
    _globals.add(global);
    return _globals.size() - 1;
  }

  @Override
  public void display(int indent) {
    indent(indent);
    Util.println("Stylesheet");
    displayContents(indent + IndentIncrement);
  }

  // do we need this wrapper ?????
  public String getNamespace(String prefix) {
    return lookupNamespace(prefix);
  }

  public String getClassName() {
    return _className;
  }

  public List<Template> getTemplates() {
    return _templates;
  }

  public List<Template> getAllValidTemplates() {
    // Return templates if no imported/included stylesheets
    if (_includedStylesheets == null)
      return _templates;

    // Is returned value cached?
    if (_allValidTemplates == null) {
      final List<Template> templates = new ArrayList<>();
      final int size = _includedStylesheets.size();
      for (int i = 0; i < size; i++) {
        final Stylesheet included = _includedStylesheets.get(i);
        templates.addAll(included.getAllValidTemplates());
      }
      templates.addAll(_templates);

      // Cache results in top-level stylesheet only
      if (_parentStylesheet != null)
        return templates;
      _allValidTemplates = templates;
    }

    return _allValidTemplates;
  }

  protected void addTemplate(Template template) {
    _templates.add(template);
  }
}
