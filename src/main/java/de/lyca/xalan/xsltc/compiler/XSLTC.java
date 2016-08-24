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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller
 * @author Morten Jorgensen
 * @author John Howard (johnh@schemasoft.com)
 */
public final class XSLTC {

  // A reference to the main stylesheet parser object.
  private final Parser _parser;

  // A reference to an external XMLReader (SAX parser) passed to us
  private XMLReader _reader = null;

  // A reference to an external SourceLoader (for use with include/import)
  private SourceLoader _loader = null;

  // A reference to the stylesheet being compiled.
  private Stylesheet _stylesheet;

  // Counters used by various classes to generate unique names.
  // private int _variableSerial = 1;
  private int _modeSerial = 1;
  private int _stylesheetSerial = 1;
  private int _stepPatternSerial = 1;
  private int _helperClassSerial = 0;
  private int _attributeSetSerial = 0;

  private JFieldVar[] _numberFieldIndexes;

  // Name index tables
  private int _nextGType; // Next available element type
  private List<String> _namesIndex; // Index of all registered QNames
  private Map<String, Integer> _elements; // Map of all registered elements
  private Map<String, Integer> _attributes; // Map of all registered attributes

  // Namespace index tables
  private int _nextNSType; // Next available namespace type
  private List<String> _namespaceIndex; // Index of all registered namespaces
  private Map<String, Integer> _namespaces; // Map of all registered namespaces
  private Map<String, Integer> _namespacePrefixes;// Map of all registered
                                                  // namespace prefixes

  // All literal text in the stylesheet
  private List<StringBuilder> m_characterData;

  // These define the various methods for outputting the translet
  public static final int FILE_OUTPUT = 0;
  public static final int JAR_OUTPUT = 1;
  public static final int BYTEARRAY_OUTPUT = 2;
  public static final int CLASSLOADER_OUTPUT = 3;
  public static final int BYTEARRAY_AND_FILE_OUTPUT = 4;
  public static final int BYTEARRAY_AND_JAR_OUTPUT = 5;

  // Compiler options (passed from command line or XSLTC client)
  private boolean _debug = false; // -x
  private String _jarFileName = null; // -j <jar-file-name>
  private String _className = null; // -o <class-name>
  private String _packageName = null; // -p <package-name>
  private File _destDir = null; // -d <directory-name>
  private int _outputType = FILE_OUTPUT; // by default

  private List<byte[]> _classes;
  private JCodeModel _codeModel;
  private boolean _callsNodeset = false;
  private boolean _multiDocument = false;
  private boolean _hasIdCall = false;

  private List<Integer> _stylesheetNSAncestorPointers;
  private List<String> _prefixURIPairs;
  private List<Integer> _prefixURIPairsIdx;

  /**
   * Set to true if template inlining is requested. Template inlining used to be
   * the default, but we have found that Hotspots does a better job with shorter
   * methods, so the default is *not* to inline now.
   */
  private boolean _templateInlining = false;

  /**
   * State of the secure processing feature.
   */
  private boolean _isSecureProcessing = false;

  /**
   * XSLTC compiler constructor
   */
  public XSLTC() {
    _parser = new Parser(this);
  }

  /**
   * Set the state of the secure processing feature.
   */
  public void setSecureProcessing(boolean flag) {
    _isSecureProcessing = flag;
  }

  /**
   * Return the state of the secure processing feature.
   */
  public boolean isSecureProcessing() {
    return _isSecureProcessing;
  }

  /**
   * Only for user by the internal TrAX implementation.
   */
  public Parser getParser() {
    return _parser;
  }

  /**
   * Only for user by the internal TrAX implementation.
   */
  public void setOutputType(int type) {
    _outputType = type;
  }

  /**
   * Only for user by the internal TrAX implementation.
   */
  public Properties getOutputProperties() {
    return _parser.getOutputProperties();
  }

  /**
   * Initializes the compiler to compile a new stylesheet
   */
  public void init() {
    reset();
    _reader = null;
    _classes = new ArrayList<>();
    _codeModel = null;
  }

