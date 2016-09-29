<?xml version="1.0"?>
<xsl:transform
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:sv="http://www.jcp.org/jcr/sv/1.0"
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
	<xsl:param name="xSiebelDesignation" />
	<xsl:param name="xFriendlyFilename" />
	<xsl:param name="xSiebelParentDesignation" />
	<xsl:param name="xSiebelOrganizationId"/>
	<xsl:param name="xSiebelWebTreatment"/>

	<xsl:template match="/">
		
		<xsl:variable name="additionalMapping" select="fn:doc(concat('give://csvAdditionalMapping?keyColumn=TREATMENT_NUMBER&amp;keyValue=', fn:encode-for-uri($xSiebelWebTreatment)))"/>
	
		<sv:node sv:name="jcr:content">
		    <sv:property sv:name="jcr:primaryType" sv:type="Name">
		        <sv:value>cq:PageContent</sv:value>
		    </sv:property>
		    <sv:property sv:name="sling:resourceType" sv:type="String">
		        <sv:value>Give/components/page/campaign</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:template" sv:type="String">
		        <sv:value>/apps/Give/templates/campaign</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:title" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="designationType" sv:type="String">
		        <sv:value>Campaign</sv:value>
		    </sv:property>
		    <sv:property sv:name="designationNumber" sv:type="String">
		        <sv:value><xsl:value-of select="$xSiebelDesignation" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="designationName" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="vanityURL" sv:type="String">
		        <sv:value><xsl:value-of select="$xFriendlyFilename" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="websiteURL" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='website']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="paragraphText" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='body']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="letterDate" sv:type="String">
		        <sv:value><xsl:value-of select="fn:doc(concat('give://formatDate?date=', fn:encode-for-uri(wcm:root/wcm:element[@name='by_line_date'])))" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="psText" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='postscript']" /></sv:value>
		    </sv:property>
		    <xsl:if test="not(empty(wcm:root/wcm:element[@name='signature']))">
			    <sv:property sv:name="signatureImage" sv:type="String">
			        <sv:value><xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='signature'])))" /></sv:value>
			    </sv:property>
		    </xsl:if>
		    <sv:property sv:name="startDate" sv:type="String">
		        <sv:value><xsl:value-of select="fn:doc(concat('give://formatDate?date=', fn:encode-for-uri($additionalMapping/data/FUND_APPEAL_START_DATE)))" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="defaultCampaign" sv:type="String">
		        <sv:value><xsl:value-of select="$additionalMapping/data/CAMPAIGN_NUMBER" /></sv:value>
		    </sv:property>
			<xsl:if test="not(empty(wcm:root/wcm:element[@name='wide_image']))">
				<sv:property sv:name="coverPhoto" sv:type="String">
					<sv:value><xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='wide_image'])))" /></sv:value>
				</sv:property>
			</xsl:if>
			<xsl:if test="not(empty(wcm:root/wcm:element[@name='image']))">
				<sv:property sv:name="secondaryPhoto" sv:type="String">
					<sv:value><xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='image'])))" /></sv:value>
				</sv:property>
			</xsl:if>
		    <sv:property sv:name="parentDesignationNumber" sv:type="String">
		        <sv:value><xsl:value-of select="$xSiebelParentDesignation" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="organizationId" sv:type="String">
		    	<sv:value><xsl:value-of select="$xSiebelOrganizationId" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="hideInNav" sv:type="Boolean">
		        <sv:value>true</sv:value>
		    </sv:property>
		    <sv:property sv:name="customized" sv:type="Boolean">
		        <sv:value>true</sv:value>
		    </sv:property>
		</sv:node>
	</xsl:template>

</xsl:transform>