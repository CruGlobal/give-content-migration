package org.cru.importer.service;

import java.util.Map;

import org.cru.importer.bean.ParametersCollector;

import net.sf.saxon.trans.XPathException;

/**
 * Defines an interface to Additional Mapping service
 * 
 * @author Nestor de Dios
 *
 */
public interface AdditionalMappingService {

    String getFormattedRow(ParametersCollector parametersCollector, String[] priority, String keyName,
            String keyValue) throws XPathException;
    
    Map<String,String> getAdditionalMapping(ParametersCollector parametersCollector, String[] priority, String keyName,
            String keyValue) throws XPathException;
    
}
