package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.xml.GiveURIResolver;

import com.day.cq.wcm.api.Page;

/**
 * Fills page content for Give import process using xslt
 * 
 * @author Nestor de Dios
 *
 */
public class ContentMapperProviderImpl implements ContentMapperProvider {

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
        GiveURIResolver resolver = new GiveURIResolver(parametersCollector.getRequest().getResourceResolver());
        comp.setURIResolver(resolver);
        XsltExecutable exp = comp.compile(new StreamSource(JcrUtils.readFile(xsltNode)));
        transformerParameters = new LinkedList<String>();
        for (QName key : exp.getGlobalParameters().keySet()) {
        	transformerParameters.add(key.getLocalName());
        }
        transformer = exp.load();
        transformer.setURIResolver(resolver);
	}

	public void mapFields(Page page, ResourceMetadata metadata, InputStream xmlInputStream) throws Exception {
		try {
			initTransformedKeys(metadata.getPropertyNames());
			
			// Copy the stream to be sure is not closed prematurelly
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(xmlInputStream, baos);
			/*String sbaos = new String(baos.toByteArray());
			
			// Ensure the stream is encoded in UTF-8 before transformation
			byte[] arrbaos = sbaos.getBytes(StandardCharsets.UTF_8);*/
			InputStream isArrBaos = new ByteArrayInputStream(baos.toByteArray());
			
			transformer.setParameter(new QName("path"), new XdmAtomicValue(page.getPath()));
			for (String key : transformerParameters) {
				if (transformedKeys.containsKey(key)) {
					String colname = transformedKeys.get(key);
					transformer.setParameter(new QName(key), new XdmAtomicValue(metadata.getValue(colname)));
				}
			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transformer.setSource(new StreamSource(isArrBaos));
			transformer.setDestination(processor.newSerializer(output));
			transformer.transform();

			InputStream result = new ByteArrayInputStream(output.toByteArray());

			importResult(page, result);
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
				//this.transformedKeys.put(key, key.replaceAll(" ", "_").replaceAll(":", "_"));
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
	private void importResult(Page page, InputStream result) throws Exception {
		// Remove the previous content to avoid xml import error
		session.removeItem(page.getContentResource().getPath());
		// Import the content
		session.importXML(page.getPath(), result, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
	}

}
