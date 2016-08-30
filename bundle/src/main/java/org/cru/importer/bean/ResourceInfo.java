package org.cru.importer.bean;

import org.apache.sling.api.resource.Resource;

public class ResourceInfo {

	private Resource resource;
	private boolean newResource;
	
	public ResourceInfo(Resource resource, boolean isNewResource) {
		this.resource = resource;
		this.newResource = isNewResource;
	}

	public Resource getResource() {
		return resource;
	}

	public boolean isNewResource() {
		return newResource;
	}

}
