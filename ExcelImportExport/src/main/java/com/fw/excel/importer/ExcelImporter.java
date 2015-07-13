package com.fw.excel.importer;

import static com.fw.excel.importer.IExcelImporterConstants.DEFAULT_DATE_FORMAT;
import static com.fw.excel.importer.IExcelImporterConstants.DEFAULT_NUMBER_FORMAT;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fw.excel.importer.data.Column;
import com.fw.excel.importer.data.ColumnType;
import com.fw.excel.importer.data.IDataDigester;
import com.fw.excel.importer.data.IExcelDataFactory;

public class ExcelImporter
{
	public <T> void importFile(String file, IExcelDataFactory<T> dataFactory, IDataDigester<T> dataDigester)
	{
		File fileObj = new File(file);
		
		try(FileInputStream fis = new FileInputStream(fileObj))
		{
			Workbook wb = new HSSFWorkbook(fis);
			Sheet sheet = wb.getSheetAt(0);

			List<String> headings = getHeadings(sheet);
			
			Iterator<Row> rowIt = sheet.rowIterator();
			Row row = null;
			
			//skip the headings row
			rowIt.next();
			
			T bean = null;
			Map<String, Object> dataMap = null;
			
			while(rowIt.hasNext())
			{
				row = rowIt.next();
				dataMap = toDataMap(row, headings, dataFactory);
				
				bean = dataFactory.newDataObject(dataMap);
				dataDigester.digest(bean);
			}
			
			wb.close();
		}catch(Exception ex)
		{
			throw new IllegalStateException(ex.getMessage(), ex);
		}finally
		{
			
		}
	}

	private List<String> getHeadings(Sheet sheet)
	{
		Row row = sheet.getRow(0);
		Iterator<Cell> cellIt = row.iterator();
		Cell cell = null;
		List<String> headings = new LinkedList<>();
		String heading = null;
		
		while(cellIt.hasNext())
		{
			cell = cellIt.next();
			
			heading = cell.getStringCellValue();
			heading = heading.replaceAll("[\\W\\_]+", "");
			heading = heading.toLowerCase();
			
			headings.add(heading);
		}
		
		return headings;
	}
	
	private <T> Map<String, Object> toDataMap(Row row, List<String> columnOrder, IExcelDataFactory<T> factory)
	{
		Iterator<Cell> cellIt = row.iterator();
		Iterator<String> headingIt = columnOrder.iterator();
		Cell cell = null;
		String heading = null;
		Map<String, Object> map = new HashMap<>();
		String strValue = null;
		Column column = null;
		
		while(cellIt.hasNext())
		{
			heading = headingIt.next();
			cell = cellIt.next();
			
			column = factory.getColumn(heading);
			
			if(column == null)
			{
				strValue = getValue(cell, ColumnType.STRING, heading);
				map.put(heading, strValue);
			}
			else
			{
				strValue = getValue(cell, column.getType(), column.getName());
				map.put(column.getName(), column.getType().parse(strValue, column.getJavaType()));
			}
		}
		
		return map;
	}

	public String getValue(Cell cell, ColumnType columnType, String columnName)
	{
		switch(cell.getCellType())
		{
			case Cell.CELL_TYPE_BOOLEAN:
			{
				if(columnType != ColumnType.BOOLEAN)
				{
					throw new IllegalStateException("Boolean cell encountered for column '" + columnName + "'. Expected type: " + columnType);
				}
				
				return "" + cell.getBooleanCellValue();
			}
			case Cell.CELL_TYPE_NUMERIC:
			{
				if(columnType == ColumnType.DATE)
				{
					return DEFAULT_DATE_FORMAT.format(cell.getDateCellValue());
				}
				
				if(columnType != ColumnType.FLOAT && columnType != ColumnType.INTEGER)
				{
					throw new IllegalStateException("Numeric cell encountered for column '" + columnName + "'. Expected type: " + columnType);
				}
				
				return DEFAULT_NUMBER_FORMAT.format(cell.getNumericCellValue());
			}
			default:
			{
				String cellValue = null;
				
				try
				{
					cellValue = cell.getStringCellValue();
				}catch(Exception ex)
				{
					cellValue = "";
				}
				
				return cellValue;
			}	
		}
	}
}
