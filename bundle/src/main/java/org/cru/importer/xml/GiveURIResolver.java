package org.cru.importer.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * Resolve specific URLs of give project.
 * 
 * NOTE: It will be used to get the dam path for large image and small image
 * 
 * usage example:
 * 
 *  <xsl:value-of select="fn:doc(concat('give://test?path=', $path))" />
 *  
 * @author Nestor de Dios
 *
 */
public class GiveURIResolver extends StandardURIResolver {

	@Override
	public Source resolve(String href, String base) throws XPathException {
		if (href.startsWith("give")) {
			// TODO: Change this and handle the URL
			// For example, search in the DAM for an image.
			String image = "";
			if (href.startsWith("give://largeImage")) {
				image = "<image>/content/dam/geometrixx/banners/shapecon.jpg</image>";
			} else {
				image = "<image>/content/dam/geometrixx/shapes/01.jpg</image>";
			}
			InputStream stream = new ByteArrayInputStream(image.getBytes(StandardCharsets.UTF_8));
			return new StreamSource(stream);
		} else {
			return super.resolve(href, base);
		}
	}

}
