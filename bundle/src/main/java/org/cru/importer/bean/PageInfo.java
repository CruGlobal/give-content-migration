package org.cru.importer.bean;

import com.day.cq.wcm.api.Page;

public class PageInfo {

	private Page page;
	private boolean newPage;
	
	public PageInfo(Page page, boolean isNewPage) {
		this.page = page;
		this.newPage = isNewPage;
	}

	public Page getPage() {
		return page;
	}

	public boolean isNewPage() {
		return newPage;
	}

}
