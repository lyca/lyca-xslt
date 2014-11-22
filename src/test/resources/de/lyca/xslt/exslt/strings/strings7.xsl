<?xml version="1.0" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:str="http://exslt.org/strings" 
	exclude-result-prefixes="str">
        <xsl:template match="/">
		<out>
			<xsl:value-of select="str:decode-uri('C%3A%5Chome%5Cmhoyt%5Cmy%20r%C3%A9sum%C3%A9.doc')"/>
		</out>
        </xsl:template>
</xsl:stylesheet>
