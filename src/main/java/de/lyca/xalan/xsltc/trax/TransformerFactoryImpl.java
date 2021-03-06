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
package de.lyca.xalan.xsltc.trax;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.lyca.xalan.xsltc.compiler.SourceLoader;
import de.lyca.xalan.xsltc.compiler.XSLTC;
import de.lyca.xalan.xsltc.compiler.XSLTC.Out;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.dom.XSLTCDTMManager;
import de.lyca.xml.utils.StopParseException;
import de.lyca.xml.utils.StylesheetPIHandler;

/**
 * Implementation of a JAXP1.1 TransformerFactory for Translets.
 * 
 * @author G. Todd Miller
 * @author Morten Jorgensen
 * @author Santiago Pericas-Geertsen
 */
public class TransformerFactoryImpl extends SAXTransformerFactory implements SourceLoader, ErrorListener {
  // Public constants for attributes supported by the XSLTC TransformerFactory.
  public final static String TRANSLET_NAME = "translet-name";
  public final static String DESTINATION_DIRECTORY = "destination-directory";
  public final static String GENERATE_TRANSLET = "generate-translet";
  public final static String DEBUG = "debug";
  public final static String INDENT_NUMBER = "indent-number";

  /**
   * This error listener is used only for this factory and is not passed to the Templates or Transformer objects that we
   * create.
   */
  private ErrorListener _errorListener = this;

  /**
   * This URIResolver is passed to all created Templates and Transformers
   */
  private URIResolver _uriResolver = null;

  /**
   * As Gregor Samsa awoke one morning from uneasy dreams he found himself transformed in his bed into a gigantic
   * insect. He was lying on his hard, as it were armour plated, back, and if he lifted his head a little he could see
   * his big, brown belly divided into stiff, arched segments, on top of which the bed quilt could hardly keep in
   * position and was about to slide off completely. His numerous legs, which were pitifully thin compared to the rest
   * of his bulk, waved helplessly before his eyes. "What has happened to me?", he thought. It was no dream....
   */
  public final static String DEFAULT_TRANSLET_NAME = "GregorSamsa";

  /**
   * The class name of the translet
   */
  private String _transletName = DEFAULT_TRANSLET_NAME;

  /**
   * The destination directory for the translet
   */
  private String _destinationDirectory = null;

  /**
   * Set to <code>true</code> when debugging is enabled.
   */
  private boolean _debug = false;

  /**
   * Set to <code>true</code> when we want to generate translet classes from the stylesheet.
   */
  private boolean _generateTranslet = false;

  /**
   * Number of indent spaces when indentation is turned on.
   */
  private int _indentNumber = -1;

  /**
   * The provider of the XSLTC DTM Manager service. This is fixed for any instance of this class. In order to change
   * service providers, a new XSLTC <code>TransformerFactory</code> must be instantiated.
   * 
   * @see XSLTCDTMManager#getDTMManagerClass()
   */
  private final Class<?> m_DTMManagerClass;

  /**
   * <p>
   * State of secure processing feature.
   * </p>
   */
  private boolean _isSecureProcessing = false;

  /**
   * SAXTransformerFactory implementation.
   */
  public TransformerFactoryImpl() {
    m_DTMManagerClass = XSLTCDTMManager.getDTMManagerClass();
  }

