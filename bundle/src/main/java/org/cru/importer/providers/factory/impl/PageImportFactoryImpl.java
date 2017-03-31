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
import org.cru.importer.providers.impl.ContentMapperProviderImpl;
import org.cru.importer.providers.impl.MetadataProviderImpl;
import org.cru.importer.providers.impl.PageProviderImpl;
import org.osgi.framework.Constants;

@Component(
	metatype = true,
	label = "Give importer - Pages provider factory",
	description = "Give importer - Pages provider factory")
@Service
@Properties({
	@Property(name = DataImportFactory.OSGI_PROPERTY_TYPE, value = "page"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class PageImportFactoryImpl implements DataImportFactory {

	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception {
		return new MetadataProviderImpl(parametersCollector);
	}

	public ResourceProvider createResourceProvider(ParametersCollector parametersCollector, MetadataProvider metadataProvider) throws Exception {
		return new PageProviderImpl(parametersCollector, metadataProvider);
	}

	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception {
		return new ContentMapperProviderImpl(parametersCollector);
	}

}
