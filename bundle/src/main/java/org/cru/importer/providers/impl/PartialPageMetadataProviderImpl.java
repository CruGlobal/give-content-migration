package org.cru.importer.providers.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.util.UrlUtil;

/**
 * Extract the metadata for fragmented pages in import process (From Excel file)
 * 
 * @author Nestor de Dios
 *
 */
public class PartialPageMetadataProviderImpl extends MetadataProviderImpl {
    
    Map<String, List<XSSFRow>> fragmentRowsCache;
    Map<String, List<String>> requiredFragments;

    public PartialPageMetadataProviderImpl(ParametersCollector parametersCollector) throws Exception {
        super(parametersCollector);
        buildFragmentRowsCache();
    }

    private void buildFragmentRowsCache() throws Exception {
        String keyColumnName = "pageContent"; // TODO: move to configuration
        if (!colnames.containsKey(keyColumnName)) {
            throw new Exception("Metadata Excel file does not contains a column called "+ keyColumnName);
        }
        int keyColumnNumber = colnames.get(keyColumnName);
        fragmentRowsCache = new HashMap<String, List<XSSFRow>>();
        requiredFragments = new HashMap<String, List<String>>();
        for (int i=0;i<workbook.getNumberOfSheets();i++) {
            XSSFSheet currentsheet = workbook.getSheetAt(i);
            for (int j = rowColumnNames + 1; j <= currentsheet.getLastRowNum(); j++){
                XSSFRow currentrow = currentsheet.getRow(j);
                if (currentrow != null) {
                    String rowFileNames = getStringValue(currentrow, keyColumnNumber);
                    try {
                        String rowFilename = decodeFilename(getStringValue(currentrow, columnFileNames));
                        List<String> fragments = extractFragments(rowFileNames);
                        requiredFragments.put(rowFilename, fragments);
                        for (String filename : fragments) {
                            List<XSSFRow> rows;
                            if (fragmentRowsCache.containsKey(filename)) {
                                rows = fragmentRowsCache.get(filename);
                            } else {
                                rows = new LinkedList<XSSFRow>();
                                fragmentRowsCache.put(filename, rows);
                            }
                            rows.add(currentrow);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private List<String> extractFragments(String rowFileNames) throws Exception {
        List<String> fragments = new LinkedList<String>();
        Map<String, String> parameters = UrlUtil.splitQuery(rowFileNames);
        for (String key : parameters.keySet()) {
            if (isApplyParameter(key)) {
                fragments.add(parameters.get(key).toLowerCase());
            }
        }
        return fragments;
    }

    private boolean isApplyParameter(String key) {
        // TODO Move to a regex in configuration
        return key.startsWith("Landing_Page_Content_");
    }

    @Override
    public List<ResourceMetadata> getMetadata(String filename) throws Exception {
        try {
            List<ResourceMetadata> metas = super.getMetadata(filename);
            String partialFilename = decodeFilename(filename);
            metas.get(0).setRequiredFragments(requiredFragments.get(partialFilename));
            return metas;
        } catch (Exception e) {
            String partialFilename = decodeFilename(filename);
            if (fragmentRowsCache.containsKey(partialFilename)) {
                List<XSSFRow> rows = fragmentRowsCache.get(partialFilename);
                List<ResourceMetadata> result = new LinkedList<ResourceMetadata>();
                for (XSSFRow xssfRow : rows) {
                    ResourceMetadata meta = new ResourceMetadata(xssfRow, colnames, excelFormatter, partialFilename, true);
                    meta.setRequiredFragments(requiredFragments.get(decodeFilename(getStringValue(xssfRow, columnFileNames))));
                    result.add(meta);
                }
                return result;
            }
            throw e;
        }
    }

    private String decodeFilename(String filename) {
        String partialFilename = FilenameUtils.getBaseName(filename);
        if (partialFilename.contains("~")) {
            partialFilename = partialFilename.substring(0,partialFilename.indexOf("~"));
        }
        return partialFilename.toLowerCase();
    }
    
}
