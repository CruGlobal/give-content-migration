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
	<xsl:param name="xDescription_6_2000" />
	<xsl:param name="dDocName_6_30" />

	<xsl:template match="/">
	
		<xsl:variable name="date" select="fn:doc(concat('give://formatDate?date=', fn:encode-for-uri(wcm:root/wcm:element[@name='email_date'])))"/>
		<xsl:variable name="dateText" select="fn:doc(concat('give://formatDate?outputformat=dd MMMM yyyy&amp;date=', fn:encode-for-uri(wcm:root/wcm:element[@name='email_date'])))"/>
		<xsl:variable name="image" select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='email_header_image'])))"/>
		<xsl:variable name="text" select="fn:doc(concat('give://transformUrls?htmlSource=', fn:encode-for-uri(wcm:root/wcm:element[@name='email_body'])))"/>
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
		    <sv:property sv:name="showCommentsAtBottom" sv:type="Boolean">
				<sv:value>true</sv:value>
			</sv:property>
		    <sv:property sv:name="cq:designPath" sv:type="String">
		        <sv:value>/etc/designs/staffweb</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:title" sv:type="String">
		    	<xsl:choose>
         			<xsl:when test="wcm:root/wcm:element[@name='email_heading']">
           				<sv:value><xsl:value-of select="wcm:root/wcm:element[@name='email_heading']" /></sv:value>
         			</xsl:when>
         			<xsl:when test="wcm:root/wcm:element[@name='heading']">
           				<sv:value><xsl:value-of select="wcm:root/wcm:element[@name='heading']" /></sv:value>
         			</xsl:when>
         			<xsl:otherwise>
          				<sv:value>All Staff Email</sv:value>
         			</xsl:otherwise>
       			</xsl:choose>
		    </sv:property>
		    <sv:property sv:name="jcr:description" sv:type="String">
		        <sv:value><xsl:value-of select="$xDescription_6_2000" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="contentId" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocName_6_30" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="author" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='email_byline']" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="hideFacebookCommentsFeed" sv:type="String">
		        <sv:value>true</sv:value>
		    </sv:property>
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
			    <xsl:if test="$text != ''">
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
			    </xsl:if>
		    </sv:node>
 			<sv:node sv:name="content-parsys-postArticle">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>wcm/foundation/components/parsys</sv:value>
			    </sv:property>
			    <xsl:apply-templates select="wcm:root/wcm:list[@name='email_headlines_list']/wcm:row">
			    	<xsl:with-param name="fragment">headlines</xsl:with-param> 
			    </xsl:apply-templates>
			    <xsl:apply-templates select="wcm:root/wcm:list[@name='email_sidebar']/wcm:row">
			    	<xsl:with-param name="fragment">sidebar</xsl:with-param> 
			    </xsl:apply-templates>
		    </sv:node>
		</sv:node>
	</xsl:template>

	<xsl:template match="wcm:row">
		<xsl:param name="fragment" />
		<xsl:variable name="plainHeadline" select="fn:doc(concat('give://escapeHTMLTags?htmlSource=', fn:encode-for-uri(wcm:element[@name='email_headline_title'])))"/>
		<xsl:variable name="linkHeadline" select="fn:doc(concat('give://getLinkFromHTML?htmlSource=', fn:encode-for-uri(wcm:element[@name='email_more_link'])))"/>
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
			<xsl:if test="not(wcm:element[@name='email_headline_description'] = '')">
				<sv:property sv:name="text" sv:type="String">
			    	<sv:value><xsl:value-of select="fn:doc(concat('give://transformUrls?htmlSource=', fn:encode-for-uri(wcm:element[@name='email_headline_description'])))" /></sv:value>
				</sv:property>
			</xsl:if>
			<xsl:if test="not(wcm:element[@name='email_headline_image'] = '')">
				<sv:property sv:name="fileReference" sv:type="String">
					<sv:value><xsl:value-of select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:element[@name='email_headline_image'])))" /></sv:value>
				</sv:property>
				<sv:property sv:name="imageRenditionName" sv:type="String">
			    	<sv:value>CruHalf432x243</sv:value>
				</sv:property>
				<sv:property sv:name="imageRotate" sv:type="String">
			    	<sv:value>0</sv:value>
				</sv:property>
			</xsl:if>
		</sv:node>
	</xsl:template>
	
</xsl:transform>