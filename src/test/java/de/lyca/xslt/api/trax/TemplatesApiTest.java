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
 * $Id: TemplatesAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * TemplatesAPITest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getSource;

import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic API coverage test for the Templates class of TRAX.
 * 
 * @author Original:shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class TemplatesApiTest {

  private static final String PACKAGE = '/' + TemplatesApiTest.class.getPackage().getName().replace('.', '/') + "/";

  @Test
  public void testTemplates1() throws Exception {
    final String name = PACKAGE + "TransformerAPIParam";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Templates templates = factory.newTemplates(getSource(name + ".xsl"));
    final Transformer transformer = templates.newTransformer();
    Assert.assertTrue(transformer != null);
    final Properties outputFormat = templates.getOutputProperties();
    Assert.assertTrue(outputFormat != null);
    Assert.assertEquals("xml", outputFormat.getProperty(OutputKeys.METHOD));
    Assert.assertEquals(null, outputFormat.get(OutputKeys.METHOD));
    Assert.assertEquals("no", outputFormat.getProperty(OutputKeys.INDENT));
    Assert.assertEquals(null, outputFormat.get(OutputKeys.INDENT));
  }

  @Test
  public void testTemplates2() throws Exception {
    final String name = PACKAGE + "TransformerAPIOutputFormat";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final Templates templates = factory.newTemplates(getSource(name + ".xsl"));
    final Transformer transformer = templates.newTransformer();
    Assert.assertTrue(transformer != null);
    final Properties outputFormat = templates.getOutputProperties();
    Assert.assertTrue(outputFormat != null);
    Assert.assertEquals("xml", outputFormat.getProperty(OutputKeys.METHOD));
    Assert.assertEquals("xml", outputFormat.get(OutputKeys.METHOD));
    Assert.assertEquals("yes", outputFormat.getProperty(OutputKeys.INDENT));
    Assert.assertEquals("yes", outputFormat.get(OutputKeys.INDENT));
    Assert.assertEquals("cdataHere ", outputFormat.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals("cdataHere ", outputFormat.get(OutputKeys.CDATA_SECTION_ELEMENTS));
    Assert.assertEquals("yes", outputFormat.getProperty(OutputKeys.OMIT_XML_DECLARATION));
    Assert.assertEquals("yes", outputFormat.get(OutputKeys.OMIT_XML_DECLARATION));
  }

}
