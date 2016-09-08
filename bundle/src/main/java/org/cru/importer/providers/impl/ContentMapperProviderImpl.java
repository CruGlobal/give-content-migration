package org.cru.importer.providers.impl;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.util.UnicodeBOMInputStream;
import org.cru.importer.util.XmlEntitiesUtil;
import org.cru.importer.xml.GiveURIResolver;
import org.slf4j.LoggerFactory;

/**
 * Fills page content for Give import process using xslt
 * 
 * @author Nestor de Dios
 *
 */
public class ContentMapperProviderImpl implements ContentMapperProvider {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ContentMapperProviderImpl.class);

	private Session session;
	private XsltTransformer transformer;
	private Processor processor;
	private Map<String,String> transformedKeys;
	private List<String> transformerParameters;
	
	public ContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
		this.transformedKeys = null;
		this.session = parametersCollector.getRequest().getResourceResolver().adaptTo(Session.class);

		Node xsltNode = this.session.getNode(parametersCollector.getXsltPath());
		processor = new Processor(false);
        XsltCompiler comp = processor.newXsltCompiler();
        GiveURIResolver resolver = new GiveURIResolver(parametersCollector);
        comp.setURIResolver(resolver);
        XsltExecutable exp = comp.compile(new StreamSource(JcrUtils.readFile(xsltNode)));
        transformerParameters = new LinkedList<String>();
        for (QName key : exp.getGlobalParameters().keySet()) {
        	transformerParameters.add(key.getLocalName());
        }
        transformer = exp.load();
        transformer.setURIResolver(resolver);
	}

	public void mapFields(Resource resource, ResourceMetadata metadata, InputStream xmlInputStream) throws Exception {
		try {
			initTransformedKeys(metadata.getPropertyNames());
			
			// Copy the stream to be sure is not closed prematurelly
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(xmlInputStream, baos);
			InputStream isArrBaos = new ByteArrayInputStream(baos.toByteArray());




	        String orig = new String(baos.toByteArray());
        	orig = XmlEntitiesUtil.fixBabEntities(orig);
        	isArrBaos = new ByteArrayInputStream(orig.getBytes("UTF-8"));

			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(isArrBaos);

			InputStreamReader isr = new InputStreamReader(ubis);
			BufferedReader br = new BufferedReader(isr);
			ubis.skipBOM();

			InputStream istwo = new ByteArrayInputStream( br.readLine().getBytes() );

			transformer.setParameter(new QName("path"), new XdmAtomicValue(resource.getPath()));
			for (String key : transformerParameters) {
				if (transformedKeys.containsKey(key)) {
					String colname = transformedKeys.get(key);
					transformer.setParameter(new QName(key), new XdmAtomicValue(metadata.getValue(colname)));
				}
			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transformer.setSource(new StreamSource(istwo));
			//transformer.setSource(new StreamSource(isArrBaos));
			transformer.setDestination(processor.newSerializer(output));
			transformer.transform();

			byte[] bresult = output.toByteArray();
			InputStream result = new ByteArrayInputStream(bresult);

			importResult(getResourceToOverride(resource), result);

			br.close();
			isr.close();
			ubis.close();

		} catch (SaxonApiException e) {
			if (e.getCause()!=null && e.getCause() instanceof XPathException) {
				XPathException ex = (XPathException)e.getCause();
				if (ex.getException()!=null && ex.getException() instanceof XPathException) {
					throw (XPathException)ex.getException();
				} else {
					throw ex;
				}
			}
			throw e;
		}
	}

	private void initTransformedKeys(Set<String> colnames) {
		if (this.transformedKeys == null) {
			this.transformedKeys = new HashMap<String, String>();
			for (String colname : colnames) {
				this.transformedKeys.put(colname.replaceAll("[^a-zA-Z0-9]", "_"), colname);
			}
		}
	}

	/**
	 * Imports the result xml file to the JCR
	 * 
	 * @param page 
	 * @param result
	 * @throws Exception
	 */
	private void importResult(Resource resource, InputStream result) throws Exception {
		// Remove the previous content to avoid xml import error
		session.removeItem(resource.getPath());
		// Import the content
		session.importXML(resource.getParent().getPath(), result, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
	}
	
	/**
	 * Returns the resource to be overrided by the impor process
	 * @param resource
	 * @return
	 */
	protected Resource getResourceToOverride(Resource resource) {
		return resource.getChild("jcr:content");
	}

	protected Session getSession() {
		return session;
	}
	
}
