package org.cru.importer.util;

public class XmlEntitiesUtil {

	/**
	 * For each bad entity found in Give files, a mapping well formed entity is provided
	 */
	private static String[][] badEntitiesMapping = {
		{"&ntilde;", "&amp;ntilde;"},
		{"&ccedil;", "&amp;ccedil;"},
		{"&Ccedil;", "&amp;Ccedil;"},
		{"&icirc;", "&amp;icirc;"},
		{"&uuml;", "&amp;uuml;"},
		{"&iquest;","&amp;iquest;"},
		{"&amp;amp;#8364;", "&amp;euro;"},
		{"&dDocName","&amp;dDocName"}
	}; 
	
	/**
	 * Fixes bad entities for XML processing
	 * 
	 * @param source
	 * @return
	 */
	public static String fixBabEntities(String source) {
		String dest = source;
		for (String[] badEntity : badEntitiesMapping) {
			dest = dest.replaceAll(badEntity[0], badEntity[1]);
		}
		return dest;
	}
	
}
