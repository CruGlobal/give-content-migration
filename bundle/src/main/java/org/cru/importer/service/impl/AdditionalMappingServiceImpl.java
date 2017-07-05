package org.cru.importer.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.service.AdditionalMappingService;
import org.cru.importer.service.DateParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import net.sf.saxon.trans.XPathException;

@Component(
    metatype = true,
    label = "Give importer - AdditionalMappingServiceImpl",
    description = "Get additional mapping info from a CVS file"
)
@Service
public class AdditionalMappingServiceImpl implements AdditionalMappingService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AdditionalMappingServiceImpl.class);

    private static final String CACHE_CSV_MAPPING_KEY = "csvMapping";

    @Reference
    private DateParserService dateFormatter;
    
    public String getFormattedRow(ParametersCollector parametersCollector, String[] priority, String keyName,
            String keyValue) throws XPathException {
        CsvCache csvCache = getCache(parametersCollector, keyName, priority);
        return csvCache.getFormattedRow(keyValue);
    }

    public Map<String,String> getAdditionalMapping(ParametersCollector parametersCollector, String[] priority, String keyName,
            String keyValue) throws XPathException {
        CsvCache csvCache = getCache(parametersCollector, keyName, priority);
        return csvCache.getAdditionalMappingRow(keyValue);
    }

    private CsvCache getCache(ParametersCollector parametersCollector, String key, String[] priority) throws XPathException {
        if (!parametersCollector.isCached(CACHE_CSV_MAPPING_KEY)) {
            CsvCache csvCache = new CsvCache(key, priority, parametersCollector);
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
            parametersCollector.putCache(CACHE_CSV_MAPPING_KEY, csvCache);
            return csvCache;
        } else {
            return (CsvCache) parametersCollector.getCached(CACHE_CSV_MAPPING_KEY);
        }
    }

    class CsvCache {

        private static final String LAST = "LAST";
        private static final String DATE = "DATE";
        private static final String INT = "INT";

        private String[] columnNames;
        private Map<String, String[]> rowsCache;
        private String key;
        private int keyPos;
        private String priorityColumn;
        private Integer priorityColumnPos;
        private String priorityType;
        private Integer priorityOrder;
        private ParametersCollector parametersCollector;

        public CsvCache(String key, String[] priority, ParametersCollector parametersCollector) {
            super();
            this.key = key;
            this.parametersCollector = parametersCollector;
            this.keyPos = -1;
            this.rowsCache = new HashMap<String, String[]>();
            if (priority != null) {
                priorityColumn = priority[0];
                if (LAST.equals(priority[1])) {
                    priorityOrder = 1;
                } else {
                    priorityOrder = 0;
                }
                priorityType = priority[2];
            }
        }

        public Map<String, String> getAdditionalMappingRow(String keyValue) {
            Map<String, String> aux = new HashMap<String, String>();
            if (rowsCache.containsKey(keyValue)) {
                String[] columnValues = rowsCache.get(keyValue);
                for (int i = 0; i < columnValues.length; i++) {
                    aux.put(this.columnNames[i], columnValues[i]);
                }
            }
            return aux;
        }

        public String getFormattedRow(String keyValue) {
            if (rowsCache.containsKey(keyValue)) {
                StringBuilder sb = new StringBuilder();
                String[] columnValues = rowsCache.get(keyValue);
                for (int i = 0; i < columnValues.length; i++) {
                    sb.append(String.format("<%s>%s</%s>", this.columnNames[i], columnValues[i], this.columnNames[i]));
                }
                return sb.toString();
            } else {
                return "";
            }
        }

        public void setColumnNames(String[] columnNames) {
            this.columnNames = new String[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                this.columnNames[i] = columnNames[i].replaceAll("[^a-zA-Z0-9]", "_");
                if (key.equals(columnNames[i])) {
                    this.keyPos = i;
                }
                if (priorityColumn != null && priorityColumn.equals(columnNames[i])) {
                    priorityColumnPos = i;
                }
            }
        }

        public void addDataRow(String[] dataRow) throws XPathException {
            String keyValue = dataRow[this.keyPos];
            String[] cacheRow = this.rowsCache.get(keyValue);
            if (cacheRow != null && priorityColumnPos != null) {
                if (keepCacheRow(cacheRow[priorityColumnPos], dataRow[priorityColumnPos])) {
                    return;
                }
            }
            this.rowsCache.put(keyValue, dataRow);
        }

        private boolean keepCacheRow(String cacheValue, String newValue) throws XPathException {
            if (DATE.equals(priorityType)) {
                Date cacheDate = dateFormatter.parseDate(parametersCollector, cacheValue);
                Date newDate = dateFormatter.parseDate(parametersCollector, newValue);
                return cacheDate.compareTo(newDate) == priorityOrder;
            } else if (INT.equals(priorityType)) {
                Integer cacheInt = Integer.parseInt(cacheValue);
                Integer newInt = Integer.parseInt(newValue);
                return cacheInt.compareTo(newInt) == priorityOrder;
            }
            return true;
        }

    }

}
