package org.cru.importer.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

import org.cru.importer.bean.ParametersCollector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * Resolve specific URLs of give project.
 * 
 * usage example:
 * 
 *  <xsl:value-of select="fn:doc(concat('give://searchImage?image=', wcm:root/wcm:element[@name='image']))" />
 *  
 * @author Nestor de Dios
 *
 */
public class GiveURIResolver extends StandardURIResolver {

	private final static Logger LOGGER = LoggerFactory.getLogger(GiveURIResolver.class);
	
	private ParametersCollector parametersCollector;
	private BundleContext bundleContext;
	private Map<String, GiveSourceFactory> cache;
	
	public GiveURIResolver(ParametersCollector parametersCollector) {
		this.parametersCollector = parametersCollector;
		this.bundleContext = FrameworkUtil.getBundle(GiveURIResolver.class).getBundleContext();
		this.cache = new HashMap<String, GiveSourceFactory>();
	}

	@Override
	public Source resolve(String href, String base) throws XPathException {
		if (href.startsWith("give")) {
			return resolveFromFactory(href, base);
		} else {
			return super.resolve(href, base);
		}
	}

	/**
	 * Search for registered factories to handle the request
	 * 
	 * @param href
	 * @param base
	 * @return
	 * @throws XPathException
	 */
	private Source resolveFromFactory(String href, String base) throws XPathException {
		try {
			String[] path = href.replace("give://", "").split("\\?");
			String factory = path[0];
			String parameters = "";
			if (path.length > 1) {
				parameters = path[1];
			}
			GiveSourceFactory sourceFactory = null;
			if (this.cache.containsKey(factory)) {
				sourceFactory = this.cache.get(factory);
			} else {
				String typeFilter = String.format("(%s=%s)", GiveSourceFactory.OSGI_PROPERTY_TYPE, factory);
				ServiceReference[] serviceReference = bundleContext.getServiceReferences(GiveSourceFactory.class.getName(), typeFilter);
				if (serviceReference != null && serviceReference.length > 0) {
					sourceFactory = (GiveSourceFactory) bundleContext.getService(serviceReference[0]);
					this.cache.put(factory, sourceFactory);
				} else {
					throw new XPathException("There are not source factory defined for " + factory);
				}
			}
			return sourceFactory.resolve(parametersCollector, parameters);
        } catch (Exception e) {
			LOGGER.error("Unable to load source factory for " + href, e);
			throw new XPathException(e.getMessage());
		}
	}

}
