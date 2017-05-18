package org.cru.importer.service;

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

    void process(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Resource currentResource) throws Exception;
    
}
