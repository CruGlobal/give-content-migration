package org.cru.importer.providers;

import java.io.InputStream;

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
	 * @param xmlInputStream
	 * @throws Exception
	 */
	public void mapFields(Resource resource, ResourceMetadata metadata, InputStream xmlInputStream) throws Exception;
	
}
