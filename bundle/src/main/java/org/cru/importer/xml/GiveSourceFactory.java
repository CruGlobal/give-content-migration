package org.cru.importer.xml;

import javax.xml.transform.Source;

import net.sf.saxon.trans.XPathException;

import org.cru.importer.bean.ParametersCollector;

/**
 * Defines an interface to build source factories for give project
 * 
 * @author Nestor de Dios
 *
 */
public interface GiveSourceFactory {

	/**
	 * Service property to be set for each implementing class to define
	 * the type of factory to handle
	 */
	public static final String OSGI_PROPERTY_TYPE = "type";
	
	/**
	 * Resolve a URI
	 * 
	 * @param parametersCollector
	 * @param parameters
	 * @return
	 * @throws XPathException
	 */
	public Source resolve(ParametersCollector parametersCollector, String parameters) throws XPathException;
	
}
