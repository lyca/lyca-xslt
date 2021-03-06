<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:baz="http://xsl.lotus.com/ns1"
                xmlns:bar="http://xsl.lotus.com/ns1"
                exclude-result-prefixes="bar baz">

  <!-- FileName: idkey53 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 12.2 -->
  <!-- Creator: David Bertoni -->
  <!-- Purpose: Test for xsl:key and key() with a qualified name, different prefix. -->

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:key name="baz:mykey" match="div" use="title"/>

<xsl:template match="doc">
  <root>
    <xsl:value-of select="key('bar:mykey', 'Introduction')/p"/><xsl:text> </xsl:text>
    <xsl:value-of select="key('bar:mykey', 'Stylesheet Structure')/p"/><xsl:text> </xsl:text>
    <xsl:value-of select="key('bar:mykey', 'Expressions')/p"/>
  </root>
</xsl:template>


  <!--
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
  -->

</xsl:stylesheet>
