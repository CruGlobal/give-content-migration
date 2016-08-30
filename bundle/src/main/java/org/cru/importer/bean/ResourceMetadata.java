package org.cru.importer.bean;

import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class ResourceMetadata {

	private XSSFRow row;
	private Map<String, Integer> colnames;
	private DataFormatter excelFormatter;
	
	public ResourceMetadata(XSSFRow row, Map<String, Integer> colnames, DataFormatter excelFormatter) {
		this.row = row;
		this.colnames = colnames;
		this.excelFormatter = excelFormatter;
	}
	
	public String getValue(String colname) {
		if (colnames.containsKey(colname)) {
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

}
