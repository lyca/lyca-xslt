<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
      xmlns:tst="test"
      exclude-result-prefixes="tst">

  <!-- FileName: namespace34 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 5.4 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test of namespace-uri() on default namespace declaration. -->
  <!-- Part 4 of the Namespaces in XML spec says "The prefix xmlns is used only for
     namespace bindings and is not itself bound to any namespace name. -->

<xsl:template match="/tst:a">
  <out>
   <xsl:value-of select="namespace-uri(namespace::*[string()='http://www.w3.org/1999/XMLSchema-instance'])"/>,
<xsl:value-of select="namespace-uri(namespace::*[string()='test'])"/>
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
