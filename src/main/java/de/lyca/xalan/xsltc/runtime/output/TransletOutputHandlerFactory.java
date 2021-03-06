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
package de.lyca.xalan.xsltc.runtime.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import de.lyca.xalan.xsltc.trax.SAX2DOM;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.serializer.ToHTMLStream;
import de.lyca.xml.serializer.ToTextStream;
import de.lyca.xml.serializer.ToUnknownStream;
import de.lyca.xml.serializer.ToXHTMLStream;
import de.lyca.xml.serializer.ToXMLSAXHandler;
import de.lyca.xml.serializer.ToXMLStream;

/**
 * @author Santiago Pericas-Geertsen
 */
public class TransletOutputHandlerFactory {

  public static final int STREAM = 0;
  public static final int SAX = 1;
  public static final int DOM = 2;

  private String _encoding = "utf-8";
  private String _method = null;
  private int _outputType = STREAM;
  private OutputStream _ostream = System.out;
  private Writer _writer = null;
  private Node _node = null;
  private Node _nextSibling = null;
  private int _indentNumber = -1;
  private ContentHandler _handler = null;
  private LexicalHandler _lexHandler = null;

  static public TransletOutputHandlerFactory newInstance() {
    return new TransletOutputHandlerFactory();
  }

  public void setOutputType(int outputType) {
    _outputType = outputType;
  }

  public void setEncoding(String encoding) {
    if (encoding != null) {
      _encoding = encoding;
    }
  }

  public void setOutputMethod(String method) {
    _method = method;
  }

  public void setOutputStream(OutputStream ostream) {
    _ostream = ostream;
  }

  public void setWriter(Writer writer) {
    _writer = writer;
  }

  public void setHandler(ContentHandler handler) {
    _handler = handler;
  }

  public void setLexicalHandler(LexicalHandler lex) {
    _lexHandler = lex;
  }

  public void setNode(Node node) {
    _node = node;
  }

  public Node getNode() {
    return _handler instanceof SAX2DOM ? ((SAX2DOM) _handler).getDOM() : null;
  }

  public void setNextSibling(Node nextSibling) {
    _nextSibling = nextSibling;
  }

  public void setIndentNumber(int value) {
    _indentNumber = value;
  }

  public SerializationHandler getSerializationHandler(Transformer transformer) throws IOException,
          ParserConfigurationException {
    SerializationHandler result = null;
    switch (_outputType) {
      case STREAM:

        if (_method == null) {
          result = new ToUnknownStream();
        } else if (_method.equalsIgnoreCase("xml")) {

          result = new ToXMLStream();

        } else if (_method.equalsIgnoreCase("html")) {

          result = new ToHTMLStream();

        } else if (_method.equalsIgnoreCase("xhtml")) {
          
          result = new ToXHTMLStream();
          
        } else if (_method.equalsIgnoreCase("text")) {

          result = new ToTextStream();

        }

        result.setTransformer(transformer);

        if (result != null && _indentNumber >= 0) {
          result.setIndentAmount(_indentNumber);
        }

        result.setEncoding(_encoding);

        if (_writer != null) {
          result.setWriter(_writer);
        } else {
          result.setOutputStream(_ostream);
        }
        return result;

      case DOM:
        _handler = _node != null ? new SAX2DOM(_node, _nextSibling) : new SAX2DOM();
        _lexHandler = (LexicalHandler) _handler;
        // falls through
      case SAX:
        if (_method == null) {
          _method = "xml"; // default case
        }

        if (_lexHandler == null) {
          result = new ToXMLSAXHandler(_handler, _encoding);
        } else {
          result = new ToXMLSAXHandler(_handler, _lexHandler, _encoding);
        }

        return result;
    }
    return null;
  }

}
