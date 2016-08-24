package org.cru.importer.providers.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Binary;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cru.importer.bean.ParametersCollector;
import org.cru.importer.providers.MetadataProvider;

/**
 * Extract the metadata for Give import process
 * 
 * @author Nestor de Dios
 *
 */
public class MetadataProviderImpl implements MetadataProvider {

	XSSFWorkbook workbook;
	List<String> colnames;
	int columnFileNames;
	int rowColumnNames;
	DataFormatter excelFormatter;
	
	public MetadataProviderImpl(ParametersCollector parametersCollector) throws Exception {
		workbook = getWorkbook(parametersCollector.getContentFile());
		columnFileNames = -1;
		rowColumnNames = parametersCollector.getRowColumnNames();
		excelFormatter = new DataFormatter();
		if (workbook != null && workbook.getNumberOfSheets()>0) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = sheet.getRow(rowColumnNames);
			if (row != null) {
				colnames = new ArrayList<String>(row.getLastCellNum() - row.getFirstCellNum() + 1);
				for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
					String colname = getStringValue(row,i);
					colnames.add(colname);
					if (columnFileNames==-1 && colname.equals(parametersCollector.getColumnFileName())) {
						columnFileNames = i;
					}
				}
			}
		} else {
			throw new Exception("The content file does not contain a metadata Excel file");
		}
	}

	public Map<String, String> getMetadata(String filename) throws Exception {
		// Step 1: Find the filename in the Excel file
		String partialName;
		try {
			partialName = filename.substring(filename.indexOf("@"));
		} catch (Exception e) {
			partialName = filename;
		}
		int sheetIndex;
		try {
			String sheetNumber = filename.split("/")[1];
			sheetIndex = Integer.parseInt(sheetNumber) - 1;
		} catch (Exception e) {
			sheetIndex = -1;
		}
		XSSFRow metadataRow = null;
		if (sheetIndex == -1) {
			for (int i=0;i<workbook.getNumberOfSheets() && metadataRow==null;i++) {
				metadataRow = getMetadataRow(i, partialName);
			}
		} else {
			metadataRow = getMetadataRow(sheetIndex, partialName);
		}
		// Step 2: extract the metadata
		if (metadataRow != null) {
			Map<String, String> metadata = new HashMap<String, String>();
			for (int i=0;i<colnames.size();i++) {
				metadata.put(colnames.get(i), getStringValue(metadataRow,i));
			}
			return metadata;
		} else {
			throw new Exception("Can not find metadata for this file in the Excel file sarching for " + partialName);
		}
	}
	
	/**
	 * Search a metadata row from a given partial filename and the sheet index
	 * @param sheetIndex
	 * @param partialName
	 * @return
	 */
	private XSSFRow getMetadataRow(int sheetIndex, String partialName) {
		XSSFRow metadataRow = null;
		XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
		for (int j = rowColumnNames + 1; j <= sheet.getLastRowNum() && metadataRow == null; j++){
			XSSFRow row = sheet.getRow(j);
			String rowFileName = getStringValue(row, columnFileNames);
			if (rowFileName.endsWith(partialName)) {
				metadataRow = row;
			}
		}
		return metadataRow;
	}
	
	/**
	 * Extract the excel file containing the metadata from the zip file
	 * @param contentFile
	 * @return
	 * @throws Exception
	 */
	private XSSFWorkbook getWorkbook(Binary contentFile) throws Exception {
		InputStream is = contentFile.getStream();
		try {
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				if (entry.getName().toLowerCase().endsWith(".xlsx")) {
					return new XSSFWorkbook(zis);
				}
			}
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**
	 * Extract a string value representation from the cell
	 * @param row
	 * @param column
	 * @return
	 */
	private String getStringValue(Row row, int column) {
		Cell cell = row.getCell(column);
		return excelFormatter.formatCellValue(cell);
		/*if (cell != null) {
			int cellType = cell.getCellType();
			if (cellType == Cell.CELL_TYPE_FORMULA) {
				cellType = cell.getCachedFormulaResultType();
			}
			switch (cellType) {
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue().toString();
				} else {
					return String.valueOf(Math.round(cell.getNumericCellValue()));
				}
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return "";
			}
		} else {
			return "";
		}*/
	}

}
