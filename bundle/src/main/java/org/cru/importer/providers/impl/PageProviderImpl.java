package org.cru.importer.providers.impl;

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.PageInfo;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.PageProvider;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Create pages for Give import process
 * 
 * @author Nestor de Dios
 *
 */
public class PageProviderImpl implements PageProvider {

	private PageManager pageManager;
	private String baselocation;
	private String pageTemplate;
	private String intermediateTemplate;
	private String columnDesignation;
	private String pageAcceptRuleKey;
	private String pageAcceptRuleValue;
	
	public PageProviderImpl(ParametersCollector parametersCollector) {
		this.pageManager = parametersCollector.getRequest().getResourceResolver().adaptTo(PageManager.class);
		this.baselocation = parametersCollector.getBaselocation();
		this.pageTemplate = parametersCollector.getPageTemplate();
		this.intermediateTemplate = parametersCollector.getIntermediateTemplate();
		this.columnDesignation = parametersCollector.getColumnDesignation();
		if (parametersCollector.getPageAcceptRule().equals("")) {
			this.pageAcceptRuleKey = null;
			this.pageAcceptRuleValue = null;
		} else {
			String[] pageAcceptRule = parametersCollector.getPageAcceptRule().split("=");
			this.pageAcceptRuleKey = pageAcceptRule[0];
			this.pageAcceptRuleValue = pageAcceptRule[1];
		}
	}

	public PageInfo getPage(Map<String, String> metadata) throws Exception {
		if (this.pageAcceptRuleKey != null && metadata.containsKey(this.pageAcceptRuleKey)) {
			String val = metadata.get(this.pageAcceptRuleKey);
			if (!val.equals(this.pageAcceptRuleValue)) {
				return null;
			}
		}
		String relativePath = getRelativePath(metadata);
		Page page = pageManager.getPage(this.baselocation + relativePath);
		if (page != null) {
			return new PageInfo(page,false);
		} else {
			page = buildPage(relativePath);
			return new PageInfo(page,true);
		}
	}

	/**
	 * Builds the relative page path from the designation number
	 * @param metadata
	 * @return
	 * @throws Exception 
	 */
	private String getRelativePath(Map<String, String> metadata) throws Exception {
		if (metadata.containsKey(this.columnDesignation)) {
			String designation = metadata.get(this.columnDesignation);
			if (!designation.equals("")) {
				StringBuilder path = new StringBuilder();
				for (int i=0;i<5;i++) {
					if (i<designation.length()) {
						path.append("/" + designation.substring(i, i+1));
					}
				}
				path.append("/" + designation);
				return path.toString();
			} else {
				throw new Exception("The excel file contains an empty value for the column \"Designation\"");
			}
		} else {
			throw new Exception("The excel file does not contains the column labeled as \"Designation\" at row 3.");
		}
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
