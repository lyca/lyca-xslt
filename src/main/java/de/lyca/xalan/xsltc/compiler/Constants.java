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
package de.lyca.xalan.xsltc.compiler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public class Constants {

  // Error categories used to report errors to Parser.reportError()

  // Unexpected internal errors, such as null-ptr exceptions, etc.
  // Immediately terminates compilation, no translet produced
  public static final int INTERNAL = 0;
  // XSLT elements that are not implemented and unsupported ext.
  // Immediately terminates compilation, no translet produced
  public static final int UNSUPPORTED = 1;
  // Fatal error in the stylesheet input (parsing or content)
  // Immediately terminates compilation, no translet produced
  public static final int FATAL = 2;
  // Other error in the stylesheet input (parsing or content)
  // Does not terminate compilation, no translet produced
  public static final int ERROR = 3;
  // Other error in the stylesheet input (content errors only)
  // Does not terminate compilation, a translet is produced
  public static final int WARNING = 4;

  public static final String NAMESPACE_FEATURE = "http://xml.org/sax/features/namespaces";

  public static final String NODE_ITERATOR = "de.lyca.xml.dtm.DTMAxisIterator";
  public static final String STRING_TO_REAL = "stringToReal";

  public static final String COMPILER_PACKAGE = "de.lyca.xalan.xsltc.compiler";

  public static final String DOM_INTF = "de.lyca.xalan.xsltc.DOM";
  public static final String STRING_CLASS = "java.lang.String";

  // output interface
  public static final String RUNTIME_NODE_CLASS = "de.lyca.xalan.xsltc.runtime.Node";

  public static final String NODE_PNAME = "node";
  public static final String TRANSLET_OUTPUT_PNAME = "handler";
  public static final String ITERATOR_PNAME = "iterator";
  public static final String DOCUMENT_PNAME = "document";
  public static final String TRANSLET_PNAME = "translet";

  // TODO add to SerializationHandler
  public static final String CHARACTERSW = "characters";
  public static final String APPLY_TEMPLATES = "applyTemplates";
  public static final String GET_ELEMENT_VALUE = "getElementValue";
  public static final String ADD_ITERATOR = "addIterator";

  public static final String NAMES_INDEX = "namesArray";
  public static final String URIS_INDEX = "urisArray";
  public static final String TYPES_INDEX = "typesArray";
  public static final String NAMESPACE_INDEX = "namespaceArray";
  public static final String HASIDCALL_INDEX = "_hasIdCall";
  public static final String TRANSLET_VERSION_INDEX = "transletVersion";

  public static final String DOM_FIELD = "_dom";
  public static final String STATIC_NAMES_ARRAY_FIELD = "_sNamesArray";
  public static final String STATIC_URIS_ARRAY_FIELD = "_sUrisArray";
  public static final String STATIC_TYPES_ARRAY_FIELD = "_sTypesArray";
  public static final String STATIC_NAMESPACE_ARRAY_FIELD = "_sNamespaceArray";
  public static final String STATIC_NS_ANCESTORS_ARRAY_FIELD = "_sNamespaceAncestorsArray";
  public static final String STATIC_PREFIX_URIS_IDX_ARRAY_FIELD = "_sPrefixURIsIdxArray";
  public static final String STATIC_PREFIX_URIS_ARRAY_FIELD = "_sPrefixURIPairsArray";
  public static final String STATIC_CHAR_DATA_FIELD = "_scharData";

  public static final String ADD_PARAMETER = "addParameter";
  public static final String PUSH_PARAM_FRAME = "pushParamFrame";
  public static final String POP_PARAM_FRAME = "popParamFrame";

  public static final String STRIP_SPACE = "stripSpace";

  public static final String XMLNS_PREFIX = "xmlns";
  public static final String XSLT_URI = "http://www.w3.org/1999/XSL/Transform";
  public static final String XHTML_URI = "http://www.w3.org/1999/xhtml";
  public static final String TRANSLET_URI = "http://xml.apache.org/xalan/xsltc";
  public static final String REDIRECT_URI = "http://xml.apache.org/xalan/redirect";

  public static final int RTF_INITIAL_SIZE = 32;
}
