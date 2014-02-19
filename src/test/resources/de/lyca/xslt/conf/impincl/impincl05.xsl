<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: IMPINCL05 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 2.6.2 -->
  <!-- Purpose: Two imports, each of which has an import, plus an
       apply-imports in main stylesheet. Precedence (low) DBECA (high) per spec. -->

<xsl:import href="b.xsl"/>
<xsl:import href="c.xsl"/>

<xsl:template match="/">
  <out><xsl:apply-templates select="doc/*" /></out>
</xsl:template>

<xsl:template match="title"><!-- This has import precedence over all others -->
  MAIN title matched<xsl:text>...</xsl:text>
  <xsl:apply-imports/>
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
