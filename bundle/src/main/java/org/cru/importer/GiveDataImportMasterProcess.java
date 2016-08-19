package org.cru.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.cru.importer.bean.PageInfo;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.providers.ContentMapperProvider;
import org.cru.importer.providers.DataImportFactory;
import org.cru.importer.providers.MetadataProvider;
import org.cru.importer.providers.PageProvider;

/**
 * Master process: Process the content file and create the content
 * 
 * @author Nestor de Dios
 *
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class GiveDataImportMasterProcess {

	@Inject
	private Session session;
	
	@Inject
	private DataImportFactory dataImportFactory;

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
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				if (entry.getName().toLowerCase().endsWith(".xml")) {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.copy(zis, baos);
						Map<String,String> metadata = metadataProvider.getMetadata(entry.getName());

						PageInfo pageInfo = pageProvider.getPage(metadata);
						contentMapperProvider.mapFields(pageInfo.getPage(), metadata, new ByteArrayInputStream(baos.toByteArray()));

						String pageReference = pageInfo.getPage().getPath();
						if (session.hasPendingChanges()) {
							session.save();
							if (pageInfo.isNewPage()) {
								resultsCollector.addCreatedPage(pageReference);
							} else {
								resultsCollector.addModifiedPage(pageReference);
							}
						} else {
							resultsCollector.addNotModifiedPage(pageReference);
						}
					} catch (Exception e) {
						if (session.hasPendingChanges()) {
							session.refresh(false);
						}
						resultsCollector.addError(entry.getName() + ": " + e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			resultsCollector.addError(e.getMessage());
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
