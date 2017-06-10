package org.cru.importer.providers;

import java.util.List;

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
	public List<ResourceMetadata> getMetadata(String filename) throws Exception;
	
	/**
	 * Decodes a property name of the metadata provider from the given name.
	 * The given name can be a property name or a regex to match with a property name.
	 * 
	 * @param propertyName
	 * @return
	 * @throws Exception 
	 */
	public String decodePropertyName(String propertyName) throws Exception;
	
}
