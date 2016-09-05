<?xml version="1.0"?>
<xsl:transform
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:tiff="http://ns.adobe.com/tiff/1.0/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dam="http://www.day.com/dam/1.0"
	xmlns:cq="http://www.day.com/jcr/cq/1.0"
	xmlns:jcr="http://www.jcp.org/jcr/1.0"
	xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
	version="2.0">

	<xsl:output method="xml" version="1.0" indent="yes" encoding="UTF-8" />

	<xsl:param name="path" />
	<!-- ============================================
      Every Excel column are available as a parameter.
      Just declare in this section the required parameter using the column label,
      replacing all non alphanumeric characters by underscore
    =============================================== -->
	<xsl:param name="dDocTitle" />
	<xsl:param name="xSiebelDesignation" />
	<xsl:param name="xSiebelParentDesignation" />
	<xsl:param name="xSiebelOrganizationId" />
	<xsl:param name="dDocAuthor_6_50" />
	<xsl:param name="dDocName" />

	<xsl:template match="/">
		<metadata>
			<xsl:attribute name="dc:title"><xsl:value-of select="$dDocTitle" /></xsl:attribute>
			<xsl:attribute name="dc:creator"><xsl:value-of select="$dDocAuthor_6_50" /></xsl:attribute>

			<!-- ============================================
		      GIVE Properties
		    =============================================== -->
			<xsl:attribute name="designationNumber"><xsl:value-of select="$xSiebelDesignation" /></xsl:attribute>
			<xsl:attribute name="parentDesignation"><xsl:value-of select="$xSiebelParentDesignation" /></xsl:attribute>
			<xsl:attribute name="organizationId"><xsl:value-of select="$xSiebelOrganizationId" /></xsl:attribute>
			<xsl:attribute name="contentId"><xsl:value-of select="$dDocName" /></xsl:attribute>
			
		</metadata>
	</xsl:template>

</xsl:transform>