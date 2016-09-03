package org.cru.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.io.JSONWriter;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResultsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Imports pages from a zip file
 * 
 * @author Cloud99
 *
 */
@SlingServlet(
		methods = {"POST"},
		paths={"/services/givedataimport"},
		metatype = true,
		label = "CRU - Give Data Import servlet",
		description = "CRU - Give Data Import servlet")
@Service
@Properties({
		@Property(name = "service.description", value = "CRU - Give Data Import servlet"),
		@Property(name = "service.vendor", value = "Cloud99"),
		@Property(name = "service.pid", value = "org.cru.importer.GiveDataImportServlet", propertyPrivate = false)
})
public class GiveDataImportServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 3742453523171288563L;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(GiveDataImportServlet.class);
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		ParametersCollector parametersCollector = new ParametersCollector();
		ResultsCollector resultsCollector = new ResultsCollector();
		if (validateParams(request, parametersCollector, resultsCollector)) {
			GiveDataImportMasterProcess process = new GiveDataImportMasterProcess(request.getResourceResolver());
			process.runProcess(parametersCollector, resultsCollector);
		}
		writeResult(response, resultsCollector);
	}

	private boolean validateParams(SlingHttpServletRequest request, ParametersCollector parametersCollector, ResultsCollector resultsCollector) throws ServletException, IOException {
		boolean isValid = true;
		try {
			parametersCollector.setRequest(request);
			String baselocation = request.getParameter("baselocation");
			if (baselocation == null || baselocation.trim().equals("")) {
				resultsCollector.addError("baselocation parameter not found.");
				isValid = false;
			} else {
				PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
				Page basePage = pageManager.getPage(baselocation);
				if (basePage == null) {
					resultsCollector.addError("baselocation parameter is not a valid page.");
					isValid = false;
				} else {
					parametersCollector.setBaselocation(baselocation);
				}
			}
			String filename = request.getParameter("filename");
			if (filename == null || filename.trim().equals("")) {
				resultsCollector.addError("filename parameter not found.");
				isValid = false;
			} else {
				Resource pagesdata = request.getResourceResolver().getResource(filename);
				if (pagesdata == null) {
					resultsCollector.addError("filename parameter value can not be found at JCR.");
					isValid = false;
				} else {
					Binary zip = loadZipFile(pagesdata);
					if (zip == null) {
						resultsCollector.addError("Content file can not be processed, please verify the file format (non empty zip file required).");
					} else {
						parametersCollector.setContentFile(zip);
					}
				}
			}
			Resource config = getConfig(request);
			if (config == null) {
				resultsCollector.addError("configpath parameter not found or node not exists at JCR.");
				isValid = false;
			} else {
				ValueMap properties = config.adaptTo(ValueMap.class);
				parametersCollector.setRowColumnNames(properties.get("rowColumnNames",Integer.class));
				parametersCollector.setColumnFileName(properties.get("columnFileName",String.class));
				parametersCollector.setPathCreationStrategy(properties.get("pathCreationStrategy",String[].class));
				parametersCollector.setXsltPath(properties.get("transformation",String.class));
				parametersCollector.setPageTemplate(properties.get("pageTemplate",String.class));
				parametersCollector.setIntermediateTemplate(properties.get("intermediateTemplate",String.class));
				parametersCollector.setPageAcceptRule(properties.get("pageAcceptRule",String.class));
				parametersCollector.setFactoryType(properties.get("factoryType",String.class));
				if (properties.get("additionalMappingFile",Boolean.class)) {
					InputStream additionalMappingFile = request.getRequestParameter("additionalMappingFile").getInputStream();
					parametersCollector.setAdditionalMappingFile(additionalMappingFile);
				}
				String ignoreFilesPattern = properties.get("ignoreFilesPattern", String.class);
				if (ignoreFilesPattern == null) {
					resultsCollector.addError("Ignore files pattern property not found at configuration node.");
					isValid = false;
				} else {
					parametersCollector.setIgnoreFilesPattern(ignoreFilesPattern);
				}
			}
		} catch (Exception e) {
			resultsCollector.addError(e.getMessage());
			isValid = false;
		}
		return isValid;
	}
	
	private Resource getConfig(SlingHttpServletRequest request) {
		String configPath = request.getParameter("configpath");
		if (configPath != null) {
			Resource config = request.getResourceResolver().getResource(configPath);
			return config;
		}
		return null;
	}

	private Binary loadZipFile(Resource resource) {
		Binary binary = null;
		InputStream in = null;
		try {
			Node node = resource.adaptTo(Node.class);
			if (node.hasProperty("jcr:content/jcr:data")) {
				javax.jcr.Property data = node.getProperty("jcr:content/jcr:data");
				binary = data.getBinary();
				in = binary.getStream();
				// Verify if the file is a zip file
				ZipInputStream zis = new ZipInputStream(in);
				ZipEntry entry = zis.getNextEntry();
				while (entry!=null && entry.isDirectory()) {
					entry = zis.getNextEntry();
				}
				if (entry!=null) {
					return binary;
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error loading ZIP file with page data",e);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
			if (binary != null) {
				binary.dispose();
			}
		}
		return null;
	}

	private void writeResult(SlingHttpServletResponse response, ResultsCollector collector) throws ServletException {
		try {
			Collections.sort(collector.getCreatedPages());
			Collections.sort(collector.getModifiedPages());
			Collections.sort(collector.getNotModifiedPages());
			Collections.sort(collector.getErrors());
			
			response.setStatus(200);
		    response.setContentType("text/html");
		    response.setCharacterEncoding("utf-8");
			JSONWriter writer = new JSONWriter(response.getWriter());
			writer.object();
			writeList(writer, "createdPages", collector.getCreatedPages());
			writeList(writer, "modifiedPages", collector.getModifiedPages());
			writeList(writer, "notModifiedPages", collector.getNotModifiedPages());
			writeList(writer, "ignoredPages", collector.getIgnoredPages());
			writeList(writer, "errors", collector.getErrors());
			writer.endObject();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ServletException(e);
		}
	}

	private void writeList(JSONWriter writer, String objectKey, List<String> stringList) throws Exception {
		writer.key(objectKey);
		writer.array();
		for (String string : stringList) {
			writer.value(string);
		}
		writer.endArray();
	}

}
