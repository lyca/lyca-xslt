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
 * $Id: DOMSourceAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * DOMSourceAPITest.java
 *
 */
package de.lyca.xslt.api.trax.dom;

import static de.lyca.xslt.ResourceUtils.getInputSource;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import de.lyca.xslt.ResourceUtils;

/**
 * API Coverage test for the DOMSource class of TRAX.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class DOMSourceApiTest {

  private static final String PACKAGE = '/' + DOMSourceApiTest.class.getPackage().getName().replace('.', '/') + "/";

  @Test
  public void test1() throws Exception {
    final DOMSource domSource = new DOMSource();
    Assert.assertEquals(null, domSource.getNode());
    Assert.assertEquals(null, domSource.getSystemId());
  }

  @Test
  public void test2() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node node = documentBuilder.newDocument();
    final DOMSource domSource = new DOMSource(node);
    Assert.assertEquals(node, domSource.getNode());
    Assert.assertEquals(null, domSource.getSystemId());
  }

  @Test
  public void test3() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node node = documentBuilder.newDocument();
    final DOMSource domSource = new DOMSource(node, "this-is-system-id");
    Assert.assertEquals(node, domSource.getNode());
    Assert.assertEquals("this-is-system-id", domSource.getSystemId());
  }

  @Test
  public void test4() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node node = documentBuilder.newDocument();
    final DOMSource domSource = new DOMSource(node, "another-system-id");
    Assert.assertEquals(node, domSource.getNode());
    Assert.assertEquals("another-system-id", domSource.getSystemId());
  }

  @Test(expected = TransformerConfigurationException.class)
  public void testTemplateFromBlankDOMSource() throws Exception {
    final DOMSource xslSource = new DOMSource();
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.newTemplates(xslSource);
  }

  @Test(expected = TransformerConfigurationException.class)
  public void testTransformerFromBlankDOMSource() throws Exception {
    final DOMSource xslSource = new DOMSource();
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.newTransformer(xslSource);
  }

  @Test
  public void testDOMSource() throws Exception {
    final String name = PACKAGE + "DOMTest";

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));
    final Node xmlNode = documentBuilder.parse(getInputSource(name + ".xml"));
    final String expected = ResourceUtils.readResource(name + ".out", StandardCharsets.UTF_8);

    final DOMSource xslSource = new DOMSource(xslNode);
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final Templates templates = transformerFactory.newTemplates(xslSource);
    final Transformer transformer = templates.newTransformer();

    final DOMSource xmlSource = new DOMSource(xmlNode);
    final Node output = documentBuilder.newDocument();
    final DOMResult blankResult = new DOMResult(output);
    transformer.transform(xmlSource, blankResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(output), new StreamResult(writer));

    Assert.assertEquals(expected, writer.toString());
  }

  @Test
  public void testEmptyDOMSource() throws Exception {
    final String name = PACKAGE + "DOMTest";

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));

    final DOMSource xslSource = new DOMSource(xslNode);
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final Templates templates = transformerFactory.newTemplates(xslSource);
    final Transformer transformer = templates.newTransformer();

    final DOMSource xmlSource = new DOMSource();
    final DOMResult blankResult = new DOMResult();
    transformer.transform(xmlSource, blankResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(blankResult.getNode()), new StreamResult(writer));

    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", writer.toString());
  }

  @Test
  public void testDOMSourceWithIncludesAndSystemId() throws Exception {
    final String name = PACKAGE + "DOMImpIncl";

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));
    final Node xmlNode = documentBuilder.parse(getInputSource(name + ".xml"));
    final String expected = ResourceUtils.readResource(name + ".out", StandardCharsets.UTF_8);

    final DOMSource xslSource = new DOMSource(xslNode);
    xslSource.setSystemId(ResourceUtils.getSystemID(name + ".xsl"));
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final Templates templates = transformerFactory.newTemplates(xslSource);
    final Transformer transformer = templates.newTransformer();

    final DOMSource xmlSource = new DOMSource(xmlNode);
    final Node output = documentBuilder.newDocument();
    final DOMResult blankResult = new DOMResult(output);
    transformer.transform(xmlSource, blankResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(output), new StreamResult(writer));

    Assert.assertEquals(expected, writer.toString());
  }

  @Test(expected = TransformerConfigurationException.class)
  public void testDOMSourceWithIncludes() throws Exception {
    final String name = PACKAGE + "DOMImpIncl";

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));

    final DOMSource xslSource = new DOMSource(xslNode);
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.newTemplates(xslSource);
  }

  @Test
  public void testReuseDOMSource() throws Exception {
    final String name = PACKAGE + "DOMTest";

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));
    final Node xmlNode = documentBuilder.parse(getInputSource(name + ".xml"));
    final String expected = ResourceUtils.readResource(name + ".out", StandardCharsets.UTF_8);

    final DOMSource xmlSource1 = new DOMSource(xmlNode);
    final DOMResult result1 = new DOMResult(documentBuilder.newDocument());
    final DOMSource xslSource = new DOMSource(xslNode);
    final Transformer transformer1 = transformerFactory.newTransformer(xslSource);
    transformer1.transform(xmlSource1, result1);
    final Node node1 = result1.getNode();
    StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(node1), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());

    // Use same Source for the stylesheet
    final DOMSource xmlSource2 = new DOMSource(xmlNode);
    final DOMResult result2 = new DOMResult(documentBuilder.newDocument());
    final Transformer transformer2 = transformerFactory.newTransformer(xslSource);
    transformer2.transform(xmlSource2, result2);
    final Node node2 = result2.getNode();
    writer = new StringWriter();
    identityTransformer.transform(new DOMSource(node2), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());

    // Re-use DOMSource for XML doc; with the same stylesheet
    final DOMResult result3 = new DOMResult(documentBuilder.newDocument());
    final Transformer transformer3 = transformerFactory.newTransformer(xslSource);
    transformer3.transform(xmlSource2, result3);
    final Node node3 = result3.getNode();
    writer = new StringWriter();
    identityTransformer.transform(new DOMSource(node3), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());
  }

  @Test
  public void testReuseDOMSource2() throws Exception {
    final String name1 = PACKAGE + "DOMTest";
    final String name2 = PACKAGE + "DOMImpIncl";

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name1 + ".xsl"));
    final Node xmlNode = documentBuilder.parse(getInputSource(name1 + ".xml"));
    final Node xslImpInclNode = documentBuilder.parse(getInputSource(name1 + ".xsl"));
    final Node xmlImpInclNode = documentBuilder.parse(getInputSource(name1 + ".xml"));
    final String expected = ResourceUtils.readResource(name1 + ".out", StandardCharsets.UTF_8);

    // Re-use DOMSource after setNode to different one
    final DOMSource xmlSource = new DOMSource(xmlNode);
    final DOMSource xslSource = new DOMSource(xslNode);
    final Transformer transformer1 = transformerFactory.newTransformer(xslSource);
    final DOMResult result1 = new DOMResult(documentBuilder.newDocument());
    transformer1.transform(xmlSource, result1);
    final Node node1 = result1.getNode();
    StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(node1), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());

    // Use same Sources, but change Nodes for xml,xsl
    xmlSource.setNode(xmlImpInclNode);
    xmlSource.setSystemId(ResourceUtils.getSystemID(name2 + ".xml"));
    xslSource.setNode(xslImpInclNode);
    xslSource.setSystemId(ResourceUtils.getSystemID(name2 + ".xsl"));
    final Transformer transformer2 = transformerFactory.newTransformer(xslSource);
    final DOMResult result2 = new DOMResult(documentBuilder.newDocument());
    transformer2.transform(xmlSource, result2);
    final Node node2 = result2.getNode();
    writer = new StringWriter();
    identityTransformer.transform(new DOMSource(node2), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());
  }

}
