package org.cru.importer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.cru.importer.bean.CurrentStatus;
import org.cru.importer.bean.NotMetadataFoundException;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.ResourceProvider;
import org.cru.importer.providers.factory.DataImportFactory;
import org.cru.importer.service.PostProcessService;
import org.cru.importer.xml.GiveURIResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.trans.XPathException;

/**
 * Master process: Process the content file and create the content
 * 
 * @author Nestor de Dios
 *
 */
public class GiveDataImportMasterProcess implements Runnable {

	private final static Logger LOGGER = LoggerFactory.getLogger(GiveDataImportMasterProcess.class);
	
	private Session session;
	private ParametersCollector parametersCollector;
	private ResultsCollector resultsCollector;
	private CurrentStatus currentStatus;

	/**
	 * Implementation for Runnable
	 */
    public void run() {
        runProcessInternal();
        currentStatus.setCurrentProcess(null);
    }

    /**
     * Starts the import process. Iterate over all xml files contained in the zip file and process each one.
     * @param parametersCollector
     * @param resultsCollector
     * @param currentStatus
     */
    public void runProcess(ParametersCollector parametersCollector, ResultsCollector resultsCollector, CurrentStatus currentStatus) {
        this.session = parametersCollector.getResourceResolver().adaptTo(Session.class);
        this.parametersCollector = parametersCollector;
        this.resultsCollector = resultsCollector;
        this.currentStatus = currentStatus;
        Thread thread = new Thread(this);
        thread.start();
        currentStatus.setCurrentProcess(thread);
    }

	private void runProcessInternal() {
		InputStream in = null;
		try {
			DataImportFactory dataImportFactory = getDataImporFactory(parametersCollector);
			MetadataProvider metadataProvider = dataImportFactory.createMetadataProvider(parametersCollector);
			ResourceProvider resourceProvider = dataImportFactory.createResourceProvider(parametersCollector, metadataProvider);
			ContentMapperProvider contentMapperProvider = dataImportFactory.createContentMapperProvider(parametersCollector);
			List<PostProcessService> postProcessServices = getPostProcessServices(parametersCollector);
			in = parametersCollector.getContentFile().getStream();
			ZipInputStream zis = new ZipInputStream(in);
			ZipEntry entry = null;
			Pattern acceptFilesPattern = null;
			if (!parametersCollector.getAcceptFilesPattern().equals("")) {
				acceptFilesPattern = Pattern.compile(parametersCollector.getAcceptFilesPattern());
			}
			Set<String> proccesed = new HashSet<String>();
			Thread currentThread = Thread.currentThread();
			while ((entry = zis.getNextEntry()) != null && !currentThread.isInterrupted()) {
				if (entry.isDirectory()) {
					continue;
				}
				String filename = entry.getName();
				LOGGER.info("Start processing file: " + filename);
				if (!proccesed.contains(filename) && acceptsFile(acceptFilesPattern, filename)) {
					proccesed.add(filename);
					String currentFilename = "";
					try {
						List<ResourceMetadata> metadataList = metadataProvider.getMetadata(filename);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.copy(zis, baos);
						byte[] fileContent = baos.toByteArray();
						for (ResourceMetadata metadata : metadataList) {
	                        ResourceInfo resourceInfo = resourceProvider.getResource(metadata, fileContent);
	                        currentFilename = filename;
	                        if (resourceInfo != null) {
	                            boolean imported = contentMapperProvider.mapFields(resourceInfo.getResource(), metadata, fileContent);
	                            String pageReference = resourceInfo.getResource().getPath();
	                            String message = currentFilename + " - " + pageReference;
	                            if (session.hasPendingChanges()) {
	                                session.save();
	                                if (resourceInfo.isNewResource()) {
	                                    resultsCollector.addCreatedPage(message);
	                                    LOGGER.info("New resource created - " + message);
	                                } else {
	                                    resultsCollector.addModifiedPage(message);
	                                    LOGGER.info("Existed resource modified - " + message);
	                                }
	                            } else {
	                                resultsCollector.addNotModifiedPage(message);
	                                LOGGER.info("Not modified resource - " + message);
	                            }
	                            if (imported) {
    	                            for (PostProcessService service : postProcessServices) {
    	                                service.process(parametersCollector, metadata, resourceInfo.getResource());
    	                            }
	                            }
	                        } else {
	                            resultsCollector.addIgnoredPages(currentFilename + " - Ignored by file name accept policy or page acceptance rules");
	                            LOGGER.info("Ignored file: " + filename);
	                        }   
                        }
					} catch (NotMetadataFoundException nme) {
					    String errorMessage = filename + " - " + nme.getMessage();
                        resultsCollector.addWarning(filename);
                        LOGGER.info("Error importing " + errorMessage);
                    } catch (Exception e) {
						if (session.hasPendingChanges()) {
							session.refresh(false);
						}
						String file = (currentFilename.equals(""))? filename : currentFilename;
						String errorMessage = file + " - " + e.getMessage();
						resultsCollector.addError(errorMessage);
						LOGGER.info("Error importing " + errorMessage);
					}
				} else {
					LOGGER.info("File not accepted: " + filename);
				}
			}
            for (PostProcessService service : postProcessServices) {
                service.process(parametersCollector, postProcessServices);
            }
			if (currentThread.isInterrupted()) {
                LOGGER.info("Import proccess interrupted.");
			} else {
			    LOGGER.info("Import proccess finished.");
			}
		} catch (Exception e) {
		    LOGGER.error("Import proccess failed.", e);
			resultsCollector.addError(e.getMessage());
		} finally {
            currentStatus.stopRunning();
		    resultsCollector.addFinishMessage();
			IOUtils.closeQuietly(in);
		}
	}

    private boolean acceptsFile(Pattern acceptFilesPattern, String filename) {
		if (acceptFilesPattern != null) {
			Matcher matcher = acceptFilesPattern.matcher(filename);
			return matcher.matches();
		}
		return true;
	}
	
	private DataImportFactory getDataImporFactory(ParametersCollector parametersCollector) throws Exception {
		Object service = getService(getBundleContext(), DataImportFactory.class.getName(), DataImportFactory.OSGI_PROPERTY_TYPE, parametersCollector.getFactoryType());
		if (service != null) {
		    return (DataImportFactory) service;
		} else {
			throw new XPathException("There are not import factory defined for " + parametersCollector.getFactoryType());
		}
	}

    private List<PostProcessService> getPostProcessServices(ParametersCollector parametersCollector) throws Exception {
        BundleContext bundleContext = getBundleContext();
        String processClassName = PostProcessService.class.getName();
        List<PostProcessService> postProcessServices = new LinkedList<PostProcessService>();
        for (String serviceName : parametersCollector.getPostProcessServices()) {
            Object service = getService(bundleContext, processClassName, PostProcessService.OSGI_PROPERTY_PROCESS, serviceName);
            if (service != null) {
                postProcessServices.add((PostProcessService) service);
            } else {
                throw new XPathException("There are not post process service defined for " + serviceName);
            }
        }
        return postProcessServices;
    }
    
    private BundleContext getBundleContext() {
        return FrameworkUtil.getBundle(GiveURIResolver.class).getBundleContext();
    }
    
    private Object getService(BundleContext bundleContext, String className, String FilterType, String filterValue) throws Exception {
        String typeFilter = String.format("(%s=%s)", FilterType, filterValue);
        ServiceReference[] serviceReference = bundleContext.getServiceReferences(className, typeFilter);
        if (serviceReference != null && serviceReference.length > 0) {
            return bundleContext.getService(serviceReference[0]);
        } else {
            return null;
        }
    }

}
