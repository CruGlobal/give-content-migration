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
	<xsl:param name="xWebsiteSection"/>
	<xsl:param name="xWebsiteSection_6_200"/>

	<xsl:template match="/">
		<xsl:variable name="date" select="fn:doc(concat('give://formatDate?date=', fn:encode-for-uri(wcm:root/wcm:element[@name='by_line_date'])))"/>
		<xsl:variable name="dateText" select="fn:doc(concat('give://formatDate?outputformat=dd MMMM yyyy&amp;date=', fn:encode-for-uri(wcm:root/wcm:element[@name='by_line_date'])))"/>
		<xsl:variable name="image" select="fn:doc(concat('give://searchImage?image=', fn:encode-for-uri(wcm:root/wcm:element[@name='article_image'])))"/>
		<xsl:variable name="text" select="fn:doc(concat('give://transformUrls?htmlSource=', fn:encode-for-uri(wcm:root/wcm:element[@name='body'])))"/>
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
		    	<sv:value>
			    	<xsl:choose> 
		        		<xsl:when test="$dDocTitle != ''"> 
		        	    	<xsl:value-of select="$dDocTitle"/>
		        		</xsl:when> 
		        		<xsl:otherwise>
		        	    	<xsl:value-of select="wcm:root/wcm:element[@name='title']" />
		        		</xsl:otherwise>
			    	</xsl:choose> 
		    	</sv:value>
		    </sv:property>
		    <sv:property sv:name="jcr:description" sv:type="String">
		        <sv:value><xsl:value-of select="$xDescription" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="hideInNav" sv:type="String">
				<sv:value>true</sv:value>
		    </sv:property>
			<xsl:call-template name="cqtags"/>
		    <sv:property sv:name="contentId" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocName" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="author" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='by_line']" /></sv:value>
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
		    <xsl:if test="wcm:root/wcm:element[@name='show_article_rating'] = 'false'">
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
		        <xsl:call-template name="recent_headlines"/>
		    </sv:node>
		</sv:node>
	</xsl:template>
	<!-- Searches the equivalent tag in StaffWeb and includes it in the content -->
	<xsl:template name="cqtags">
		<xsl:variable name="tag">   
     		<xsl:choose> 
        		<xsl:when test="$xWebsiteSection_6_200 != ''"> 
        	    	<xsl:value-of select="$xWebsiteSection_6_200"/> 
        		</xsl:when> 
        		<xsl:when test="$xWebsiteSection != ''"> 
            		<xsl:value-of select="$xWebsiteSection"/> 
        		</xsl:when> 
		    </xsl:choose> 
		</xsl:variable>   
		<xsl:if test="$tag != ''">
			<sv:property sv:name="cq:tags" sv:type="String">
				<sv:value>
				<xsl:choose>
					<xsl:when test="$tag = 'StaffWeb:mpd-email-tips'">Staffweb:MPD_Tips</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:mpd-tips'">Staffweb:MPD_Tips</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:timeless-tips'">Staffweb:MPD_Tips</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:AllStaffEmail'">Staffweb:All_Hands_Email</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:news-archives'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:day-of-prayer'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:ibs'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:news-events'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:connection-newsletter'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:leadership'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:legacy-archives-hidden'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:legacy-project'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:branding-naming'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:new-staff-training'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:travel'">Staffweb:News_Events</xsl:when>
					<xsl:when test="$tag = 'StaffWeb:news-events-video'">Staffweb:News_Events</xsl:when>
				</xsl:choose>
				</sv:value>
			</sv:property>
		</xsl:if>
	</xsl:template>
	
	<!-- Includes 'recent headlines' component and configures it -->
	<xsl:template name="recent_headlines">
		<xsl:variable name="xWebsiteSectionValue">   
     		<xsl:choose> 
        		<xsl:when test="$xWebsiteSection_6_200 != ''"> 
        	    	<xsl:value-of select="$xWebsiteSection_6_200"/> 
        		</xsl:when> 
        		<xsl:when test="$xWebsiteSection != ''"> 
            		<xsl:value-of select="$xWebsiteSection"/> 
        		</xsl:when> 
		    </xsl:choose> 
		</xsl:variable>   
		<xsl:variable name="tags" as="element()*">   
     		<xsl:choose>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:mpd-email-tips'">
					<Item>Staffweb:MPD_Tips</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:mpd-tips'">
					<Item>Staffweb:MPD_Tips</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:timeless-tips'">
					<Item>Staffweb:MPD_Tips</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:AllStaffEmail'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:news-archives'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:day-of-prayer'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:ibs'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:news-events'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:connection-newsletter'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:leadership'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:legacy-archives-hidden'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:legacy-project'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:branding-naming'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:new-staff-training'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:travel'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>
				<xsl:when test="$xWebsiteSectionValue = 'StaffWeb:news-events-video'">
					<Item>Staffweb:MPD_Tips</Item>
					<Item>Staffweb:All_Hands_Email</Item>
					<Item>Staffweb:News_Events</Item>
				</xsl:when>						
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>   
		<xsl:if test="$tags != ''">
			<sv:node sv:name="recent_headlines">
				<sv:property sv:name="jcr:primaryType" sv:type="Name">
					<sv:value>nt:unstructured</sv:value>
				</sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/recent-headlines</sv:value>
				    </sv:property>
				    <sv:property sv:name="itemQuantity" sv:type="String">
				        <sv:value> 4 </sv:value>
				    </sv:property>
				    <sv:property sv:name="pathRef" sv:type="String">
				        <sv:value> /content/staffweb/us/en </sv:value>
				    </sv:property>
				    <sv:property sv:name="tags" sv:type="String">
						<xsl:for-each select="$tags">
    						<sv:value>
    							<xsl:value-of select="."/>
    						</sv:value>
						</xsl:for-each>
					</sv:property>
			  </sv:node>
		</xsl:if>
	</xsl:template>
</xsl:transform>