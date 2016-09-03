package org.cru.importer.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section of a relative path to build the final resource path
 * 
 * @author Nestor de Dios
 *
 */
public class RelativePathSection {

	private String paramName;
	private Pattern pattern;
	private String replacement;

	private RelativePathSection(String paramName, Pattern pattern, String replacement) {
		this.paramName = paramName;
		this.pattern = pattern;
		this.replacement = replacement;
	}

	public static List<RelativePathSection> buildFromStrategy(String[] strategy) {
		List<RelativePathSection> result = new LinkedList<RelativePathSection>();
		for (int i=0;i<strategy.length;i++) {
			String[] path = strategy[i].split(":");
			String paramName = path[0];
			String replacement = path[2];
			Pattern pattern = Pattern.compile(path[1]);
			RelativePathSection section = new RelativePathSection(paramName, pattern, replacement);
			result.add(section);
		}
		return result;
	}
	
	public static String buildPath(List<RelativePathSection> pathSections, ResourceMetadata metadata) {
		StringBuilder finalPathBuilder = new StringBuilder();
		for (RelativePathSection relativePathSection : pathSections) {
			Matcher m = relativePathSection.pattern.matcher(metadata.getValue(relativePathSection.paramName));
			if (m.find()) {
				finalPathBuilder.append(m.replaceFirst(relativePathSection.replacement));
			}
		}
		return finalPathBuilder.toString();
	}
	
}
