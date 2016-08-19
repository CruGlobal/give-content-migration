package org.cru.importer.providers.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.DataImportFactory;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.PageProvider;
import org.osgi.framework.Constants;

@Component(
	metatype = true,
	label = "Give importer - Provider factory",
	description = "Give importer - Provider factory")
@Service
@Properties({
	@Property(name = Constants.SERVICE_RANKING, intValue = 100)
})
public class DataImportFactoryImpl implements DataImportFactory {

	public MetadataProvider createMetadataProvider(ParametersCollector parametersCollector) throws Exception {
		return new MetadataProviderImpl(parametersCollector);
	}

	public PageProvider createPageProvider(ParametersCollector parametersCollector) throws Exception {
		return new PageProviderImpl(parametersCollector);
	}

	public ContentMapperProvider createContentMapperProvider(ParametersCollector parametersCollector) throws Exception {
		return new ContentMapperProviderImpl(parametersCollector);
	}

}
