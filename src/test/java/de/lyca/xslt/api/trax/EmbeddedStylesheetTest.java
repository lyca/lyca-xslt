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
 * $Id: EmbeddedStylesheetTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * EmbeddedStylesheetTest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getResourcePath;
import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test behavior of various kinds of embedded stylesheets.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class EmbeddedStylesheetTest {

  private static final String PACKAGE = '/' + EmbeddedStylesheetTest.class.getPackage().getName().replace('.', '/')
          + "/";

  @Test
  public void testEmbeddedIdentity() throws Exception {
    final String name = PACKAGE + "embeddedIdentity";
    final String other = PACKAGE + "SystemIdTest";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

    // Verify the stylesheet you get from an embedded source can be reused
    embedTransformer = embedTemplates.newTransformer();
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

    // Verify the transformer itself can be reused on a different document
    embedTransformer = embedTemplates.newTransformer();
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(other + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(other + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }

  }

  @Test
  public void testEmbeddedFragment() throws Exception {
    final String name = PACKAGE + "EmbeddedFragment";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testRelativeEmbeddedOneLevelUp() throws Exception {
    final String name = PACKAGE + "EmbeddedRelative";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testRelativeEmbeddedOneLevelUp.out");
    final Path current = getResourcePath(name + ".xml");
    final File file = current.toFile();
    // Path resolving one level up
    final String systemId = current.getParent().getParent().toUri().toASCIIString();
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        final Source baseSource = new StreamSource(new FileInputStream(file));
        baseSource.setSystemId(systemId);
        final Source stylesheet = factory.getAssociatedStylesheet(baseSource, media, title, charset);
        final Templates embedTemplates = factory.newTemplates(stylesheet);
        final Transformer embedTransformer = embedTemplates.newTransformer();
        final Source transformSource = new StreamSource(new FileInputStream(file));
        transformSource.setSystemId(systemId);
        embedTransformer.transform(transformSource, new StreamResult(outputStream));
        final String expected = readResource(name + "0.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testRelativeEmbeddedSameLevel() throws Exception {
    final String name = PACKAGE + "EmbeddedRelative";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testRelativeEmbeddedSameLevel.out");
    final Path current = getResourcePath(name + ".xml");
    final File file = current.toFile();
    // Path resolving one level up
    final String systemId = current.getParent().toUri().toASCIIString();
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        final Source baseSource = new StreamSource(new FileInputStream(file));
        baseSource.setSystemId(systemId);
        final Source stylesheet = factory.getAssociatedStylesheet(baseSource, media, title, charset);
        final Templates embedTemplates = factory.newTemplates(stylesheet);
        final Transformer embedTransformer = embedTemplates.newTransformer();
        final Source transformSource = new StreamSource(new FileInputStream(file));
        transformSource.setSystemId(systemId);
        embedTransformer.transform(transformSource, new StreamResult(outputStream));
        final String expected = readResource(name + "1.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testRelativeEmbeddedOneLevelDown() throws Exception {
    final String name = PACKAGE + "EmbeddedRelative";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "testRelativeEmbeddedOneLevelDown.out");
    final Path current = getResourcePath(name + ".xml");
    final File file = current.toFile();
    // Path resolving one level up
    final String systemId = Paths.get(current.getParent().toString(), "systemid").toUri().toASCIIString();
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        final Source baseSource = new StreamSource(new FileInputStream(file));
        baseSource.setSystemId(systemId);
        final Source stylesheet = factory.getAssociatedStylesheet(baseSource, media, title, charset);
        final Templates embedTemplates = factory.newTemplates(stylesheet);
        final Transformer embedTransformer = embedTemplates.newTransformer();
        final Source transformSource = new StreamSource(new FileInputStream(file));
        transformSource.setSystemId(systemId);
        embedTransformer.transform(transformSource, new StreamResult(outputStream));
        final String expected = readResource(name + "2.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testEmbeddedTextXsl() throws Exception {
    final String name = PACKAGE + "EmbeddedType-text-xsl";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedType.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testEmbeddedTextXml() throws Exception {
    final String name = PACKAGE + "EmbeddedType-text-xml";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedType.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void testEmbeddedApplicationXml() throws Exception {
    final String name = PACKAGE + "EmbeddedType-application-xml-xslt";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = null, charset = null; // often ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedType.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test1() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "foo/media", title = null, charset = null; // often
                                                                    // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedRelative1.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test2() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "bar/media", title = null, charset = null; // often
                                                                    // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedRelative0.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test3() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = "foo-title", charset = null; // often
                                                                    // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedRelative1.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test4() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = "bar-title", charset = null; // often
                                                                    // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedRelative0.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test5() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "alt/media", title = null, charset = null; // often
                                                                    // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    final Templates embedTemplates = factory.newTemplates(stylesheet);
    final Transformer embedTransformer = embedTemplates.newTransformer();
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "test1.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        embedTransformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(PACKAGE + "EmbeddedRelative2.out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
  }

  @Test
  public void test6() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = null, title = "title-not-found", charset = null; // often
                                                                          // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    Assert.assertEquals(null, stylesheet);
  }

  @Test
  public void test7() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "media/notfound", title = null, charset = null; // often
                                                                         // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    Assert.assertEquals(null, stylesheet);
  }

  @Test
  public void test8() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "media/notfound", title = "alt-title", charset = null; // often
                                                                                // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    Assert.assertEquals(null, stylesheet);
  }

  @Test
  public void test9() throws Exception {
    final String name = PACKAGE + "EmbeddedMediaTitle";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final String media = "alt/media", title = "title-not-found", charset = null; // often
                                                                                 // ignored
    final Source stylesheet = factory.getAssociatedStylesheet(getSource(name + ".xml"), media, title, charset);
    Assert.assertEquals(null, stylesheet);
  }

}
