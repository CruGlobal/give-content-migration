package org.cru.importer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceInfo;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.providers.ResourceProvider;
import org.cru.importer.providers.factory.impl.FragmentedPageImportFactoryImpl;
import org.cru.importer.providers.impl.PartialPageContentMapperProviderImpl;
import org.cru.importer.providers.impl.PartialPageMetadataProviderImpl;
import org.cru.importer.service.PostProcessService;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    metatype = true,
    label = "Give importer - BuildUncompletedFragmentedPages",
    description = "Give importer - Post process service to update Stellent references of the imported page")
@Service
@Properties({
    @Property(name = PostProcessService.OSGI_PROPERTY_PROCESS, value = "buildUncompletedFragmentedPages"),
    @Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class BuildUncompletedFragmentedPages implements PostProcessService {

    private final static Logger LOGGER = LoggerFactory.getLogger(BuildUncompletedFragmentedPages.class);
    
    @Reference
    private ResultsCollector resultsCollector;
    
    public void process(ParametersCollector parametersCollector, ResourceMetadata currentMetadata,
            Resource currentResource) throws Exception {
         // Nothing to do here.
    }
    
    public void process(ParametersCollector parametersCollector, List<PostProcessService> postProcessServices) throws Exception {
        PartialPageMetadataProviderImpl metadataProvider = (PartialPageMetadataProviderImpl)
                parametersCollector.getCached(FragmentedPageImportFactoryImpl.CACHE_KEY_METADATA_PROVIDER);
        PartialPageContentMapperProviderImpl contentMapperProvider = (PartialPageContentMapperProviderImpl)
                parametersCollector.getCached(FragmentedPageImportFactoryImpl.CACHE_KEY_CONTENT_MAPPER_PROVIDER);
        ResourceProvider resourceProvider = (ResourceProvider)
                parametersCollector.getCached(FragmentedPageImportFactoryImpl.CACHE_KEY_RESOURCE_PROVIDER);
        Map<String, byte[]> pendingImportFiles = contentMapperProvider.getPendingImportFiles();
        if (!pendingImportFiles.isEmpty()) {
            Session session = parametersCollector.getResourceResolver().adaptTo(Session.class);
            Set<String> fileNames = pendingImportFiles.keySet();
            resultsCollector.addWarning("------------------------------------------------------------------------------------");
            resultsCollector.addWarning("Start importing uncompleted files: " + fileNames.size() + " pending.");
            resultsCollector.addWarning("------------------------------------------------------------------------------------");
            for (String primaryFile : fileNames) {
                try {
                    ResourceMetadata metadata = metadataProvider.getMetadata(primaryFile).get(0);
                    byte[] fileContent = pendingImportFiles.get(primaryFile);
                    ResourceInfo resource = resourceProvider.getResource(metadata, fileContent);
                    contentMapperProvider.processImport(fileContent, metadata, resource.getResource());
                    String message = "Build from uncompleted fragmented page - " + primaryFile + " - " + resource.getResource().getPath();
                    if (session.hasPendingChanges()) {
                        session.save();
                        if (resource.isNewResource()) {
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
                    for (PostProcessService service : postProcessServices) {
                        if (!service.equals(this)) {
                            service.process(parametersCollector, metadata, resource.getResource());
                        }
                    }
                } catch (Exception e) {
                    if (session.hasPendingChanges()) {
                        session.refresh(false);
                    }
                    String errorMessage = primaryFile + " - " + e.getMessage();
                    resultsCollector.addError(errorMessage);
                    LOGGER.info("Error importing " + errorMessage);
                }
            }
        }
    }

}
