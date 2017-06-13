package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ContentMapperProvider;

public class PartialPageContentMapperProviderImpl implements ContentMapperProvider {
    
    private static final String CACHE_KEY_FRAGMENT_FILE = "fragment";
 
    String columnFilename;
    
    DocumentBuilder xmlBuilder;
    Transformer transformer;
    
    Map<String, byte[]> fragmentsCache;
    Map<String, byte[]> principalCache;
    
    ContentMapperProviderImpl principalContentMapper;
    ContentMapperProviderImpl fragmentContentMapper;
    
    ParametersCollector parametersCollector;

    public PartialPageContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
        this.fragmentsCache = new HashMap<String, byte[]>();
        this.principalCache = new HashMap<String, byte[]>();
        this.columnFilename = parametersCollector.getColumnFileName();
        this.parametersCollector = parametersCollector;
        this.principalContentMapper = new ContentMapperProviderImpl(parametersCollector);
        this.fragmentContentMapper = new ContentMapperProviderImpl(parametersCollector, "/apps/cru-content-importer/transformations/staffweb/landingpage-fragment.xsl");
    }

    public boolean mapFields(Resource resource, ResourceMetadata metadata, byte[] fileContent) throws Exception {
        if (metadata.isFragment()) {
            fragmentsCache.put(metadata.getFilename(), fileContent);
            String baseFileName = FilenameUtils.getBaseName(metadata.getValue(columnFilename));
            if (principalCache.containsKey(baseFileName) && verifyFragments(metadata)) {
                byte[] principalContent = principalCache.remove(baseFileName);
                processImport(principalContent, metadata, resource);
                return true;
            }
        } else {
            if (verifyFragments(metadata)) {
                processImport(fileContent, metadata, resource);
                return true;
            } else {
                principalCache.put(metadata.getFilename(), fileContent);
            }
        }
        return false;
    }

    private boolean verifyFragments(ResourceMetadata metadata) {
        for (String fragment : metadata.getRequiredFragments()) {
            if (!fragmentsCache.containsKey(fragment)) {
                return false;
            }
        }
        return true;
    }
    
    public void processImport(byte[] principalContent, ResourceMetadata metadata, Resource resource) throws Exception {
        byte[] content = principalContentMapper.processTransformation(resource, metadata, principalContent);
        for (String fragment : metadata.getRequiredFragments()) {
            if (fragmentsCache.containsKey(fragment)) {
                byte[] fragmentContent = fragmentsCache.get(fragment);
                parametersCollector.putCache(fragment, clearXml(fragmentContent));
                fragmentContentMapper.setTransformParameter(CACHE_KEY_FRAGMENT_FILE, fragment);
                content = fragmentContentMapper.processTransformation(resource, metadata, content);
            }
        }
        InputStream result = new ByteArrayInputStream(content);
        principalContentMapper.importResult(principalContentMapper.getResourceToOverride(resource), result);
    }

    private String clearXml(byte[] fragmentContent) {
        String fragment = new String(fragmentContent);
        fragment = fragment.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim(); //Remove xml declaration
        fragment = fragmentContentMapper.sanitizeXml(fragment);
        return fragment;
    }
    
    public Map<String, byte[]> getPendingImportFiles() {
        return principalCache;
    }

}
