<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:jcr="http://www.jcp.org/jcr/1.0"
	xmlns:cq="http://www.day.com/jcr/cq/1.0"
	xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
	xmlns:dam="http://www.day.com/dam/1.0">

	<xsl:output method="xml" version="1.0" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="path" />
	<xsl:param name="metadata" />

	<xsl:template match="/">
	    <jcr:content>
	    	<xsl:attribute name="jcr:primaryType">cq:PageContent</xsl:attribute>
	    	<xsl:attribute name="sling:resourceType">Give/components/page/designation</xsl:attribute>
	    	<xsl:attribute name="cq:template">/apps/Give/templates/designation</xsl:attribute>
	    	<xsl:attribute name="jcr:title"><xsl:value-of select="$path"/></xsl:attribute>
	    </jcr:content>
	</xsl:template>
	
</xsl:stylesheet>
