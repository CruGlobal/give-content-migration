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
		        <sv:value><xsl:value-of select="$dDocTitle" /></sv:value>
		    </sv:property>
		    <sv:property sv:name="subtitle" sv:type="String">
		        <sv:value><xsl:value-of select="wcm:root/wcm:element[@name='subtitle']" /></sv:value>
		    </sv:property>
		    <sv:node sv:name="image">
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
			    <xsl:apply-templates select="wcm:root/wcm:list/wcm:row"/>
		    </sv:node>
		</sv:node>
	</xsl:template>
	
	<xsl:template match="wcm:row">
		<sv:node >
			<xsl:attribute name="sv:name"><xsl:value-of select="concat('article_long_form_',fn:position())"/></xsl:attribute>
			<sv:property sv:name="jcr:primaryType" sv:type="Name">
				<sv:value>nt:unstructured</sv:value>
			</sv:property>
			<sv:property sv:name="sling:resourceType" sv:type="String">
				<sv:value>StaffWeb/components/section/article-long-form</sv:value>
			</sv:property>
			<sv:property sv:name="title" sv:type="String">
				<sv:value><xsl:value-of select="wcm:element[@name='section_name']" /></sv:value>
			</sv:property>
			<sv:property sv:name="show" sv:type="Boolean">
				<sv:value>true</sv:value>
			</sv:property>
			<sv:property sv:name="usePageTitle" sv:type="Boolean">
				<sv:value>true</sv:value>
			</sv:property>
			 <sv:node sv:name="inset-sidebar">
				    <sv:property sv:name="jcr:primaryType" sv:type="Name">
				        <sv:value>nt:unstructured</sv:value>
				    </sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/inset-sidebar</sv:value>
				    </sv:property>
				    <sv:property sv:name="useLetters" sv:type="Boolean">
				    	<sv:value>true</sv:value>
				    </sv:property>
				    <sv:property sv:name="usePageSubTitle" sv:type="Boolean">
				        <sv:value>true</sv:value>
				   	</sv:property>
				    <sv:node sv:name="search-box">
						<sv:property sv:name="jcr:primaryType" sv:type="Name">
				        	<sv:value>nt:unstructured</sv:value>
				    	</sv:property>
				    	<sv:property sv:name="sling:resourceType" sv:type="String">
				        	<sv:value>StaffWeb/components/section/article-long-form/searchbox</sv:value>
				    	</sv:property>
				    	<sv:property sv:name="searchBoxPlaceHolder" sv:type="String">
				        	<sv:value>Search This Handbook</sv:value>
				    	</sv:property>
		    		</sv:node>
		    </sv:node>
			<sv:node sv:name="article-long-form-parsys">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>wcm/foundation/components/parsys</sv:value>
			    </sv:property>
			    <sv:node sv:name="text">
				    <sv:property sv:name="jcr:primaryType" sv:type="Name">
				        <sv:value>nt:unstructured</sv:value>
				    </sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/text</sv:value>
				    </sv:property>
				     <sv:property sv:name="text" sv:type="String">
				     	<sv:value><xsl:value-of select="wcm:element[@name='section_content']" /></sv:value>
				    </sv:property>
		    	</sv:node>
		    </sv:node>
		</sv:node>	
	</xsl:template>

</xsl:transform>