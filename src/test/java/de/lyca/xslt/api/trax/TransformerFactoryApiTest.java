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
 * $Id: TransformerFactoryAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * TransformerFactoryAPITest.java
 *
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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import de.lyca.xml.utils.DefaultErrorHandler;
import de.lyca.xslt.ResourceUtils;

/**
 * API Coverage test for TransformerFactory class of TRAX.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class TransformerFactoryApiTest {

  private static final String PACKAGE = '/' + TransformerFactoryApiTest.class.getPackage().getName().replace('.', '/')
          + "/";

  /** System default TransformerFactory class name */
  private static final String DEFAULT_TRANSFORMER_FACTORY_CLASS_NAME = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
  /** System property name, from TransformerFactory */
  private static final String DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME = "javax.xml.transform.TransformerFactory";

  /** System property name for Lyca Xalan-J impl. */
  private static final String XALAN_TRANSFORMER_FACTORY_CLASS_NAME = "de.lyca.xalan.xsltc.trax.TransformerFactoryImpl";
  private static final String XALAN_TRANSFORMER_CLASS_NAME = "de.lyca.xalan.xsltc.trax.TransformerImpl";
  private static final String XALAN_TEMPLATES_CLASS_NAME = "de.lyca.xalan.xsltc.trax.TemplatesImpl";

  @Test
  public void testDefaultTransformerFactory() throws Exception {
    final String transformerFactoryClassName = System.getProperty(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME);
    final TransformerFactory factory = TransformerFactory.newInstance();
    if (transformerFactoryClassName == null) {
      Assert.assertEquals(XALAN_TRANSFORMER_FACTORY_CLASS_NAME, factory.getClass().getName());
    } else {
      Assert.assertEquals(transformerFactoryClassName, factory.getClass().getName());
    }
  }

  @Test
  public void testSystemPropertyTransformerFactory() throws Exception {
    try {
      System.setProperty(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME, DEFAULT_TRANSFORMER_FACTORY_CLASS_NAME);
      final TransformerFactory factory = TransformerFactory.newInstance();
      Assert.assertEquals(DEFAULT_TRANSFORMER_FACTORY_CLASS_NAME, factory.getClass().getName());
    } finally {
      System.getProperties().remove(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME);
    }
  }

  @Test(expected = TransformerFactoryConfigurationError.class)
  public void testNonExistantClassTransformerFactory() throws Exception {
    try {
      System.setProperty(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME, "this.class.does.not.exist");
      TransformerFactory.newInstance();
    } finally {
      System.getProperties().remove(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME);
    }
  }

  @Test(expected = TransformerFactoryConfigurationError.class)
  public void testWrongClassTransformerFactory() throws Exception {
    try {
      System.setProperty(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME, "java.lang.String");
      TransformerFactory.newInstance();
    } finally {
      System.getProperties().remove(DEFAULT_TRANSFORMER_FACTORY_PROPERTY_NAME);
    }
  }

  @Test
  public void testNewIdentityTransformer() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(new DefaultErrorHandler());
    final Transformer identityTransformer = factory.newTransformer();
    Assert.assertEquals(XALAN_TRANSFORMER_CLASS_NAME, identityTransformer.getClass().getName());
  }

  @Test
  public void testNewSourceTransformer() throws Exception {
    final String name = PACKAGE + "identity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(new DefaultErrorHandler());
    final Source xsl = getSource(name + ".xsl");
    final Transformer sourceTransformer = factory.newTransformer(xsl);
    Assert.assertEquals(XALAN_TRANSFORMER_CLASS_NAME, sourceTransformer.getClass().getName());
  }

  @Test
  public void testNewTemplates() throws Exception {
    final String name = PACKAGE + "identity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(new DefaultErrorHandler());
    Assert.assertEquals(true, factory.getFeature(StreamSource.FEATURE));
    final Source xsl = getSource(name + ".xsl");
    final Templates templates = factory.newTemplates(xsl);
    Assert.assertEquals(XALAN_TEMPLATES_CLASS_NAME, templates.getClass().getName());
  }

  @Test
  public void testAssociatedStylesheet() throws Exception {
    final String name = PACKAGE + "embeddedIdentity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(new DefaultErrorHandler());
    Assert.assertEquals(true, factory.getFeature(StreamSource.FEATURE));
    final String media = null;
    final String title = null;
    final String charset = null;

    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testAssociatedStylesheet.out");
    try (OutputStream outputStream = Files.newOutputStream(outPath)) {
      final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
      final Transformer transformer = factory.newTransformer(stylesheet);
      transformer.setErrorListener(new DefaultErrorHandler());
      transformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
      final String expected = readResource(name + ".out", UTF_8);
      final byte[] encoded = Files.readAllBytes(outPath);
      final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
      Assert.assertEquals(expected, generated);
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testErrorListener() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    Assert.assertEquals(XALAN_TRANSFORMER_FACTORY_CLASS_NAME, factory.getErrorListener().getClass().getName());
    factory.setErrorListener(new DefaultErrorHandler());
    Assert.assertEquals(DefaultErrorHandler.class, factory.getErrorListener().getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullErrorListener() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(null);
  }

  @Test
  public void testURIResolver() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    Assert.assertEquals(null, factory.getURIResolver());
    factory.setURIResolver(new URIResolver() {
      @Override
      public Source resolve(String href, String base) throws TransformerException {
        return null;
      }
    });
    Assert.assertEquals(TransformerFactoryApiTest.class.getName() + "$1", factory.getURIResolver().getClass().getName());
    factory.setURIResolver(null);
    Assert.assertEquals(null, factory.getURIResolver());
  }

  @Test
  public void testAssociatedStylesheet2() throws Exception {
    final String name = PACKAGE + "TransformerFactoryAPIModern";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    final DocumentBuilder db = dbf.newDocumentBuilder();
    final Document document = db.parse(TransformerFactoryApiTest.class.getResourceAsStream(name + ".xml"));
    final DOMSource domSource = new DOMSource(document);
    Source source = factory.getAssociatedStylesheet(domSource, "screen", "Modern", null);
    Assert.assertEquals(null, source);
    domSource.setSystemId(TransformerFactoryApiTest.class.getResource(name + ".xml").toURI().toASCIIString());
    source = factory.getAssociatedStylesheet(domSource, "screen", "Modern", null);
    Assert.assertEquals(null, source);
  }

  @Test
  public void testAssociatedStylesheet3() throws Exception {
    final String name = PACKAGE + "TransformerFactoryAPIModern";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Source streamSource = ResourceUtils.getSource(name + ".xml");
    Source source = factory.getAssociatedStylesheet(streamSource, "screen", "Modern", null);
    Assert.assertEquals(null, source);
    streamSource.setSystemId(TransformerFactoryApiTest.class.getResource(name + ".xml").toURI().toASCIIString());
    source = factory.getAssociatedStylesheet(streamSource, "screen", "Modern", null);
    Assert.assertEquals(null, source);
  }

  @Test
  public void testGetNonExistantFeature() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    Assert.assertFalse(factory.getFeature("fnord:this/feature/does/not/exist"));
  }

  @Test(expected = TransformerConfigurationException.class)
  public void testSetNonExistantFeature() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature("fnord:this/feature/does/not/exist", true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNonExistantAttribute() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute("fnord:this/feature/does/not/exist", "on");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonExistantAttribute() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.getAttribute("fnord:this/feature/does/not/exist");
  }

  @Test
  public void testGetFeature() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    Assert.assertEquals(true, factory.getFeature(StreamSource.FEATURE));
    Assert.assertEquals(true, factory.getFeature(StreamResult.FEATURE));
  }

  @Test
  public void testSetFeature() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Assert.assertEquals(true, factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
    Assert.assertEquals(false, factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
  }

  @Test(expected = NullPointerException.class)
  public void testSetFeatureNull() throws Exception {
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(null, true);
  }

}
