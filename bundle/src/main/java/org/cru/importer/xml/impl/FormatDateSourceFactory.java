package org.cru.importer.xml.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.service.DateParserService;
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
public class FormatDateSourceFactory extends GiveSourceFactoryBase {

    @Reference
    private DateParserService dateFormatter;
    
	private static final String AEM_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.'000+00:00'";
    private static final DateFormat AEM_DATE_FORMATTER = new SimpleDateFormat(AEM_FORMAT, Locale.US);

    private static final String PARAM_DATE = "date";
	private static final String PARAM_OUTPUT_FORMAT = "outputformat";
	
    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String date = "";
        if (params.containsKey(PARAM_DATE) && !params.get(PARAM_DATE).equals("")) {
            Date extractedDate = dateFormatter.parseDate(parametersCollector, params.get(PARAM_DATE));
            String outputFormat = params.get(PARAM_OUTPUT_FORMAT);
            if (outputFormat!=null && !"".equals(outputFormat)){
                date = new SimpleDateFormat(outputFormat, Locale.US).format(extractedDate);
            } else {
                date = AEM_DATE_FORMATTER.format(extractedDate);
            }
        }
        return "<date>" + date + "</date>";
    }
	


}
