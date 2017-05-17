package org.cru.importer.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.trans.XPathException;

/**
 * Implements commons behavior for all factories
 * 
 * @author Nestor de Dios
 *
 */
public abstract class GiveSourceFactoryBase implements GiveSourceFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(GiveSourceFactoryBase.class);
    
    private ParametersCollector parametersCollector;
    private ResourceMetadata currentMetadata;
    
    public Source resolve(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String parameters) throws XPathException {
        this.parametersCollector = parametersCollector;
        this.currentMetadata = currentMetadata;
        Map<String, String> params;
        try {
            params = UrlUtil.splitQuery(parameters);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Error parsing parmeters: " + parameters + "  - Error: " + e.getMessage());
            params = new HashMap<String, String>();
        }
        String response = resolve(params);
        InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        return new StreamSource(stream);
    }

    /**
     * Resolve a URI
     * 
     * @param params
     * @return
     * @throws XPathException
     */
    protected abstract String resolve(Map<String, String> params) throws XPathException;

    public ParametersCollector getParametersCollector() {
        return parametersCollector;
    }

    public ResourceMetadata getCurrentMetadata() {
        return currentMetadata;
    }
    
    public String getCurrentFilename() {
        return currentMetadata.getValue(parametersCollector.getColumnFileName());
    }

}
