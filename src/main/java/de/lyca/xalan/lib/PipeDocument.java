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
package de.lyca.xalan.lib;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 */
// Imported Serializer classes

import de.lyca.xalan.extensions.XSLProcessorContext;
import de.lyca.xalan.templates.AVT;
import de.lyca.xalan.templates.ElemExtensionCall;
import de.lyca.xalan.templates.ElemLiteralResult;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xml.serializer.Serializer;
import de.lyca.xml.serializer.SerializerFactory;
import de.lyca.xml.utils.SystemIDResolver;
import de.lyca.xpath.XPathContext;

/**
 * PipeDocument is a Xalan extension element to set stylesheet params and pipes
 * an XML document through a series of 1 or more stylesheets. PipeDocument is
 * invoked from a stylesheet as the {@link #pipeDocument pipeDocument extension
 * element}.
 * 
 * It is accessed by specifying a namespace URI as follows:
 * 
 * <pre>
 *    xmlns:pipe="http://xml.apache.org/xalan/PipeDocument"
 * </pre>
 * 
 * @author Donald Leslie
 */
public class PipeDocument {
  /**
   * Extension element for piping an XML document through a series of 1 or more
   * transformations.
   * 
   * <pre>
   * Common usage pattern: A stylesheet transforms a listing of documents to be
   * transformed into a TOC. For each document in the listing calls the pipeDocument
   * extension element to pipe that document through a series of 1 or more stylesheets 
   * to the desired output document.
   * 
   * Syntax:
   * &lt;xsl:stylesheet version="1.0"
   *                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   *                xmlns:pipe="http://xml.apache.org/xalan/PipeDocument"
   *                extension-element-prefixes="pipe"&gt;
   * ...
   * &lt;pipe:pipeDocument   source="source.xml" target="target.xml"&gt;
   *   &lt;stylesheet href="ss1.xsl"&gt;
   *     &lt;param name="param1" value="value1"/&gt;
   *   &lt;/stylesheet&gt;
   *   &lt;stylesheet href="ss2.xsl"&gt;
   *     &lt;param name="param1" value="value1"/&gt;
   *     &lt;param name="param2" value="value2"/&gt;
   *   &lt;/stylesheet&gt;
   *   &lt;stylesheet href="ss1.xsl"/&gt;     
   * &lt;/pipe:pipeDocument&gt;
   * 
   * Notes:
   * </pre>
   * <ul>
   * <li>The base URI for the source attribute is the XML "listing" document.
   * <li/>
   * <li>The target attribute is taken as is (base is the current user
   * directory).
   * <li/>
   * <li>The stylsheet containg the extension element is the base URI for the
   * stylesheet hrefs.
   * <li/>
   * </ul>
   */
  public void pipeDocument(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException,
          TransformerConfigurationException, SAXException, IOException, FileNotFoundException {

    final SAXTransformerFactory saxTFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

    // XML doc to transform.
    final String source = elem.getAttribute("source", context.getContextNode(), context.getTransformer());
    final TransformerImpl transImpl = context.getTransformer();

    // Base URI for input doc, so base for relative URI to XML doc to transform.
    final String baseURLOfSource = transImpl.getBaseURLOfSource();
    // Absolute URI for XML doc to transform.
    final String absSourceURL = SystemIDResolver.getAbsoluteURI(source, baseURLOfSource);

    // Transformation target
    final String target = elem.getAttribute("target", context.getContextNode(), context.getTransformer());

    final XPathContext xctxt = context.getTransformer().getXPathContext();
    final int xt = xctxt.getDTMHandleFromNode(context.getContextNode());

    // Get System Id for stylesheet; to be used to resolve URIs to other
    // stylesheets.
    final String sysId = elem.getSystemId();

    NodeList ssNodes = null;
    NodeList paramNodes = null;
    Node ssNode = null;
    Node paramNode = null;
    if (elem.hasChildNodes()) {
      ssNodes = elem.getChildNodes();
      // Vector to contain TransformerHandler for each stylesheet.
      final List<TransformerHandler> vTHandler = new ArrayList<>(ssNodes.getLength());

      // The child nodes of an extension element node are instances of
      // ElemLiteralResult, which requires does not fully support the standard
      // Node interface. Accordingly, some special handling is required (see
      // below)
      // to get attribute values.
      for (int i = 0; i < ssNodes.getLength(); i++) {
        ssNode = ssNodes.item(i);
        if (ssNode.getNodeType() == Node.ELEMENT_NODE && ((Element) ssNode).getTagName().equals("stylesheet")
                && ssNode instanceof ElemLiteralResult) {
          AVT avt = ((ElemLiteralResult) ssNode).getLiteralResultAttribute("href");
          final String href = avt.evaluate(xctxt, xt, elem);
          final String absURI = SystemIDResolver.getAbsoluteURI(href, sysId);
          final Templates tmpl = saxTFactory.newTemplates(new StreamSource(absURI));
          final TransformerHandler tHandler = saxTFactory.newTransformerHandler(tmpl);
          final Transformer trans = tHandler.getTransformer();

          // AddTransformerHandler to vector
          vTHandler.add(tHandler);

          paramNodes = ssNode.getChildNodes();
          for (int j = 0; j < paramNodes.getLength(); j++) {
            paramNode = paramNodes.item(j);
            if (paramNode.getNodeType() == Node.ELEMENT_NODE && ((Element) paramNode).getTagName().equals("param")
                    && paramNode instanceof ElemLiteralResult) {
              avt = ((ElemLiteralResult) paramNode).getLiteralResultAttribute("name");
              final String pName = avt.evaluate(xctxt, xt, elem);
              avt = ((ElemLiteralResult) paramNode).getLiteralResultAttribute("value");
              final String pValue = avt.evaluate(xctxt, xt, elem);
              trans.setParameter(pName, pValue);
            }
          }
        }
      }
      usePipe(vTHandler, absSourceURL, target);
    }
  }

  /**
   * Uses a Vector of TransformerHandlers to pipe XML input document through a
   * series of 1 or more transformations. Called by {@link #pipeDocument}.
   * 
   * @param vTHandler
   *          Vector of Transformation Handlers (1 per stylesheet).
   * @param source
   *          absolute URI to XML input
   * @param target
   *          absolute path to transformation output.
   */
  public void usePipe(List<TransformerHandler> vTHandler, String source, String target) throws TransformerException,
          TransformerConfigurationException, FileNotFoundException, IOException, SAXException,
          SAXNotRecognizedException {
    final XMLReader reader = XMLReaderFactory.createXMLReader();
    final TransformerHandler tHFirst = vTHandler.get(0);
    reader.setContentHandler(tHFirst);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHFirst);
    for (int i = 1; i < vTHandler.size(); i++) {
      final TransformerHandler tHFrom = vTHandler.get(i - 1);
      final TransformerHandler tHTo = vTHandler.get(i);
      tHFrom.setResult(new SAXResult(tHTo));
    }
    final TransformerHandler tHLast = vTHandler.get(vTHandler.size() - 1);
    final Transformer trans = tHLast.getTransformer();
    final Properties outputProps = trans.getOutputProperties();
    final Serializer serializer = SerializerFactory.getSerializer(outputProps);

    final FileOutputStream out = new FileOutputStream(target);
    try {
      serializer.setOutputStream(out);
      tHLast.setResult(new SAXResult(serializer.asContentHandler()));
      reader.parse(source);
    } finally {
      // Always clean up the FileOutputStream,
      // even if an exception was thrown in the try block
      if (out != null) {
        out.close();
      }
    }
  }
}
