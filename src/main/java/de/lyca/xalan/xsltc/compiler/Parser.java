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

import static de.lyca.xalan.xsltc.compiler.Constants.COMPILER_PACKAGE;
import static de.lyca.xalan.xsltc.compiler.Constants.ERROR;
import static de.lyca.xalan.xsltc.compiler.Constants.FATAL;
import static de.lyca.xalan.xsltc.compiler.Constants.REDIRECT_URI;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_URI;
import static de.lyca.xalan.xsltc.compiler.Constants.UNSUPPORTED;
import static de.lyca.xalan.xsltc.compiler.Constants.WARNING;
import static de.lyca.xalan.xsltc.compiler.Constants.XMLNS_PREFIX;
import static de.lyca.xalan.xsltc.compiler.Constants.XSLT_URI;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.lyca.xalan.ObjectFactory;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.AttributeList;
import java_cup.runtime.Symbol;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
public class Parser implements ContentHandler {

  private static final String XSL = "xsl"; // standard prefix
  private static final String TRANSLET = "translet"; // extension prefix

  private Locator _locator = null;

  private XSLTC _xsltc; // Reference to the compiler object.
  private XPathParser _xpathParser; // Reference to the XPath parser.
  private List<ErrorMsg> _errors; // Contains all compilation errors
  private List<ErrorMsg> _warnings; // Contains all compilation errors

  private Map<QName, String> _instructionClasses; // Maps instructions to classes
  private Map<QName, String[]> _instructionAttrs;; // reqd and opt attrs
  private Map<String, QName> _qNames;
  private Map<String, Map<String, QName>> _namespaces;
  private QName _useAttributeSets;
  private QName _excludeResultPrefixes;
  private QName _extensionElementPrefixes;
  private Map<QName, Deque<VariableBase>> _variableScope;
  private Stylesheet _currentStylesheet;
  private SymbolTable _symbolTable; // Maps QNames to syntax-tree nodes
  private Output _output;
  private Template _template; // Reference to the template being parsed.

  private boolean _rootNamespaceDef; // Used for validity check

  private SyntaxTreeNode _root;

  private String _systemId;
  private String _target;

  private int _currentImportPrecedence;

  public Parser(XSLTC xsltc) {
    _xsltc = xsltc;
  }

  public void init() {
    _qNames = new HashMap<>(512);
    _namespaces = new HashMap<>();
    _instructionClasses = new HashMap<>();
    _instructionAttrs = new HashMap<>();
    _variableScope = new HashMap<>();
    _template = null;
    _errors = new ArrayList<>();
    _warnings = new ArrayList<>();
    _symbolTable = new SymbolTable();
    _xpathParser = new XPathParser(this);
    _currentStylesheet = null;
    _output = null;
    _root = null;
    _rootNamespaceDef = false;
    _currentImportPrecedence = 1;

    initStdClasses();
    initInstructionAttrs();
    initExtClasses();
    initSymbolTable();

    _useAttributeSets = getQName(XSLT_URI, XSL, "use-attribute-sets");
    _excludeResultPrefixes = getQName(XSLT_URI, XSL, "exclude-result-prefixes");
    _extensionElementPrefixes = getQName(XSLT_URI, XSL, "extension-element-prefixes");
  }

  public void setOutput(Output output) {
    if (_output != null) {
      if (_output.getImportPrecedence() <= output.getImportPrecedence()) {
        output.mergeOutput(_output);
        _output.disable();
        _output = output;
      } else {
        output.disable();
      }
    } else {
      _output = output;
    }
  }

  public Output getOutput() {
    return _output;
  }

  public Properties getOutputProperties() {
    return getTopLevelStylesheet().getOutputProperties();
  }

  public void addVariable(Variable var) {
    addVariableOrParam(var);
  }

  public void addParameter(Param param) {
    addVariableOrParam(param);
  }

  private void addVariableOrParam(VariableBase var) {
    Deque<VariableBase> stack = _variableScope.get(var.getName());
    if (stack == null) {
      stack = new ArrayDeque<>();
      stack.push(var);
      _variableScope.put(var.getName(), stack);
    } else {
      stack.push(var);
    }
  }

  public void removeVariable(QName name) {
    final Deque<VariableBase> stack = _variableScope.get(name);
    if (stack != null) {
      if (!stack.isEmpty()) {
        stack.pop();
      }
      if (!stack.isEmpty())
        return;
      _variableScope.remove(name);
    }
  }

  public VariableBase lookupVariable(QName name) {
    final Deque<VariableBase> existing = _variableScope.get(name);
    return existing == null ? null : existing.peek();
  }

  public void setXSLTC(XSLTC xsltc) {
    _xsltc = xsltc;
  }

  public XSLTC getXSLTC() {
    return _xsltc;
  }

  public int getCurrentImportPrecedence() {
    return _currentImportPrecedence;
  }

  public int getNextImportPrecedence() {
    return ++_currentImportPrecedence;
  }

  public void setCurrentStylesheet(Stylesheet stylesheet) {
    _currentStylesheet = stylesheet;
  }

  public Stylesheet getCurrentStylesheet() {
    return _currentStylesheet;
  }

  public Stylesheet getTopLevelStylesheet() {
    return _xsltc.getStylesheet();
  }

  public QName getQNameSafe(final String stringRep) {
    // parse and retrieve namespace
    final int colon = stringRep.lastIndexOf(':');
    if (colon != -1) {
      final String prefix = stringRep.substring(0, colon);
      final String localname = stringRep.substring(colon + 1);
      String namespace = null;

      // Get the namespace uri from the symbol table
      if (prefix.equals(XMLNS_PREFIX) == false) {
        namespace = _symbolTable.lookupNamespace(prefix);
        if (namespace == null) {
          namespace = "";
        }
      }
      return getQName(namespace, prefix, localname);
    } else {
      final String uri = stringRep.equals(XMLNS_PREFIX) ? null : _symbolTable.lookupNamespace("");
      return getQName(uri, null, stringRep);
    }
  }

