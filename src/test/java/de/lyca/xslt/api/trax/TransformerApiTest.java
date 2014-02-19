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
 * $Id: TransformerAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xml.utils.DefaultErrorHandler;

/**
 * Basic API coverage test for the Transformer class of TRAX. This test focuses
 * on coverage testing for the API's, and very brief functional testing. Also
 * see tests in the trax\sax, trax\dom, and trax\stream directories for specific
 * coverage of Transformer API's in those usage cases.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class TransformerApiTest {

  private static final String PACKAGE = '/' + TransformerFactoryApiTest.class.getPackage().getName().replace('.', '/')
          + "/";

  @Test
  public void testGetNotExistingParam() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    Assert.assertEquals(null, identityTransformer.getParameter("This-param-does-not-exist"));
  }

  @Test
  public void testSetGetParam() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    identityTransformer.setParameter("foo", "bar");
    Assert.assertEquals("bar", identityTransformer.getParameter("foo"));
  }

  @Test
  public void testResetParameter() throws Exception {
    final String name = PACKAGE + "TransformerAPIParam";
    final Templates templates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer transformer = templates.newTransformer();
    Assert.assertEquals(null, transformer.getParameter("This-param-does-not-exist"));
    Assert.assertEquals(null, transformer.getParameter("param1s"));
    transformer.setParameter("param1s", "new value1s");
    Assert.assertEquals("new value1s", transformer.getParameter("param1s"));
    transformer.setParameter("param3s", 1234);
    Assert.assertEquals(1234, transformer.getParameter("param3s"));
    transformer.setParameter("param3s", 99);
    Assert.assertEquals(99, transformer.getParameter("param3s"));
    transformer.setParameter("param3s", "new value3s");
    Assert.assertEquals("new value3s", transformer.getParameter("param3s"));
  }

  @Test
  public void testTransformWithParameters() throws Exception {
    final String name = PACKAGE + "TransformerAPIParam";
    final Templates templates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer transformer = templates.newTransformer();
    transformer.setParameter("param1s", "'test-param-1s'"); // note single
                                                            // quotes
    transformer.setParameter("param1n", new Integer(1234));
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testTransformWithParameters.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        transformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
    Assert.assertEquals("'test-param-1s'", transformer.getParameter("param1s"));
    Assert.assertEquals(1234, transformer.getParameter("param1n"));
  }

  @Test
  public void testDefaultTransformerOutputProperties() throws Exception {
    final Transformer defaultTransformer = TransformerFactory.newInstance().newTransformer();
    final Properties defaultProperties = defaultTransformer.getOutputProperties();
    Assert.assertEquals(null, defaultProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, defaultProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, defaultProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-8", defaultProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("no", defaultProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/xml", defaultProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("xml", defaultProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("no", defaultProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("no", defaultProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("1.0", defaultProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals(null, defaultProperties.get(OutputKeys.VERSION));
    defaultTransformer.setOutputProperty(OutputKeys.METHOD, "text");
    Assert.assertEquals("text", defaultTransformer.getOutputProperty(OutputKeys.METHOD));
  }

  @Test
  public void testIdentityTransformerOutputProperties() throws Exception {
    final String name = PACKAGE + "identity";
    final Templates identityTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer identityTransformer = identityTemplates.newTransformer();
    final Properties identityProperties = identityTransformer.getOutputProperties();
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-8", identityProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/xml", identityProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("xml", identityProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("1.0", identityProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.VERSION));
    identityTransformer.setOutputProperty(OutputKeys.METHOD, "text");
    Assert.assertEquals("text", identityTransformer.getOutputProperty(OutputKeys.METHOD));
  }

  @Test
  public void testHtmlTransformerOutputProperties() throws Exception {
    final String name = PACKAGE + "TransformerAPIHTMLFormat";
    final Templates identityTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer identityTransformer = identityTemplates.newTransformer();
    final Properties identityProperties = identityTransformer.getOutputProperties();
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-8", identityProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("yes", identityProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/test/xml", identityProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("html", identityProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("yes", identityProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("4.0", identityProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.VERSION));
    identityTransformer.setOutputProperty(OutputKeys.METHOD, "text");
    Assert.assertEquals("text", identityTransformer.getOutputProperty(OutputKeys.METHOD));
  }

  @Test
  public void testOutputTemplateOutputProperties() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Properties outputProperties = outputTemplates.getOutputProperties();
    Assert.assertEquals("cdataHere ", outputProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals("cdataHere ", outputProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals("this-is-doctype-public", outputProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals("this-is-doctype-public", outputProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals("this-is-doctype-system", outputProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("this-is-doctype-system", outputProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-16", outputProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals("UTF-16", outputProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals("yes", outputProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/test/xml", outputProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("text/test/xml", outputProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("xml", outputProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals("xml", outputProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("yes", outputProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals("yes", outputProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("123.45", outputProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals("123.45", outputProperties.get(OutputKeys.VERSION));
  }

  @Test
  public void testOutputTransformerOutputProperties() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    final Properties outputProperties = outputTransformer.getOutputProperties();
    Assert.assertEquals("cdataHere ", outputProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals("this-is-doctype-public", outputProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals("this-is-doctype-system", outputProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-16", outputProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/test/xml", outputProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("xml", outputProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("yes", outputProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("123.45", outputProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals(null, outputProperties.get(OutputKeys.VERSION));
    outputTransformer.setOutputProperty(OutputKeys.METHOD, "text");
    Assert.assertEquals("text", outputTransformer.getOutputProperty(OutputKeys.METHOD));
  }

  @Test
  public void testOutputTransformerTransform() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testOutputTransformerTransform.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        outputTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + "UTF16.out", StandardCharsets.UTF_16);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_16.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testOutputTransformerTransformUTF8() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    outputTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testOutputTransformerTransform.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        outputTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + "UTF8.out", StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testDefaultTransformer() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Transformer identityTransformer = factory.newTransformer();
    final Properties identityProperties = identityTransformer.getOutputProperties();
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_PUBLIC));
    Assert.assertEquals(null, identityProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.DOCTYPE_SYSTEM));
    Assert.assertEquals("UTF-8", identityProperties.getProperty(OutputKeys.ENCODING));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.ENCODING));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.INDENT));
    Assert.assertEquals("text/xml", identityProperties.getProperty(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.MEDIA_TYPE));
    Assert.assertEquals("xml", identityProperties.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.METHOD));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("no", identityProperties.getProperty(OutputKeys.STANDALONE));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.STANDALONE));
    Assert.assertEquals("1.0", identityProperties.getProperty(OutputKeys.VERSION));
    Assert.assertEquals(null, identityProperties.get(OutputKeys.VERSION));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    identityTransformer.getOutputProperty("bogus-name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty2() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    identityTransformer.getOutputProperty("bogus-{name}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty3() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    identityTransformer.getOutputProperty("{bogus-name");
  }

  @Test
  public void testUnknownOutputProperty4() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    Assert.assertEquals(null, identityTransformer.getOutputProperty("{some-namespace}bogus-name"));
  }

  @Test
  public void testUnknownOutputProperty5() throws Exception {
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    Assert.assertEquals(null, identityTransformer.getOutputProperty("{just-some-namespace}"));
  }

  @Test
  public void testUnknownOutputProperty6() throws Exception {
    // TODO is this correct ?
    final Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
    Assert.assertEquals(null, identityTransformer.getOutputProperty("{}no-namespace-at-all"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty7() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    outputTransformer.getOutputProperty("bogus-name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty8() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    outputTransformer.getOutputProperty("bogus-{name}");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownOutputProperty9() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    outputTransformer.getOutputProperty("{bogus-name");
  }

  @Test
  public void testUnknownOutputProperty10() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    Assert.assertEquals(null, outputTransformer.getOutputProperty("{some-namespace}bogus-name"));
  }

  @Test
  public void testUnknownOutputProperty11() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    Assert.assertEquals(null, outputTransformer.getOutputProperty("{just-some-namespace}"));
  }

  @Test
  public void testUnknownOutputProperty12() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    Assert.assertEquals(null, outputTransformer.getOutputProperty("{}no-namespace-at-all"));
  }

  @Test
  public void testIdentityMultipleTransforms() throws Exception {
    final String name = PACKAGE + "identity";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    for (int i = 0; i < 3; i++) {
      final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "multipleTransforms" + i + "1.out");
      try {
        try (OutputStream outputStream = Files.newOutputStream(outPath)) {
          outputTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
          final String expected = readResource(name + ".out", StandardCharsets.UTF_8);
          final byte[] encoded = Files.readAllBytes(outPath);
          final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
          Assert.assertEquals(expected, generated);
        }
      } finally {
        Files.delete(outPath);
      }
    }
  }

  @Test
  public void testMultipleTransforms() throws Exception {
    final String name1 = PACKAGE + "TransformerAPIVar";
    final String name2 = PACKAGE + "TransformerAPIVar2";
    final Templates outputTemplates = TransformerFactory.newInstance().newTemplates(getSource(name1 + ".xsl"));
    final Transformer outputTransformer = outputTemplates.newTransformer();
    for (int i = 0; i < 5; i++) {
      String name = name1;
      if (i == 4) {
        name = name2;
      }
      final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "multipleTransforms" + i + ".out");
      try {
        try (OutputStream outputStream = Files.newOutputStream(outPath)) {
          outputTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
          final String expected = readResource(name + ".out", StandardCharsets.UTF_8);
          final byte[] encoded = Files.readAllBytes(outPath);
          final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
          Assert.assertEquals(expected, generated);
        }
      } finally {
        Files.delete(outPath);
      }
      if (i == 1 || i == 3) {
        outputTransformer.reset();
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErrorListener() throws Exception {
    final String name = PACKAGE + "identity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Templates identityTemplates = factory.newTemplates(getSource(name + ".xsl"));
    final Transformer identityTransformer = identityTemplates.newTransformer();
    Assert.assertEquals(identityTransformer.getClass(), identityTransformer.getErrorListener().getClass());
    identityTransformer.setErrorListener(new DefaultErrorHandler());
    Assert.assertEquals(DefaultErrorHandler.class, identityTransformer.getErrorListener().getClass());
    identityTransformer.setErrorListener(null);
  }

  @Test
  public void testUriResolver() throws Exception {
    final String name = PACKAGE + "identity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Templates identityTemplates = factory.newTemplates(getSource(name + ".xsl"));
    final Transformer identityTransformer = identityTemplates.newTransformer();
    Assert.assertEquals(null, identityTransformer.getURIResolver());
    identityTransformer.setURIResolver(new URIResolver() {
      @Override
      public Source resolve(String href, String base) throws TransformerException {
        return null;
      }
    });
    Assert.assertEquals(TransformerApiTest.class.getName() + "$1", identityTransformer.getURIResolver().getClass()
            .getName());
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "identity.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        identityTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", StandardCharsets.UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
    identityTransformer.setURIResolver(null);
    Assert.assertEquals(null, identityTransformer.getURIResolver());
  }

}
