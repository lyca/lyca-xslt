<?xml version="1.0" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="http://exslt.org/strings"
                exclude-result-prefixes="str">

    <xsl:template match="/">
        <out>
            <xsl:value-of select="str:decode-uri('a_%5E_%C3%A9_%E4%B8%AD_%F0%90%80%84')" />
        </out>
    </xsl:template>

</xsl:stylesheet>
