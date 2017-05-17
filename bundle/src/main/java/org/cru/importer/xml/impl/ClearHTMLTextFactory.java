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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.osgi.framework.Constants;

import net.sf.saxon.trans.XPathException;

@Component(
	metatype = true,
	label = "Give importer - escapeHTMLTags",
	description = "Extracts and returns a plain text whithout html tags from a html source"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "escapeHTMLTags"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class ClearHTMLTextFactory extends GiveSourceFactoryBase {
	
	private static final String PARAM_TEXT = "htmlSource";

    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String plainText = "";
        if (params.containsKey(PARAM_TEXT) && !params.get(PARAM_TEXT).equals("")) {
            Document doc = Jsoup.parse(params.get(PARAM_TEXT));
            doc.outputSettings().charset("UTF-8");
            plainText = Jsoup.clean(doc.body().html(), Whitelist.simpleText());
        }
        return "<plaintext>" + plainText + "</plaintext>";
    }
}
