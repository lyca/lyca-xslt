<?xml version="1.0" encoding="iso-8859-1" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="text"/>

  <!-- FileName: sort35 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 10 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test the famous technique for choosing the sort key dynamically. -->

<xsl:variable name="sortcol" select="'left'"/>
<!-- Set value to either 'left' or 'right' above. Could change to external param. -->

<xsl:template match="/">
  <xsl:for-each select="doc/alpha">
    <xsl:sort select="./*[name(.) = $sortcol]" data-type="text" />
    <xsl:value-of select="concat(left,right,orig)"/><xsl:text>,</xsl:text>
  </xsl:for-each>
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
