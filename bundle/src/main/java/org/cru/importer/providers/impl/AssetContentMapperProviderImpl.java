package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
	public void mapFields(Resource resource, ResourceMetadata metadata, InputStream imageInputStream) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		super.getSession().exportDocumentView(resource.getChild(METADATA_NODE).getPath(), out, true, true);
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		super.mapFields(resource, metadata, in);
	}
	
	@Override
	protected Resource getResourceToOverride(Resource resource) {
		return resource.getChild(METADATA_NODE);
	}
	
}