  /**
   * Initializes the compiler to produce a new translet
   */
  private void reset() {
    _nextGType = DTM.NTYPES;
    _elements = new HashMap<>();
    _attributes = new HashMap<>();
    _namespaces = new HashMap<>();
    _namespaces.put("", new Integer(_nextNSType));
    _namesIndex = new ArrayList<>(128);
    _namespaceIndex = new ArrayList<>(32);
    _namespacePrefixes = new HashMap<>();
    _stylesheet = null;
    _parser.init();
    // _variableSerial = 1;
    _modeSerial = 1;
    _stylesheetSerial = 1;
    _stepPatternSerial = 1;
    _helperClassSerial = 0;
    _attributeSetSerial = 0;
    _multiDocument = false;
    _hasIdCall = false;
    _stylesheetNSAncestorPointers = null;
    _prefixURIPairs = null;
    _prefixURIPairsIdx = null;
    _numberFieldIndexes = new JFieldVar[] { null, // LEVEL_SINGLE
        null, // LEVEL_MULTIPLE
        null // LEVEL_ANY
    };
  }

  /**
   * Defines an external SourceLoader to provide the compiler with documents
   * referenced in xsl:include/import
   * 
   * @param loader
   *          The SourceLoader to use for include/import
   */
  public void setSourceLoader(SourceLoader loader) {
    _loader = loader;
  }

  /**
   * Set a flag indicating if templates are to be inlined or not. The default is
   * to do inlining, but this causes problems when the stylesheets have a large
   * number of templates (e.g. branch targets exceeding 64K or a length of a
   * method exceeding 64K).
   */
  public void setTemplateInlining(boolean templateInlining) {
    _templateInlining = templateInlining;
  }

  /**
   * Return the state of the template inlining feature.
   */
  public boolean getTemplateInlining() {
    return _templateInlining;
  }

  /**
   * Set the parameters to use to locate the correct <?xml-stylesheet ...?>
   * processing instruction in the case where the input document to the compiler
   * (and parser) is an XML document.
   * 
   * @param media
   *          The media attribute to be matched. May be null, in which case the
   *          prefered templates will be used (i.e. alternate = no).
   * @param title
   *          The value of the title attribute to match. May be null.
   * @param charset
   *          The value of the charset attribute to match. May be null.
   */
  public void setPIParameters(String media, String title, String charset) {
    _parser.setPIParameters(media, title, charset);
  }

  /**
   * Compiles an XSL stylesheet pointed to by a URL
   * 
   * @param url
   *          An URL containing the input XSL stylesheet
   */
  public boolean compile(URL url) {
    try {
      // Open input stream from URL and wrap inside InputSource
      final InputStream stream = url.openStream();
      final InputSource input = new InputSource(stream);
      input.setSystemId(url.toString());
      return compile(input, _className);
    } catch (final IOException e) {
      _parser.reportError(Constants.FATAL, new ErrorMsg(e));
      return false;
    }
  }

  /**
   * Compiles an XSL stylesheet pointed to by a URL
   * 
   * @param url
   *          An URL containing the input XSL stylesheet
   * @param name
   *          The name to assign to the translet class
   */
  public boolean compile(URL url, String name) {
    try {
      // Open input stream from URL and wrap inside InputSource
      final InputStream stream = url.openStream();
      final InputSource input = new InputSource(stream);
      input.setSystemId(url.toString());
      return compile(input, name);
    } catch (final IOException e) {
      _parser.reportError(Constants.FATAL, new ErrorMsg(e));
      return false;
    }
  }

  /**
   * Compiles an XSL stylesheet passed in through an InputStream
   * 
   * @param stream
   *          An InputStream that will pass in the stylesheet contents
   * @param name
   *          The name of the translet class to generate
   * @return 'true' if the compilation was successful
   */
  public boolean compile(InputStream stream, String name) {
    final InputSource input = new InputSource(stream);
    input.setSystemId(name); // We have nothing else!!!
    return compile(input, name);
  }

