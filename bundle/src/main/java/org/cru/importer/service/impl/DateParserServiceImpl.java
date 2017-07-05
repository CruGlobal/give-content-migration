package org.cru.importer.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.service.DateParserService;

import net.sf.saxon.trans.XPathException;

@Component(
    metatype = true,
    label = "Give importer - DateParserServiceImpl",
    description = "Format a date with a set of predefined formats"
)
@Service
public class DateParserServiceImpl implements DateParserService {

    private static final String CACHE_DATE_FORMATS_KEY = "dateFormats";
    private static final String CACHE_DATE_SANITIZATION_MAP_KEY = "dateSanitizationMap";
    
    public Date parseDate(ParametersCollector parametersCollector, String origDate) throws XPathException {
        String sanitizedDate = sanitizeDate(parametersCollector, origDate);
        for (DateFormat formatter : getIncomingDateFormatters(parametersCollector)) {
            try {
                return formatter.parse(sanitizedDate);
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

    public String sanitizeDate(ParametersCollector parametersCollector, String date) {
        String result = date;
        Map<String, String> dateSanitizationMap = getDateSanitizationMap(parametersCollector);
        for (String key : dateSanitizationMap.keySet()) {
            result = result.replaceAll(key, dateSanitizationMap.get(key));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDateSanitizationMap(ParametersCollector parametersCollector) {
        if (!parametersCollector.isCached(CACHE_DATE_SANITIZATION_MAP_KEY)) {
            parametersCollector.putCache(CACHE_DATE_SANITIZATION_MAP_KEY, parametersCollector.getDateSanitizationMap());
        }  
        return (Map<String, String>) parametersCollector.getCached(CACHE_DATE_SANITIZATION_MAP_KEY);
    }
}
