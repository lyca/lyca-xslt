<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- FileName: imp20d -->
<!-- Purpose: Used by impincl20 -->

<xsl:template match="/">
  <xsl:text>WRONG Match on / in imp20d.xsl&#xa;</xsl:text>
  <xsl:apply-templates select="foo"/>
</xsl:template>

<xsl:template match="foo">
  <D>
    <xsl:apply-imports/><!-- no imports here, so it will revert to built-ins -->
  </D>
</xsl:template>

<xsl:template match="bar">
  <xsl:text>match on bar in imp20d.xsl</xsl:text>
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