  /**
   * Compiles an XSL stylesheet passed in through an InputStream
   * 
   * @param input
   *          An InputSource that will pass in the stylesheet contents
   * @param name
   *          The name of the translet class to generate - can be null
   * @return 'true' if the compilation was successful
   */
  public boolean compile(InputSource input, String name) {
    try {
      // Reset globals in case we're called by compile(List v);
      reset();

      // The systemId may not be set, so we'll have to check the URL
      String systemId = null;
      if (input != null) {
        systemId = input.getSystemId();
      }

      // Set the translet class name if not already set
      if (_className == null) {
        if (name != null) {
          setClassName(name);
        } else if (systemId != null && systemId.length() != 0) {
          setClassName(Util.baseName(systemId));
        }

        // Ensure we have a non-empty class name at this point
        if (_className == null || _className.length() == 0) {
          setClassName("GregorSamsa"); // default translet name
        }
      }

      // Get the root node of the abstract syntax tree
      SyntaxTreeNode element = null;
      if (_reader == null) {
        element = _parser.parse(input);
      } else {
        element = _parser.parse(_reader, input);
      }

      // Compile the translet - this is where the work is done!
      if (!_parser.errorsFound() && element != null) {
        // Create a Stylesheet element from the root node
        _stylesheet = _parser.makeStylesheet(element);
        _stylesheet.setSourceLoader(_loader);
        _stylesheet.setSystemId(systemId);
        _stylesheet.setParentStylesheet(null);
        _stylesheet.setTemplateInlining(_templateInlining);
        _parser.setCurrentStylesheet(_stylesheet);

        // Create AST under the Stylesheet element (parse & type-check)
        _parser.createAST(_stylesheet);
      }
      // Generate the bytecodes and output the translet class(es)
      if (!_parser.errorsFound() && _stylesheet != null) {
        _stylesheet.setCallsNodeset(_callsNodeset);
        _stylesheet.setMultiDocument(_multiDocument);
        _stylesheet.setHasIdCall(_hasIdCall);

        // Class synchronization is needed for BCEL
        synchronized (getClass()) {
          _stylesheet.generate();
        }
      }
    } catch (final Exception e) {
      /* if (_debug) */e.printStackTrace();
      _parser.reportError(Constants.FATAL, new ErrorMsg(e));
    } catch (final Error e) {
      if (_debug) {
        e.printStackTrace();
      }
      _parser.reportError(Constants.FATAL, new ErrorMsg(e));
    } finally {
      _reader = null; // reset this here to be sure it is not re-used
    }
    return !_parser.errorsFound();
  }

  /**
   * Compiles a set of stylesheets pointed to by a List of URLs
   * 
   * @param stylesheetURLs
   *          A List containing URLs pointing to the stylesheets
   * @return 'true' if the compilation was successful
   */
  public boolean compile(List<URL> stylesheetURLs) {
    // Get the number of stylesheets (ie. URLs) in the list
    final int count = stylesheetURLs.size();

    // Return straight away if the list is empty
    if (count == 0)
      return true;

    // Special handling needed if the URL count is one, becuase the
    // _className global must not be reset if it was set explicitly
    if (count == 1) {
      final Object url = stylesheetURLs.get(0);
      if (url instanceof URL)
        return compile((URL) url);
      else
        return false;
    } else {
      // Traverse all elements in the list and compile
      for (final URL url : stylesheetURLs) {
        _className = null; // reset, so that new name will be computed
        if (!compile(url))
          return false;
      }
    }
    return true;
  }

  /**
   * Returns an array of bytecode arrays generated by a compilation.
   * 
   * @return JVM bytecodes that represent translet class definition
   */
  public byte[][] getBytecodes() {
    return _classes.toArray(new byte[0][0]);
  }

  /**
   * Compiles a stylesheet pointed to by a URL. The result is put in a set of
   * byte arrays. One byte array for each generated class.
   * 
   * @param name
   *          The name of the translet class to generate
   * @param input
   *          An InputSource that will pass in the stylesheet contents
   * @param outputType
   *          The output type
   * @return JVM bytecodes that represent translet class definition
   */
  public byte[][] compile(String name, InputSource input, int outputType) {
    _outputType = outputType;
    if (compile(input, name))
      return getBytecodes();
    else
      return null;
  }

