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
import org.jsoup.safety.Whitelist;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ClearHTMLTextFactory implements GiveSourceFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ClearHTMLTextFactory.class);
	private static final String PARAM_TEXT = "htmlSource";

	public Source resolve(ParametersCollector parametersCollector, String parameters) throws XPathException {
		Map<String, String> params;
		try {
			params = UrlUtil.splitQuery(parameters);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Error parsing parmeters: " + parameters + "  - Error: " + e.getMessage());
			params = new HashMap<String, String>();
		}
		String plainText = "";
		if (params.containsKey(PARAM_TEXT) && !params.get(PARAM_TEXT).equals("")) {
			String htmlSourceParameter = params.get(PARAM_TEXT);
			if (htmlSourceParameter != null) {
			    Document doc = Jsoup.parse( htmlSourceParameter );
			    doc.outputSettings().charset("UTF-8");
			    plainText = Jsoup.clean( doc.body().html(), Whitelist.simpleText() );
			}
		}
		plainText = "<plaintext>" + plainText + "</plaintext>";
		InputStream stream = new ByteArrayInputStream(plainText.getBytes(StandardCharsets.UTF_8));
		return new StreamSource(stream);
	}
}
