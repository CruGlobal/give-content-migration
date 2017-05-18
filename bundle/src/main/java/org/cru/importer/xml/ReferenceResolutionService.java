package org.cru.importer.xml;

import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;

/**
 * Defines an interface to reference resolution service. It's used to
 * convert links from Stellent to AEM
 * 
 * @author Nestor de Dios
 *
 */
public interface ReferenceResolutionService {
    
    /**
     * Resolves a reference from Stellent to AEM
     * 
     * @param parametersCollector
     * @param currentMetadata
     * @param originalReference
     * @return
     */
    String resolveReference(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String originalReference) ;

    /**
     * Resolves all references from Stellent to AEM in a HTML source
     * 
     * @param parametersCollector
     * @param currentMetadata
     * @param htmlSourceParameter
     * @return
     */
    String resolveAllReferences(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String htmlSourceParameter);
    
}
