package org.cru.importer.providers;

import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ResourceMetadata;

/**
 * Fills the resource content
 * 
 * @author Nestor de Dios
 *
 */
public interface ContentMapperProvider {

	/**
	 * Fills the resource content
	 * 
	 * @param resource
	 * @param metadata
	 * @param fileContent
	 * @return 
	 * @throws Exception
	 */
	public boolean mapFields(Resource resource, ResourceMetadata metadata, byte[] fileContent) throws Exception;
	
}
