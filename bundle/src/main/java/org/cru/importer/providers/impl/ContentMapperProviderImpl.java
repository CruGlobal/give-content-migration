package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.jackrabbit.commons.JcrUtils;
import org.cru.importer.bean.ParametersCollector;
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
	
	public ContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
		this.transformedKeys = null;
		this.session = parametersCollector.getRequest().getResourceResolver().adaptTo(Session.class);

		Node xsltNode = this.session.getNode(parametersCollector.getXsltPath());
		processor = new Processor(false);
        XsltCompiler comp = processor.newXsltCompiler();
        GiveURIResolver resolver = new GiveURIResolver();
        comp.setURIResolver(resolver);
        XsltExecutable exp = comp.compile(new StreamSource(JcrUtils.readFile(xsltNode)));
        transformer = exp.load();
        transformer.setURIResolver(resolver);
	}

	public void mapFields(Page page, Map<String, String> metadata, InputStream xmlInputStream) throws Exception {
		initTransformedKeys(metadata);
		transformer.setParameter(new QName("path"), new XdmAtomicValue(page.getPath()));
		for (String key : metadata.keySet()) {
			transformer.setParameter(new QName(this.transformedKeys.get(key)), new XdmAtomicValue(metadata.get(key)));
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		transformer.setSource(new StreamSource(xmlInputStream));
		transformer.setDestination(processor.newSerializer(output));
		transformer.transform();

        InputStream result = new ByteArrayInputStream(output.toByteArray());

        importResult(page, result);
	}

	private void initTransformedKeys(Map<String, String> metadata) {
		if (this.transformedKeys == null) {
			this.transformedKeys = new HashMap<String, String>();
			for (String key : metadata.keySet()) {
				this.transformedKeys.put(key, key.replaceAll(" ", "_").replaceAll(":", "_"));
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
