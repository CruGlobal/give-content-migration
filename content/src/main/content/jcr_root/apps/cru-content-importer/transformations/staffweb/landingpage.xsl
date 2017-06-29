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

	<xsl:output method="xml" omit-xml-declaration="yes" version="1.0" indent="yes" encoding="UTF-8" />

	<xsl:param name="path" />
	<!-- ============================================
      Every Excel column are available as a parameter.
      Just declare in this section the required parameter using the column label,
      replacing all non alphanumeric characters by underscore
    =============================================== -->
	<xsl:param name="urlLabel" />
	<xsl:param name="dDocName_6_30" />
	
	<xsl:template match="/">
		<sv:node sv:name="jcr:content">
		    <sv:property sv:name="jcr:primaryType" sv:type="Name">
		        <sv:value>cq:PageContent</sv:value>
		    </sv:property>
		    <sv:property sv:name="sling:resourceType" sv:type="String">
		        <sv:value>StaffWeb/components/page/content</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:template" sv:type="String">
		        <sv:value>/apps/StaffWeb/templates/content</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:designPath" sv:type="String">
		        <sv:value>/etc/designs/staffweb</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:title" sv:type="String">
		        <sv:value><xsl:value-of select="$urlLabel" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="contentId" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocName_6_30" /></sv:value>
		    </sv:property>
 			<sv:property sv:name="hideInNav" sv:type="Boolean">
		        <sv:value>true</sv:value>
		    </sv:property>
		    <sv:node sv:name="image">
			    <sv:property sv:name="imageRotate" sv:type="Name">
			        <sv:value>0</sv:value>
			    </sv:property>
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
		    </sv:node>
		    <sv:node sv:name="content-parsys">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>wcm/foundation/components/parsys</sv:value>
			    </sv:property>
			    <sv:node sv:name="section_title">
				    <sv:property sv:name="jcr:primaryType" sv:type="Name">
				        <sv:value>nt:unstructured</sv:value>
				    </sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/section-title</sv:value>
				    </sv:property>
				    <sv:property sv:name="text" sv:type="String">
				    	<sv:value><xsl:value-of select="$urlLabel" /></sv:value>
				    </sv:property>
		    	</sv:node>
		    	<xsl:apply-templates select="wcm:root/wcm:list/wcm:row"/>
			</sv:node>
		</sv:node>
	</xsl:template>
	
	<xsl:template match="wcm:row">
		<xsl:variable name="plainHeadline" select="fn:doc(concat('give://escapeHTMLTags?htmlSource=', fn:encode-for-uri(wcm:element[@name='headline'])))"/>
		<xsl:variable name="linkHeadline" select="fn:doc(concat('give://getLinkFromHTML?htmlSource=', fn:encode-for-uri(wcm:element[@name='headline'])))"/>
		<xsl:variable name="plainReadMore" select="fn:doc(concat('give://escapeHTMLTags?htmlSource=', fn:encode-for-uri(wcm:element[@name='more_link'])))"/>
		<sv:node >
			<xsl:attribute name="sv:name"><xsl:value-of select="concat('tile_',fn:position())"/></xsl:attribute>
			<sv:property sv:name="jcr:primaryType" sv:type="Name">
				<sv:value>nt:unstructured</sv:value>
			</sv:property>
			<sv:property sv:name="sling:resourceType" sv:type="String">
			    <sv:value>StaffWeb/components/section/tile</sv:value>
			</sv:property>
			<sv:property sv:name="width" sv:type="String">
			    	<sv:value>col-md-12</sv:value>
			</sv:property>
			<xsl:if test="not(wcm:element[@name='headline'] = '')">
				<sv:property sv:name="title" sv:type="String">
			    	<sv:value><xsl:value-of select="$plainHeadline" /></sv:value>
				</sv:property>
				<xsl:if test="$linkHeadline != ''">
					<sv:property sv:name="href" sv:type="String">
				    	<sv:value><xsl:value-of select="$linkHeadline" /></sv:value>
					</sv:property>
					<sv:property sv:name="readMoreStyle" sv:type="String">
				    	<sv:value>btn-subtle</sv:value>
					</sv:property>
				</xsl:if>
			</xsl:if>
			<xsl:if test="not(wcm:element[@name='more_link'] = '')">
				<sv:property sv:name="readMore" sv:type="String">
			    	<sv:value><xsl:value-of select="$plainReadMore" /></sv:value>
				</sv:property>
			</xsl:if>
			<xsl:if test="not(wcm:element[@name='teaser'] = '')">
				<sv:property sv:name="text" sv:type="String">
			    	<sv:value><xsl:value-of select="fn:doc(concat('give://transformUrls?htmlSource=', fn:encode-for-uri(wcm:element[@name='teaser'])))" /></sv:value>
				</sv:property>
			</xsl:if>
			<xsl:if test="not(wcm:element[@name='image_large'] = '')">
				<sv:property sv:name="fileReference" sv:type="String">
					<sv:value><xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:element[@name='image_large'])))" /></sv:value>
				</sv:property>
				<sv:property sv:name="imageRenditionName" sv:type="String">
			    	<sv:value>CruWhole848x477</sv:value>
				</sv:property>
				<sv:property sv:name="imageRotate" sv:type="String">
			    	<sv:value>0</sv:value>
				</sv:property>
			</xsl:if>
		</sv:node>	
	</xsl:template>

</xsl:transform>