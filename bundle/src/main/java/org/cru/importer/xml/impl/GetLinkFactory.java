package org.cru.importer.xml.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.service.ReferenceResolutionService;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.osgi.framework.Constants;

import net.sf.saxon.trans.XPathException;

@Component(
	metatype = true,
	label = "Give importer - getLinkFromHTML",
    description = "Extracts and returns a link from a html source"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "getLinkFromHTML"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class GetLinkFactory extends GiveSourceFactoryBase {
	
	private static final String PARAM_TEXT = "htmlSource";
	private static final String TAG_A = "a";
	private static final String ATTRIB_HREF = "href";
	
    @Reference
    private ReferenceResolutionService referenceResolutionService;
	
    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String link = "";
        if (params.containsKey(PARAM_TEXT) && !params.get(PARAM_TEXT).equals("")) {
            link = extractLink(parametersCollector, currentMetadata, params.get(PARAM_TEXT));
        }
        return "<link>" + link + "</link>";
    }

    private String extractLink(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String htmlSourceParameter) {
        Document doc = Jsoup.parse(htmlSourceParameter);
        String link = doc.select(TAG_A).attr(ATTRIB_HREF);
        link = referenceResolutionService.resolveReference(parametersCollector, currentMetadata, link);
        return link;
    }

}
