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
	<xsl:param name="fragment" />
	
	<xsl:template match="*">
	    <xsl:element name="{name()}">
	        <xsl:apply-templates select="@*" />
	        <xsl:apply-templates />
	    </xsl:element>
	</xsl:template>
	
	<xsl:template match="@*">
	    <xsl:attribute name="{name()}"><xsl:value-of select="." /></xsl:attribute>
	</xsl:template>

	<xsl:template match="sv:node[@sv:name='content-parsys']">
		<sv:node sv:name="content-parsys">
			<xsl:variable name="fragment" select="fn:doc(concat('give://collectorCache?key=',$fragment))"/>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates />
			<xsl:apply-templates select="$fragment/cacheValue/wcm:root/wcm:list/wcm:row"/>
		</sv:node>
	</xsl:template>

	<xsl:template match="wcm:row">
		<xsl:if test="fn:position() &lt; 3">
			<xsl:variable name="plainHeadline" select="fn:doc(concat('give://escapeHTMLTags?htmlSource=', fn:encode-for-uri(wcm:element[@name='headline'])))"/>
			<xsl:variable name="linkHeadline" select="fn:doc(concat('give://getLinkFromHTML?htmlSource=', fn:encode-for-uri(wcm:element[@name='headline'])))"/>
			<xsl:variable name="plainReadMore" select="fn:doc(concat('give://escapeHTMLTags?htmlSource=', fn:encode-for-uri(wcm:element[@name='more_link'])))"/>
			<sv:node >
				<xsl:attribute name="sv:name"><xsl:value-of select="concat('tile_',$fragment,fn:position())"/></xsl:attribute>
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
				    	<sv:value>StaffwebInternalImage</sv:value>
					</sv:property>
					<sv:property sv:name="imageRotate" sv:type="String">
				    	<sv:value>0</sv:value>
					</sv:property>
					<sv:property sv:name="imagePosition" sv:type="String">
				    	<sv:value>tile-image-left</sv:value>
					</sv:property>
				</xsl:if>
			</sv:node>
		</xsl:if>
	</xsl:template>

</xsl:transform>