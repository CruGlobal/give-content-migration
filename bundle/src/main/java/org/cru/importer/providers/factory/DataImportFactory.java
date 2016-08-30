package org.cru.importer.providers.factory;

import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.ResourceProvider;

/**
 * Defines afactory to create the required providers to the import process
 * 
 * @author Nestor de Dios
 *
 */
public interface DataImportFactory {

	public static final String OSGI_PROPERTY_TYPE = "type";
	
	/**
	 * Creates a metadata provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception;
	
	/**
	 * Cretes a resource provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public ResourceProvider createResourceProvider(ParametersCollector parametersCollector) throws Exception;
	
	/**
	 * Creates a content mapper provider
	 * 
	 * @param parametersCollector
	 * @return
	 * @throws Exception
	 */
	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception;
	
}
