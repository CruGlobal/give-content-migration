package org.cru.importer.providers;

import org.cru.importer.bean.ResourceMetadata;

/**
 * Extract the metadata corresponding to a given file
 * 
 * @author Nestor de Dios
 *
 */
public interface MetadataProvider {

	/**
	 * Extract the metadata corresponding to a given file
	 * 
	 * @param filename
	 * @return
	 * @throws Exception 
	 */
	public ResourceMetadata getMetadata(String filename) throws Exception;
	
}
