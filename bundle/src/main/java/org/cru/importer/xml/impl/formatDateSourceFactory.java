package org.cru.importer.xml.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.trans.XPathException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.util.UrlUtil;
import org.cru.importer.xml.GiveSourceFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	metatype = true,
	label = "Give importer - formatDate",
	description = "Format a date from imported xml files to AEM date format. Use the formatDate URL with parameter date"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "formatDate"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class formatDateSourceFactory implements GiveSourceFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(formatDateSourceFactory.class);
	
	private static final String PARAM_DATE = "date";
	private DateFormat incomingDateFormatter = null;
	private DateFormat alternativeIncomingDateFormatter = null;
	private DateFormat aemDateFormatter = null;
	
	public Source resolve(ResourceResolver resourceResolver, String parameters) throws XPathException {
		Map<String, String> params;
		try {
			params = UrlUtil.splitQuery(parameters);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Error parsing parmeters: " + parameters + "  - Error: " + e.getMessage());
			params = new HashMap<String, String>();
		}
		String date = "";
		if (params.containsKey(PARAM_DATE) && !params.get(PARAM_DATE).equals("")) {
			String origDate = params.get(PARAM_DATE);
			Date extractedDate = null;
			try {
				extractedDate = getIncomingDateFormatter().parse(origDate);
			} catch (ParseException e) {
				try {
					extractedDate = getAlternativeIncomingDateFormatter().parse(origDate);
				} catch (ParseException e1) {
					LOGGER.warn("Error parsing date: " + origDate);
				}
			}
			if (extractedDate != null) {
				date = getAemDateFormatter().format(extractedDate) + "T00:00:00.000+00:00";
			}
		}
		date = "<date>" + date + "</date>";
		InputStream stream = new ByteArrayInputStream(date.getBytes(StandardCharsets.UTF_8));
		return new StreamSource(stream);
	}

	public DateFormat getIncomingDateFormatter() {
		if (incomingDateFormatter == null) {
			incomingDateFormatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
		}
		return incomingDateFormatter;
	}

	public DateFormat getAemDateFormatter() {
		if (aemDateFormatter == null) {
			aemDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		}
		return aemDateFormatter;
	}

	public DateFormat getAlternativeIncomingDateFormatter() {
		if (alternativeIncomingDateFormatter == null) {
			alternativeIncomingDateFormatter = new SimpleDateFormat("MMMM yyyy", Locale.US);
		}
		return alternativeIncomingDateFormatter;
	}

}
