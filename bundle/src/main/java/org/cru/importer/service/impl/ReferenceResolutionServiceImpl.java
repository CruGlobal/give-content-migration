package org.cru.importer.service.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ReferenceReplacement;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.service.ReferenceResolutionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

@Component(
    metatype = true,
    label = "Give importer - ReferenceResolutionService",
    description = "Resolves a URL coming from Stellent to an AEM reference"
)
@Service
public class ReferenceResolutionServiceImpl implements ReferenceResolutionService {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ReferenceResolutionServiceImpl.class);
    
    private static final String CACHE_REFERENCES_KEY = "references";
    
    private static final String TYPE_LINK = "link";
    private static final String TYPE_NODELINK = "nodelink";
    private static final String TYPE_RESOURCE = "resource";
    private static final String TYPE_RENDITION = "rendition";

    private static final String PROPERTY_PAGE = "cq:Page";
    private static final String PROPERTY_ASSET = "dam:Asset";
    private static final String PROPERTY_PAGE_CONTENTID = "jcr:content/contentId";
    private static final String PROPERTY_ASSET_CONTENTID = "jcr:content/metadata/contentId";

    private static final String STELLENT_EXRACTOR_REGEX = "wcmUrl\\(\\s*?'(.*?)'\\s*?,\\s*?'(.*?)'";
    private static Pattern pattern = Pattern.compile(STELLENT_EXRACTOR_REGEX);
    
    @Reference
    private ResultsCollector resultsCollector;
    
    public String resolveAllReferences(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String htmlSourceParameter) {
        Document doc = Jsoup.parse(htmlSourceParameter);
        for (ReferenceReplacement referenceReplacement : getReferenceReplacements(parametersCollector)) {
            for (Element element : doc.select(referenceReplacement.getSelector())) {
                String href = element.attr(referenceReplacement.getAttribute());
                href = resolveReference(parametersCollector, currentMetadata, href);
                element.attr(referenceReplacement.getAttribute(), href);
            }
        }
        return doc.body().html();
    }
    
    @SuppressWarnings("unchecked")
    public List<ReferenceReplacement> getReferenceReplacements(ParametersCollector parametersCollector) {
        if (!parametersCollector.isCached(CACHE_REFERENCES_KEY)) {
            List<ReferenceReplacement> referenceReplacements = new LinkedList<ReferenceReplacement>();
            for (String reference : parametersCollector.getReferenceResolutionReplacements()) {
                referenceReplacements.add(new ReferenceReplacement(reference));
            }
            parametersCollector.putCache(CACHE_REFERENCES_KEY, referenceReplacements);
            return referenceReplacements;
        }
        return (List<ReferenceReplacement>) parametersCollector.getCached(CACHE_REFERENCES_KEY);
    }
    
    public String resolveReference(ParametersCollector parametersCollector, ResourceMetadata currentMetadata, String originalReference) {
        if (originalReference == null || originalReference.trim().equals("") || isIgnoredPrefix(parametersCollector, originalReference)){
            return originalReference;
        }
        String message = "";
        String currentFilename = currentMetadata.getValue(parametersCollector.getColumnFileName());
        try {
            Matcher matcher = pattern.matcher(originalReference);
            if (matcher.find()) {
                String type = matcher.group(1);
                String dDocName = matcher.group(2);
                String reference = resolveContentReference(parametersCollector, type, dDocName);
                if (reference != null) {
                    return reference;
                } else {
                    message = currentFilename + " - Cannot find content - type: " + type + " - dDocName: " + dDocName;
                }
            } else {
                message = currentFilename + " - Cannot extract content ID from string: " + originalReference;
            }
            LOGGER.warn(message);
        } catch (Exception e) {
            message = currentFilename + " - Error resolving reference for: " + originalReference;
            LOGGER.error(message, e);
        }
        resultsCollector.addWarning(message);
        return originalReference;
    }

    private boolean isIgnoredPrefix(ParametersCollector parametersCollector, String originalReference) {
        String reference = originalReference.trim().toLowerCase();
        for (String prefix : parametersCollector.getReferenceResolutionIgnoredPrefixes()) {
            if (reference.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String resolveContentReference(ParametersCollector parametersCollector, String type, String dDocName) throws Exception {
        if (TYPE_LINK.equalsIgnoreCase(type)) {
            return searchContent(parametersCollector, PROPERTY_PAGE, parametersCollector.getReferenceResolutionBasePathPages(), PROPERTY_PAGE_CONTENTID, dDocName);
        } else if (TYPE_NODELINK.equalsIgnoreCase(type)) {
            return null; // TODO: Verify how to search this type
        } else if (TYPE_RESOURCE.equalsIgnoreCase(type)) {
            return searchContent(parametersCollector, PROPERTY_ASSET, parametersCollector.getReferenceResolutionBasePathAssets(), PROPERTY_ASSET_CONTENTID, dDocName);
        } else if (TYPE_RENDITION.equalsIgnoreCase(type)) {
            return searchContent(parametersCollector, PROPERTY_ASSET, parametersCollector.getReferenceResolutionBasePathAssets(), PROPERTY_ASSET_CONTENTID, dDocName);
        } else {
            return null;
        }
    }
    
    private String searchContent(ParametersCollector parametersCollector, String contentType, String path, String property, String dDocName) throws Exception {
        ResourceResolver resourceResolver = parametersCollector.getResourceResolver();
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", contentType);
        map.put("path", path);
        map.put("1_property", property);
        map.put("1_property.value", dDocName);
        Query query = resourceResolver.adaptTo(QueryBuilder.class).createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();
        List<Hit> hits = result.getHits();
        if (hits.size() > 0) {
            return hits.get(0).getPath();
        } else {
            return null;
        }
    }
    
}