  /**
   * Compiles a stylesheet pointed to by a URL. The result is put in a set of
   * byte arrays. One byte array for each generated class.
   * 
   * @param name
   *          The name of the translet class to generate
   * @param input
   *          An InputSource that will pass in the stylesheet contents
   * @return JVM bytecodes that represent translet class definition
   */
  public byte[][] compile(String name, InputSource input) {
    return compile(name, input, BYTEARRAY_OUTPUT);
  }

  /**
   * Set the XMLReader to use for parsing the next input stylesheet
   * 
   * @param reader
   *          XMLReader (SAX2 parser) to use
   */
  public void setXMLReader(XMLReader reader) {
    _reader = reader;
  }

  /**
   * Get the XMLReader to use for parsing the next input stylesheet
   */
  public XMLReader getXMLReader() {
    return _reader;
  }

  /**
   * Get a List containing all compile error messages
   * 
   * @return A List containing all compile error messages
   */
  public List<ErrorMsg> getErrors() {
    return _parser.getErrors();
  }

  /**
   * Get a List containing all compile warning messages
   * 
   * @return A List containing all compile error messages
   */
  public List<ErrorMsg> getWarnings() {
    return _parser.getWarnings();
  }

  /**
   * Print all compile error messages to standard output
   */
  public void printErrors() {
    _parser.printErrors();
  }

  /**
   * Print all compile warning messages to standard output
   */
  public void printWarnings() {
    _parser.printWarnings();
  }

  /**
   * This method is called by the XPathParser when it encounters a call to the
   * document() function. Affects the DOM used by the translet.
   */
  protected void setMultiDocument(boolean flag) {
    _multiDocument = flag;
  }

  public boolean isMultiDocument() {
    return _multiDocument;
  }

  /**
   * This method is called by the XPathParser when it encounters a call to the
   * nodeset() extension function. Implies multi document.
   */
  protected void setCallsNodeset(boolean flag) {
    if (flag) {
      setMultiDocument(flag);
    }
    _callsNodeset = flag;
  }

  public boolean callsNodeset() {
    return _callsNodeset;
  }

  protected void setHasIdCall(boolean flag) {
    _hasIdCall = flag;
  }

  public boolean hasIdCall() {
    return _hasIdCall;
  }

  /**
   * Set the class name for the generated translet. This class name is
   * overridden if multiple stylesheets are compiled in one go using the
   * compile(List urls) method.
   * 
   * @param className
   *          The name to assign to the translet class
   */
  public void setClassName(String className) {
    final String base = Util.baseName(className);
    final String noext = Util.noExtName(base);
    final String name = Util.toJavaName(noext);

    if (_packageName == null) {
      _className = name;
    } else {
      _className = _packageName + '.' + name;
    }
  }

  /**
   * Get the class name for the generated translet.
   */
  public String getClassName() {
    return _className;
  }

  /**
   * Convert for Java class name of local system file name. (Replace '.' with
   * '/' on UNIX and replace '.' by '\' on Windows/DOS.)
   */
  private String classFileName(final String className) {
    return className.replace('.', File.separatorChar) + ".class";
  }

  /**
   * Convert for Java source name of local system file name. (Replace '.' with
   * '/' on UNIX and replace '.' by '\' on Windows/DOS.)
   */
  private String sourceFileName(final String className) {
    return className.replace('.', File.separatorChar) + ".java";
  }

  /**
   * Generate an output File object to send the translet to
   */
  private File getOutputFile(String className) {
    if (_destDir != null)
      return new File(_destDir, classFileName(className));
    else
      return new File(classFileName(className));
  }

  /**
   * Generate an output File object to send the translet to
   */
  private File getInputFile(String className) {
    if (_destDir != null)
      return new File(_destDir, sourceFileName(className));
    else
      return new File(sourceFileName(className));
  }

