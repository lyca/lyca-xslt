<?xml version="1.0" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:str="http://exslt.org/strings" 
	exclude-result-prefixes="str">
        <xsl:template match="/">
		<out>
			<xsl:value-of select="str:encode-uri('http://www.example.com/my résumé',false())"/>
		</out>
        </xsl:template>
</xsl:stylesheet>
