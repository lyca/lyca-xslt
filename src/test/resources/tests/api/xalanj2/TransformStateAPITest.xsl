<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- xsl-comment-1 Filename: TransformStateAPITest.xsl -->


<xsl:variable name="variable1" select="variable-1-value"/>
<xsl:param name="param1" select="param-1-value-default"/>


  <xsl:template match="/" name="template-1-root">
    <doc>
      <mode-header>
        <xsl:text>xsl-text-1</xsl:text>
        <xsl:value-of select="$variable1" />
        <xsl:value-of select="$param1" />
        <xsl:element name="xsl-element-1">
          <xsl:attribute name="xsl-attribute-1">xsl-attribute-1-value</xsl:attribute>xsl-element-content-newline
        </xsl:element>
      </mode-header>
      <mode-none><xsl:apply-templates select="item" /></mode-none>
      <mode-ala><xsl:call-template name="apple" /></mode-ala>
    </doc>
  </xsl:template>

  <xsl:template match="item">
    <pie><xsl:copy/></pie>
  </xsl:template>

  <xsl:template name="apple">
    <apple><xsl:apply-templates select="list" mode="ala" /></apple>
  </xsl:template>

  <xsl:template match="list" mode="ala">
    <icecream>text-literal-chars<xsl:text>xsl-text-2a</xsl:text><xsl:copy-of select="."/></icecream>
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