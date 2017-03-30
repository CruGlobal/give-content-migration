package org.cru.importer.bean;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;

import org.apache.sling.api.resource.ResourceResolver;

public class ParametersCollector {

    private ResourceResolver resourceResolver;
	private String baselocation;
	private Binary contentFile;
	private String columnFileName;
	private String columnMimeType;
	private String[] pathCreationStrategy;
	private int rowColumnNames;
	private String xsltPath;
	private String pageTemplate;
	private String intermediateTemplate;
	private String acceptFilesPattern;
	private String pageAcceptRule;
	private String factoryType;
	private InputStream additionalMappingFile;
	private Map<String, String> sanitizationMap;
	private String[] acceptedDateFormats;
	private Map<String, Object> cache;
	
	public ParametersCollector() {
	    cache = new HashMap<String, Object>();
	}
	
	public boolean isCached(String key) {
	    return cache.containsKey(key);
	}
	
	public void putCache(String key, Object value){
	    cache.put(key, value);
	}
	
	public Object getCached(String key) {
	    return cache.get(key);
	}
	
    public String getBaselocation() {
		return baselocation;
	}
	
	public void setBaselocation(String baselocation) {
		this.baselocation = baselocation;
	}

	public Binary getContentFile() throws Exception {
		return this.contentFile;
	}

	public void setContentFile(Binary binary) {
		this.contentFile = binary;
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

	public InputStream getAdditionalMappingFile() {
		return additionalMappingFile;
	}

	public void setAdditionalMappingFile(InputStream additionalMappingFile) {
		this.additionalMappingFile = additionalMappingFile;
	}

	public String[] getPathCreationStrategy() {
		return pathCreationStrategy;
	}

	public void setPathCreationStrategy(String[] pathCreationStrategy) {
		this.pathCreationStrategy = pathCreationStrategy;
	}

	public String getColumnMimeType() {
		return columnMimeType;
	}

	public void setColumnMimeType(String columnMimeType) {
		this.columnMimeType = columnMimeType;
	}

	public String getAcceptFilesPattern() {
		return acceptFilesPattern;
	}

	public void setAcceptFilesPattern(String acceptFilesPattern) {
		this.acceptFilesPattern = acceptFilesPattern;
	}

    public Map<String, String> getSanitizationMap() {
        return sanitizationMap;
    }

    public void setSanitizationMap(Map<String, String> sanitizationMap) {
        this.sanitizationMap = sanitizationMap;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public void setResourceResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public String[] getAcceptedDateFormats() {
        return acceptedDateFormats;
    }
    
    public void setAcceptedDateFormats(String[] acceptedDateFormats) {
       this.acceptedDateFormats = acceptedDateFormats;
    }
}
