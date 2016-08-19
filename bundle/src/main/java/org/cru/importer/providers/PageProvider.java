package org.cru.importer.providers;

import java.util.Map;

import org.cru.importer.bean.PageInfo;

/**
 * Create or get a page related to a given metadata
 * 
 * @author Nestor de Dios
 *
 */
public interface PageProvider {

	/**
	 * Create or get a page related to a given metadata
	 * 
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	public PageInfo getPage(Map<String, String> metadata) throws Exception;
	
}