  /**
   * Set the destination directory for the translet. The current working
   * directory will be used by default.
   */
  public boolean setDestDirectory(String dstDirName) {
    final File dir = new File(dstDirName);
    if (dir.exists() || dir.mkdirs()) {
      _destDir = dir;
      return true;
    } else {
      _destDir = null;
      return false;
    }
  }

  /**
   * Set an optional package name for the translet and auxiliary classes
   */
  public void setPackageName(String packageName) {
    _packageName = packageName;
    if (_className != null) {
      setClassName(_className);
    }
  }

  /**
   * Set the name of an optional JAR-file to dump the translet and auxiliary
   * classes to
   */
  public void setJarFileName(String jarFileName) {
    final String JAR_EXT = ".jar";
    if (jarFileName.endsWith(JAR_EXT)) {
      _jarFileName = jarFileName;
    } else {
      _jarFileName = jarFileName + JAR_EXT;
    }
    _outputType = JAR_OUTPUT;
  }

  public String getJarFileName() {
    return _jarFileName;
  }

  /**
   * Set the top-level stylesheet
   */
  public void setStylesheet(Stylesheet stylesheet) {
    if (_stylesheet == null) {
      _stylesheet = stylesheet;
    }
  }

  /**
   * Returns the top-level stylesheet
   */
  public Stylesheet getStylesheet() {
    return _stylesheet;
  }

  /**
   * Registers an attribute and gives it a type so that it can be mapped to DOM
   * attribute types at run-time.
   */
  public int registerAttribute(QName name) {
    Integer code = _attributes.get(name.toString());
    if (code == null) {
      code = new Integer(_nextGType++);
      _attributes.put(name.toString(), code);
      final String uri = name.getNamespace();
      final String local = "@" + name.getLocalPart();
      if (uri != null && uri.length() != 0) {
        _namesIndex.add(uri + ":" + local);
      } else {
        _namesIndex.add(local);
      }
      if (name.getLocalPart().equals("*")) {
        registerNamespace(name.getNamespace());
      }
    }
    return code.intValue();
  }

  /**
   * Registers an element and gives it a type so that it can be mapped to DOM
   * element types at run-time.
   */
  public int registerElement(QName name) {
    // Register element (full QName)
    Integer code = _elements.get(name.toString());
    if (code == null) {
      _elements.put(name.toString(), code = new Integer(_nextGType++));
      _namesIndex.add(name.toString());
    }
    if (name.getLocalPart().equals("*")) {
      registerNamespace(name.getNamespace());
    }
    return code.intValue();
  }

  /**
   * Registers a namespace prefix and gives it a type so that it can be mapped
   * to DOM namespace types at run-time.
   */

  public int registerNamespacePrefix(QName name) {

    Integer code = _namespacePrefixes.get(name.toString());
    if (code == null) {
      code = new Integer(_nextGType++);
      _namespacePrefixes.put(name.toString(), code);
      final String uri = name.getNamespace();
      if (uri != null && uri.length() != 0) {
        // namespace::ext2:ped2 will be made empty in TypedNamespaceIterator
        _namesIndex.add("?");
      } else {
        _namesIndex.add("?" + name.getLocalPart());
      }
    }
    return code.intValue();
  }

  /**
   * Registers a namespace and gives it a type so that it can be mapped to DOM
   * namespace types at run-time.
   */
  public int registerNamespacePrefix(String name) {
    Integer code = _namespacePrefixes.get(name);
    if (code == null) {
      code = new Integer(_nextGType++);
      _namespacePrefixes.put(name, code);
      _namesIndex.add("?" + name);
    }
    return code.intValue();
  }

  /**
   * Registers a namespace and gives it a type so that it can be mapped to DOM
   * namespace types at run-time.
   */
  public int registerNamespace(String namespaceURI) {
    Integer code = _namespaces.get(namespaceURI);
    if (code == null) {
      code = new Integer(_nextNSType++);
      _namespaces.put(namespaceURI, code);
      _namespaceIndex.add(namespaceURI);
    }
    return code.intValue();
  }

