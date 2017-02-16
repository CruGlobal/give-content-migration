package org.cru.importer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.ResourceProvider;
import org.cru.importer.providers.factory.DataImportFactory;
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

	/**
	 * Implementation for Runnable
	 */
    public void run() {
        runProcessInternal();
    }

    /**
     * Starts the import process. Iterate over all xml files contained in the zip file and process each one.
     * @param parametersCollector
     * @param resultsCollector
     */
    public void runProcess(ParametersCollector parametersCollector, ResultsCollector resultsCollector) {
        this.session = parametersCollector.getResourceResolver().adaptTo(Session.class);
        this.parametersCollector = parametersCollector;
        this.resultsCollector = resultsCollector;
        Thread thread = new Thread(this);
        thread.start();
    }

	private void runProcessInternal() {
		InputStream in = null;
		try {
			DataImportFactory dataImportFactory = getDataImporFactory(parametersCollector);
			MetadataProvider metadataProvider = dataImportFactory.createMetadataProvider(parametersCollector);
			ResourceProvider resourceProvider = dataImportFactory.createResourceProvider(parametersCollector);
			ContentMapperProvider contentMapperProvider = dataImportFactory.createContentMapperProvider(parametersCollector);
			in = parametersCollector.getContentFile().getStream();
			ZipInputStream zis = new ZipInputStream(in);
			ZipEntry entry = null;
			Pattern acceptFilesPattern = null;
			if (!parametersCollector.getAcceptFilesPattern().equals("")) {
				acceptFilesPattern = Pattern.compile(parametersCollector.getAcceptFilesPattern());
			}
			Set<String> proccesed = new HashSet<String>();
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				String filename = entry.getName();
				LOGGER.info("Start processing file: " + filename);
				if (!proccesed.contains(filename) && acceptsFile(acceptFilesPattern, filename)) {
					proccesed.add(filename);
					try {
						ResourceMetadata metadata = metadataProvider.getMetadata(filename);
						ResourceInfo resourceInfo = resourceProvider.getResource(metadata, zis);
						if (resourceInfo != null) {
							contentMapperProvider.mapFields(resourceInfo.getResource(), metadata, zis);
							String pageReference = resourceInfo.getResource().getPath();
							if (session.hasPendingChanges()) {
								session.save();
								if (resourceInfo.isNewResource()) {
									resultsCollector.addCreatedPage(pageReference);
									LOGGER.info("New resource created at: " + pageReference);
								} else {
									resultsCollector.addModifiedPage(pageReference);
									LOGGER.info("Existed resource modified at: " + pageReference);
								}
							} else {
								resultsCollector.addNotModifiedPage(pageReference);
								LOGGER.info("Not modified resource at: " + pageReference);
							}
						} else {
							resultsCollector.addIgnoredPages(filename);
							LOGGER.info("Ignored file: " + filename);
						}
					} catch (Exception e) {
						if (session.hasPendingChanges()) {
							session.refresh(false);
						}
						String errorMessage = filename + ": " + StringEscapeUtils.escapeJson(e.getMessage());
						resultsCollector.addError(errorMessage);
						LOGGER.info("Error importing " + errorMessage);
					}
				} else {
					LOGGER.info("File not accepted: " + filename);
				}
			}
		} catch (Exception e) {
			resultsCollector.addError(e.getMessage());
		} finally {
		    resultsCollector.stopRunning();
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
		BundleContext bundleContext = FrameworkUtil.getBundle(GiveURIResolver.class).getBundleContext();
		String typeFilter = String.format("(%s=%s)", DataImportFactory.OSGI_PROPERTY_TYPE, parametersCollector.getFactoryType());
		ServiceReference[] serviceReference = bundleContext.getServiceReferences(DataImportFactory.class.getName(), typeFilter);
		if (serviceReference != null && serviceReference.length > 0) {
			return (DataImportFactory) bundleContext.getService(serviceReference[0]);
		} else {
			throw new XPathException("There are not import factory defined for " + parametersCollector.getFactoryType());
		}
	}

}
