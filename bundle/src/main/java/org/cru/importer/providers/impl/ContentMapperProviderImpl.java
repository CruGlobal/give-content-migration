package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.lib.UnparsedTextURIResolver;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.xml.ImporterURIResolver;
import org.cru.importer.xml.RejectingEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.day.cq.wcm.api.Page;

/**
 * Fills page content for Give import process using xslt
 * 
 * @author Nestor de Dios
 *
 */
public class ContentMapperProviderImpl implements ContentMapperProvider {

	private Session session;
	private XMLReader xmlReader;
	private Map<String, Transformer> cachedTransformers;
	private String xsltPath;
	
	public ContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
		this.session = parametersCollector.getRequest().getResourceResolver().adaptTo(Session.class);
		this.xmlReader = XMLReaderFactory.createXMLReader();
		this.xmlReader.setEntityResolver(new RejectingEntityResolver());
		this.cachedTransformers = new HashMap<String, Transformer>();
		this.xsltPath = parametersCollector.getXsltPath();
	}

	public void mapFields(Page page, Map<String, String> metadata, InputStream xmlInputStream) throws Exception {
		Transformer transformer = getTransformer();
		transformer.setParameter("metadata", metadata);
		transformer.setParameter("path", page.getPath());

		ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new SAXSource(this.xmlReader, new InputSource(xmlInputStream)), new StreamResult(output));
        InputStream result = new ByteArrayInputStream(output.toByteArray());

        importResult(page, result);
	}
	
	/**
	 * Gets the transformer from the cached instances
	 * @return
	 * @throws Exception
	 */
	private Transformer getTransformer() throws Exception{
		if (!cachedTransformers.containsKey(xsltPath)) {
			Node xsltNode = this.session.getNode(xsltPath);
			Transformer transformer = createTransformer(xsltNode, this.xmlReader);
			cachedTransformers.put(xsltPath, transformer);
			return transformer;
		} else {
			return cachedTransformers.get(xsltPath);
		}
	}
	
	/**
	 * Creates the transformer and adds the parameters
	 * 
	 * @return
	 * @throws Exception
	 */
	private Transformer createTransformer(Node xsltNode, XMLReader xmlReader) throws Exception {
        URIResolver uriResolver = new ImporterURIResolver(this.session, xmlReader);
        TransformerFactoryImpl transformerFactoryImpl = new TransformerFactoryImpl();
        transformerFactoryImpl.setURIResolver(uriResolver);
        Transformer transformer = transformerFactoryImpl.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));
        TransformerImpl transformerImpl = (TransformerImpl) transformer;
        transformerImpl.getUnderlyingController().setUnparsedTextURIResolver(new DocImporterUnparsedTextURIResolver(xsltNode.getParent()));
        return transformer;
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

    private class DocImporterUnparsedTextURIResolver implements UnparsedTextURIResolver {
        private Node srcNode;

        public DocImporterUnparsedTextURIResolver(Node srcNode) {
            this.srcNode = srcNode;
        }

        public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws net.sf.saxon.trans.XPathException {
            String absolutePath = absoluteURI.getPath();
            InputStreamReader isr;

            // Hardcoded hack, requires that HTML files are always in the html/ subdir of the src/ dir
            int pos = absolutePath.lastIndexOf("html/");
            String relativePath = absolutePath.substring(pos);

            try {
                if(this.srcNode.hasNode(relativePath)) {
                    isr = new InputStreamReader(JcrUtils.readFile(this.srcNode.getNode(relativePath)));
                } else {
                    String message = "<html><body><h2>HTML file " + relativePath + " not found<h2></body></html>";
                    isr = new InputStreamReader(IOUtils.toInputStream(message, "UTF-8"));
                }
                return isr;
            } catch (RepositoryException e) {
                throw new net.sf.saxon.trans.XPathException("Oops...", e);
            } catch (IOException e) {
                throw new net.sf.saxon.trans.XPathException("Oops...", e);
            }
        }
    }

}
