package org.cru.importer.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ReferenceReplacement;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.service.PostProcessService;
import org.cru.importer.service.ReferenceResolutionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;

@Component(
    metatype = true,
    label = "Give importer - UpdatePageReferences",
    description = "Give importer - Post process service to update Stellent references of the imported page")
@Service
@Properties({
    @Property(name = PostProcessService.OSGI_PROPERTY_PROCESS, value = "UpdatePageReferences"),
    @Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class UpdatePageReferences implements PostProcessService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdatePageReferences.class);
    
    private static final String CACHE_UPDATE_POLICIES_KEY = "updatePolicies";

    private static final String POLICY_OVERRIDE = "override";
    private static final String POLICY_HTML = "html";

    private static final String PROPERTY_CONTENTID = "contentId";
    private static final String PROPERTY_FULL_CONTENTID = "jcr:content/contentId";
    
    @Reference
    private ResultsCollector resultsCollector;
    
    @Reference
    private ReferenceResolutionService referenceResolutionService;
    
    public void process(ParametersCollector parametersCollector, ResourceMetadata currentMetadata,
            Resource currentResource) throws Exception {
        String contentId = getCurrentContentId(currentResource);
        if (contentId != null) {
            Iterator<Resource> pages = searchRelatedPages(parametersCollector.getResourceResolver(), parametersCollector.getReferenceResolutionBasePathPages(), contentId);
            if (pages.hasNext()) {
                List<UpdatePageReferencesPolicy> updatePageReferencesPolicies = getUpdatePageReferencesPolicy(parametersCollector);
                Session session = parametersCollector.getResourceResolver().adaptTo(Session.class);
                List<ReferenceReplacement> referenceReplacements = referenceResolutionService.getReferenceReplacements(parametersCollector);
                String currentFilename = currentMetadata.getValue(parametersCollector.getColumnFileName());
                while (pages.hasNext()) {
                    Resource pageResource = pages.next();
                    updateResource(referenceReplacements, updatePageReferencesPolicies, contentId, currentResource.getPath(), pageResource);
                    if (session.hasPendingChanges()) {
                        session.save();
                        String message = currentFilename + " - Related page - " + pageResource.getPath();
                        resultsCollector.addModifiedPage(message);
                        LOGGER.info("Existed resource modified - " + message);
                    }
                }
            }
        }
    }

    private String getCurrentContentId(Resource currentResource) throws Exception {
        Page page = currentResource.adaptTo(Page.class);
        if (page != null && page.hasContent()) {
            Node contentNode = page.getContentResource().adaptTo(Node.class);
            javax.jcr.Property property = contentNode.getProperty(PROPERTY_CONTENTID);
            return property.getString();
        } else {
            return null;
        }
    }

    private Iterator<Resource> searchRelatedPages(ResourceResolver resourceResolver, String path, String dDocName) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", "cq:Page");
        map.put("path", path);
        map.put("fulltext", dDocName);
        map.put("property", PROPERTY_FULL_CONTENTID);
        map.put("property.value", dDocName);
        map.put("property.operation", "unequals");
        Query query = resourceResolver.adaptTo(QueryBuilder.class).createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        query.setHitsPerPage(0);
        SearchResult result = query.getResult();
        return result.getResources();
    }
    
    @SuppressWarnings("unchecked")
    public List<UpdatePageReferencesPolicy> getUpdatePageReferencesPolicy(ParametersCollector parametersCollector) {
        if (!parametersCollector.isCached(CACHE_UPDATE_POLICIES_KEY)) {
            List<UpdatePageReferencesPolicy> updatePageReferencesPolicies = new LinkedList<UpdatePageReferencesPolicy>();
            for (String policy : parametersCollector.getUpdatePageReferencesPolicy()) {
                updatePageReferencesPolicies.add(new UpdatePageReferencesPolicy(policy));
            }
            parametersCollector.putCache(CACHE_UPDATE_POLICIES_KEY, updatePageReferencesPolicies);
            return updatePageReferencesPolicies;
        }
        return (List<UpdatePageReferencesPolicy>) parametersCollector.getCached(CACHE_UPDATE_POLICIES_KEY);
    }
    
    private void updateResource(List<ReferenceReplacement> referenceReplacements, List<UpdatePageReferencesPolicy> updatePageReferencesPolicies,
            String contentId, String path, Resource resource) throws Exception {
        for (UpdatePageReferencesPolicy updatePageReferencesPolicy : updatePageReferencesPolicies) {
            if (updatePageReferencesPolicy.getComponent().equals(resource.getResourceType())) {
                updateProperty(referenceReplacements, updatePageReferencesPolicy, contentId, path, resource);
            }
        }
        Iterator<Resource> children = resource.listChildren();
        while (children.hasNext()) {
            updateResource(referenceReplacements, updatePageReferencesPolicies, contentId, path, children.next());
        }
    }
    
    private void updateProperty(List<ReferenceReplacement> referenceReplacements, UpdatePageReferencesPolicy updatePageReferencesPolicy,
            String contentId, String path, Resource resource) throws Exception {
        Node resourceNode = resource.adaptTo(Node.class);
        String propertyName = updatePageReferencesPolicy.getProperty();
        if (resourceNode != null && resourceNode.hasProperty(propertyName)) {
            String option = updatePageReferencesPolicy.getOption();
            javax.jcr.Property property = resourceNode.getProperty(propertyName);
            if (POLICY_OVERRIDE.equals(option)) {
                if (property.getString().contains(contentId)) {
                    property.setValue(path);
                }
            } else if (POLICY_HTML.equals(option)) {
                Document doc = Jsoup.parse(property.getString());
                for (ReferenceReplacement referenceReplacement : referenceReplacements) {
                    for (Element element : doc.select(referenceReplacement.getSelector())) {
                        String href = element.attr(referenceReplacement.getAttribute());
                        if (href.contains(contentId)) {
                            href = path;
                        }
                        element.attr(referenceReplacement.getAttribute(), href);
                    }
                }
                property.setValue(doc.body().html());
            }
        }
    }

    private class UpdatePageReferencesPolicy {

        private String component;
        private String property;
        private String option;
        
        public UpdatePageReferencesPolicy(String policy) {
            String[] split = policy.split(":");
            component = split[0];
            property = split[1];
            option = split[2];
        }

        public String getComponent() {
            return component;
        }

        public String getProperty() {
            return property;
        }

        public String getOption() {
            return option;
        }

    }

}
