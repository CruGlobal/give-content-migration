package org.cru.importer.xml.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.trans.XPathException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.util.UrlUtil;
import org.cru.importer.xml.GiveSourceFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

@Component(
	metatype = true,
	label = "Give importer - csvAdditionalMapping",
	description = "Get additional mapping data from a CSV source. Use the formatDate URL with parameter keyColumn and keyValue"
)
@Service
@Properties({
	@Property(name = GiveSourceFactory.OSGI_PROPERTY_TYPE, value = "csvAdditionalMapping"),
	@Property(name = Constants.SERVICE_RANKING, intValue = 1)
})
public class AdditionalCSVMappingSourceFactory implements GiveSourceFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(AdditionalCSVMappingSourceFactory.class);
	
	private static final String PARAM_KEY_COLUMN = "keyColumn";
	private static final String PARAM_KEY_VALUE = "keyValue";
	
	public Source resolve(ParametersCollector parametersCollector, String parameters) throws XPathException {
		Map<String, String> params;
		try {
			params = UrlUtil.splitQuery(parameters);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Error parsing parmeters: " + parameters + "  - Error: " + e.getMessage());
			params = new HashMap<String, String>();
		}
		String additionalMapping = "";
		if (params.containsKey(PARAM_KEY_COLUMN) && !params.get(PARAM_KEY_COLUMN).equals("") &&
				params.containsKey(PARAM_KEY_VALUE) && !params.get(PARAM_KEY_VALUE).equals("")) {
			String key = params.get(PARAM_KEY_COLUMN);
			CsvCache csvCache = getCache(parametersCollector, key);
			additionalMapping = csvCache.getFormattedRow(params.get(PARAM_KEY_VALUE));
		}
		additionalMapping = "<data>" + additionalMapping + "</data>";
		InputStream stream = new ByteArrayInputStream(additionalMapping.getBytes(StandardCharsets.UTF_8));
		return new StreamSource(stream);
	}

	private CsvCache getCache(ParametersCollector parametersCollector, String key) {
		if (parametersCollector.getAdditionalMappingCache() == null) {
			CsvCache csvCache = new CsvCache(key);
			try {
				CSVReader reader = new CSVReader(new InputStreamReader(parametersCollector.getAdditionalMappingFile()));
				String[] row;
				boolean columnNamesSet = false;
				while ((row = reader.readNext()) != null) {
					if (!columnNamesSet) {
						csvCache.setColumnNames(row);
						columnNamesSet = true;
					} else {
						csvCache.addDataRow(row);
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error("Error loading CSV file", e);
			}
			parametersCollector.setAdditionalMappingCache(csvCache);
			return csvCache;
		} else {
			return (CsvCache) parametersCollector.getAdditionalMappingCache();
		}
	}
	
	class CsvCache {
		
		private String[] columnNames;
		private Map<String,String[]> rowsCache;
		private String key;
		private int keyPos;
		
		public CsvCache(String key) {
			super();
			this.key = key;
			this.keyPos = -1;
			this.rowsCache = new HashMap<String, String[]>();
		}

		public String getFormattedRow(String keyValue) {
			if (rowsCache.containsKey(keyValue)) {
				StringBuilder sb = new StringBuilder();
				String[] columnValues = rowsCache.get(keyValue);
				for (int i=0;i<columnValues.length;i++) {
					sb.append(String.format("<%s>%s</%s>", this.columnNames[i],columnValues[i],this.columnNames[i]));
				}
				return sb.toString();
			} else {
				return "";
			}
		}

		public void setColumnNames(String[] columnNames) {
			this.columnNames = new String[columnNames.length];
			for (int i=0;i<columnNames.length;i++) {
				this.columnNames[i] = columnNames[i].replaceAll("[^a-zA-Z0-9]", "_");
				if (key.equals(columnNames[i])) {
					this.keyPos = i;
				}
			}
		}
		
		public void addDataRow(String[] dataRow) {
			String keyValue = dataRow[this.keyPos];
			this.rowsCache.put(keyValue, dataRow);
		}
		
	}

}
