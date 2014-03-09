<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: idkey03 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 12.2 -->
  <!-- Purpose: Test for key() in template pattern matching. --> 

<xsl:key name="mykey" match="div" use="title"/>

<xsl:template match="doc">
 <out>
  <xsl:apply-templates select="key('mykey', 'Introduction')/p"/>
  <xsl:apply-templates select="key('mykey', 'Stylesheet Structure')"/>
  <xsl:apply-templates select="key('mykey', 'Expressions')/p"/>
  <xsl:apply-templates select="key('mykey', 'Patterns')"/>
 </out>
</xsl:template>

<xsl:template match="key('mykey', 'Introduction')/p">
  <xsl:value-of select="."/><xsl:text>,</xsl:text>
</xsl:template>

<xsl:template match="key('mykey', 'Stylesheet Structure')">
  <xsl:value-of select="p"/><xsl:text>,</xsl:text>
</xsl:template>

<xsl:template match="key('mykey', 'Expressions')/p">
  <xsl:value-of select="."/><xsl:text>,</xsl:text>
</xsl:template>

<xsl:template match="key('mykey', 'Patterns')">
  <xsl:value-of select="p"/>
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