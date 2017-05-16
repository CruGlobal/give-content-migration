package org.cru.importer.xml.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.xml.GiveSourceFactory;
import org.cru.importer.xml.GiveSourceFactoryBase;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

import net.sf.saxon.trans.XPathException;

@Component(
	metatype = true,
	label = "Give importer - searchImage",
	description = "Search for an image in the DAM to get a reference. Use the searchImage URL withh parameter image"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "searchImage"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class ImageSourceFactory extends GiveSourceFactoryBase {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ImageSourceFactory.class);
	
	private static final String PARAM_IMAGE = "image";
	private static final String IMAGES_CONTENT_PATH = "/content/dam"; // TODO: Move to service property
	private static final String IMAGE_ID_EXRACTOR_REGEX = "wcmUrl\\(\\s*?'.*?'\\s*?,\\s*?'(.*?)'"; // TODO: Move to service property
	private static Pattern pattern = Pattern.compile(IMAGE_ID_EXRACTOR_REGEX);

    @Reference
    private ResultsCollector resultsCollector;
	
    @Override
    protected String resolve(ParametersCollector parametersCollector, Map<String, String> params)
            throws XPathException {
        String image = "";
        if (params.containsKey(PARAM_IMAGE) && !params.get(PARAM_IMAGE).equals("")) {
            String imageCode = captureImageCode(params.get(PARAM_IMAGE));
            if (imageCode != null) {
                image = searchInDam(parametersCollector.getResourceResolver(), imageCode);
            }
        }
        return "<image>" + image + "</image>";
    }

	/**
	 * Get the image id from the img tag
	 * @param imageStr
	 * @return
	 * @throws XPathException
	 */
	private String captureImageCode(String imageStr) throws XPathException {
		// Example: imageStr = "<img src=\"[!--$wcmUrl('resource','CMS3_254491')--]\" alt="Photo of Cherry Fields" />";
		Matcher matcher = pattern.matcher(imageStr);
		if (matcher.find()) {
		    return matcher.group(1);
		} else {
			LOGGER.warn("Impossible to extract image ID from " + imageStr);
			return null;
			//throw new XPathException("Impossible to extract image ID from " + imageStr);
		}
	}

	/**
	 * Search the image ID in the DAM and return the path for the image
	 * @param resourceResolver
	 * @param imageCode
	 * @return
	 * @throws XPathException
	 */
	private String searchInDam(ResourceResolver resourceResolver, String imageCode) throws XPathException {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("path", IMAGES_CONTENT_PATH);
			map.put("type", "dam:Asset");
			map.put("1_property", "jcr:content/metadata/contentId"); // TODO: check the property name with CRU team
			map.put("1_property.value", imageCode);

			Query query = resourceResolver.adaptTo(QueryBuilder.class).createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
			SearchResult result = query.getResult();
			List<Hit> hits = result.getHits();
			if (hits.size() > 0) {
				return hits.get(0).getPath();
			} else {
			    String message = "Image " + imageCode + " cannot be found";
				LOGGER.warn(message);
				resultsCollector.addWarning(message);
				return "";
			}
		} catch (RepositoryException e) {
			throw new XPathException("Cannot get the image path for " + imageCode);
		}
	}

}