  /**
   * Registers namespace declarations that the stylesheet might need to look up
   * dynamically - for instance, if an <code>xsl:element</code> has a a
   * <code>name</code> attribute with variable parts and has no
   * <code>namespace</code> attribute.
   * 
   * @param prefixMap
   *          a <code>Map</code> mapping namespace prefixes to URIs. Must not be
   *          <code>null</code>. The default namespace and namespace
   *          undeclarations are represented by a zero-length string.
   * @param ancestorID
   *          The <code>int</code> node ID of the nearest ancestor in the
   *          stylesheet that declares namespaces, or a value less than zero if
   *          there is no such ancestor
   * @return A new node ID for the stylesheet element
   */
  public int registerStylesheetPrefixMappingForRuntime(Map<String, String> prefixMap, int ancestorID) {
    if (_stylesheetNSAncestorPointers == null) {
      _stylesheetNSAncestorPointers = new ArrayList<>();
    }

    if (_prefixURIPairs == null) {
      _prefixURIPairs = new ArrayList<>();
    }

    if (_prefixURIPairsIdx == null) {
      _prefixURIPairsIdx = new ArrayList<>();
    }

    final int currentNodeID = _stylesheetNSAncestorPointers.size();
    _stylesheetNSAncestorPointers.add(new Integer(ancestorID));

    final int prefixNSPairStartIdx = _prefixURIPairs.size();
    _prefixURIPairsIdx.add(new Integer(prefixNSPairStartIdx));

    for (final Map.Entry<String, String> entry : prefixMap.entrySet()) {
      _prefixURIPairs.add(entry.getKey());
      _prefixURIPairs.add(entry.getValue());
    }

    return currentNodeID;
  }

  public List<Integer> getNSAncestorPointers() {
    return _stylesheetNSAncestorPointers;
  }

  public List<String> getPrefixURIPairs() {
    return _prefixURIPairs;
  }

  public List<Integer> getPrefixURIPairsIdx() {
    return _prefixURIPairsIdx;
  }

  public int nextModeSerial() {
    return _modeSerial++;
  }

  public int nextStylesheetSerial() {
    return _stylesheetSerial++;
  }

  public int nextStepPatternSerial() {
    return _stepPatternSerial++;
  }

  public JFieldVar[] getNumberFieldIndexes() {
    return _numberFieldIndexes;
  }

  public int nextHelperClassSerial() {
    return _helperClassSerial++;
  }

  public int nextAttributeSetSerial() {
    return _attributeSetSerial++;
  }

  public List<String> getNamesIndex() {
    return _namesIndex;
  }

  public List<String> getNamespaceIndex() {
    return _namespaceIndex;
  }

  /**
   * Returns a unique name for every helper class needed to execute a translet.
   */
  public String getHelperClassName() {
    return getClassName() + '_' + _helperClassSerial++;
  }

