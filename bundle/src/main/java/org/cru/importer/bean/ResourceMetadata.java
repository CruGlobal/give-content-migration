package org.cru.importer.bean;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class ResourceMetadata {

	private XSSFRow row;
	private Map<String, Integer> colnames;
	private DataFormatter excelFormatter;
	private String filename;
	private boolean fragment;
	private List<String> requiredFragments;
	
	public ResourceMetadata(XSSFRow row, Map<String, Integer> colnames, DataFormatter excelFormatter, String filename,
            boolean fragment) {
        super();
        this.row = row;
        this.colnames = colnames;
        this.excelFormatter = excelFormatter;
        this.filename = filename;
        this.fragment = fragment;
    }

	public String getValue(String colname) {
		if (row != null && colnames.containsKey(colname)) {
			int index = colnames.get(colname);
			Cell cell = row.getCell(index);
			return excelFormatter.formatCellValue(cell);
		} else {
			return "";
		}
	}
	
	public Set<String> getPropertyNames() {
		return colnames.keySet();
	}

    public String getFilename() {
        return filename;
    }

    public boolean isFragment() {
        return fragment;
    }

    public List<String> getRequiredFragments() {
        return requiredFragments;
    }

    public void setRequiredFragments(List<String> requiredFragments) {
        this.requiredFragments = requiredFragments;
    }

}
