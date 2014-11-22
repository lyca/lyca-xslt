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
 * $Id: OutputPropertiesTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * OutputPropertiesTest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.MEDIA_TYPE;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.transform.OutputKeys.STANDALONE;
import static javax.xml.transform.OutputKeys.VERSION;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xslt.ResourceUtils;

/**
 * Verify how output properties are handled from stylesheets and the API.
 * 
 * @author shane_curcuru@lotus.com
 * @author Krishna.Meduri@eng.sun.com
 * @author Refactored: l.michele@lyca.de
 */
public class OutputPropertiesTest {

  private static final String PACKAGE = '/' + OutputPropertiesTest.class.getPackage().getName().replace('.', '/') + "/";
  private static final String PACKAGE_SAX = '/' + OutputPropertiesTest.class.getPackage().getName().replace('.', '/')
          + "/sax/";

  @Test
  public void testDefaultHtmlOutput() throws Exception {
    final String name = PACKAGE + "OutputPropertiesHTML";
    Transformer transformer = TransformerFactory.newInstance().newTransformer(ResourceUtils.getSource(name + ".xsl"));
    StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));
    final String expected = readResource(name + ".out", UTF_8);
    Assert.assertEquals(expected, writer.toString());
  }

  @Test
  public void testNoIndentHtmlOutput() throws Exception {
    final String name = PACKAGE + "OutputPropertiesHTML";
    Templates templates = TransformerFactory.newInstance().newTemplates(ResourceUtils.getSource(name + ".xsl"));

    Transformer transformer1 = templates.newTransformer();
    transformer1.setOutputProperty(INDENT, "no");
    StringWriter writer1 = new StringWriter();
    transformer1.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer1));

    Transformer transformer2 = templates.newTransformer();
    Properties properties = new Properties();
    properties.put(INDENT, "no");
    transformer2.setOutputProperties(properties);
    StringWriter writer2 = new StringWriter();
    transformer2.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer2));
    Assert.assertEquals(writer1.toString(), writer2.toString());
  }

  @Test
  public void testXmlOutput() throws Exception {
    final String name = PACKAGE + "OutputPropertiesHTML";
    Templates templates = TransformerFactory.newInstance().newTemplates(ResourceUtils.getSource(name + ".xsl"));

    Transformer transformer1 = templates.newTransformer();
    transformer1.setOutputProperty(METHOD, "xml");
    StringWriter writer1 = new StringWriter();
    transformer1.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer1));

    Transformer transformer2 = templates.newTransformer();
    Properties properties = new Properties();
    properties.put(METHOD, "xml");
    transformer2.setOutputProperties(properties);
    StringWriter writer2 = new StringWriter();
    transformer2.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer2));

    Assert.assertEquals(writer1.toString(), writer2.toString());
  }

  @Test
  public void testNoStandaloneHtmlOutput() throws Exception {
    final String name = PACKAGE + "OutputPropertiesHTML";
    Templates templates = TransformerFactory.newInstance().newTemplates(ResourceUtils.getSource(name + ".xsl"));

    Transformer transformer1 = templates.newTransformer();
    transformer1.setOutputProperty(STANDALONE, "no");
    StringWriter writer1 = new StringWriter();
    transformer1.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer1));

    Transformer transformer2 = templates.newTransformer();
    Properties properties = new Properties();
    properties.put(STANDALONE, "no");
    transformer2.setOutputProperties(properties);
    StringWriter writer2 = new StringWriter();
    transformer2.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer2));

    Assert.assertEquals(writer1.toString(), writer2.toString());
    final String expected = readResource(name + ".out", UTF_8);
    Assert.assertEquals(expected, writer1.toString());
    Assert.assertEquals(expected, writer2.toString());
  }

  /**
   * testDefaultSaxCities: transform with just stylesheet properties.
   * 
   * @author shane_curcuru@lotus.com
   */
  @Test
  public void testDefaultSaxCities() throws Exception {
    final String name = PACKAGE_SAX + "cities";
    Transformer transformer = TransformerFactory.newInstance().newTransformer(ResourceUtils.getSource(name + ".xsl"));
    StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));
    final String expected = readResource(name + ".out", UTF_8);
    Assert.assertEquals(expected, writer.toString());
  }

  /**
   * testSetOutputProperties: verify setOutputProperties(Properties).
   * 
   * @author Krishna.Meduri@eng.sun.com
   */
  @Test
  public void testSetOutputProperties() throws Exception {
    final String name = PACKAGE_SAX + "cities";
    final String outName = PACKAGE_SAX + "cities-indent-no.out";
    Transformer transformer = TransformerFactory.newInstance().newTransformer(ResourceUtils.getSource(name + ".xsl"));

    // Verify setting a whole block of properties
    Properties properties = new Properties();
    properties.put(METHOD, "xml");
    properties.put(ENCODING, "UTF-8");
    properties.put(OMIT_XML_DECLARATION, "no");
    properties.put("{http://xml.apache.org/xslt}indent-amount", "0");
    // This should override the indent=yes in the stylesheet
    properties.put(INDENT, "no");
    properties.put(STANDALONE, "no");
    properties.put(VERSION, "1.0");
    properties.put(MEDIA_TYPE, "text/xml");
    transformer.setOutputProperties(properties);

    StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));
    final String expected = readResource(outName, UTF_8);
    Assert.assertEquals(expected, writer.toString());
  }

  /**
   * testSetSingleOutputProperties: verify setOutputProperty(s, s).
   * 
   * @author Krishna.Meduri@eng.sun.com
   */
  @Test
  public void testSetSingleOutputProperties() throws Exception {
    final String name = PACKAGE_SAX + "cities";
    final String outName = PACKAGE_SAX + "cities-indent-no.out";
    Transformer transformer = TransformerFactory.newInstance().newTransformer(ResourceUtils.getSource(name + ".xsl"));

    // Verify setting a whole block of properties
    transformer.setOutputProperty(METHOD, "xml");
    transformer.setOutputProperty(ENCODING, "UTF-8");
    transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
    // This should override the indent=yes in the stylesheet
    transformer.setOutputProperty(INDENT, "no");
    transformer.setOutputProperty(STANDALONE, "no");
    transformer.setOutputProperty(VERSION, "1.0");
    transformer.setOutputProperty(MEDIA_TYPE, "text/xml");

    StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));
    final String expected = readResource(outName, UTF_8);
    Assert.assertEquals(expected, writer.toString());
  }

  /**
   * testSetMethodText: verify setOutputProperty(s, s).
   * 
   * @author Krishna.Meduri@eng.sun.com
   */
  @Test
  public void testSetMethodText() throws Exception {
    final String name = PACKAGE_SAX + "cities";
    final String outName = PACKAGE_SAX + "cities-method-text.out";
    Transformer transformer = TransformerFactory.newInstance().newTransformer(ResourceUtils.getSource(name + ".xsl"));
    transformer.setOutputProperty(METHOD, "text");
    StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));
    final String expected = readResource(outName, UTF_8);
    Assert.assertEquals(expected, writer.toString());
  }

}
