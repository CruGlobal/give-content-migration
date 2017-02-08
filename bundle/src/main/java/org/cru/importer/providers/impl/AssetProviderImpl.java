package org.cru.importer.providers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.RelativePathSection;
import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.providers.ResourceProvider;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.AssetManager;

/**
 * Create assets for Give import process
 * 
 * @author Nestor de Dios
 *
 */
public class AssetProviderImpl implements ResourceProvider {

	private AssetManager assetManager;
	private Node baselocation;
	private Session session;
	private List<RelativePathSection> pathSections;
	private String pageAcceptRuleKey;
	private String pageAcceptRuleValue;
	private String columnMimeType;
	private ResourceResolver resolver;
	
	public AssetProviderImpl(ParametersCollector parametersCollector) {
		this.resolver = parametersCollector.getResourceResolver();
		this.assetManager = resolver.adaptTo(AssetManager.class);
		this.session = resolver.adaptTo(Session.class);
		this.baselocation = resolver.getResource(parametersCollector.getBaselocation()).adaptTo(Node.class);
		this.pathSections = RelativePathSection.buildFromStrategy(parametersCollector.getPathCreationStrategy());
		this.columnMimeType = parametersCollector.getColumnMimeType();
		if (parametersCollector.getPageAcceptRule().equals("")) {
			this.pageAcceptRuleKey = null;
			this.pageAcceptRuleValue = null;
		} else {
			String[] pageAcceptRule = parametersCollector.getPageAcceptRule().split("=");
			this.pageAcceptRuleKey = pageAcceptRule[0];
			this.pageAcceptRuleValue = pageAcceptRule[1];
		}
	}

	public ResourceInfo getResource(ResourceMetadata metadata, InputStream inputStream) throws Exception {
		if (this.pageAcceptRuleKey != null && metadata.getPropertyNames().contains(this.pageAcceptRuleKey)) {
			String val = metadata.getValue(this.pageAcceptRuleKey);
			if (!val.equals(this.pageAcceptRuleValue)) {
				return null;
			}
		}
		String relativePath = getRelativePath(metadata);
		Resource asset = this.resolver.getResource(this.baselocation.getPath() + relativePath);
		if (asset != null) {
			return new ResourceInfo(asset,false);
		} else {
			// Copy the stream to be sure is not closed prematurely
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, baos);
			InputStream isArrBaos = new ByteArrayInputStream(baos.toByteArray());
			
			// Create the asset
			asset = buildAsset(metadata, relativePath, isArrBaos);
			return new ResourceInfo(asset,true);
		}
	}

	/**
	 * Builds the relative page path from the path creation strategy
	 * @param metadata
	 * @return
	 * @throws Exception 
	 */
	private String getRelativePath(ResourceMetadata metadata) throws Exception {
		return RelativePathSection.buildPath(pathSections, metadata);
	}

	private Resource buildAsset(ResourceMetadata metadata, String relativePath, InputStream inputStream) throws Exception {
		Node baseNode = this.baselocation;
		String[] targetPathChunks = relativePath.substring(1).split("/");
		for (int i=0; i<targetPathChunks.length; i++) {
			String partialPath = targetPathChunks[i];
			if (!baseNode.hasNode(partialPath)) {
				if (i<targetPathChunks.length-1) {
					baseNode = JcrUtil.createPath(baseNode.getPath() + "/" + partialPath + "/jcr:content", "sling:OrderedFolder", "nt:unstructured", session, false);
					baseNode.setProperty("jcr:title", partialPath);
					baseNode = baseNode.getParent();
				} else {
					return assetManager.createAsset(baseNode.getPath() + "/" + partialPath, inputStream, metadata.getValue(this.columnMimeType), false).adaptTo(Resource.class);
				}
			} else {
				baseNode = baseNode.getNode(partialPath);
			}
		}
		return null;
	}
	
}
