package org.cru.importer.xml.impl;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.cru.importer.xml.ReferenceResolutionService;
import org.osgi.framework.Constants;

import net.sf.saxon.trans.XPathException;

@Component(
	metatype = true,
	label = "Give importer - UrlTransformerFactory",
    description = "Transforms all URL references from stellent to AEM in a html source"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "transformUrls"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class UrlTransformerFactory extends GiveSourceFactoryBase {
	
	private static final String PARAM_TEXT = "htmlSource";
	
	@Reference
	private ReferenceResolutionService referenceResolutionService;
	
    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String html = "";
        if (params.containsKey(PARAM_TEXT) && !params.get(PARAM_TEXT).equals("")) {
            html = referenceResolutionService.resolveAllReferences(parametersCollector, currentMetadata, params.get(PARAM_TEXT));
            html = StringEscapeUtils.escapeXml11(html);
        }
        return "<html>" + html + "</html>";
    }

}