  /**
   * SAXTransformerFactory implementation. Set the error event listener for the TransformerFactory, which is used for
   * the processing of transformation instructions, and not for the transformation itself.
   * 
   * @param listener The error listener to use with the TransformerFactory
   * @throws IllegalArgumentException TODO
   */
  @Override
  public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
    if (listener == null) {
      final ErrorMsg err = new ErrorMsg(Messages.get().errorListenerNullErr("TransformerFactory"));
      throw new IllegalArgumentException(err.toString());
    }
    _errorListener = listener;
  }

  /**
   * SAXTransformerFactory implementation. Get the error event handler for the TransformerFactory.
   * 
   * @return The error listener used with the TransformerFactory
   */
  @Override
  public ErrorListener getErrorListener() {
    return _errorListener;
  }

  /**
   * SAXTransformerFactory implementation. Returns the value set for a TransformerFactory attribute
   * 
   * @param name The attribute name
   * @return An object representing the attribute value
   * @throws IllegalArgumentException TODO
   */
  @Override
  public Object getAttribute(String name) throws IllegalArgumentException {
    // Return value for attribute 'translet-name'
    if (name.equals(TRANSLET_NAME))
      return _transletName;
    else if (name.equals(GENERATE_TRANSLET))
      return _generateTranslet ? Boolean.TRUE : Boolean.FALSE;

    // Throw an exception for all other attributes
    final ErrorMsg err = new ErrorMsg(Messages.get().jaxpInvalidAttrErr(name));
    throw new IllegalArgumentException(err.toString());
  }

  /**
   * SAXTransformerFactory implementation. Sets the value for a TransformerFactory attribute.
   * 
   * @param name The attribute name
   * @param value An object representing the attribute value
   * @throws IllegalArgumentException TODO
   */
  @Override
  public void setAttribute(String name, Object value) throws IllegalArgumentException {
    // Set the default translet name (ie. class name), which will be used
    // for translets that cannot be given a name from their system-id.
    if (name.equals(TRANSLET_NAME) && value instanceof String) {
      _transletName = (String) value;
      return;
    } else if (name.equals(DESTINATION_DIRECTORY) && value instanceof String) {
      _destinationDirectory = (String) value;
      return;
    } else if (name.equals(GENERATE_TRANSLET)) {
      if (value instanceof Boolean) {
        _generateTranslet = ((Boolean) value).booleanValue();
        return;
      } else if (value instanceof String) {
        _generateTranslet = ((String) value).equalsIgnoreCase("true");
        return;
      }
    } else if (name.equals(DEBUG)) {
      if (value instanceof Boolean) {
        _debug = ((Boolean) value).booleanValue();
        return;
      } else if (value instanceof String) {
        _debug = ((String) value).equalsIgnoreCase("true");
        return;
      }
    } else if (name.equals(INDENT_NUMBER)) {
      if (value instanceof String) {
        try {
          _indentNumber = Integer.parseInt((String) value);
          return;
        } catch (final NumberFormatException e) {
          // Falls through
        }
      } else if (value instanceof Integer) {
        _indentNumber = ((Integer) value).intValue();
        return;
      }
    }

    // Throw an exception for all other attributes
    final ErrorMsg err = new ErrorMsg(Messages.get().jaxpInvalidAttrErr(name));
    throw new IllegalArgumentException(err.toString());
  }

  /**
   * <p>
   * Set a feature for this <code>TransformerFactory</code> and <code>Transformer</code>s or <code>Template</code>s
   * created by this factory.
   * </p>
   * 
   * <p>
   * Feature names are fully qualified {@link java.net.URI}s. Implementations may define their own features. An
   * {@link TransformerConfigurationException} is thrown if this <code>TransformerFactory</code> or the
   * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature. It is possible for an
   * <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
   * </p>
   * 
   * <p>
   * See {@link javax.xml.transform.TransformerFactory} for full documentation of specific features.
   * </p>
   * 
   * @param name Feature name.
   * @param value Is feature state <code>true</code> or <code>false</code>.
   * 
   * @throws TransformerConfigurationException if this <code>TransformerFactory</code> or the <code>Transformer</code>s
   *         or <code>Template</code>s it creates cannot support this feature.
   * @throws NullPointerException If the <code>name</code> parameter is null.
   */
  @Override
  public void setFeature(String name, boolean value) throws TransformerConfigurationException {

    // feature name cannot be null
    if (name == null) {
      final ErrorMsg err = new ErrorMsg(Messages.get().jaxpSetFeatureNullName());
      throw new NullPointerException(err.toString());
    }
    // secure processing?
    else if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
      _isSecureProcessing = value;
      // all done processing feature
      return;
    } else {
      // unknown feature
      final ErrorMsg err = new ErrorMsg(Messages.get().jaxpUnsupportedFeature(name));
      throw new TransformerConfigurationException(err.toString());
    }
  }

  /**
   * SAXTransformerFactory implementation. Look up the value of a feature (to see if it is supported). This method must
   * be updated as the various methods and features of this class are implemented.
   * 
   * @param name The feature name
   * @return 'true' if feature is supported, 'false' if not
   */
  @Override
  public boolean getFeature(String name) {
    // All supported features should be listed here
    final String[] features = { DOMSource.FEATURE, DOMResult.FEATURE, SAXSource.FEATURE, SAXResult.FEATURE,
        StreamSource.FEATURE, StreamResult.FEATURE, SAXTransformerFactory.FEATURE,
        SAXTransformerFactory.FEATURE_XMLFILTER };

    // feature name cannot be null
    if (name == null) {
      final ErrorMsg err = new ErrorMsg(Messages.get().jaxpGetFeatureNullName());
      throw new NullPointerException(err.toString());
    }

    // Inefficient, but array is small
    for (int i = 0; i < features.length; i++) {
      if (name.equals(features[i]))
        return true;
    }
    // secure processing?
    if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING))
      return _isSecureProcessing;

    // Feature not supported
    return false;
  }

  /**
   * SAXTransformerFactory implementation. Get the object that is used by default during the transformation to resolve
   * URIs used in document(), xsl:import, or xsl:include.
   * 
   * @return The URLResolver used for this TransformerFactory and all Templates and Transformer objects created using
   *         this factory
   */
  @Override
  public URIResolver getURIResolver() {
    return _uriResolver;
  }

  /**
   * SAXTransformerFactory implementation. Set the object that is used by default during the transformation to resolve
   * URIs used in document(), xsl:import, or xsl:include. Note that this does not affect Templates and Transformers that
   * are already created with this factory.
   * 
   * @param resolver The URLResolver used for this TransformerFactory and all Templates and Transformer objects created
   *        using this factory
   */
  @Override
  public void setURIResolver(URIResolver resolver) {
    _uriResolver = resolver;
  }

  /**
   * SAXTransformerFactory implementation. Get the stylesheet specification(s) associated via the xml-stylesheet
   * processing instruction (see http://www.w3.org/TR/xml-stylesheet/) with the document document specified in the
   * source parameter, and that match the given criteria.
   * 
   * @param source The XML source document.
   * @param media The media attribute to be matched. May be null, in which case the prefered templates will be used
   *        (i.e. alternate = no).
   * @param title The value of the title attribute to match. May be null.
   * @param charset The value of the charset attribute to match. May be null.
   * @return A Source object suitable for passing to the TransformerFactory.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public Source getAssociatedStylesheet(Source source, String media, String title, String charset)
      throws TransformerConfigurationException {

    String baseId;
    XMLReader reader = null;
    InputSource isource = null;

    /**
     * Fix for bugzilla bug 24187
     */
    final StylesheetPIHandler _stylesheetPIHandler = new StylesheetPIHandler(null, media, title, charset);

    try {

      if (source instanceof DOMSource) {
        final DOMSource domsrc = (DOMSource) source;
        baseId = domsrc.getSystemId();
        final org.w3c.dom.Node node = domsrc.getNode();
        final DOM2SAX dom2sax = new DOM2SAX(node);

        _stylesheetPIHandler.setBaseId(baseId);

        dom2sax.setContentHandler(_stylesheetPIHandler);
        dom2sax.parse();
      } else {
        isource = SAXSource.sourceToInputSource(source);
        baseId = isource.getSystemId();

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        if (_isSecureProcessing) {
          try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
          } catch (final SAXException e) {
          }
        }

        final SAXParser jaxpParser = factory.newSAXParser();

        reader = jaxpParser.getXMLReader();
        if (reader == null) {
          reader = XMLReaderFactory.createXMLReader();
        }

        _stylesheetPIHandler.setBaseId(baseId);
        reader.setContentHandler(_stylesheetPIHandler);
        reader.parse(isource);

      }

      if (_uriResolver != null) {
        _stylesheetPIHandler.setURIResolver(_uriResolver);
      }

    } catch (final StopParseException e) {
      // startElement encountered so do not parse further

    } catch (final ParserConfigurationException e) {

      throw new TransformerConfigurationException("getAssociatedStylesheets failed", e);

    } catch (final SAXException se) {

      throw new TransformerConfigurationException("getAssociatedStylesheets failed", se);

    } catch (final IOException ioe) {
      throw new TransformerConfigurationException("getAssociatedStylesheets failed", ioe);

    }

    return _stylesheetPIHandler.getAssociatedStylesheet();

  }

  /**
   * SAXTransformerFactory implementation. Create a Transformer object that copies the input document to the result.
   * 
   * @return A Transformer object that simply copies the source to the result.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TransformerImpl newTransformer() throws TransformerConfigurationException {
    final TransformerImpl result = new TransformerImpl(new Properties(), _indentNumber, this);
    if (_uriResolver != null) {
      result.setURIResolver(_uriResolver);
    }

    if (_isSecureProcessing) {
      result.setSecureProcessing(true);
    }
    return result;
  }

  /**
   * SAXTransformerFactory implementation. Process the Source into a Templates object, which is a a compiled
   * representation of the source. Note that this method should not be used with XSLTC, as the time-consuming
   * compilation is done for each and every transformation.
   * 
   * @return A Templates object that can be used to create Transformers.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TransformerImpl newTransformer(Source source) throws TransformerConfigurationException {
    final TemplatesImpl templates = newTemplates(source);
    final TransformerImpl transformer = templates.newTransformer();
    if (_uriResolver != null) {
      transformer.setURIResolver(_uriResolver);
    }
    return transformer;
  }

  /**
   * Pass warning messages from the compiler to the error listener
   */
  private void passWarningsToListener(List<ErrorMsg> messages) throws TransformerException {
    if (_errorListener == null || messages == null)
      return;
    // Pass messages to listener, one by one
    final int count = messages.size();
    for (int pos = 0; pos < count; pos++) {
      final ErrorMsg msg = messages.get(pos);
      // TODO Workaround for the TCK failure ErrorListener.errorTests.error001.
      _errorListener.warning(new TransformerConfigurationException(msg.toString()));
    }
  }

  /**
   * Pass error messages from the compiler to the error listener
   */
  private void passErrorsToListener(List<ErrorMsg> messages) {
    try {
      if (_errorListener == null || messages == null)
        return;
      // Pass messages to listener, one by one
      final int count = messages.size();
      for (int pos = 0; pos < count; pos++) {
        final String message = messages.get(pos).toString();
        _errorListener.error(new TransformerException(message));
      }
    } catch (final TransformerException e) {
      // nada
    }
  }

  /**
   * SAXTransformerFactory implementation. Process the Source into a Templates object, which is a a compiled
   * representation of the source.
   * 
   * @param source The input stylesheet - DOMSource not supported!!!
   * @return A Templates object that can be used to create Transformers.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TemplatesImpl newTemplates(Source source) throws TransformerConfigurationException {
    // Create and initialize a stylesheet compiler
    final XSLTC xsltc = new XSLTC();
    if (_debug) {
      xsltc.setDebug(true);
    }

    if (_isSecureProcessing) {
      xsltc.setSecureProcessing(true);
    }
    xsltc.init();

    // Set a document loader (for xsl:include/import) if defined
    if (_uriResolver != null) {
      xsltc.setSourceLoader(this);
    }

    // Set the attributes for translet generation
    Set<Out> outputType = XSLTC.BYTEARRAY_AND_CLASS_FILES;
    if (_generateTranslet) {
      // Set the translet name
      xsltc.setClassName(getTransletBaseName(source));

      if (_destinationDirectory != null) {
        xsltc.setDestDirectory(_destinationDirectory);
      } else {
        final String xslName = getStylesheetFileName(source);
        if (xslName != null) {
          final File xslFile = new File(xslName);
          final String xslDir = xslFile.getParent();

          if (xslDir != null) {
            xsltc.setDestDirectory(xslDir);
          }
        }
      }

      outputType = XSLTC.BYTEARRAY_AND_CLASS_FILES;
    }

    // Compile the stylesheet
    final InputSource input = Util.getInputSource(xsltc, source);
    final byte[][] bytecodes = xsltc.compile(null, input, outputType);
    final String transletName = xsltc.getClassName();

    // Reset the per-session attributes to their default values
    // after each newTemplates() call.
    resetTransientAttributes();

    // Pass compiler warnings to the error listener
    if (_errorListener != this) {
      try {
        passWarningsToListener(xsltc.getWarnings());
      } catch (final TransformerException e) {
        throw new TransformerConfigurationException(e);
      }
    } else {
      xsltc.printWarnings();
    }

    // Check that the transformation went well before returning
    if (bytecodes == null) {

      final ErrorMsg err = new ErrorMsg(Messages.get().jaxpCompileErr());
      final TransformerConfigurationException exc = new TransformerConfigurationException(err.toString());

      // Pass compiler errors to the error listener
      if (_errorListener != null) {
        passErrorsToListener(xsltc.getErrors());

        // As required by TCK 1.2, send a fatalError to the
        // error listener because compilation of the stylesheet
        // failed and no further processing will be possible.
        try {
          _errorListener.fatalError(exc);
        } catch (final TransformerException te) {
          // well, we tried.
        }
      } else {
        xsltc.printErrors();
      }
      throw exc;
    }
    final TemplatesImpl templates = new TemplatesImpl(bytecodes, transletName, xsltc.getOutputProperties(),
        _indentNumber, this);
    // pass uriResolver to templates
    if (_uriResolver != null) {
      templates.setURIResolver(_uriResolver);
    }
    return templates;
  }

  /**
   * SAXTransformerFactory implementation. Get a TemplatesHandler object that can process SAX ContentHandler events into
   * a Templates object.
   * 
   * @return A TemplatesHandler object that can handle SAX events
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TemplatesHandlerImpl newTemplatesHandler() throws TransformerConfigurationException {
    final TemplatesHandlerImpl handler = new TemplatesHandlerImpl(_indentNumber, this);
    if (_uriResolver != null) {
      handler.setURIResolver(_uriResolver);
    }
    return handler;
  }

  /**
   * SAXTransformerFactory implementation. Get a TransformerHandler object that can process SAX ContentHandler events
   * into a Result. This method will return a pure copy transformer.
   * 
   * @return A TransformerHandler object that can handle SAX events
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TransformerHandlerImpl newTransformerHandler() throws TransformerConfigurationException {
    final TransformerImpl transformer = newTransformer();
    if (_uriResolver != null) {
      transformer.setURIResolver(_uriResolver);
    }
    return new TransformerHandlerImpl(transformer);
  }

  /**
   * SAXTransformerFactory implementation. Get a TransformerHandler object that can process SAX ContentHandler events
   * into a Result, based on the transformation instructions specified by the argument.
   * 
   * @param src The source of the transformation instructions.
   * @return A TransformerHandler object that can handle SAX events
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TransformerHandlerImpl newTransformerHandler(Source src) throws TransformerConfigurationException {
    final TransformerImpl transformer = newTransformer(src);
    if (_uriResolver != null) {
      transformer.setURIResolver(_uriResolver);
    }
    return new TransformerHandlerImpl(transformer);
  }

  /**
   * SAXTransformerFactory implementation. Get a TransformerHandler object that can process SAX ContentHandler events
   * into a Result, based on the transformation instructions specified by the argument.
   * 
   * @param templates Represents a pre-processed stylesheet
   * @return A TransformerHandler object that can handle SAX events
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public TransformerHandlerImpl newTransformerHandler(Templates templates) throws TransformerConfigurationException {
    final TransformerImpl transformer = ((TemplatesImpl) templates).newTransformer();
    return new TransformerHandlerImpl(transformer);
  }

  /**
   * SAXTransformerFactory implementation. Create an XMLFilter that uses the given source as the transformation
   * instructions.
   * 
   * @param src The source of the transformation instructions.
   * @return An XMLFilter object, or null if this feature is not supported.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public XMLFilter newXMLFilter(Source src) throws TransformerConfigurationException {
    final Templates templates = newTemplates(src);
    if (templates == null)
      return null;
    return newXMLFilter(templates);
  }

  /**
   * SAXTransformerFactory implementation. Create an XMLFilter that uses the given source as the transformation
   * instructions.
   * 
   * @param templates The source of the transformation instructions.
   * @return An XMLFilter object, or null if this feature is not supported.
   * @throws TransformerConfigurationException TODO
   */
  @Override
  public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
    try {
      return new TrAXFilter(templates);
    } catch (final TransformerConfigurationException e1) {
      if (_errorListener != null) {
        try {
          _errorListener.fatalError(e1);
          return null;
        } catch (final TransformerException e2) {
          new TransformerConfigurationException(e2);
        }
      }
      throw e1;
    }
  }

  /**
   * Receive notification of a recoverable error. The transformer must continue to provide normal parsing events after
   * invoking this method. It should still be possible for the application to process the document through to the end.
   * 
   * @param e The warning information encapsulated in a transformer exception.
   * @throws TransformerException if the application chooses to discontinue the transformation (always does in our
   *         case).
   */
  @Override
  public void error(TransformerException e) throws TransformerException {
    final Throwable wrapped = e.getException();
    if (wrapped != null) {
      System.err
          .println(new ErrorMsg(Messages.get().errorPlusWrappedMsg(e.getMessageAndLocation(), wrapped.getMessage())));
    } else {
      System.err.println(new ErrorMsg(Messages.get().errorMsg(e.getMessageAndLocation())));
    }
    // throw e;
  }

  /**
   * Receive notification of a non-recoverable error. The application must assume that the transformation cannot
   * continue after the Transformer has invoked this method, and should continue (if at all) only to collect addition
   * error messages. In fact, Transformers are free to stop reporting events once this method has been invoked.
   * 
   * @param e warning information encapsulated in a transformer exception.
   * @throws TransformerException if the application chooses to discontinue the transformation (always does in our
   *         case).
   */
  @Override
  public void fatalError(TransformerException e) throws TransformerException {
    final Throwable wrapped = e.getException();
    if (wrapped != null) {
      System.err.println(
          new ErrorMsg(Messages.get().fatalErrPlusWrappedMsg(e.getMessageAndLocation(), wrapped.getMessage())));
    } else {
      System.err.println(new ErrorMsg(Messages.get().fatalErrMsg(e.getMessageAndLocation())));
    }
    throw e;
  }

  /**
   * Receive notification of a warning. Transformers can use this method to report conditions that are not errors or
   * fatal errors. The default behaviour is to take no action. After invoking this method, the Transformer must continue
   * with the transformation. It should still be possible for the application to process the document through to the
   * end.
   * 
   * @param e The warning information encapsulated in a transformer exception.
   * @throws TransformerException if the application chooses to discontinue the transformation (never does in our case).
   */
  @Override
  public void warning(TransformerException e) throws TransformerException {
    final Throwable wrapped = e.getException();
    if (wrapped != null) {
      System.err
          .println(new ErrorMsg(Messages.get().warningPlusWrappedMsg(e.getMessageAndLocation(), wrapped.getMessage())));
    } else {
      System.err.println(new ErrorMsg(Messages.get().warningMsg(e.getMessageAndLocation())));
    }
  }

  /**
   * This method implements XSLTC's SourceLoader interface. It is used to glue a TrAX URIResolver to the XSLTC
   * compiler's Input and Import classes.
   * 
   * @param href The URI of the document to load
   * @param context The URI of the currently loaded document
   * @param xsltc The compiler that resuests the document
   * @return An InputSource with the loaded document
   */
  @Override
  public InputSource loadSource(String href, String context, XSLTC xsltc) {
    try {
      if (_uriResolver != null) {
        final Source source = _uriResolver.resolve(href, context);
        if (source != null)
          return Util.getInputSource(xsltc, source);
      }
    } catch (final TransformerException e) {
      // Falls through
    }
    return null;
  }

  /**
   * Reset the per-session attributes to their default values
   */
  private void resetTransientAttributes() {
    _transletName = DEFAULT_TRANSLET_NAME;
    _destinationDirectory = null;
  }

  /**
   * Return the base class name of the translet. The translet name is resolved using the following rules: 1. if the
   * _transletName attribute is set and its value is not "GregorSamsa", then _transletName is returned. 2. otherwise get
   * the translet name from the base name of the system ID 3. return "GregorSamsa" if the result from step 2 is null.
   * 
   * @param source The input Source
   * @return The name of the translet class
   */
  private String getTransletBaseName(Source source) {
    String transletBaseName = null;
    if (!_transletName.equals(DEFAULT_TRANSLET_NAME))
      return _transletName;
    else {
      final String systemId = source.getSystemId();
      if (systemId != null) {
        String baseName = Util.baseName(systemId);
        if (baseName != null) {
          baseName = Util.noExtName(baseName);
          transletBaseName = Util.toJavaName(baseName);
        }
      }
    }

    return transletBaseName != null ? transletBaseName : DEFAULT_TRANSLET_NAME;
  }

  /**
   * Return the local file name from the systemId of the Source object
   * 
   * @param source The Source
   * @return The file name in the local filesystem, or null if the systemId does not represent a local file.
   */
  private String getStylesheetFileName(Source source) {
    final String systemId = source.getSystemId();
    if (systemId != null) {
      final File file = new File(systemId);
      if (file.exists())
        return systemId;
      else {
        URL url = null;
        try {
          url = new URL(systemId);
        } catch (final MalformedURLException e) {
          return null;
        }

        if ("file".equals(url.getProtocol()))
          return url.getFile();
        else
          return null;
      }
    } else
      return null;
  }

  /**
   * Returns the Class object the provides the XSLTC DTM Manager service.
   * 
   * @return TODO
   */
  protected Class<?> getDTMManagerClass() {
    return m_DTMManagerClass;
  }
}
