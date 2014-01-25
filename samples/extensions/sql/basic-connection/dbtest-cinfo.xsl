<?xml version="1.0"?>

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:sql="de.lyca.xalan.lib.sql.XConnection"
                extension-element-prefixes="sql">

<xsl:output method="html" indent="yes"/>

<xsl:param name="cinfo" select="//DBINFO"/>
<xsl:param name="query" select="'SELECT * FROM import1'"/>

<xsl:template match="/">
    <xsl:variable name="db" select="sql:new()"/>

	
    <!-- Connect to the database with minimal error detection -->
      <xsl:if test="not(sql:connect($db, $cinfo))" >
    	<xsl:message>Error Connecting to the Database</xsl:message>
      <xsl:copy-of select="sql:getError($db)/ext-error" />
    </xsl:if>

    <CINFO>
 	<xsl:copy-of select="//DBINFO"/>
    </CINFO>    

    <HTML>
      <HEAD>
        <TITLE>List of products</TITLE>
      </HEAD>
      <BODY>
        <TABLE border="1">
          <xsl:variable name="table" select='sql:query($db, $query)'/>
          
          <!-- 
          	Let's include Error Checking, the error is actually stored 
            in the connection since $table will be either data or null
          -->
             
          <xsl:if test="not($table)" >
          	<xsl:message>Error in Query</xsl:message>
            <xsl:copy-of select="sql:getError($db)/ext-error" />
          </xsl:if>
          
          
          <TR>
             <xsl:for-each select="$table/sql/metadaata/column-header">
               <TH><xsl:value-of select="@column-label"/></TH>
             </xsl:for-each>
          </TR>
          <xsl:apply-templates select="$table/sql/row-set"/>
        </TABLE>
      </BODY>
    </HTML>
    <xsl:value-of select="sql:close($db)"/>
</xsl:template>

<xsl:template match="row">
  <TR><xsl:apply-templates select="col"/></TR>
</xsl:template>

<xsl:template match="col">
  <TD><xsl:value-of select="text()"/></TD>
</xsl:template>

</xsl:stylesheet>