<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- FileName: position90 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 3.3 -->
  <!-- Creator: David Marston, from remarks of Mukund Raghavachari (who read Mike Kay) -->
  <!-- Purpose: Test () grouping with expanded version of // axis. This test attempts to
     give an explicit representation of one possible fallacious interpretation of
     chapter//footnote[6]. -->

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:template match="/">
  <out>
    <xsl:for-each select="chapter/descendant::footnote[6]">
      <greeting>
        <xsl:value-of select="."/>
      </greeting>
      <xsl:text>
</xsl:text>
    </xsl:for-each>
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
