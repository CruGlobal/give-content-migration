package org.cru.importer.providers;

import org.cru.importer.bean.PageInfo;
import org.cru.importer.bean.ResourceMetadata;

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
	public PageInfo getPage(ResourceMetadata metadata) throws Exception;
	
}
