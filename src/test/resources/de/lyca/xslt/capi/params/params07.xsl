<?xml version="1.0"?> 
<?xml-stylesheet type="text/xsl" href="#style1-23.34.123456789_345"?>
<!DOCTYPE doc [
<!ELEMENT doc (#PCDATA | head | body)*>

<!ELEMENT head (#PCDATA | xsl:stylesheet)*>
<!ELEMENT body (#PCDATA|para)*>

<!ELEMENT xsl:stylesheet (#PCDATA | xsl:key | xsl:template)*>
<!ATTLIST xsl:stylesheet 
		  id ID #REQUIRED
		  xmlns:xsl CDATA #FIXED "http://www.w3.org/1999/XSL/Transform"
		  version NMTOKEN #REQUIRED>

<!ELEMENT xsl:key EMPTY>
<!ATTLIST xsl:key
    name NMTOKENS #REQUIRED
    match CDATA #REQUIRED
    use CDATA #REQUIRED>

<!ELEMENT xsl:template (#PCDATA | out)*>
<!ATTLIST xsl:template  match CDATA #IMPLIED>

<!ELEMENT out (#PCDATA | xsl:value-of | xsl:text)*>

<!ELEMENT xsl:value-of EMPTY>
<!ATTLIST xsl:value-of select CDATA #REQUIRED>

<!ELEMENT xsl:text (#PCDATA)>

<!ELEMENT para (#PCDATA)*>
<!ATTLIST para id ID #REQUIRED>
]>

<doc>
  <head>
	<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                id="style1-23.34.123456789_345">

	<!-- FileName: embed01 -->
	<!-- Document: http://www.w3.org/TR/xslt -->
	<!-- DocVersion: 19991116 -->
	<!-- Section: 2.7 Embedding Stylesheets. -->
	<!-- Purpose: General test of embedded stylesheet using fragment identifier -->

	  <xsl:key name="test" match="para" use="@id"/>

		<xsl:template match="/">
  			<out>
    			<xsl:value-of select="doc/body/para"/>
				<xsl:value-of select="key('test','foey')"/><xsl:text>&#10;</xsl:text>
  			</out>
		</xsl:template>
		<xsl:template match="xsl:stylesheet" />
	</xsl:stylesheet>
  </head>

  <body>
  	<para id="foo">
Hello
	</para>
	<para id="foey">
Goodbye
	</para>
  </body>

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

</doc>
