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
 * $Id: ExamplesTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * ExamplesTest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getResourceFile;
import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.getSystemID;
import static de.lyca.xslt.ResourceUtils.readResource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import de.lyca.xml.serializer.OutputPropertiesFactory;
import de.lyca.xml.serializer.Serializer;
import de.lyca.xml.serializer.SerializerFactory;

/**
 * Test version of xml-xalan/java/samples/trax/Examples.java.
 * <p>
 * This file is essentially copied from the Examples for TRAX, or
 * javax.xml.transform; however this file actually validates most output and
 * behavior for correctness. Hopefully, we can get this file updated at the same
 * time as Examples.java in the future.
 * </p>
 * <p>
 * In general, I merely copied each method from Examples.java and made minor
 * updates (try...catch within methods, call to reporter.logBlah to output
 * messages, etc.) then added validation of actual output files. Note that each
 * method validates it's output by calling fileChecker.check(...) explicitly, so
 * we can't change the input files without carefully changing the gold files for
 * each area.
 * </p>
 * <p>
 * Note that some tests may use NOT_DEFINED for their gold file if we haven't
 * yet validated what the 'correct' output should be for each case - these
 * should be updated as time permits.
 * </p>
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Original: scott_boag@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class ExamplesTest {

  private static final String PACKAGE = '/' + ExamplesTest.class.getPackage().getName().replace('.', '/') + "/";

  /**
   * Show the simplest possible transformation from system id to output stream.
   */
  @Test
  public void exampleSimple1() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_1.out";
    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer(getSource(xslName));
    // No need to setSystemId, the transformer can get it from the URL

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleSimple1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        // Transform the source XML
        transformer.transform(getSource(xmlName), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show the simplest possible transformation from File to a File.
   */
  @Test
  public void exampleSimple2() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_2.out";

    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer(getSource(xslName));
    // No need to setSystemId, the transformer can get it from the File

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testOutputTransformerTransform.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        // Transform the source XML
        transformer.transform(getSource(xmlName), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

  }

  /**
   * Show simple transformation from input stream to output stream.
   */
  @Test
  public void exampleFromStream() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_3.out";
    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance();

    final InputStream xslIS = new BufferedInputStream(new FileInputStream(getResourceFile(xslName)));
    final StreamSource xslSource = new StreamSource(xslIS);
    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xslSource.setSystemId(getSystemID(xslName));

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer(xslSource);

    final InputStream xmlIS = new BufferedInputStream(new FileInputStream(getResourceFile(xmlName)));
    final StreamSource xmlSource = new StreamSource(xmlIS);
    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xmlSource.setSystemId(getSystemID(xmlName));

    // Transform the source XML to System.out.
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testOutputTransformerTransform.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        // Transform the source XML
        transformer.transform(xmlSource, new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show simple transformation from reader to output stream. In general this
   * use case is discouraged, since the XML encoding can not be processed.
   */
  @Test
  public void exampleFromReader() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_4.out";
    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // Note that in this case the XML encoding can not be processed!
    final Reader xslReader = new BufferedReader(new InputStreamReader(new FileInputStream(getResourceFile(xslName)),
            "UTF-8")); // @DEM
    // Reader xslReader = new BufferedReader(new FileReader(xslID)); @DEM
    final StreamSource xslSource = new StreamSource(xslReader);
    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xslSource.setSystemId(getSystemID(xslName));

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer(xslSource);

    // Note that in this case the XML encoding can not be processed!
    final Reader xmlReader = new BufferedReader(new InputStreamReader(new FileInputStream(getResourceFile(xmlName)),
            "UTF-8")); // @DEM
    // Reader xmlReader = new BufferedReader(new FileReader(sourceID)); @DEM
    final StreamSource xmlSource = new StreamSource(xmlReader);
    // Note that if we don't do this, relative URLs can not be resolved
    // correctly!
    xmlSource.setSystemId(getSystemID(xmlName));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testOutputTransformerTransform.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        // Transform the source XML
        transformer.transform(xmlSource, new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show the simplest possible transformation from system id to output stream.
   */
  @Test
  public void exampleUseTemplatesObj() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlFooName = PACKAGE + "xml/foo.xml";
    final String xmlBazName = PACKAGE + "xml/baz.xml";
    final String outFooName = PACKAGE + "ExamplesTest_5.out";
    final String outBazName = PACKAGE + "ExamplesTest_6.out";

    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // Create a templates object, which is the processed,
    // thread-safe representation of the stylesheet.
    final Templates templates = tfactory.newTemplates(new StreamSource(getSystemID(xslName)));

    // Illustrate the fact that you can make multiple transformers
    // from the same template.
    final Transformer transformer1 = templates.newTransformer();
    final Transformer transformer2 = templates.newTransformer();

    final Path outPathFoo = Paths.get(System.getProperty("java.io.tmpdir"), "exampleUseTemplatesObjFoo.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPathFoo)) {
        // Transform the source XML
        transformer1.transform(new StreamSource(getSystemID(xmlFooName)), new StreamResult(outputStream));
        final String expected = readResource(outFooName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPathFoo);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPathFoo);
    }

    final Path outPathBaz = Paths.get(System.getProperty("java.io.tmpdir"), "exampleUseTemplatesObjBaz.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPathBaz)) {
        // Transform the source XML
        transformer2.transform(new StreamSource(getSystemID(xmlBazName)), new StreamResult(outputStream));
        final String expected = readResource(outBazName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPathBaz);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPathBaz);
    }
  }

  /**
   * Show the Transformer using SAX events in and SAX events out.
   */
  @Test
  public void exampleContentHandlerToContentHandler() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_7.out";

    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // If so, we can safely cast.
    final SAXTransformerFactory stfactory = (SAXTransformerFactory) tfactory;

    // A TransformerHandler is a ContentHandler that will listen for
    // SAX events, and transform them to the result.
    final TransformerHandler handler = stfactory.newTransformerHandler(new StreamSource(getSystemID(xslName)));

    // Set the result handling to be a serialization to the file output stream.
    final Serializer serializer = SerializerFactory.getSerializer(OutputPropertiesFactory
            .getDefaultMethodProperties("xml"));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleContentHandlerToContentHandler.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        serializer.setOutputStream(outputStream);
        final Result result = new SAXResult(serializer.asContentHandler());
        handler.setResult(result);

        // Use JAXP1.1
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final SAXParser jaxpParser = factory.newSAXParser();
        // Create a reader, and set it's content handler to be the
        // TransformerHandler.
        final XMLReader reader = jaxpParser.getXMLReader();
        reader.setContentHandler(handler);

        // It's a good idea for the parser to send lexical events.
        // The TransformerHandler is also a LexicalHandler.
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

        // Parse the source XML, and send the parse events to the
        // TransformerHandler.
        reader.parse(getSystemID(xmlName));

        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

  }

  /**
   * Show the Transformer as a SAX2 XMLReader. An XMLFilter obtained from
   * newXMLFilter should act as a transforming XMLReader if setParent is not
   * called. Internally, an XMLReader is created as the parent for the
   * XMLFilter.
   */
  @Test
  public void exampleXMLReader() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_8.out";
    final TransformerFactory tfactory = TransformerFactory.newInstance();
    final XMLReader reader = ((SAXTransformerFactory) tfactory).newXMLFilter(new StreamSource(getSystemID(xslName)));

    // Set the result handling to be a serialization to the file output stream.
    final Serializer serializer = SerializerFactory.getSerializer(OutputPropertiesFactory
            .getDefaultMethodProperties("xml"));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleXMLReader.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        serializer.setOutputStream(outputStream);
        reader.setContentHandler(serializer.asContentHandler());
        reader.parse(new InputSource(getSystemID(xmlName)));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show the Transformer as a simple XMLFilter. This is pretty similar to
   * exampleXMLReader, except that here the parent XMLReader is created by the
   * caller, instead of automatically within the XMLFilter. This gives the
   * caller more direct control over the parent reader.
   */
  @Test
  public void exampleXMLFilter() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_9.out";
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    // Use JAXP1.1
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware(true);
    final SAXParser saxParser = saxParserFactory.newSAXParser();
    final XMLReader xmlReader = saxParser.getXMLReader();

    // Set the result handling to be a serialization to the file output stream.
    final Serializer serializer = SerializerFactory.getSerializer(OutputPropertiesFactory
            .getDefaultMethodProperties("xml"));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleXMLFilter.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        serializer.setOutputStream(outputStream);
        xmlReader.setContentHandler(serializer.asContentHandler());
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlReader.setFeature("http://apache.org/xml/features/validation/dynamic", true);

        final XMLFilter filter = ((SAXTransformerFactory) transformerFactory).newXMLFilter(new StreamSource(
                getSystemID(xslName)));
        filter.setParent(xmlReader);

        // Now, when you call transformer.parse, it will set itself as
        // the content handler for the parser object (it's "parent"), and
        // will then call the parse method on the parser.
        filter.parse(new InputSource(getSystemID(xmlName)));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * This example shows how to chain events from one Transformer to another
   * transformer, using the Transformer as a SAX2 XMLFilter/XMLReader.
   */
  @Test
  public void exampleXMLFilterChain() throws Exception {
    final String xslName1 = PACKAGE + "xsl/foo.xsl";
    final String xslName2 = PACKAGE + "xsl/foo2.xsl";
    final String xslName3 = PACKAGE + "xsl/foo3.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_10.out";

    final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
    // Use JAXP1.1 ( if possible )
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    final SAXParser jaxpParser = factory.newSAXParser();
    final XMLReader reader = jaxpParser.getXMLReader();

    final XMLFilter filter1 = stf.newXMLFilter(new StreamSource(getSystemID(xslName1)));
    final XMLFilter filter2 = stf.newXMLFilter(new StreamSource(getSystemID(xslName2)));
    final XMLFilter filter3 = stf.newXMLFilter(new StreamSource(getSystemID(xslName3)));

    // transformer1 will use a SAX parser as it's reader.
    filter1.setParent(reader);
    // transformer2 will use transformer1 as it's reader.
    filter2.setParent(filter1);
    // transform3 will use transform2 as it's reader.
    filter3.setParent(filter2);

    // Set the result handling to be a serialization to the file output stream.
    final Serializer serializer = SerializerFactory.getSerializer(OutputPropertiesFactory
            .getDefaultMethodProperties("xml"));
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleXMLFilter.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        serializer.setOutputStream(outputStream);
        reader.setContentHandler(serializer.asContentHandler());
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);

        filter3.setContentHandler(serializer.asContentHandler());

        // Now, when you call transformer3 to parse, it will set
        // itself as the ContentHandler for transform2, and
        // call transform2.parse, which will set itself as the
        // content handler for transform1, and call transform1.parse,
        // which will set itself as the content listener for the
        // SAX parser, and call parser.parse(new InputSource(fooFile.xmlName)).
        filter3.parse(new InputSource(getSystemID(xmlName)));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show how to transform a DOM tree into another DOM tree. This uses the
   * javax.xml.parsers to parse an XML file into a DOM, and create an output
   * DOM.
   */
  @Test
  public void exampleDOM2DOM() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_11.out";

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    final Node xslNode = documentBuilder.parse(new InputSource(getSystemID(xslName)));
    final DOMSource dsource = new DOMSource(xslNode);
    // If we don't do this, the transformer won't know how to
    // resolve relative URLs in the stylesheet.
    dsource.setSystemId(getSystemID(xslName));

    final Templates templates = transformerFactory.newTemplates(dsource);

    final Transformer transformer = templates.newTransformer();
    final Node xmlNode = documentBuilder.parse(new InputSource(getSystemID(xmlName)));

    final Document outNode = documentBuilder.newDocument();
    transformer.transform(new DOMSource(xmlNode), new DOMResult(outNode));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleDOM2DOM.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        final Transformer identityTransformer = transformerFactory.newTransformer();
        identityTransformer.transform(new DOMSource(outNode), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * This shows how to set a parameter for use by the templates. Use two
   * transformers to show that different parameters may be set on different
   * transformers.
   */
  @Test
  public void exampleParam() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName1 = PACKAGE + "ExamplesTest_12.out";
    final String outName2 = PACKAGE + "ExamplesTest_13.out";
    final TransformerFactory tfactory = TransformerFactory.newInstance();
    final Templates templates = tfactory.newTemplates(new StreamSource(getSystemID(xslName)));

    final Transformer transformer1 = templates.newTransformer();
    transformer1.setParameter("a-param", "hello to you!");
    final Path outPath1 = Paths.get(System.getProperty("java.io.tmpdir"), "exampleParam1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath1)) {
        transformer1.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName1, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath1);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath1);
    }

    final Transformer transformer2 = templates.newTransformer();
    transformer2.setOutputProperty(OutputKeys.INDENT, "yes");
    final Path outPath2 = Paths.get(System.getProperty("java.io.tmpdir"), "exampleParam2.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath2)) {
        transformer2.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName2, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath2);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath2);
    }
  }

  /**
   * Show the that a transformer can be reused, and show resetting a parameter
   * on the transformer.
   */
  @Test
  public void exampleTransformerReuse() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName1 = PACKAGE + "ExamplesTest_14.out";
    final String outName2 = PACKAGE + "ExamplesTest_15.out";
    // Create a transform factory instance.
    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // Create a transformer for the stylesheet.
    final Transformer transformer = tfactory.newTransformer(new StreamSource(getSystemID(xslName)));
    transformer.setParameter("a-param", "hello to you!");
    final Path outPath1 = Paths.get(System.getProperty("java.io.tmpdir"), "exampleParam1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath1)) {
        transformer.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName1, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath1);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath1);
    }

    transformer.setParameter("a-param", "hello to me!");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    final Path outPath2 = Paths.get(System.getProperty("java.io.tmpdir"), "exampleParam2.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath2)) {
        transformer.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName2, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath2);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath2);
    }
  }

  /**
   * Show how to override output properties.
   */
  @Test
  public void exampleOutputProperties() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_16.out";
    final TransformerFactory tfactory = TransformerFactory.newInstance();
    final Templates templates = tfactory.newTemplates(new StreamSource(getSystemID(xslName)));
    final Properties outputProperties = templates.getOutputProperties();
    outputProperties.put(OutputKeys.INDENT, "yes");

    final Transformer transformer = templates.newTransformer();
    transformer.setOutputProperties(outputProperties);

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleOutputProperties.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        transformer.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show how to get stylesheets that are associated with a given xml document
   * via the xml-stylesheet PI (see http://www.w3.org/TR/xml-stylesheet/).
   */
  @Test
  public void exampleUseAssociated() throws Exception {
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_17.out";
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) transformerFactory;
    final Source sources = saxTransformerFactory.getAssociatedStylesheet(new StreamSource(getSystemID(xmlName)), null,
            null, null);

    final Transformer transformer = transformerFactory.newTransformer(sources);

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleOutputProperties.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        transformer.transform(new StreamSource(getSystemID(xmlName)), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  /**
   * Show the Transformer using SAX events in and DOM nodes out.
   */
  @Test
  public void exampleContentHandler2DOM() throws Exception {
    final String xslName = PACKAGE + "xsl/foo.xsl";
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_18.out";

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) transformerFactory;

    // Create an Document node as the root for the output.
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Document outNode = documentBuilder.newDocument();

    // Create a ContentHandler that can liston to SAX events and transform the
    // output to DOM nodes.
    final TransformerHandler handler = saxTransformerFactory.newTransformerHandler(new StreamSource(
            getSystemID(xslName)));
    handler.setResult(new DOMResult(outNode));

    // Create a reader and set it's ContentHandler to be the transformer.
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    final XMLReader reader = factory.newSAXParser().getXMLReader();

    reader.setContentHandler(handler);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

    // Send the SAX events from the parser to the transformer, and thus to the
    // DOM tree.
    reader.parse(getSystemID(xmlName));

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleContentHandler2DOM.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        final Transformer identityTransformer = transformerFactory.newTransformer();
        identityTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        identityTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        identityTransformer.transform(new DOMSource(outNode), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

  }

  /**
   * A fuller example showing how the TrAX interface can be used to serialize a
   * DOM tree.
   */
  @Test
  public void exampleAsSerializer() throws Exception {
    final String xmlName = PACKAGE + "xml/foo.xml";
    final String outName = PACKAGE + "ExamplesTest_19.out";
    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    final Node doc = docBuilder.parse(new InputSource(getSystemID(xmlName)));

    final TransformerFactory tfactory = TransformerFactory.newInstance();

    // This creates a transformer that does a simple identity transform,
    // and thus can be used for all intents and purposes as a serializer.
    final Transformer transformer = tfactory.newTransformer();

    final Properties properties = new Properties();
    properties.put("method", "html");
    properties.put("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperties(properties);

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "exampleAsSerializer.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        final String expected = readResource(outName, StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

}
