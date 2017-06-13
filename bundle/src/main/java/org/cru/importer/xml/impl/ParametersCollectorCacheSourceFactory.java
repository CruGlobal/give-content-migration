package org.cru.importer.xml.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.osgi.framework.Constants;

import net.sf.saxon.trans.XPathException;

@Component(
	metatype = true,
	label = "Give importer - collectorCache",
	description = "extract information from Parameters Collector Cache"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "collectorCache"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class ParametersCollectorCacheSourceFactory extends GiveSourceFactoryBase {

    private static final String PARAM_KEY = "key";

    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String cacheValue = "";
        if (params.containsKey(PARAM_KEY) && !params.get(PARAM_KEY).equals("")) {
            cacheValue = (String) parametersCollector.getCached(params.get(PARAM_KEY));
        }
        return "<cacheValue>" + cacheValue + "</cacheValue>";
    }
	
}
