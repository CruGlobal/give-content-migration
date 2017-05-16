package org.cru.importer.xml;

import java.util.Date;

import org.cru.importer.bean.ParametersCollector;

import net.sf.saxon.trans.XPathException;

/**
 * Defines an interface to date parser service
 * 
 * @author Nestor de Dios
 *
 */
public interface DateParserService {

    public Date parseDate(ParametersCollector parametersCollector, String origDate) throws XPathException;
    
}
