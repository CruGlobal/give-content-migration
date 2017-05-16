package org.cru.importer.xml.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.xml.DateParserService;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.osgi.framework.Constants;

import net.sf.saxon.trans.XPathException;

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
public class FormatDateSourceFactory extends GiveSourceFactoryBase implements DateParserService {
	
    private static final String CACHE_DATE_FORMATS_KEY = "dateFormats";

	private static final String AEM_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.'000+00:00'";
    private static final DateFormat AEM_DATE_FORMATTER = new SimpleDateFormat(AEM_FORMAT, Locale.US);

    private static final String PARAM_DATE = "date";
	private static final String PARAM_OUTPUT_FORMAT = "outputformat";
	
    @Override
    protected String resolve(ParametersCollector parametersCollector, Map<String, String> params)
            throws XPathException {
        String date = "";
        if (params.containsKey(PARAM_DATE) && !params.get(PARAM_DATE).equals("")) {
            String origDate = params.get(PARAM_DATE);
            Date extractedDate = parseDate(parametersCollector, origDate);
            String outputFormat = params.get(PARAM_OUTPUT_FORMAT);
            if (outputFormat!=null && !"".equals(outputFormat)){
                date = new SimpleDateFormat(outputFormat, Locale.US).format(extractedDate);
            } else {
                date = AEM_DATE_FORMATTER.format(extractedDate);
            }
        }
        return "<date>" + date + "</date>";
    }
	
	public Date parseDate(ParametersCollector parametersCollector, String origDate) throws XPathException {
        for (DateFormat formatter : getIncomingDateFormatters(parametersCollector)) {
            try {
                Date extractedDate = formatter.parse(origDate);
                return extractedDate;
            } catch (ParseException e) {
            }
        }
        throw new XPathException("Invalid date format: " + origDate);
	}

	@SuppressWarnings("unchecked")
    public List<DateFormat> getIncomingDateFormatters(ParametersCollector parametersCollector) {
	    if (!parametersCollector.isCached(CACHE_DATE_FORMATS_KEY)) {
	        List<DateFormat> incomingDateFormatters = new LinkedList<DateFormat>();
			for (String format : parametersCollector.getAcceptedDateFormats()) {
				incomingDateFormatters.add(new SimpleDateFormat(format, Locale.US));
			}
			parametersCollector.putCache(CACHE_DATE_FORMATS_KEY, incomingDateFormatters);
			return incomingDateFormatters;
		}
		return (List<DateFormat>) parametersCollector.getCached(CACHE_DATE_FORMATS_KEY);
	}

}
