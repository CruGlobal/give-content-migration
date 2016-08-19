package org.cru.importer.providers;

import java.io.InputStream;
import java.util.Map;

import com.day.cq.wcm.api.Page;

/**
 * Fills the page content
 * 
 * @author Nestor de Dios
 *
 */
public interface ContentMapperProvider {

	/**
	 * Fills the page content
	 * 
	 * @param page
	 * @param metadata
	 * @param xmlInputStream
	 * @throws Exception
	 */
	public void mapFields(Page page, Map<String,String> metadata, InputStream xmlInputStream) throws Exception;
	
}
