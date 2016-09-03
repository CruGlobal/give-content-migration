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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.trans.XPathException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
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
	
	private static final String[] AVAILABLE_INCOMING_FORMATS = {"MMMM dd, yyyy","MMMM yyyy","dd-MMMM-yy"};
	private static final String AEM_FORMAT = "yyyy-MM-dd";
	
	private static final String PARAM_DATE = "date";
	private List<DateFormat> incomingDateFormatters = null;
	private DateFormat aemDateFormatter = null;
	
	public Source resolve(ParametersCollector parametersCollector, String parameters) throws XPathException {
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
			for (DateFormat formatter : getIncomingDateFormatters()) {
				try {
					extractedDate = formatter.parse(origDate);
					break;
				} catch (ParseException e) {
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

	public List<DateFormat> getIncomingDateFormatters() {
		if (incomingDateFormatters == null) {
			incomingDateFormatters = new LinkedList<DateFormat>();
			for (String format : AVAILABLE_INCOMING_FORMATS) {
				incomingDateFormatters.add(new SimpleDateFormat(format, Locale.US));
			}
		}
		return incomingDateFormatters;
	}

	public DateFormat getAemDateFormatter() {
		if (aemDateFormatter == null) {
			aemDateFormatter = new SimpleDateFormat(AEM_FORMAT, Locale.US);
		}
		return aemDateFormatter;
	}

}
