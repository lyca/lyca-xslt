<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- FileName: jira2419 -->
<!-- Document: http://www.w3.org/TR/xpath -->
<!-- DocVersion: 19991116 -->
<!-- Section: ??? -->
<!-- Creator: Lars Michele -->
<!-- Purpose: Test for nth-descendant axis. -->

<xsl:output method="xml" encoding="UTF-8" indent="no" />

<xsl:template match="/">
  <out>
    <xsl:text>&#10;</xsl:text>
    <xsl:text>ASCII: abc&#10;</xsl:text>
    <xsl:text>ISO-8859-1: &#230;&#248;&#229;&#10;</xsl:text>
    <xsl:text>CHINESE: &#38345;&#28023;&#20117;&#10;</xsl:text>
    <xsl:text>ASTRAL: &#65584;&#164;&#65586;&#10;</xsl:text>
  </out>
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
