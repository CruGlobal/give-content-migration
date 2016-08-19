package org.cru.importer.providers;

import java.util.Map;

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
	public Map<String,String> getMetadata(String filename) throws Exception;
	
}