  public QName getQName(final String stringRep) {
    return getQName(stringRep, true, false);
  }

  public QName getQNameIgnoreDefaultNs(final String stringRep) {
    return getQName(stringRep, true, true);
  }

  public QName getQName(final String stringRep, boolean reportError) {
    return getQName(stringRep, reportError, false);
  }

  private QName getQName(final String stringRep, boolean reportError, boolean ignoreDefaultNs) {
    // parse and retrieve namespace
    final int colon = stringRep.lastIndexOf(':');
    if (colon != -1) {
      final String prefix = stringRep.substring(0, colon);
      final String localname = stringRep.substring(colon + 1);
      String namespace = null;

      // Get the namespace uri from the symbol table
      if (prefix.equals(XMLNS_PREFIX) == false) {
        namespace = _symbolTable.lookupNamespace(prefix);
        if (namespace == null && reportError) {
          final int line = getLineNumber();
          final ErrorMsg err = new ErrorMsg(ErrorMsg.NAMESPACE_UNDEF_ERR, line, prefix);
          reportError(ERROR, err);
        }
      }
      return getQName(namespace, prefix, localname);
    } else {
      if (stringRep.equals(XMLNS_PREFIX)) {
        ignoreDefaultNs = true;
      }
      final String defURI = ignoreDefaultNs ? null : _symbolTable.lookupNamespace("");
      return getQName(defURI, null, stringRep);
    }
  }

  public QName getQName(String namespace, String prefix, String localname) {
    if (namespace == null || namespace.isEmpty()) {
      QName name = _qNames.get(localname);
      if (name == null) {
        name = new QName(null, prefix, localname);
        _qNames.put(localname, name);
      }
      return name;
    } else {
      Map<String, QName> space = _namespaces.get(namespace);
      final String lexicalQName = prefix == null || prefix.length() == 0 ? localname : prefix + ':' + localname;

      if (space == null) {
        final QName name = new QName(namespace, prefix, localname);
        _namespaces.put(namespace, space = new HashMap<>());
        space.put(lexicalQName, name);
        return name;
      } else {
        QName name = space.get(lexicalQName);

        if (name == null) {
          name = new QName(namespace, prefix, localname);
          space.put(lexicalQName, name);
        }
        return name;
      }
    }
  }

  public QName getQName(String scope, String name) {
    return getQName(scope + name);
  }

  public QName getQName(QName scope, QName name) {
    return getQName(scope.toString() + name.toString());
  }

  public QName getUseAttributeSets() {
    return _useAttributeSets;
  }

  public QName getExtensionElementPrefixes() {
    return _extensionElementPrefixes;
  }

  public QName getExcludeResultPrefixes() {
    return _excludeResultPrefixes;
  }

