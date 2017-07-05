package org.cru.importer.xml.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.service.AdditionalMappingService;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.osgi.framework.Constants;

import com.google.common.base.Strings;

import net.sf.saxon.trans.XPathException;

@Component(
    metatype = true,
    label = "Give importer - csvAdditionalMapping",
    description = "Get additional mapping data from a CSV source. Use the formatDate URL with parameter keyColumn and keyValue"
)
@Service
@Properties({
    @Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "csvAdditionalMapping"),
    @Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class AdditionalCSVMappingSourceFactory extends GiveSourceFactoryBase {

    private static final String PARAM_KEY_COLUMN = "keyColumn";
    private static final String PARAM_KEY_VALUE = "keyValue";
    private static final String PARAM_ORDER_BY = "priority";

    @Reference
    private AdditionalMappingService additionalMappingService;

    @Override
    protected String resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, Map<String, String> params)
            throws XPathException {
        String additionalMapping = "";
        if (params.containsKey(PARAM_KEY_COLUMN) && !params.get(PARAM_KEY_COLUMN).equals("")
                && params.containsKey(PARAM_KEY_VALUE) && !params.get(PARAM_KEY_VALUE).equals("")) {
            String key = params.get(PARAM_KEY_COLUMN);
            String[] priority = getArray(params.get(PARAM_ORDER_BY));
            additionalMapping = additionalMappingService.getFormattedRow(parametersCollector, priority, key, params.get(PARAM_KEY_VALUE));
        }
        return "<data>" + additionalMapping + "</data>";
    }

    private String[] getArray(String param) {
        String[] result = null;
        if (!Strings.isNullOrEmpty(param)) {
            String[] arrays = param.split("\\[");
            String actual = arrays[1].split("\\]")[0];
            result = actual.split(",");
        }
        return result;
    }

}
