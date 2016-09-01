<?xml version="1.0"?>
<xsl:transform
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:jcr="http://www.jcp.org/jcr/1.0"
	xmlns:cq="http://www.day.com/jcr/cq/1.0"
	xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
	xmlns:wcm="http://www.stellent.com/wcm-data/ns/8.0.0"
	version="2.0">

	<xsl:output method="xml" version="1.0" indent="yes" encoding="UTF-8" />

	<xsl:param name="path" />
	<!-- ============================================
      Every Excel column are available as a parameter.
      Just declare in this section the required parameter using the column label,
      replacing all non alphanumeric characters by underscore
    =============================================== -->
	<xsl:param name="Designation" />
	<xsl:param name="xSiebelParentDesignation" />
	<!-- The column name is different according to the excel sheet -->
	<xsl:param name="xSiebelOrganizationId" select="'N/A'"/>
	<xsl:param name="xSiebelOrganizationId_6_200" select="'N/A'"/>

	<xsl:template match="/">
		<jcr:content>
			<xsl:attribute name="jcr:primaryType">cq:PageContent</xsl:attribute>
			<xsl:attribute name="sling:resourceType">Give/components/page/designation</xsl:attribute>
			<xsl:attribute name="cq:template">/apps/Give/templates/designation</xsl:attribute>
			<xsl:attribute name="jcr:title"><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></xsl:attribute>

			<xsl:attribute name="designationType">Ministry</xsl:attribute>
			<xsl:attribute name="designationNumber"><xsl:value-of select="$Designation" /></xsl:attribute>
			<xsl:attribute name="designationName"><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></xsl:attribute>
			<xsl:attribute name="vanityURL"><xsl:value-of select="$Designation" /></xsl:attribute>
			<xsl:attribute name="websiteURL"><xsl:value-of select="wcm:root/wcm:element[@name='website']" /></xsl:attribute>
			<xsl:attribute name="paragraphText"><xsl:value-of select="wcm:root/wcm:element[@name='body']" /></xsl:attribute>

			<xsl:if test="not(empty(wcm:root/wcm:element[@name='wide_image']))">
				<xsl:attribute name="coverPhoto">
					<xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='wide_image'])))" />
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="not(empty(wcm:root/wcm:element[@name='image']))">
				<xsl:attribute name="secondaryPhoto">
					<xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='image'])))" />
				</xsl:attribute>
			</xsl:if>
			<xsl:attribute name="parentDesignationNumber"><xsl:value-of select="$xSiebelParentDesignation" /></xsl:attribute>
			<xsl:attribute name="organizationID">
				<xsl:choose>
					<xsl:when test="$xSiebelOrganizationId != 'N/A'">
						<xsl:value-of select="$xSiebelOrganizationId" />
					</xsl:when>
					<xsl:when test="$xSiebelOrganizationId_6_200 != 'N/A'">
						<xsl:value-of select="$xSiebelOrganizationId_6_200" />
					</xsl:when>
					<xsl:otherwise>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</jcr:content>
	</xsl:template>

</xsl:transform>