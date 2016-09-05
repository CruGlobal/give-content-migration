package org.cru.importer.providers;

import java.io.InputStream;

import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;

/**
 * Create or get a resource related to a given metadata
 * 
 * @author Nestor de Dios
 *
 */
public interface ResourceProvider {

	/**
	 * Create or get a resource related to a given metadata
	 * 
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	public ResourceInfo getResource(ResourceMetadata metadata, InputStream inputStream) throws Exception;
	
}
