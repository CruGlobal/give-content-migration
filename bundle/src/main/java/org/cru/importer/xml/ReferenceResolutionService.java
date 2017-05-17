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
    
    String resolveReference(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String originalReference) ;

}
