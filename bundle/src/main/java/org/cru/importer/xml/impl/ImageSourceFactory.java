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
	label = "Give importer - searchImage",
	description = "Search for an image in the DAM to get a reference. Use the searchImage URL withh parameter image"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "searchImage"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class ImageSourceFactory extends GiveSourceFactoryBase {
	
	private static final String PARAM_IMAGE = "image";

	private static final String TAG_IMG = "img";
	private static final String ATTRIB_SRC = "src";
	    
    @Reference
    private ReferenceResolutionService referenceResolutionService;

    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String image = "";
        if (params.containsKey(PARAM_IMAGE) && !params.get(PARAM_IMAGE).equals("")) {
            image = extractImage(parametersCollector, currentMetadata, params.get(PARAM_IMAGE));
        }
        return "<image>" + image + "</image>";
    }
    
    private String extractImage(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String htmlSource) {
        Document doc = Jsoup.parse(htmlSource);
        String image = doc.select(TAG_IMG).attr(ATTRIB_SRC);
        return referenceResolutionService.resolveReference(parametersCollector, currentMetadata, image);
    }

}
