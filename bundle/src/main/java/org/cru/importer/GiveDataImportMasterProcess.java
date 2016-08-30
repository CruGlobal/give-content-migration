package org.cru.importer;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.bean.PageInfo;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.DataImportFactory;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.PageProvider;
import org.cru.importer.providers.impl.DataImportFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master process: Process the content file and create the content
 * 
 * @author Nestor de Dios
 *
 */
public class GiveDataImportMasterProcess {

	private final static Logger LOGGER = LoggerFactory.getLogger(GiveDataImportMasterProcess.class);
	
	private Session session;
	
	private DataImportFactory dataImportFactory;
	
	public GiveDataImportMasterProcess(ResourceResolver resolver) {
		this.session = resolver.adaptTo(Session.class);
		this.dataImportFactory = new DataImportFactoryImpl(); // TODO: Get it from bundle context
	}

	/**
	 * Starts the import process. Iterate over all xml files contained in the zip file and process each one.
	 * 
	 * @param parametersCollector
	 * @param resultsCollector
	 */
	public void runProcess(ParametersCollector parametersCollector, ResultsCollector resultsCollector) {
		InputStream in = null;
		try {
			MetadataProvider metadataProvider = dataImportFactory.createMetadataProvider(parametersCollector);
			PageProvider pageProvider = dataImportFactory.createPageProvider(parametersCollector);
			ContentMapperProvider contentMapperProvider = dataImportFactory.createContentMapperProvider(parametersCollector);
			in = parametersCollector.getContentFile().getStream();
			ZipInputStream zis = new ZipInputStream(in);
			ZipEntry entry = null;
			Pattern ignoreFilesPattern = null;
			if (!parametersCollector.getIgnoreFilesPattern().equals("")) {
				ignoreFilesPattern = Pattern.compile(parametersCollector.getIgnoreFilesPattern());
			}
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				String filename = entry.getName();
				LOGGER.info("Start processing file: " + filename);
				if (acceptsFile(ignoreFilesPattern, filename)) {
					try {
						ResourceMetadata metadata = metadataProvider.getMetadata(filename);
						PageInfo pageInfo = pageProvider.getPage(metadata);
						if (pageInfo != null) {
							contentMapperProvider.mapFields(pageInfo.getPage(), metadata, zis);
							String pageReference = pageInfo.getPage().getPath();
							if (session.hasPendingChanges()) {
								session.save();
								if (pageInfo.isNewPage()) {
									resultsCollector.addCreatedPage(pageReference);
									LOGGER.info("New page created at: " + pageReference);
								} else {
									resultsCollector.addModifiedPage(pageReference);
									LOGGER.info("Existed page modified at: " + pageReference);
								}
							} else {
								resultsCollector.addNotModifiedPage(pageReference);
								LOGGER.info("Not modified page at: " + pageReference);
							}
						} else {
							resultsCollector.addIgnoredPages(filename);
							LOGGER.info("Ignored file: " + filename);
						}
					} catch (Exception e) {
						if (session.hasPendingChanges()) {
							session.refresh(false);
						}
						String errorMessage = filename + ": " + StringEscapeUtils.escapeHtml4(e.getMessage());
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
			IOUtils.closeQuietly(in);
		}
	}

	private boolean acceptsFile(Pattern ignoreFilesPattern, String filename) {
		if (!filename.toLowerCase().endsWith(".xml")) {
			return false;
		}
		if (ignoreFilesPattern != null) {
			Matcher matcher = ignoreFilesPattern.matcher(filename);
			return !matcher.matches();
		}
		return true;
	}

}
