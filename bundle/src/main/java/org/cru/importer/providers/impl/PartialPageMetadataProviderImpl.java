package org.cru.importer.providers.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.cru.importer.bean.NotMetadataFoundException;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.bean.ResourceMetadata;
import org.cru.importer.bean.ResultsCollector;
import org.cru.importer.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Extract the metadata for fragmented pages in import process (From Excel file)
 * 
 * @author Nestor de Dios
 *
 */
public class PartialPageMetadataProviderImpl extends MetadataProviderImpl {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(PartialPageMetadataProviderImpl.class);
    
    Map<String, List<XSSFRow>> fragmentRowsCache;
    Map<String, List<String>> requiredFragments;

    public PartialPageMetadataProviderImpl(ParametersCollector parametersCollector, ResultsCollector resultsCollector) throws Exception {
        super(parametersCollector);
        buildFragmentRowsCache(parametersCollector, resultsCollector);
    }

    private void buildFragmentRowsCache(ParametersCollector parametersCollector, ResultsCollector resultsCollector) throws Exception {
        String keyColumnName = parametersCollector.getFragmentsColumnFileName();
        if (!colnames.containsKey(keyColumnName)) {
            throw new Exception("Metadata Excel file does not contains a column called "+ keyColumnName);
        }
        String fragmentPattern = parametersCollector.getFragmentsAcceptanceParameterPattern();
        fragmentPattern = (Strings.isNullOrEmpty(fragmentPattern)) ? ".*" : fragmentPattern;
        Pattern fragmentColumnPattern = Pattern.compile(fragmentPattern);
        int keyColumnNumber = colnames.get(keyColumnName);
        fragmentRowsCache = new HashMap<String, List<XSSFRow>>();
        requiredFragments = new HashMap<String, List<String>>();
        for (int i=0;i<workbook.getNumberOfSheets();i++) {
            XSSFSheet currentsheet = workbook.getSheetAt(i);
            for (int j = rowColumnNames + 1; j <= currentsheet.getLastRowNum(); j++){
                XSSFRow currentrow = currentsheet.getRow(j);
                if (currentrow != null) {
                    String rowFileNames = getStringValue(currentrow, keyColumnNumber);
                    String rowFilename = decodeFilename(getStringValue(currentrow, columnFileNames));
                    try {
                        List<String> fragments = extractFragments(fragmentColumnPattern, rowFileNames);
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
                        resultsCollector.addWarning("Building metadata from Excel file: " + rowFilename + " - " + e.getMessage());
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private List<String> extractFragments(Pattern fragmentColumnPattern, String rowFileNames) throws Exception {
        List<String> fragments = new LinkedList<String>();
        if (!Strings.isNullOrEmpty(rowFileNames)) {
            Map<String, String> parameters = UrlUtil.splitQuery(rowFileNames);
            for (String key : new TreeSet<String>(parameters.keySet())) {
                if (isApplyParameter(fragmentColumnPattern, key)) {
                    fragments.add(parameters.get(key).toLowerCase());
                }
            }
        }
        return fragments;
    }

    private boolean isApplyParameter(Pattern fragmentColumnPattern, String key) {
        return fragmentColumnPattern.matcher(key).matches();
    }

    @Override
    public List<ResourceMetadata> getMetadata(String filename) throws NotMetadataFoundException {
        try {
            List<ResourceMetadata> metas = super.getMetadata(filename);
            String partialFilename = decodeFilename(filename);
            metas.get(0).setRequiredFragments(requiredFragments.get(partialFilename));
            return metas;
        } catch (NotMetadataFoundException e) {
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
