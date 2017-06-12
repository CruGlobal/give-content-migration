package org.cru.importer.service;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;

/**
 * Defines a post processing service
 * 
 * @author Nestor de Dios
 *
 */
public interface PostProcessService {
    
    public static final String OSGI_PROPERTY_PROCESS = "process";

    /**
     * Process tasks after each individual file is imported.
     * 
     * @param parametersCollector
     * @param currentMetadata
     * @param currentResource
     * @throws Exception
     */
    void process(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Resource currentResource) throws Exception;

    /**
     * Process tasks after all individual files are imported
     * 
     * @param parametersCollector
     * @param postProcessServices 
     * @throws Exception 
     */
    void process(ParametersCollector parametersCollector, List<PostProcessService> postProcessServices) throws Exception;
    
}