  public void dumpClass(JCodeModel jCodeModel, JDefinedClass definedClass) {

    String fullName = definedClass.fullName();
    if (_outputType == FILE_OUTPUT || _outputType == BYTEARRAY_AND_FILE_OUTPUT) {
      final File outFile = getOutputFile(fullName);
      final String parentDir = outFile.getParent();
      if (parentDir != null) {
        final File parentFile = new File(parentDir);
        if (!parentFile.exists()) {
          parentFile.mkdirs();
        }
      }
    }

    try {
      switch (_outputType) {
      case FILE_OUTPUT:
        jCodeModel.build(getOutputFile(fullName));
        break;
      case JAR_OUTPUT:
        _codeModel = jCodeModel;
        // FIXME
        // _bcelClasses.add(jCodeModel);
        break;
      case BYTEARRAY_OUTPUT:
      case BYTEARRAY_AND_FILE_OUTPUT:
      case BYTEARRAY_AND_JAR_OUTPUT:
      case CLASSLOADER_OUTPUT:
        final File inFile = getInputFile(fullName);
        Path parentDir = inFile.toPath().getParent();
        if (parentDir == null) {
          parentDir = Paths.get(System.getProperty("user.dir"));
        }
        Files.createDirectories(parentDir);
        jCodeModel.build(parentDir.toFile());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(inFile);
        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, fileObjects);
        Boolean result = task.call();
        if (result == true) {
          System.out.println("Compilation has succeeded");
        }
        _classes.add(Files.readAllBytes(Paths.get(getOutputFile(fullName).toURI())));
        for (Iterator<JDefinedClass> iterator = definedClass.classes(); iterator.hasNext();) {
          JDefinedClass definedInnerClass = iterator.next();
          _classes.add(Files.readAllBytes(Paths.get(getOutputFile(definedInnerClass.binaryName()).toURI())));
        }
        if (_outputType == BYTEARRAY_AND_FILE_OUTPUT) {
          // jCodeModel.build(getOutputFile(definedClass.fullName()));
        } else if (_outputType == BYTEARRAY_AND_JAR_OUTPUT) {
          _codeModel = jCodeModel;
        }

        break;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * TODO Generate output JAR-file and packages
   */
  public void outputToJar() throws IOException {
    // create the manifest
    final Manifest manifest = new Manifest();
    final Attributes atrs = manifest.getMainAttributes();
    atrs.put(Attributes.Name.MANIFEST_VERSION, "1.2");

    final Map<String, Attributes> map = manifest.getEntries();
    // create manifest
    final String now = new Date().toString();
    final Attributes.Name dateAttr = new Attributes.Name("Date");
    // for (final JavaClass clazz : _bcelClasses) {
    // final String className = clazz.getClassName().replace('.', '/');
    // final Attributes attr = new Attributes();
    // attr.put(dateAttr, now);
    // map.put(className + ".class", attr);
    // }

    final File jarFile = new File(_destDir, _jarFileName);
    final JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);
    // for (final JavaClass clazz : _bcelClasses) {
    // final String className = clazz.getClassName().replace('.', '/');
    // jos.putNextEntry(new JarEntry(className + ".class"));
    // final ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
    // clazz.dump(out); // dump() closes it's output stream
    // out.writeTo(jos);
    // }
    jos.close();
  }

  /**
   * Turn debugging messages on/off
   */
  public void setDebug(boolean debug) {
    _debug = debug;
  }

  /**
   * Get current debugging message setting
   */
  public boolean debug() {
    return _debug;
  }

  /**
   * Retrieve a string representation of the character data to be stored in the
   * translet as a <code>char[]</code>. There may be more than one such array
   * required.
   * 
   * @param index
   *          The index of the <code>char[]</code>. Zero-based.
   * @return String The character data to be stored in the corresponding
   *         <code>char[]</code>.
   */
  public String getCharacterData(int index) {
    return m_characterData.get(index).toString();
  }

  /**
   * Get the number of char[] arrays, thus far, that will be created to store
   * literal text in the stylesheet.
   */
  public int getCharacterDataCount() {
    return m_characterData == null ? 0 : m_characterData.size();
  }

  /**
   * Add literal text to char arrays that will be used to store character data
   * in the stylesheet.
   * 
   * @param newData
   *          String data to be added to char arrays. Pre-condition:
   *          <code>newData.length() &le; 21845</code>
   * @return int offset at which character data will be stored
   */
  public int addCharacterData(String newData) {
    StringBuilder currData;
    if (m_characterData == null) {
      m_characterData = new ArrayList<>();
      currData = new StringBuilder();
      m_characterData.add(currData);
    } else {
      currData = m_characterData.get(m_characterData.size() - 1);
    }

    // Character data could take up to three-times as much space when
    // written to the class file as UTF-8. The maximum size for a
    // constant is 65535/3. If we exceed that,
    // (We really should use some "bin packing".)
    if (newData.length() + currData.length() > 21845) {
      currData = new StringBuilder();
      m_characterData.add(currData);
    }

    final int newDataOffset = currData.length();
    currData.append(newData);

    return newDataOffset;
  }
}
