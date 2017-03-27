package org.cru.importer.xml.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.util.UrlUtil;
import org.cru.importer.xml.GiveSourceFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class GetLinkFactory implements GiveSourceFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(GetLinkFactory.class);
	private static final String PARAM_TEXT = "htmlSource";
	private static final String TAG_A = "a";
	private static final String ATTRIB_HREF = "href";
	
	public Source resolve(ParametersCollector parametersCollector, String parameters) throws XPathException {
		Map<String, String> params;
		try {
			params = UrlUtil.splitQuery(parameters);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Error parsing parmeters: " + parameters + "  - Error: " + e.getMessage());
			params = new HashMap<String, String>();
		}
		String link = "";
		if (params.containsKey(PARAM_TEXT) && !params.get(PARAM_TEXT).equals("")) {
			String htmlSourceParameter = params.get(PARAM_TEXT);
			if (htmlSourceParameter != null) {
			    link = extractLink(htmlSourceParameter);
			}
		}
		link = "<link>" + link + "</link>";
		InputStream stream = new ByteArrayInputStream(link.getBytes(StandardCharsets.UTF_8));
		return new StreamSource(stream);
	}

    private String extractLink(String htmlSourceParameter) {
        Document doc = Jsoup.parse( htmlSourceParameter );
        String link = doc.select(TAG_A).attr(ATTRIB_HREF);
        if(!link.startsWith("http")){
            //TODO: TBD how to process internal links
            link = "#";
        }
        return link;
    }
}
