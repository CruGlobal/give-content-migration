package org.cru.importer.xml;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.apache.jackrabbit.commons.JcrUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class ImporterURIResolver implements URIResolver {
	
	private Session session;
    private XMLReader xmlReader;

    public ImporterURIResolver(Session session, XMLReader xmlReader) {
    	this.session = session;
        this.xmlReader = xmlReader;
    }

    public Source resolve(String href, String base) throws TransformerException {
        try {
            final Node node = this.session.getNode(href);
            return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
        } catch (RepositoryException e) {
            throw new TransformerException("Cannot resolve " + href);
        }
    }
}