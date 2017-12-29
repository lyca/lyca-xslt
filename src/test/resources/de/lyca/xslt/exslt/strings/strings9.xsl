<?xml version="1.0" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:str="http://exslt.org/strings" exclude-result-prefixes="str" >
	<xsl:template match="/">
		<out>
			<test>
				<desc>No encoding specified</desc>
				<result>
					<xsl:value-of select="str:encode-uri('http://www.example.com/my  résumé',false())"/>
				</result>
			</test>
			<test>
				<desc> UTF-8 specified</desc>
				<result>
					<xsl:value-of select="str:encode-uri('http://www.example.com/my  résumé',false(),'UTF-8')"/>
				</result>
			</test>
			<test>
				<desc>Utf-8 specified</desc>
				<result>
					<xsl:value-of select="str:encode-uri('http://www.example.com/my  résumé',false(), 'Utf-8')"/>
				</result>
			</test>
			<test>
				<desc>ISO-8859-1 specified</desc>
				<result>
					<xsl:value-of select="str:encode-uri('http://www.example.com/my  résumé',false(), 'iso-8859-1')"/>
				</result>
			</test>
			<test>
				<desc>US-ASCII specified</desc>
				<result>
					<xsl:value-of select="str:encode-uri('http://www.example.com/my  résumé',false(), 'US-ASCII')"/>
				</result>
			</test>
		</out>
	</xsl:template>

</xsl:stylesheet>
