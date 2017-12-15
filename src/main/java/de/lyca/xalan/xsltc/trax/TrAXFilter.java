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

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import de.lyca.xml.utils.XMLReaderManager;

/**
 * skeleton extension of XMLFilterImpl for now.
 * 
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller
 */
public class TrAXFilter extends XMLFilterImpl {
  private final Templates _templates;
  private final TransformerImpl _transformer;
  private final TransformerHandlerImpl _transformerHandler;

  public TrAXFilter(Templates templates) throws TransformerConfigurationException {
    _templates = templates;
    _transformer = (TransformerImpl) templates.newTransformer();
    _transformerHandler = new TransformerHandlerImpl(_transformer);
  }

  public Transformer getTransformer() {
    return _transformer;
  }

  private void createParent() throws SAXException {
    XMLReader parent = null;
    try {
      final SAXParserFactory pfactory = SAXParserFactory.newInstance();
      pfactory.setNamespaceAware(true);

      if (_transformer.isSecureProcessing()) {
        try {
          pfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (final SAXException e) {
        }
      }

      final SAXParser saxparser = pfactory.newSAXParser();
      parent = saxparser.getXMLReader();
    } catch (final ParserConfigurationException e) {
      throw new SAXException(e);
    } catch (final FactoryConfigurationError e) {
      throw new SAXException(e.toString());
    }

    if (parent == null) {
      parent = XMLReaderFactory.createXMLReader();
    }

    // make this XMLReader the parent of this filter
    setParent(parent);
  }

  @Override
  public void parse(InputSource input) throws SAXException, IOException {
    XMLReader managedReader = null;

    try {
      if (getParent() == null) {
        try {
          managedReader = XMLReaderManager.getInstance().getXMLReader();
          setParent(managedReader);
        } catch (final SAXException e) {
          throw new SAXException(e.toString());
        }
      }

      // call parse on the parent
      getParent().parse(input);
    } finally {
      if (managedReader != null) {
        XMLReaderManager.getInstance().releaseXMLReader(managedReader);
      }
    }
  }

  @Override
  public void parse(String systemId) throws SAXException, IOException {
    parse(new InputSource(systemId));
  }

  @Override
  public void setContentHandler(ContentHandler handler) {
    _transformerHandler.setResult(new SAXResult(handler));
    if (getParent() == null) {
      try {
        createParent();
      } catch (final SAXException e) {
        return;
      }
    }
    getParent().setContentHandler(_transformerHandler);
  }

  /**
   * Set the parent reader.
   * 
   * <p>
   * This is the {@link org.xml.sax.XMLReader XMLReader} from which this filter
   * will obtain its events and to which it will pass its configuration
   * requests. The parent may itself be another filter.
   * </p>
   * 
   * <p>
   * If there is no parent reader set, any attempt to parse or to set or get a
   * feature or property will fail.
   * </p>
   * 
   * @param parent
   *          The parent XML reader.
   * @throws java.lang.NullPointerException
   *           If the parent is null.
   */
  @Override
  public void setParent(XMLReader parent) {
    super.setParent(parent);

    if (null != parent.getContentHandler()) {
      this.setContentHandler(parent.getContentHandler());
    }

    // Not really sure if we should do this here, but
    // it seems safer in case someone calls parse() on
    // the parent.
    // setupParse ();
  }

  public void setErrorListener(ErrorListener handler) {
    _transformer.setErrorListener(handler);
  }
}
