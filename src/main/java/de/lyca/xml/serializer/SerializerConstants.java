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
package de.lyca.xml.serializer;

/**
 * Constants used in serialization, such as the string "xmlns"
 */
class SerializerConstants {

  /**
   * To insert ]]> in a CDATA section by ending the last CDATA section with ]]
   * and starting the next CDATA section with >
   */
  public static final String CDATA_CONTINUE = "]]]]><![CDATA[>";
  /**
   * The constant "]]>"
   */
  public static final String CDATA_DELIMITER_CLOSE = "]]>";
  public static final String CDATA_DELIMITER_OPEN = "<![CDATA[";

  public static final String ENTITY_AMP = "&amp;";
  public static final String ENTITY_CRLF = "&#xA;";
  public static final String ENTITY_GT = "&gt;";
  public static final String ENTITY_LT = "&lt;";
  public static final String ENTITY_QUOT = "&quot;";

  public static final String XML_PREFIX = "xml";
  public static final String XMLNS_PREFIX = "xmlns";
  public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

  public static final String DEFAULT_SAX_SERIALIZER = SerializerBase.PKG_NAME + ".ToXMLSAXHandler";

  /**
   * Define the XML version.
   */
  public static final String XMLVERSION11 = "1.1";
  public static final String XMLVERSION10 = "1.0";
}
