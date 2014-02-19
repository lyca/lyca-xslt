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
 * $Id: DOMResultAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * DOMResultAPITest.java
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.lyca.xslt.ResourceUtils;

/**
 * API Coverage test for the DOMResult class of TRAX.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class DOMResultApiTest {

  private static final String PACKAGE = '/' + DOMResultApiTest.class.getPackage().getName().replace('.', '/') + "/";

  @Test
  public void testEmptyDOMResult() throws Exception {
    final DOMResult domResult = new DOMResult();
    Assert.assertEquals(null, domResult.getNode());
    Assert.assertEquals(null, domResult.getSystemId());
  }

  @Test
  public void testNodeDOMResult() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node n = documentBuilder.newDocument();
    final DOMResult domResult = new DOMResult(n);
    Assert.assertEquals(n, domResult.getNode());
    Assert.assertEquals(null, domResult.getSystemId());
  }

  @Test
  public void testNodeDOMResultWithSystemId() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node node = documentBuilder.newDocument();
    final DOMResult domResult = new DOMResult(node, "this-is-system-id");
    Assert.assertEquals(node, domResult.getNode());
    Assert.assertEquals("this-is-system-id", domResult.getSystemId());
  }

  @Test
  public void testDOMResultSetNodeSetSystemId() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final DOMResult domResult = new DOMResult();
    final Node node = documentBuilder.newDocument();
    domResult.setNode(node);
    Assert.assertEquals(node, domResult.getNode());
    domResult.setSystemId("another-system-id");
    Assert.assertEquals("another-system-id", domResult.getSystemId());
  }

  @Test
  public void testDOMResultNextSibling() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Document document = documentBuilder.newDocument();
    final Element a = document.createElementNS("", "a");
    final Element b = document.createElementNS("", "b");
    final Element c = document.createElementNS("", "c");
    document.appendChild(a);
    a.appendChild(b);
    a.appendChild(c);
    final DOMResult domResult = new DOMResult(a, c);
    Assert.assertEquals(a, domResult.getNode());
    Assert.assertEquals(c, domResult.getNextSibling());
  }

  @Test
  public void testDOMResultNextSiblingWithSystemId() throws Exception {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Document document = documentBuilder.newDocument();
    final Element a = document.createElementNS("", "a");
    final Element b = document.createElementNS("", "b");
    final Element c = document.createElementNS("", "c");
    document.appendChild(a);
    a.appendChild(b);
    a.appendChild(c);
    final DOMResult domResult = new DOMResult(a, b, "this-is-system-id");
    Assert.assertEquals(a, domResult.getNode());
    Assert.assertEquals(b, domResult.getNextSibling());
    Assert.assertEquals("this-is-system-id", domResult.getSystemId());
  }

  @Test
  public void testBlankDOMResult() throws Exception {
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
    final DOMResult blankResult = new DOMResult();
    transformer.transform(xmlSource, blankResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(blankResult.getNode()), new StreamResult(writer));

    Assert.assertEquals(expected, writer.toString());
  }

  @Test(expected = TransformerException.class)
  public void testReuseResult() throws Exception {
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
    Transformer transformer = templates.newTransformer();

    final DOMSource xmlSource = new DOMSource(xmlNode);
    final DOMResult blankResult = new DOMResult();
    transformer.transform(xmlSource, blankResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(blankResult.getNode()), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());

    transformer = templates.newTransformer();
    // Throws Exception
    transformer.transform(xmlSource, blankResult);
  }

  @Test
  public void testReuseResultWithNewNode() throws Exception {
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
    final DOMResult domResult = new DOMResult();
    transformer.transform(xmlSource, domResult);

    StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(domResult.getNode()), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());

    domResult.setNode(documentBuilder.newDocument());
    transformer.transform(xmlSource, domResult);

    writer = new StringWriter();
    identityTransformer.transform(new DOMSource(domResult.getNode()), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());
  }

  public void testPreparedDomResult() throws Exception {
    final String name = PACKAGE + "DOMTest";

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final Node xslNode = documentBuilder.parse(getInputSource(name + ".xsl"));
    final Node xmlNode = documentBuilder.parse(getInputSource(name + ".xml"));
    final String expected = ResourceUtils.readResource(PACKAGE + "DOMTest2.out", StandardCharsets.UTF_8);

    final DOMSource xslSource = new DOMSource(xslNode);
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Transformer identityTransformer = transformerFactory.newTransformer();
    final Templates templates = transformerFactory.newTemplates(xslSource);
    final Transformer transformer = templates.newTransformer();

    final DOMSource xmlSource = new DOMSource(xmlNode);
    final Document document = documentBuilder.newDocument();
    final Element a = document.createElementNS("", "a");
    final Element b = document.createElementNS("", "b");
    final Element c = document.createElementNS("", "c");
    document.appendChild(a);
    a.appendChild(b);
    a.appendChild(c);
    final DOMResult domResult = new DOMResult(a, c);

    transformer.transform(xmlSource, domResult);

    final StringWriter writer = new StringWriter();
    identityTransformer.transform(new DOMSource(domResult.getNode()), new StreamResult(writer));
    Assert.assertEquals(expected, writer.toString());
  }

}
