<?xml version="1.0" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:str="http://exslt.org/strings" 
	exclude-result-prefixes="str" >
	<xsl:template match="documents">
		<out>
			<xsl:apply-templates />
		</out>
	</xsl:template>

	<xsl:template match="document">
		<xsl:element name="a">
			<xsl:attribute name="href">
				<xsl:text>http://www.example.com/lookup?</xsl:text>
				<xsl:value-of select="str:encode-uri(@location,true())"/>
			</xsl:attribute>
			<xsl:value-of select="@name"/>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
