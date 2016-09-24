<?xml version="1.0"?>
<xsl:transform
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:sv="http://www.jcp.org/jcr/sv/1.0"
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

	<xsl:template match="/">
		<sv:node sv:name="jcr:content">
		    <sv:property sv:name="jcr:primaryType" sv:type="Name">
		        <sv:value>cq:PageContent</sv:value>
		    </sv:property>
		    <sv:property sv:name="sling:resourceType" sv:type="String">
		        <sv:value>Give/components/page/designation</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:template" sv:type="String">
		        <sv:value>/apps/Give/templates/designation</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:title" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="designationType" sv:type="String">
		        <sv:value>People</sv:value>
		    </sv:property>
		    <sv:property sv:name="designationNumber" sv:type="String">
		        <sv:value><xsl:value-of select="$Designation" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="designationName" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='title']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="vanityURL" sv:type="String">
		        <sv:value><xsl:value-of select="$Designation" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="websiteURL" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='website']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="paragraphText" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='body']" /></sv:value>
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
		        <sv:value></sv:value>
		    </sv:property>
		    <sv:property sv:name="organizationID" sv:type="String">
		        <sv:value></sv:value>
		    </sv:property>
		    <sv:property sv:name="secure" sv:type="Boolean">
		        <sv:value>false</sv:value>
		    </sv:property>
		    <sv:property sv:name="hideInNav" sv:type="Boolean">
		        <sv:value>true</sv:value>
		    </sv:property>
		    <sv:property sv:name="customized" sv:type="Boolean">
		        <sv:value>true</sv:value>
		    </sv:property>
		    <sv:property sv:name="firstName" sv:type="String">
		        <sv:value></sv:value>
		    </sv:property>
		    <sv:property sv:name="lastName" sv:type="String">
		        <sv:value></sv:value>
		    </sv:property>
		    <sv:property sv:name="spouseName" sv:type="String">
		        <sv:value></sv:value>
		    </sv:property>
		</sv:node>
	</xsl:template>

</xsl:transform>