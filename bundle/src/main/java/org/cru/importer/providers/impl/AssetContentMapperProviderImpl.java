package org.cru.importer.providers.impl;

import java.io.ByteArrayOutputStream;

import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;

/**
 * Fills page content for Give import process using xslt. due to the inputStream is the image itselft, the
 * content of "metadata" node is sent to the transformation
 * 
 * @author Nestor de Dios
 *
 */
public class AssetContentMapperProviderImpl extends ContentMapperProviderImpl {

	private static final String METADATA_NODE = "jcr:content/metadata";
	
	public AssetContentMapperProviderImpl(ParametersCollector parametersCollector) throws Exception {
		super(parametersCollector);
	}

	@Override
	public void mapFields(Resource resource, ResourceMetadata metadata, byte[] fileContent) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		super.getSession().exportDocumentView(resource.getChild(METADATA_NODE).getPath(), out, true, true);
		super.mapFields(resource, metadata, fileContent);
	}
	
	@Override
	public Resource getResourceToOverride(Resource resource) {
		return resource.getChild(METADATA_NODE);
	}
	
}
