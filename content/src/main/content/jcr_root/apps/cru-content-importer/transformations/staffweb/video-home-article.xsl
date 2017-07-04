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
	<xsl:param name="dDocTitle" />
	<xsl:param name="xDescription" />
	<xsl:param name="dDocName" />

	<xsl:template match="/">
	
		<xsl:variable name="date" select="fn:doc(concat('give://formatDate?date=', fn:encode-for-uri(wcm:root/wcm:element[@name='by_line_date'])))"/>
		<xsl:variable name="dateText" select="fn:doc(concat('give://formatDate?outputformat=dd MMMM yyyy&amp;date=', fn:encode-for-uri(wcm:root/wcm:element[@name='by_line_date'])))"/>
		<xsl:variable name="image" select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='article_image'])))"/>
		<xsl:variable name="text" select="fn:doc(concat('give://transformUrls?htmlSource=', fn:encode-for-uri(wcm:root/wcm:element[@name='teaser'])))"/>
	
		<sv:node sv:name="jcr:content">
		    <sv:property sv:name="jcr:primaryType" sv:type="Name">
		        <sv:value>cq:PageContent</sv:value>
		    </sv:property>
		    <sv:property sv:name="sling:resourceType" sv:type="String">
		        <sv:value>StaffWeb/components/page/article</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:template" sv:type="String">
		        <sv:value>/apps/StaffWeb/components/page/article</sv:value>
		    </sv:property>
		    <sv:property sv:name="cq:designPath" sv:type="String">
		        <sv:value>/etc/designs/staffweb</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:title" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocTitle" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:description" sv:type="String">
		        <sv:value><xsl:value-of select="$xDescription" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="contentId" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocName" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="author" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='by_line']" /></sv:value>
		    </sv:property>
		    <xsl:if test="wcm:root/wcm:element[@name='by_line'] = ''">
		    	<sv:property sv:name="author" sv:type="String">
		        	<sv:value><xsl:value-of select="wcm:root/wcm:element[@name='byline']" /></sv:value>
		    	</sv:property>
		    </xsl:if>
		    <xsl:if test="$dateText != ''">
		    	<sv:property sv:name="dateText" sv:type="String">
		        	<sv:value><xsl:value-of select="$dateText" /></sv:value>
		    	</sv:property>
		    </xsl:if>
		    <xsl:if test="$date != ''">
		    	<sv:property sv:name="date" sv:type="Date">
		        	<sv:value><xsl:value-of select="$date" /></sv:value>
		    	</sv:property>
		    </xsl:if>
		    <xsl:if test="wcm:root/wcm:element[@name='show_video_ratings'] = 'false'">
		    	<sv:property sv:name="hideCommentsFeed" sv:type="String">
		    		<sv:value>true</sv:value>
		    	</sv:property>
		    </xsl:if>
		    <sv:node sv:name="image">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>foundation/components/image</sv:value>
			    </sv:property>
			    <xsl:if test="$image != ''">
			        <sv:property sv:name="fileReference" sv:type="String">
			            <sv:value><xsl:value-of select="$image" /></sv:value>
			        </sv:property>
			    </xsl:if>
		    </sv:node>
		    <sv:node sv:name="post-body-parsys">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>foundation/components/parsys</sv:value>
			    </sv:property>
		        <xsl:if test="wcm:root/wcm:element[@name='video_link'] != ''">
			        <sv:node sv:name="media_embed">
				        <sv:property sv:name="jcr:primaryType" sv:type="Name">
				            <sv:value>nt:unstructured</sv:value>
				        </sv:property>
				        <sv:property sv:name="sling:resourceType" sv:type="String">
				            <sv:value>StaffWeb/components/section/media-embed</sv:value>
				        </sv:property>
				        <sv:property sv:name="mediaEmbed" sv:type="String">
						    <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='video_link']" /></sv:value>				    
				        </sv:property>
			        </sv:node>
		        </xsl:if>
			    <sv:node sv:name="text">
				    <sv:property sv:name="jcr:primaryType" sv:type="Name">
				        <sv:value>nt:unstructured</sv:value>
				    </sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/text</sv:value>
				    </sv:property>
				    <sv:property sv:name="text" sv:type="String">
				        <sv:value><xsl:value-of select="$text" /></sv:value>
				    </sv:property>
			    </sv:node>
		    </sv:node>
		</sv:node>
	</xsl:template>
</xsl:transform>