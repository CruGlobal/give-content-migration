package org.cru.importer.providers;

import org.cru.importer.bean.ParametersCollector;

/**
 * Defines afactory to create the required providers to the import process
 * 
 * @author Nestor de Dios
 *
 */
public interface DataImportFactory {

	/**
	 * Creates a metadata provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception;
	
	/**
	 * Cretes a page provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public PageProvider createPageProvider(ParametersCollector parametersCollector) throws Exception;
	
	/**
	 * Creates a content mapper provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception;
	
}
