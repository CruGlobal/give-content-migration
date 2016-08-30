package org.cru.importer.bean;

import javax.jcr.Binary;

import org.apache.sling.api.SlingHttpServletRequest;

public class ParametersCollector {

	private SlingHttpServletRequest request;
	private String baselocation;
	private String configpath;
	private Binary contentFile;
	private String columnFileName;
	private String columnDesignation;
	private int rowColumnNames;
	private String xsltPath;
	private String pageTemplate;
	private String intermediateTemplate;
	private String ignoreFilesPattern;
	private String pageAcceptRule;
	private String factoryType;
	
	public String getBaselocation() {
		return baselocation;
	}
	
	public void setBaselocation(String baselocation) {
		this.baselocation = baselocation;
	}
	
	public String getConfigpath() {
		return configpath;
	}
	
	public void setConfigpath(String configpath) {
		this.configpath = configpath;
	}

	public Binary getContentFile() throws Exception {
		return this.contentFile;
	}

	public void setContentFile(Binary binary) {
		this.contentFile = binary;
	}

	public SlingHttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(SlingHttpServletRequest request) {
		this.request = request;
	}

	public String getColumnFileName() {
		return columnFileName;
	}

	public void setColumnFileName(String columnFileName) {
		this.columnFileName = columnFileName;
	}

	public int getRowColumnNames() {
		return rowColumnNames;
	}

	public void setRowColumnNames(int rowColumnNames) {
		this.rowColumnNames = rowColumnNames;
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setXsltPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	public String getPageTemplate() {
		return pageTemplate;
	}

	public void setPageTemplate(String pageTemplate) {
		this.pageTemplate = pageTemplate;
	}

	public String getIntermediateTemplate() {
		return intermediateTemplate;
	}

	public void setIntermediateTemplate(String intermediateTemplate) {
		this.intermediateTemplate = intermediateTemplate;
	}

	public String getColumnDesignation() {
		return columnDesignation;
	}

	public void setColumnDesignation(String columnDesignation) {
		this.columnDesignation = columnDesignation;
	}

	public void setIgnoreFilesPattern(String ignoreFilesPattern) {
		this.ignoreFilesPattern = ignoreFilesPattern;
	}

	public String getIgnoreFilesPattern() {
		return ignoreFilesPattern;
	}

	public String getPageAcceptRule() {
		return pageAcceptRule;
	}

	public void setPageAcceptRule(String pageAcceptRule) {
		this.pageAcceptRule = pageAcceptRule;
	}

	public String getFactoryType() {
		return factoryType;
	}

	public void setFactoryType(String factoryType) {
		this.factoryType = factoryType;
	}

}
