package org.cru.importer.providers.impl;

import java.io.InputStream;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.RelativePathSection;
import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ResourceProvider;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Create pages for Give import process
 * 
 * @author Nestor de Dios
 *
 */
public class PageProviderImpl implements ResourceProvider {

	private PageManager pageManager;
	private String baselocation;
	private String pageTemplate;
	private String intermediateTemplate;
	private List<RelativePathSection> pathSections;
	private String pageAcceptRuleKey;
	private String pageAcceptRuleValue;
	
	public PageProviderImpl(ParametersCollector parametersCollector) {
		this.pageManager = parametersCollector.getResourceResolver().adaptTo(PageManager.class);
		this.baselocation = parametersCollector.getBaselocation();
		this.pageTemplate = parametersCollector.getPageTemplate();
		this.intermediateTemplate = parametersCollector.getIntermediateTemplate();
		this.pathSections = RelativePathSection.buildFromStrategy(parametersCollector.getPathCreationStrategy());
		if (parametersCollector.getPageAcceptRule().equals("")) {
			this.pageAcceptRuleKey = null;
			this.pageAcceptRuleValue = null;
		} else {
			String[] pageAcceptRule = parametersCollector.getPageAcceptRule().split("=");
			this.pageAcceptRuleKey = pageAcceptRule[0];
			this.pageAcceptRuleValue = pageAcceptRule[1];
		}
	}

	public ResourceInfo getResource(ResourceMetadata metadata, InputStream inputStream) throws Exception {
		if (this.pageAcceptRuleKey != null && metadata.getPropertyNames().contains(this.pageAcceptRuleKey)) {
			String val = metadata.getValue(this.pageAcceptRuleKey);
			if (!val.equals(this.pageAcceptRuleValue)) {
				return null;
			}
		}
		String relativePath = getRelativePath(metadata);
		Page page = pageManager.getPage(this.baselocation + relativePath);
		if (page != null) {
			return new ResourceInfo(page.adaptTo(Resource.class),false);
		} else {
			page = buildPage(relativePath);
			return new ResourceInfo(page.adaptTo(Resource.class),true);
		}
	}

	/**
	 * Builds the relative page path from the path creation strategy
	 * @param metadata
	 * @return
	 * @throws Exception 
	 */
	private String getRelativePath(ResourceMetadata metadata) throws Exception {
		return RelativePathSection.buildPath(pathSections, metadata);
	}
	
	private Page buildPage(String relativePath) throws Exception {
		Page basePage = pageManager.getPage(baselocation);
		String[] targetPathChunks = relativePath.substring(1).split("/");
		for (int i=0; i<targetPathChunks.length; i++) {
			Resource res = basePage.adaptTo(Resource.class);
			res = res.getChild(targetPathChunks[i]);
			if (res == null) {
				String template = null;
				if (i<targetPathChunks.length-1) {
					template = intermediateTemplate;
				} else {
					template = pageTemplate;
				}
				basePage = pageManager.create(basePage.getPath(), targetPathChunks[i], template, targetPathChunks[i], false);
			} else {
				basePage = res.adaptTo(Page.class);
			}
		}
		return basePage;
	}

}
