package org.cru.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.cru.importer.bean.CurrentStatus;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ProcessMessage;
import org.cru.importer.bean.ResultsCollector;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports pages from a zip file
 * 
 * @author Cloud99
 *
 */
@SlingServlet(
		methods = {"POST","GET"},
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
public class GiveDataImportServlet extends HttpServlet {

	private static final long serialVersionUID = 3742453523171288563L;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(GiveDataImportServlet.class);
	
	private static CurrentStatus currentStatus = new CurrentStatus();
	
	@Reference
	private ResultsCollector resultsCollector;
	
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    @Deactivate
    protected void deactivate(final ComponentContext componentContext) {
        LOGGER.info("deactivate import servlet.");
        Thread currentProcess = currentStatus.getCurrentProcess();
        if (currentProcess != null) {
            currentProcess.interrupt();
        }
    }

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            LOGGER.info("Start import process");
            if (!currentStatus.checkRunning()) {
                ParametersCollector parametersCollector = new ParametersCollector();
                ResourceResolver resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                parametersCollector.setResourceResolver(resourceResolver);
                if (validateParams(request, parametersCollector, resultsCollector)) {
                	GiveDataImportMasterProcess process = new GiveDataImportMasterProcess();
                	process.runProcess(parametersCollector, resultsCollector, currentStatus);
                	LOGGER.info("Import process started");
                } else {
                    currentStatus.stopRunning();
                    LOGGER.info("Import process not started (validations fail)");
                }
                response.getWriter().write(ProcessMessage.createStartMessage().toString());
                response.setStatus(SlingHttpServletResponse.SC_OK);
                response.setContentType("application/json");
            } else {
                LOGGER.info("Import process already running");
                response.getWriter().write(ProcessMessage.createRunningMessage().toString());
                response.setStatus(SlingHttpServletResponse.SC_OK);
                response.setContentType("application/json");
            }
        } catch (LoginException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e);
        }
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    String check = request.getParameter("check");
	    if (check == null) {
    	    LOGGER.info("Generating status updates");
    	    response.setStatus(200);
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control","no-cache");
            response.setHeader("Connection","keep-alive");
    	    final AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(0);
    	    resultsCollector.addObserver(new Observer() {
                public void update(Observable o, Object arg) {
                    try {
                        ProcessMessage message = (ProcessMessage) arg;
                        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                        response.getOutputStream().print("data: " + message.toString() + "\n\n");
                        response.getOutputStream().flush();
                        if (message.getType().equals(ProcessMessage.FINISH)) {
                            resultsCollector.deleteObserver(this);
                            asyncContext.complete();
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            });
            response.getOutputStream().print("event: open\n\n");
            for (ProcessMessage message: resultsCollector.getCachedMessages()) {
                response.getOutputStream().print("data: " + message.toString() + "\n\n");
            }
            resultsCollector.clearCachedMessages();
            response.getOutputStream().flush();
	    } else {
	        if (currentStatus.isRunning()) {
	            response.setStatus(200);
	            response.getWriter().write(ProcessMessage.createRunningMessage().toString());
	        } else {
	            response.setStatus(204);
	        }
	    }
	}

	private boolean validateParams(HttpServletRequest request, ParametersCollector parametersCollector, ResultsCollector resultsCollector) throws ServletException, IOException {
		boolean isValid = true;
		try {
			String baselocation = request.getParameter("baselocation");
			if (baselocation == null || baselocation.trim().equals("")) {
				resultsCollector.addError("baselocation parameter not found.");
				isValid = false;
			} else {
				Resource resource = parametersCollector.getResourceResolver().getResource(baselocation);
				if (resource == null) {
					resultsCollector.addError("baselocation parameter is not a valid resource.");
					isValid = false;
				} else {
					parametersCollector.setBaselocation(resource.getPath());
				}
			}
			String filename = request.getParameter("filename");
			if (filename == null || filename.trim().equals("")) {
				resultsCollector.addError("filename parameter not found.");
				isValid = false;
			} else {
				Resource pagesdata = parametersCollector.getResourceResolver().getResource(filename);
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
			Resource config = getConfig(request, parametersCollector);
			if (config == null) {
				resultsCollector.addError("configpath parameter not found or node not exists at JCR.");
				isValid = false;
			} else {
				ValueMap properties = config.adaptTo(ValueMap.class);
				parametersCollector.setRowColumnNames(properties.get("rowColumnNames",Integer.class));
				parametersCollector.setColumnFileName(properties.get("columnFileName",String.class));
				parametersCollector.setColumnMimeType(properties.get("columnMimeType",String.class));
				parametersCollector.setPathCreationStrategy(properties.get("pathCreationStrategy",String[].class));
				parametersCollector.setXsltPath(properties.get("transformation",String.class));
				parametersCollector.setPageTemplate(properties.get("pageTemplate",String.class));
				parametersCollector.setIntermediateTemplate(properties.get("intermediateTemplate",String.class));
				parametersCollector.setPageAcceptRule(properties.get("pageAcceptRule",String.class));
				parametersCollector.setFactoryType(properties.get("factoryType",String.class));
				if (properties.get("additionalMappingFile",Boolean.class)) {
				    Resource additionalMappingFileResource = parametersCollector.getResourceResolver().getResource(request.getParameter("additionalMappingFile"));
				    if (additionalMappingFileResource == null) {
	                    resultsCollector.addError("additional mapping file parameter value can not be found at JCR.");
	                    isValid = false;
				    } else {
			            Node node = additionalMappingFileResource.adaptTo(Node.class);
			            if (node.hasProperty("jcr:content/jcr:data")) {
			                javax.jcr.Property data = node.getProperty("jcr:content/jcr:data");
			                parametersCollector.setAdditionalMappingFile(data.getBinary().getStream());
			            }
				    }
				}
				String acceptFilesPattern = properties.get("acceptFilesPattern", String.class);
				if (acceptFilesPattern == null) {
					resultsCollector.addError("Accept files pattern property not found at configuration node.");
					isValid = false;
				} else {
					parametersCollector.setAcceptFilesPattern(acceptFilesPattern);
				}
	            Resource globalConfigs = getGlobalConfig(request, properties, parametersCollector);
	            if (globalConfigs == null) {
	                resultsCollector.addError("globalConfigs configuration not found at configpath or node not exists at JCR.");
	                isValid = false;
	            } else {
	                ValueMap globalProperties = globalConfigs.adaptTo(ValueMap.class);
	                parametersCollector.setSanitizationMap(PropertiesUtil.toMap(globalProperties.get("sanitizationMap",String[].class), new String[]{}));
	                String[] acceptedDateFormats = PropertiesUtil.toStringArray(globalProperties.get("acceptedDateFormats",String[].class), new String[]{});
	                String[] trimmedAcceptedDateFormats = new String[acceptedDateFormats.length];
	                for (int i = 0; i < acceptedDateFormats.length; i++) {
	                    trimmedAcceptedDateFormats[i] = acceptedDateFormats[i].trim();
	                }
	                parametersCollector.setAcceptedDateFormats(trimmedAcceptedDateFormats);
	            }
			}
		} catch (Exception e) {
			resultsCollector.addError(e.getMessage());
			isValid = false;
		}
		return isValid;
	}
	
    private Resource getConfig(HttpServletRequest request, ParametersCollector parametersCollector) {
        String configPath = request.getParameter("configpath");
        if (configPath != null) {
            Resource config = parametersCollector.getResourceResolver().getResource(configPath);
            return config;
        }
        return null;
    }

    private Resource getGlobalConfig(HttpServletRequest request, ValueMap properties, ParametersCollector parametersCollector) {
        String globalConfigs = properties.get("globalConfigs",String.class);
        if (globalConfigs != null) {
            Resource config = parametersCollector.getResourceResolver().getResource(globalConfigs);
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

}
