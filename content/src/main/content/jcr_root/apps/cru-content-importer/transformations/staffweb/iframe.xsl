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
	<xsl:param name="dDocName" />

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
		    <sv:property sv:name="contentId" sv:type="String">
		        <sv:value><xsl:value-of select="$dDocName" /></sv:value>
		    </sv:property>
		    <sv:node sv:name="content-parsys">
			    <sv:property sv:name="jcr:primaryType" sv:type="Name">
			        <sv:value>nt:unstructured</sv:value>
			    </sv:property>
			    <sv:property sv:name="sling:resourceType" sv:type="String">
			        <sv:value>wcm/foundation/components/parsys</sv:value>
			    </sv:property>
			    <sv:node sv:name="free_form">
				    <sv:property sv:name="jcr:primaryType" sv:type="Name">
				        <sv:value>nt:unstructured</sv:value>
				    </sv:property>
				    <sv:property sv:name="sling:resourceType" sv:type="String">
				        <sv:value>StaffWeb/components/section/free-form</sv:value>
				    </sv:property>
				    <sv:property sv:name="htmlSource" sv:type="String">
						<sv:value>&lt;div class=&quot;masonry__item&quot;> &lt;div class=&quot;panel  panel-default&quot; > &lt;iframe style=&quot;height:1000px&quot; src=&quot;<xsl:value-of select="wcm:root/wcm:element[@name='iframe_src']" />&quot; id=&quot;iframe_id&quot; width=&quot;100%&quot; scrolling=&quot;no&quot; frameborder=&quot;0&quot;>&lt;/iframe>&lt;/div> &lt;/div> &lt;script> $(&quot;#iframe_id&quot;).load(function(){ StaffWeb.global.resizeIframe(); }); &lt;/script> </sv:value>
				    </sv:property>
			    </sv:node>
		    </sv:node>
		</sv:node>
	</xsl:template>

</xsl:transform>