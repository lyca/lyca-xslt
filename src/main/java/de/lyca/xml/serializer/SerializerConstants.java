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
package de.lyca.xml.serializer;

/**
 * Constants used in serialization, such as the string "xmlns"
 * 
 * @xsl.usage internal
 */
interface SerializerConstants {

  /**
   * To insert ]]> in a CDATA section by ending the last CDATA section with ]]
   * and starting the next CDATA section with >
   */
  String CDATA_CONTINUE = "]]]]><![CDATA[>";
  /**
   * The constant "]]>"
   */
  String CDATA_DELIMITER_CLOSE = "]]>";
  String CDATA_DELIMITER_OPEN = "<![CDATA[";

  String EMPTYSTRING = "";

  String ENTITY_AMP = "&amp;";
  String ENTITY_CRLF = "&#xA;";
  String ENTITY_GT = "&gt;";
  String ENTITY_LT = "&lt;";
  String ENTITY_QUOT = "&quot;";

  String XML_PREFIX = "xml";
  String XMLNS_PREFIX = "xmlns";
  String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

  String DEFAULT_SAX_SERIALIZER = SerializerBase.PKG_NAME + ".ToXMLSAXHandler";

  /**
   * Define the XML version.
   */
  String XMLVERSION11 = "1.1";
  String XMLVERSION10 = "1.0";
}