  /**
   * Create an instance of the <code>Stylesheet</code> class, and then parse,
   * typecheck and compile the instance. Must be called after
   * <code>parse()</code>.
   */
  public Stylesheet makeStylesheet(SyntaxTreeNode element) throws CompilerException {
    try {
      Stylesheet stylesheet;

      if (element instanceof Stylesheet) {
        stylesheet = (Stylesheet) element;
      } else {
        stylesheet = new Stylesheet();
        stylesheet.setSimplified();
        stylesheet.addElement(element);
        stylesheet.setAttributes((AttributeList) element.getAttributes());

        // Map the default NS if not already defined
        if (element.lookupNamespace("") == null) {
          element.addPrefixMapping("", "");
        }
      }
      stylesheet.setParser(this);
      return stylesheet;
    } catch (final ClassCastException e) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.NOT_STYLESHEET_ERR, element);
      throw new CompilerException(err.toString());
    }
  }

  /**
   * Instanciates a SAX2 parser and generate the AST from the input.
   */
  public void createAST(Stylesheet stylesheet) {
    try {
      if (stylesheet != null) {
        stylesheet.parseContents(this);
        final int precedence = stylesheet.getImportPrecedence();
        final ListIterator<SyntaxTreeNode> elements = stylesheet.elements();
        while (elements.hasNext()) {
          final Object child = elements.next();
          if (child instanceof Text) {
            final int l = getLineNumber();
            final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_TEXT_NODE_ERR, l, null);
            reportError(ERROR, err);
          }
        }
        if (!errorsFound()) {
          stylesheet.typeCheck(_symbolTable);
        }
      }
    } catch (final TypeCheckError e) {
      reportError(ERROR, new ErrorMsg(e));
    }
  }

  /**
   * Parses a stylesheet and builds the internal abstract syntax tree
   * 
   * @param reader
   *          A SAX2 SAXReader (parser)
   * @param input
   *          A SAX2 InputSource can be passed to a SAX reader
   * @return The root of the abstract syntax tree
   */
  public SyntaxTreeNode parse(XMLReader reader, InputSource input) {
    try {
      // Parse the input document and build the abstract syntax tree
      reader.setContentHandler(this);
      reader.parse(input);
      // Find the start of the stylesheet within the tree
      return getStylesheet(_root);
    } catch (final IOException e) {
      if (_xsltc.debug()) {
        e.printStackTrace();
      }
      reportError(ERROR, new ErrorMsg(e));
    } catch (final SAXException e) {
      final Throwable ex = e.getException();
      if (_xsltc.debug()) {
        e.printStackTrace();
        if (ex != null) {
          ex.printStackTrace();
        }
      }
      reportError(ERROR, new ErrorMsg(e));
    } catch (final CompilerException e) {
      if (_xsltc.debug()) {
        e.printStackTrace();
      }
      reportError(ERROR, new ErrorMsg(e));
    } catch (final Exception e) {
      if (_xsltc.debug()) {
        e.printStackTrace();
      }
      reportError(ERROR, new ErrorMsg(e));
    }
    return null;
  }

  /**
   * Parses a stylesheet and builds the internal abstract syntax tree
   * 
   * @param input
   *          A SAX2 InputSource can be passed to a SAX reader
   * @return The root of the abstract syntax tree
   */
  public SyntaxTreeNode parse(InputSource input) {
    _systemId = input.getSystemId();
    try {
      // Create a SAX parser and get the XMLReader object it uses
      final SAXParserFactory factory = SAXParserFactory.newInstance();

      if (_xsltc.isSecureProcessing()) {
        try {
          factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (final SAXException e) {
        }
      }

      try {
        factory.setFeature(Constants.NAMESPACE_FEATURE, true);
      } catch (final Exception e) {
        factory.setNamespaceAware(true);
      }
      final SAXParser parser = factory.newSAXParser();
      final XMLReader reader = parser.getXMLReader();
      return parse(reader, input);
    } catch (final ParserConfigurationException e) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.SAX_PARSER_CONFIG_ERR);
      reportError(ERROR, err);
    } catch (final SAXParseException e) {
      reportError(ERROR, new ErrorMsg(e.getMessage(), e.getLineNumber()));
    } catch (final SAXException e) {
      reportError(ERROR, new ErrorMsg(e.getMessage()));
    }
    return null;
  }

  public SyntaxTreeNode getDocumentRoot() {
    return _root;
  }

  private String _PImedia = null;
  private String _PItitle = null;
  private String _PIcharset = null;

  /**
   * Set the parameters to use to locate the correct <?xml-stylesheet ...?>
   * processing instruction in the case where the input document is an XML
   * document with one or more references to a stylesheet.
   * 
   * @param media
   *          The media attribute to be matched. May be null, in which case the
   *          prefered templates will be used (i.e. alternate = no).
   * @param title
   *          The value of the title attribute to match. May be null.
   * @param charset
   *          The value of the charset attribute to match. May be null.
   */
  protected void setPIParameters(String media, String title, String charset) {
    _PImedia = media;
    _PItitle = title;
    _PIcharset = charset;
  }

  /**
   * Extracts the DOM for the stylesheet. In the case of an embedded stylesheet,
   * it extracts the DOM subtree corresponding to the embedded stylesheet that
   * has an 'id' attribute whose value is the same as the value declared in the
   * <?xml-stylesheet...?> processing instruction (P.I.). In the xml-stylesheet
   * P.I. the value is labeled as the 'href' data of the P.I. The extracted DOM
   * representing the stylesheet is returned as an Element object.
   */
  private SyntaxTreeNode getStylesheet(SyntaxTreeNode root) throws CompilerException {

    // Assume that this is a pure XSL stylesheet if there is not
    // <?xml-stylesheet ....?> processing instruction
    if (_target == null) {
      if (!_rootNamespaceDef) {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.MISSING_XSLT_URI_ERR);
        throw new CompilerException(msg.toString());
      }
      return root;
    }

    // Find the xsl:stylesheet or xsl:transform with this reference
    if (_target.charAt(0) == '#') {
      final SyntaxTreeNode element = findStylesheet(root, _target.substring(1));
      if (element == null) {
        final ErrorMsg msg = new ErrorMsg(ErrorMsg.MISSING_XSLT_TARGET_ERR, _target, root);
        throw new CompilerException(msg.toString());
      }
      return element;
    } else
      return loadExternalStylesheet(_target);
  }

  /**
   * Find a Stylesheet element with a specific ID attribute value. This method
   * is used to find a Stylesheet node that is referred in a <?xml-stylesheet
   * ... ?> processing instruction.
   */
  private SyntaxTreeNode findStylesheet(SyntaxTreeNode root, String href) {

    if (root == null)
      return null;

    if (root instanceof Stylesheet) {
      final String id = root.getAttribute("id");
      if (id.equals(href))
        return root;
    }
    final List<SyntaxTreeNode> children = root.getContents();
    if (children != null) {
      for (SyntaxTreeNode child : children) {
        final SyntaxTreeNode node = findStylesheet(child, href);
        if (node != null)
          return node;
      }
    }
    return null;
  }

  /**
   * For embedded stylesheets: Load an external file with stylesheet
   */
  private SyntaxTreeNode loadExternalStylesheet(String location) throws CompilerException {

    InputSource source;

    // Check if the location is URL or a local file
    if (new File(location).exists()) {
      source = new InputSource("file:" + location);
    } else {
      try {
        URI uri = new URL(_systemId).toURI();
        Path path = Paths.get(uri).resolveSibling(location);
        source = new InputSource(path.toUri().toURL().toString());
      } catch (MalformedURLException | URISyntaxException e) {
        source = new InputSource(location);
      }
    }

    final SyntaxTreeNode external = parse(source);
    return external;
  }

  private void initAttrTable(String elementName, String[] attrs) {
    _instructionAttrs.put(getQName(XSLT_URI, XSL, elementName), attrs);
  }

  private void initInstructionAttrs() {
    initAttrTable("template", new String[] { "match", "name", "priority", "mode" });
    initAttrTable("stylesheet",
            new String[] { "id", "version", "extension-element-prefixes", "exclude-result-prefixes" });
    initAttrTable("transform",
            new String[] { "id", "version", "extension-element-prefixes", "exclude-result-prefixes" });
    initAttrTable("text", new String[] { "disable-output-escaping" });
    initAttrTable("if", new String[] { "test" });
    initAttrTable("choose", new String[] {});
    initAttrTable("when", new String[] { "test" });
    initAttrTable("otherwise", new String[] {});
    initAttrTable("for-each", new String[] { "select" });
    initAttrTable("message", new String[] { "terminate" });
    initAttrTable("number", new String[] { "level", "count", "from", "value", "format", "lang", "letter-value",
            "grouping-separator", "grouping-size" });
    initAttrTable("comment", new String[] {});
    initAttrTable("copy", new String[] { "use-attribute-sets" });
    initAttrTable("copy-of", new String[] { "select" });
    initAttrTable("param", new String[] { "name", "select" });
    initAttrTable("with-param", new String[] { "name", "select" });
    initAttrTable("variable", new String[] { "name", "select" });
    initAttrTable("output", new String[] { "method", "version", "encoding", "omit-xml-declaration", "standalone",
            "doctype-public", "doctype-system", "cdata-section-elements", "indent", "media-type" });
    initAttrTable("sort", new String[] { "select", "order", "case-order", "lang", "data-type" });
    initAttrTable("key", new String[] { "name", "match", "use" });
    initAttrTable("fallback", new String[] {});
    initAttrTable("attribute", new String[] { "name", "namespace" });
    initAttrTable("attribute-set", new String[] { "name", "use-attribute-sets" });
    initAttrTable("value-of", new String[] { "select", "disable-output-escaping" });
    initAttrTable("element", new String[] { "name", "namespace", "use-attribute-sets" });
    initAttrTable("call-template", new String[] { "name" });
    initAttrTable("apply-templates", new String[] { "select", "mode" });
    initAttrTable("apply-imports", new String[] {});
    initAttrTable("decimal-format", new String[] { "name", "decimal-separator", "grouping-separator", "infinity",
            "minus-sign", "NaN", "percent", "per-mille", "zero-digit", "digit", "pattern-separator" });
    initAttrTable("import", new String[] { "href" });
    initAttrTable("include", new String[] { "href" });
    initAttrTable("strip-space", new String[] { "elements" });
    initAttrTable("preserve-space", new String[] { "elements" });
    initAttrTable("processing-instruction", new String[] { "name" });
    initAttrTable("namespace-alias", new String[] { "stylesheet-prefix", "result-prefix" });
  }

  /**
   * Initialize the _instructionClasses Map, which maps XSL element names to
   * Java classes in this package.
   */
  private void initStdClasses() {
    initStdClass("template", "Template");
    initStdClass("stylesheet", "Stylesheet");
    initStdClass("transform", "Stylesheet");
    initStdClass("text", "Text");
    initStdClass("if", "If");
    initStdClass("choose", "Choose");
    initStdClass("when", "When");
    initStdClass("otherwise", "Otherwise");
    initStdClass("for-each", "ForEach");
    initStdClass("message", "Message");
    initStdClass("number", "Number");
    initStdClass("comment", "Comment");
    initStdClass("copy", "Copy");
    initStdClass("copy-of", "CopyOf");
    initStdClass("param", "Param");
    initStdClass("with-param", "WithParam");
    initStdClass("variable", "Variable");
    initStdClass("output", "Output");
    initStdClass("sort", "Sort");
    initStdClass("key", "Key");
    initStdClass("fallback", "Fallback");
    initStdClass("attribute", "XslAttribute");
    initStdClass("attribute-set", "AttributeSet");
    initStdClass("value-of", "ValueOf");
    initStdClass("element", "XslElement");
    initStdClass("call-template", "CallTemplate");
    initStdClass("apply-templates", "ApplyTemplates");
    initStdClass("apply-imports", "ApplyImports");
    initStdClass("decimal-format", "DecimalFormatting");
    initStdClass("import", "Import");
    initStdClass("include", "Include");
    initStdClass("strip-space", "Whitespace");
    initStdClass("preserve-space", "Whitespace");
    initStdClass("processing-instruction", "ProcessingInstruction");
    initStdClass("namespace-alias", "NamespaceAlias");
  }

  private void initStdClass(String elementName, String className) {
    _instructionClasses.put(getQName(XSLT_URI, XSL, elementName), Constants.COMPILER_PACKAGE + '.' + className);
  }

  public boolean elementSupported(String namespace, String localName) {
    return _instructionClasses.get(getQName(namespace, XSL, localName)) != null;
  }

  public boolean functionSupported(String fname) {
    return _symbolTable.lookupPrimop(fname) != null;
  }

  private void initExtClasses() {
    initExtClass("output", "TransletOutput");
    initExtClass(REDIRECT_URI, "write", "TransletOutput");
  }

  private void initExtClass(String elementName, String className) {
    _instructionClasses.put(getQName(TRANSLET_URI, TRANSLET, elementName), COMPILER_PACKAGE + '.' + className);
  }

  private void initExtClass(String namespace, String elementName, String className) {
    _instructionClasses.put(getQName(namespace, TRANSLET, elementName), COMPILER_PACKAGE + '.' + className);
  }

  /**
   * Add primops and base functions to the symbol table.
   */
  private void initSymbolTable() {
    final MethodType I_V = new MethodType(Type.Int, Type.Void);
    final MethodType I_R = new MethodType(Type.Int, Type.Real);
    final MethodType I_S = new MethodType(Type.Int, Type.String);
    final MethodType I_D = new MethodType(Type.Int, Type.NodeSet);
    final MethodType R_I = new MethodType(Type.Real, Type.Int);
    final MethodType R_V = new MethodType(Type.Real, Type.Void);
    final MethodType R_R = new MethodType(Type.Real, Type.Real);
    final MethodType R_D = new MethodType(Type.Real, Type.NodeSet);
    final MethodType R_O = new MethodType(Type.Real, Type.Reference);
    final MethodType I_I = new MethodType(Type.Int, Type.Int);
    final MethodType D_O = new MethodType(Type.NodeSet, Type.Reference);
    final MethodType D_V = new MethodType(Type.NodeSet, Type.Void);
    final MethodType D_S = new MethodType(Type.NodeSet, Type.String);
    final MethodType D_D = new MethodType(Type.NodeSet, Type.NodeSet);
    final MethodType A_V = new MethodType(Type.Node, Type.Void);
    final MethodType S_V = new MethodType(Type.String, Type.Void);
    final MethodType S_S = new MethodType(Type.String, Type.String);
    final MethodType S_A = new MethodType(Type.String, Type.Node);
    final MethodType S_D = new MethodType(Type.String, Type.NodeSet);
    final MethodType S_O = new MethodType(Type.String, Type.Reference);
    final MethodType B_O = new MethodType(Type.Boolean, Type.Reference);
    final MethodType B_V = new MethodType(Type.Boolean, Type.Void);
    final MethodType B_B = new MethodType(Type.Boolean, Type.Boolean);
    final MethodType B_S = new MethodType(Type.Boolean, Type.String);
    final MethodType D_X = new MethodType(Type.NodeSet, Type.Object);
    final MethodType R_RR = new MethodType(Type.Real, Type.Real, Type.Real);
    final MethodType I_II = new MethodType(Type.Int, Type.Int, Type.Int);
    final MethodType B_RR = new MethodType(Type.Boolean, Type.Real, Type.Real);
    final MethodType B_II = new MethodType(Type.Boolean, Type.Int, Type.Int);
    final MethodType S_SS = new MethodType(Type.String, Type.String, Type.String);
    final MethodType S_DS = new MethodType(Type.String, Type.Real, Type.String);
    final MethodType S_SR = new MethodType(Type.String, Type.String, Type.Real);
    final MethodType O_SO = new MethodType(Type.Reference, Type.String, Type.Reference);

    final MethodType D_SS = new MethodType(Type.NodeSet, Type.String, Type.String);
    final MethodType D_SD = new MethodType(Type.NodeSet, Type.String, Type.NodeSet);
    final MethodType B_BB = new MethodType(Type.Boolean, Type.Boolean, Type.Boolean);
    final MethodType B_SS = new MethodType(Type.Boolean, Type.String, Type.String);
    final MethodType S_SD = new MethodType(Type.String, Type.String, Type.NodeSet);
    final MethodType S_DSS = new MethodType(Type.String, Type.Real, Type.String, Type.String);
    final MethodType S_SRR = new MethodType(Type.String, Type.String, Type.Real, Type.Real);
    final MethodType S_SSS = new MethodType(Type.String, Type.String, Type.String, Type.String);

    /*
     * Standard functions: implemented but not in this table concat(). When
     * adding a new function make sure to uncomment the corresponding line in
     * <tt>FunctionAvailableCall</tt>.
     */

    // The following functions are inlined

    _symbolTable.addPrimop("current", A_V);
    _symbolTable.addPrimop("last", I_V);
    _symbolTable.addPrimop("position", I_V);
    _symbolTable.addPrimop("true", B_V);
    _symbolTable.addPrimop("false", B_V);
    _symbolTable.addPrimop("not", B_B);
    _symbolTable.addPrimop("name", S_V);
    _symbolTable.addPrimop("name", S_A);
    _symbolTable.addPrimop("generate-id", S_V);
    _symbolTable.addPrimop("generate-id", S_A);
    _symbolTable.addPrimop("ceiling", R_R);
    _symbolTable.addPrimop("floor", R_R);
    _symbolTable.addPrimop("round", R_R);
    _symbolTable.addPrimop("contains", B_SS);
    _symbolTable.addPrimop("number", R_O);
    _symbolTable.addPrimop("number", R_V);
    _symbolTable.addPrimop("boolean", B_O);
    _symbolTable.addPrimop("string", S_O);
    _symbolTable.addPrimop("string", S_V);
    _symbolTable.addPrimop("translate", S_SSS);
    _symbolTable.addPrimop("string-length", I_V);
    _symbolTable.addPrimop("string-length", I_S);
    _symbolTable.addPrimop("starts-with", B_SS);
    _symbolTable.addPrimop("format-number", S_DS);
    _symbolTable.addPrimop("format-number", S_DSS);
    _symbolTable.addPrimop("unparsed-entity-uri", S_S);
    _symbolTable.addPrimop("key", D_SS);
    _symbolTable.addPrimop("key", D_SD);
    _symbolTable.addPrimop("id", D_S);
    _symbolTable.addPrimop("id", D_D);
    _symbolTable.addPrimop("namespace-uri", S_V);
    _symbolTable.addPrimop("function-available", B_S);
    _symbolTable.addPrimop("element-available", B_S);
    _symbolTable.addPrimop("document", D_S);
    _symbolTable.addPrimop("document", D_V);

    // The following functions are implemented in the basis library
    _symbolTable.addPrimop("count", I_D);
    _symbolTable.addPrimop("sum", R_D);
    _symbolTable.addPrimop("local-name", S_V);
    _symbolTable.addPrimop("local-name", S_D);
    _symbolTable.addPrimop("namespace-uri", S_V);
    _symbolTable.addPrimop("namespace-uri", S_D);
    _symbolTable.addPrimop("substring", S_SR);
    _symbolTable.addPrimop("substring", S_SRR);
    _symbolTable.addPrimop("substring-after", S_SS);
    _symbolTable.addPrimop("substring-before", S_SS);
    _symbolTable.addPrimop("normalize-space", S_V);
    _symbolTable.addPrimop("normalize-space", S_S);
    _symbolTable.addPrimop("system-property", S_S);

    // Extensions
    _symbolTable.addPrimop("nodeset", D_O);
    _symbolTable.addPrimop("objectType", S_O);
    _symbolTable.addPrimop("cast", O_SO);

    // Operators +, -, *, /, % defined on real types.
    _symbolTable.addPrimop("+", R_RR);
    _symbolTable.addPrimop("-", R_RR);
    _symbolTable.addPrimop("*", R_RR);
    _symbolTable.addPrimop("/", R_RR);
    _symbolTable.addPrimop("%", R_RR);

    // Operators +, -, * defined on integer types.
    // Operators / and % are not defined on integers (may cause exception)
    _symbolTable.addPrimop("+", I_II);
    _symbolTable.addPrimop("-", I_II);
    _symbolTable.addPrimop("*", I_II);

    // Operators <, <= >, >= defined on real types.
    _symbolTable.addPrimop("<", B_RR);
    _symbolTable.addPrimop("<=", B_RR);
    _symbolTable.addPrimop(">", B_RR);
    _symbolTable.addPrimop(">=", B_RR);

    // Operators <, <= >, >= defined on int types.
    _symbolTable.addPrimop("<", B_II);
    _symbolTable.addPrimop("<=", B_II);
    _symbolTable.addPrimop(">", B_II);
    _symbolTable.addPrimop(">=", B_II);

    // Operators <, <= >, >= defined on boolean types.
    _symbolTable.addPrimop("<", B_BB);
    _symbolTable.addPrimop("<=", B_BB);
    _symbolTable.addPrimop(">", B_BB);
    _symbolTable.addPrimop(">=", B_BB);

    // Operators 'and' and 'or'.
    _symbolTable.addPrimop("or", B_BB);
    _symbolTable.addPrimop("and", B_BB);

    // Unary minus.
    _symbolTable.addPrimop("u-", R_R);
    _symbolTable.addPrimop("u-", I_I);
  }

  public SymbolTable getSymbolTable() {
    return _symbolTable;
  }

  public Template getTemplate() {
    return _template;
  }

  public void setTemplate(Template template) {
    _template = template;
  }

  private int _templateIndex = 0;

  public int getTemplateIndex() {
    return _templateIndex++;
  }

  /**
   * Creates a new node in the abstract syntax tree. This node can be o) a
   * supported XSLT 1.0 element o) an unsupported XSLT element (post 1.0) o) a
   * supported XSLT extension o) an unsupported XSLT extension o) a literal
   * result element (not an XSLT element and not an extension) Unsupported
   * elements do not directly generate an error. We have to wait until we have
   * received all child elements of an unsupported element to see if any
   * <xsl:fallback> elements exist.
   */

  private boolean versionIsOne = true;

  public SyntaxTreeNode makeInstance(String uri, String prefix, String local, Attributes attributes) {
    SyntaxTreeNode node = null;
    final QName qname = getQName(uri, prefix, local);
    final String className = _instructionClasses.get(qname);

    if (className != null) {
      try {
        final Class<?> clazz = ObjectFactory.findProviderClass(className, ObjectFactory.findClassLoader(), true);
        node = (SyntaxTreeNode) clazz.newInstance();
        node.setQName(qname);
        node.setParser(this);
        if (_locator != null) {
          node.setLineNumber(getLineNumber());
        }
        if (node instanceof Stylesheet) {
          _xsltc.setStylesheet((Stylesheet) node);
        }
        checkForSuperfluousAttributes(node, attributes);
      } catch (final ClassNotFoundException e) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, node);
        reportError(ERROR, err);
      } catch (final Exception e) {
        final ErrorMsg err = new ErrorMsg(ErrorMsg.INTERNAL_ERR, e.getMessage(), node);
        reportError(FATAL, err);
      }
    } else {
      if (uri != null) {
        // Check if the element belongs in our namespace
        if (uri.equals(XSLT_URI)) {
          node = new UnsupportedElement(uri, prefix, local, false);
          final UnsupportedElement element = (UnsupportedElement) node;
          final ErrorMsg msg = new ErrorMsg(ErrorMsg.UNSUPPORTED_XSL_ERR, getLineNumber(), local);
          element.setErrorMessage(msg);
          if (versionIsOne) {
            reportError(UNSUPPORTED, msg);
          }
        }
        // Check if this is an XSLTC extension element
        else if (uri.equals(TRANSLET_URI)) {
          node = new UnsupportedElement(uri, prefix, local, true);
          final UnsupportedElement element = (UnsupportedElement) node;
          final ErrorMsg msg = new ErrorMsg(ErrorMsg.UNSUPPORTED_EXT_ERR, getLineNumber(), local);
          element.setErrorMessage(msg);
        }
        // Check if this is an extension of some other XSLT processor
        else {
          final Stylesheet sheet = _xsltc.getStylesheet();
          if (sheet != null && sheet.isExtension(uri)) {
            if (sheet != _parentStack.peek()) {
              node = new UnsupportedElement(uri, prefix, local, true);
              final UnsupportedElement elem = (UnsupportedElement) node;
              final ErrorMsg msg = new ErrorMsg(ErrorMsg.UNSUPPORTED_EXT_ERR, getLineNumber(), prefix + ":" + local);
              elem.setErrorMessage(msg);
            }
          }
        }
      }
      if (node == null) {
        node = new LiteralElement();
        node.setLineNumber(getLineNumber());
      }
      node.setParser(this);
    }
    if (node != null && node instanceof LiteralElement) {
      ((LiteralElement) node).setQName(qname);
    }
    return node;
  }

  /**
   * checks the list of attributes against a list of allowed attributes for a
   * particular element node.
   */
  private void checkForSuperfluousAttributes(SyntaxTreeNode node, Attributes attrs) {
    final QName qname = node.getQName();
    final boolean isStylesheet = node instanceof Stylesheet;
    final String[] legal = _instructionAttrs.get(qname);
    if (versionIsOne && legal != null) {
      int j;
      final int n = attrs.getLength();

      for (int i = 0; i < n; i++) {
        final String attrQName = attrs.getQName(i);

        if (isStylesheet && attrQName.equals("version")) {
          versionIsOne = attrs.getValue(i).equals("1.0");
        }

        // Ignore if special or if it has a prefix
        if (attrQName.startsWith("xml") || attrQName.indexOf(':') > 0) {
          continue;
        }

        for (j = 0; j < legal.length; j++) {
          if (attrQName.equalsIgnoreCase(legal[j])) {
            break;
          }
        }
        if (j == legal.length) {
          final ErrorMsg err = new ErrorMsg(ErrorMsg.ILLEGAL_ATTRIBUTE_ERR, attrQName, node);
          // Workaround for the TCK failure ErrorListener.errorTests.error001..
          err.setWarningError(true);
          reportError(WARNING, err);
        }
      }
    }
  }

  /**
   * Parse an XPath expression:
   * 
   * @param parent
   *          - XSL element where the expression occured
   * @param exp
   *          - textual representation of the expression
   */
  public Expression parseExpression(SyntaxTreeNode parent, String exp) {
    return (Expression) parseTopLevel(parent, "<EXPRESSION>" + exp, null);
  }

  /**
   * Parse an XPath expression:
   * 
   * @param parent
   *          - XSL element where the expression occured
   * @param attr
   *          - name of this element's attribute to get expression from
   * @param def
   *          - default expression (if the attribute was not found)
   */
  public Expression parseExpression(SyntaxTreeNode parent, String attr, String def) {
    // Get the textual representation of the expression (if any)
    String exp = parent.getAttribute(attr);
    // Use the default expression if none was found
    if (exp.length() == 0 && def != null) {
      exp = def;
    }
    // Invoke the XPath parser
    return (Expression) parseTopLevel(parent, "<EXPRESSION>" + exp, exp);
  }

  /**
   * Parse an XPath pattern:
   * 
   * @param parent
   *          - XSL element where the pattern occured
   * @param pattern
   *          - textual representation of the pattern
   */
  public Pattern parsePattern(SyntaxTreeNode parent, String pattern) {
    return (Pattern) parseTopLevel(parent, "<PATTERN>" + pattern, pattern);
  }

  /**
   * Parse an XPath pattern:
   * 
   * @param parent
   *          - XSL element where the pattern occured
   * @param attr
   *          - name of this element's attribute to get pattern from
   * @param def
   *          - default pattern (if the attribute was not found)
   */
  public Pattern parsePattern(SyntaxTreeNode parent, String attr, String def) {
    // Get the textual representation of the pattern (if any)
    String pattern = parent.getAttribute(attr);
    // Use the default pattern if none was found
    if (pattern.length() == 0 && def != null) {
      pattern = def;
    }
    // Invoke the XPath parser
    return (Pattern) parseTopLevel(parent, "<PATTERN>" + pattern, pattern);
  }

  /**
   * Parse an XPath expression or pattern using the generated XPathParser The
   * method will return a Dummy node if the XPath parser fails.
   */
  private SyntaxTreeNode parseTopLevel(SyntaxTreeNode parent, String text, String expression) {
    final int line = getLineNumber();

    try {
      _xpathParser.setScanner(new XPathLexer(new StringReader(text)));
      final Symbol result = _xpathParser.parse(expression, line);
      if (result != null) {
        final SyntaxTreeNode node = (SyntaxTreeNode) result.value;
        if (node != null) {
          node.setParser(this);
          node.setParent(parent);
          node.setLineNumber(line);
          // System.out.println("e = " + text + " " + node);
          return node;
        }
      }
      reportError(ERROR, new ErrorMsg(ErrorMsg.XPATH_PARSER_ERR, expression, parent));
    } catch (final Exception e) {
      if (_xsltc.debug()) {
        e.printStackTrace();
      }
      reportError(ERROR, new ErrorMsg(ErrorMsg.XPATH_PARSER_ERR, expression, parent));
    }

    // Return a dummy pattern (which is an expression)
    SyntaxTreeNode.Dummy.setParser(this);
    return SyntaxTreeNode.Dummy;
  }

  /************************ ERROR HANDLING SECTION ************************/

  /**
   * Returns true if there were any errors during compilation
   */
  public boolean errorsFound() {
    return _errors.size() > 0;
  }

  /**
   * Prints all compile-time errors
   */
  public void printErrors() {
    final int size = _errors.size();
    if (size > 0) {
      System.err.println(new ErrorMsg(ErrorMsg.COMPILER_ERROR_KEY));
      for (ErrorMsg error : _errors) {
        System.err.println("  " + error);
      }
    }
  }

  /**
   * Prints all compile-time warnings
   */
  public void printWarnings() {
    final int size = _warnings.size();
    if (size > 0) {
      System.err.println(new ErrorMsg(ErrorMsg.COMPILER_WARNING_KEY));
      for (ErrorMsg warning : _warnings) {
        System.err.println("  " + warning);
      }
    }
  }

  /**
   * Common error/warning message handler
   */
  public void reportError(final int category, final ErrorMsg error) {
    switch (category) {
      case Constants.INTERNAL:
        // Unexpected internal errors, such as null-ptr exceptions, etc.
        // Immediately terminates compilation, no translet produced
        _errors.add(error);
        break;
      case Constants.UNSUPPORTED:
        // XSLT elements that are not implemented and unsupported ext.
        // Immediately terminates compilation, no translet produced
        _errors.add(error);
        break;
      case Constants.FATAL:
        // Fatal error in the stylesheet input (parsing or content)
        // Immediately terminates compilation, no translet produced
        _errors.add(error);
        break;
      case Constants.ERROR:
        // Other error in the stylesheet input (parsing or content)
        // Does not terminate compilation, no translet produced
        _errors.add(error);
        break;
      case Constants.WARNING:
        // Other error in the stylesheet input (content errors only)
        // Does not terminate compilation, a translet is produced
        _warnings.add(error);
        break;
    }
  }

  public List<ErrorMsg> getErrors() {
    return _errors;
  }

  public List<ErrorMsg> getWarnings() {
    return _warnings;
  }

  /************************ SAX2 ContentHandler INTERFACE *****************/

  private Deque<SyntaxTreeNode> _parentStack = null;
  private Map<String, String> _prefixMapping = null;

  /**
   * SAX2: Receive notification of the beginning of a document.
   */
  @Override
  public void startDocument() {
    _root = null;
    _target = null;
    _prefixMapping = null;
    _parentStack = new ArrayDeque<>();
  }

  /**
   * SAX2: Receive notification of the end of a document.
   */
  @Override
  public void endDocument() {
  }

  /**
   * SAX2: Begin the scope of a prefix-URI Namespace mapping. This has to be
   * passed on to the symbol table!
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) {
    if (_prefixMapping == null) {
      _prefixMapping = new HashMap<>();
    }
    _prefixMapping.put(prefix, uri);
  }

  /**
   * SAX2: End the scope of a prefix-URI Namespace mapping. This has to be
   * passed on to the symbol table!
   */
  @Override
  public void endPrefixMapping(String prefix) {
  }

  /**
   * SAX2: Receive notification of the beginning of an element. The parser may
   * re-use the attribute list that we're passed so we clone the attributes in
   * our own Attributes implementation
   */
  @Override
  public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
    final int col = qname.lastIndexOf(':');
    final String prefix = col == -1 ? null : qname.substring(0, col);

    final SyntaxTreeNode element = makeInstance(uri, prefix, localname, attributes);
    if (element == null) {
      final ErrorMsg err = new ErrorMsg(ErrorMsg.ELEMENT_PARSE_ERR, prefix + ':' + localname);
      throw new SAXException(err.toString());
    }

    // If this is the root element of the XML document we need to make sure
    // that it contains a definition of the XSL namespace URI
    if (_root == null) {
      if (_prefixMapping == null || _prefixMapping.containsValue(Constants.XSLT_URI) == false) {
        _rootNamespaceDef = false;
      } else {
        _rootNamespaceDef = true;
      }
      _root = element;
    } else {
      final SyntaxTreeNode parent = _parentStack.peek();
      parent.addElement(element);
      element.setParent(parent);
    }
    element.setAttributes(new AttributeList(attributes));
    element.setPrefixMapping(_prefixMapping);

    if (element instanceof Stylesheet) {
      // Extension elements and excluded elements have to be
      // handled at this point in order to correctly generate
      // Fallback elements from <xsl:fallback>s.
      getSymbolTable().setCurrentNode(element);
      ((Stylesheet) element).declareExtensionPrefixes(this);
    }

    _prefixMapping = null;
    _parentStack.push(element);
  }

  /**
   * SAX2: Receive notification of the end of an element.
   */
  @Override
  public void endElement(String uri, String localname, String qname) {
    _parentStack.pop();
  }

  /**
   * SAX2: Receive notification of character data.
   */
  @Override
  public void characters(char[] ch, int start, int length) {
    final String string = new String(ch, start, length);
    final SyntaxTreeNode parent = _parentStack.peek();

    if (string.length() == 0)
      return;

    // If this text occurs within an <xsl:text> element we append it
    // as-is to the existing text element
    if (parent instanceof Text) {
      ((Text) parent).setText(string);
      return;
    }

    // Ignore text nodes that occur directly under <xsl:stylesheet>
    if (parent instanceof Stylesheet)
      return;

    final SyntaxTreeNode bro = parent.lastChild();
    if (bro != null && bro instanceof Text) {
      final Text text = (Text) bro;
      if (!text.isTextElement()) {
        if (length > 1 || ch[0] < 0x100) {
          text.setText(string);
          return;
        }
      }
    }

    // Add it as a regular text node otherwise
    Text text = new Text(string);
    text.setParser(this);
    parent.addElement(text);
  }

  private String getTokenValue(String token) {
    final int start = token.indexOf('"');
    final int stop = token.lastIndexOf('"');
    return token.substring(start + 1, stop);
  }

  /**
   * SAX2: Receive notification of a processing instruction. These require
   * special handling for stylesheet PIs.
   */
  @Override
  public void processingInstruction(String name, String value) {
    // We only handle the <?xml-stylesheet ...?> PI
    if (_target == null && name.equals("xml-stylesheet")) {

      String href = null; // URI of stylesheet found
      String media = null; // Media of stylesheet found
      String title = null; // Title of stylesheet found
      String charset = null; // Charset of stylesheet found

      // Get the attributes from the processing instruction
      final StringTokenizer tokens = new StringTokenizer(value);
      while (tokens.hasMoreElements()) {
        final String token = (String) tokens.nextElement();
        if (token.startsWith("href")) {
          href = getTokenValue(token);
        } else if (token.startsWith("media")) {
          media = getTokenValue(token);
        } else if (token.startsWith("title")) {
          title = getTokenValue(token);
        } else if (token.startsWith("charset")) {
          charset = getTokenValue(token);
        }
      }

      // Set the target to this PI's href if the parameters are
      // null or match the corresponding attributes of this PI.
      if ((_PImedia == null || _PImedia.equals(media)) && (_PItitle == null || _PImedia.equals(title))
              && (_PIcharset == null || _PImedia.equals(charset))) {
        _target = href;
      }
    }
  }

  /**
   * IGNORED - all ignorable whitespace is ignored
   */
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) {
  }

  /**
   * IGNORED - we do not have to do anything with skipped entities
   */
  @Override
  public void skippedEntity(String name) {
  }

  /**
   * Store the document locator to later retrieve line numbers of all elements
   * from the stylesheet
   */
  @Override
  public void setDocumentLocator(Locator locator) {
    _locator = locator;
  }

  /**
   * Get the line number, or zero if there is no _locator.
   */
  private int getLineNumber() {
    int line = 0;
    if (_locator != null) {
      line = _locator.getLineNumber();
    }
    return line;
  }

}
