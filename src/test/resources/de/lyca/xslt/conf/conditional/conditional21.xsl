<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: conditional21 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 9 -->
  <!-- Creator: Carmelo Montanez --><!-- DataManipulation002 in NIST suite -->
  <!-- Purpose: Test xsl:if inside xsl:otherwise. -->

<xsl:template match="doc">
  <out>
    <xsl:choose>
      <xsl:when test = "2 &gt; 3">
        <xsl:text>Test failed!!</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test = "9 mod 3 = 0">
          <xsl:text>Test executed successfully.</xsl:text>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
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
