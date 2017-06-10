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

	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception {
		return new PartialPageMetadataProviderImpl(parametersCollector);
	}

	public ResourceProvider createResourceProvider(ParametersCollector parametersCollector, MetadataProvider metadataProvider) throws Exception {
		return new PageProviderImpl(parametersCollector, metadataProvider);
	}

	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception {
		return new PartialPageContentMapperProviderImpl(parametersCollector);
	}

}
