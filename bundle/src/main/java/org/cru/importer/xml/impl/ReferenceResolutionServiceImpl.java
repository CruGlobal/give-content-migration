package org.cru.importer.xml.impl;

import java.util.HashMap;
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
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.xml.ReferenceResolutionService;
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
