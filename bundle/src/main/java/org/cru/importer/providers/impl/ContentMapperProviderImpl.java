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

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.xml.GiveURIResolver;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

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
	private Map<String, String> sanitizationMap;
	private List<String> transformerParameters;
	private GiveURIResolver resolver;
	
    public ContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
        this(parametersCollector, parametersCollector.getXsltPath());
    }

    public ContentMapperProviderImpl(ParametersCollector parametersCollector, String xsltPath) throws Exception {
        this.transformedKeys = null;
        this.session = parametersCollector.getResourceResolver().adaptTo(Session.class);
        this.sanitizationMap = parametersCollector.getSanitizationMap();

        Node xsltNode = this.session.getNode(xsltPath);
        processor = new Processor(false);
        XsltCompiler comp = processor.newXsltCompiler();
        resolver = new GiveURIResolver(parametersCollector);
        comp.setURIResolver(resolver);
        XsltExecutable exp = comp.compile(new StreamSource(JcrUtils.readFile(xsltNode)));
        transformerParameters = new LinkedList<String>();
        for (QName key : exp.getGlobalParameters().keySet()) {
            transformerParameters.add(key.getLocalName());
        }
        transformer = exp.load();
        transformer.setURIResolver(resolver);
    }

	//@SuppressWarnings("resource")
	public boolean mapFields(Resource resource, ResourceMetadata metadata, byte[] fileContent) throws Exception {
		try {
			byte[] bresult = processTransformation(resource, metadata, fileContent);
			InputStream result = new ByteArrayInputStream(bresult);
			importResult(getResourceToOverride(resource), result);
			return true;
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
	
	protected byte[] processTransformation(Resource resource, ResourceMetadata metadata, byte[] fileContent) throws Exception {
        initTransformedKeys(metadata.getPropertyNames());
        InputStream isArrBaos = new ByteArrayInputStream(fileContent);
        CharsetDetector detector = new CharsetDetector();
        detector.setText(isArrBaos);
        CharsetMatch match = detector.detect();
        String orig = sanitizeXml(new String(fileContent, match.getName()));
        isArrBaos = new ByteArrayInputStream(orig.getBytes("UTF-8"));

        transformer.setParameter(new QName("path"), new XdmAtomicValue(resource.getPath()));
        for (String key : transformerParameters) {
            if (transformedKeys.containsKey(key)) {
                String colname = transformedKeys.get(key);
                transformer.setParameter(new QName(key), new XdmAtomicValue(metadata.getValue(colname)));
            }
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resolver.setCurrentMetadata(metadata);
        transformer.setSource(new StreamSource(isArrBaos));
        transformer.setDestination(processor.newSerializer(output));
        transformer.transform();

        return output.toByteArray();
	}
	
	public String sanitizeXml(String orig) {
        for (String key : sanitizationMap.keySet()) {
            orig = orig.replaceAll(key, sanitizationMap.get(key));
        }
        return orig;
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
	 * @param resource
	 * @param result
	 * @throws Exception
	 */
	public void importResult(Resource resource, InputStream result) throws Exception {
		// Get the parent path
		String path = resource.getParent().getPath();
		// Remove the previous content to avoid xml import error
		session.removeItem(resource.getPath());
		// Import the content
		session.importXML(path, result, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
	}
	
	/**
	 * Returns the resource to be overrided by the impor process
	 * @param resource
	 * @return
	 */
	public Resource getResourceToOverride(Resource resource) {
		return resource.getChild("jcr:content");
	}

	protected Session getSession() {
		return session;
	}

    public void setTransformParameter(String name, String value) {
        transformer.setParameter(new QName(name), new XdmAtomicValue(value));
    }
	
}
