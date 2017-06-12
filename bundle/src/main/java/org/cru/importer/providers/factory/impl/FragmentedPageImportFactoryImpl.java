package org.cru.importer.providers.factory.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.ResourceProvider;
import org.cru.importer.providers.factory.DataImportFactory;
import org.cru.importer.providers.impl.PageProviderImpl;
import org.cru.importer.providers.impl.PartialPageContentMapperProviderImpl;
import org.cru.importer.providers.impl.PartialPageMetadataProviderImpl;
import org.osgi.framework.Constants;

@Component(
	metatype = true,
	label = "Give importer - Fragmented pages provider factory",
	description = "Give importer - Fragmented pages provider factory")
@Service
@Properties({
	@Property(name = DataImportFactory.OSGI_PROPERTY_TYPE, value = "fragmentedPage"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class FragmentedPageImportFactoryImpl implements DataImportFactory {
    
    public static final String CACHE_KEY_METADATA_PROVIDER = "fragmentMetadataProvider";
    public static final String CACHE_KEY_CONTENT_MAPPER_PROVIDER = "fragmentContentMapperProvider";
    public static final String CACHE_KEY_RESOURCE_PROVIDER = "fragmentResourceProvider";
    

	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception {
	    PartialPageMetadataProviderImpl provider = new PartialPageMetadataProviderImpl(parametersCollector);
        parametersCollector.putCache(CACHE_KEY_METADATA_PROVIDER, provider);
		return provider;
	}

	public ResourceProvider createResourceProvider(ParametersCollector parametersCollector, MetadataProvider metadataProvider) throws Exception {
	    PageProviderImpl provider = new PageProviderImpl(parametersCollector, metadataProvider);
        parametersCollector.putCache(CACHE_KEY_RESOURCE_PROVIDER, provider);
        return provider;
	}

	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception {
		PartialPageContentMapperProviderImpl provider = new PartialPageContentMapperProviderImpl(parametersCollector);
        parametersCollector.putCache(CACHE_KEY_CONTENT_MAPPER_PROVIDER, provider);
        return provider;
	}

}
