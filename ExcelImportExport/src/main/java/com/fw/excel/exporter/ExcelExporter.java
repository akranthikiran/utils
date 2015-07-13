package com.fw.excel.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fw.excel.exporter.data.IExcelDataReport;

public class ExcelExporter
{
	private CellStyle createHeadingCell(Workbook wb)
	{
		CellStyle headingStyle = wb.createCellStyle();
		
		headingStyle.setFillBackgroundColor(HSSFColor.GREY_40_PERCENT.index);
		headingStyle.setAlignment(CellStyle.ALIGN_CENTER);

		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headingStyle.setFont(font);
		
		return headingStyle;
	}
	
	private void addReport(Workbook wb, IExcelDataReport report)
	{
		Sheet sheet = wb.createSheet(report.getName());
		String headings[] = report.headings();
		Cell cell = null;
		int rowIndex = 0;
		
		if(headings != null)
		{
			CellStyle headingStyle = createHeadingCell(wb);
			Row headingRow = sheet.createRow(rowIndex);
			int headingIndex = 0;
			
			for(String heading: headings)
			{
				cell = headingRow.createCell(headingIndex);
				cell.setCellValue(heading);
				cell.setCellStyle(headingStyle);
				
				headingIndex++;
			}
			
			rowIndex++;
		}
		
		List<List<com.fw.excel.exporter.data.Cell>> rows = report.rows();
		
		if(rows != null)
		{
			Row currentRow = null;
			int cellIndex = 0;
			
			for(List<com.fw.excel.exporter.data.Cell> row: rows)
			{
				currentRow = sheet.createRow(rowIndex);
				cellIndex = 0;
				
				for(com.fw.excel.exporter.data.Cell dataCell: row)
				{
					cell = currentRow.createCell(cellIndex);
					cell.setCellValue(dataCell.getValue());
					sheet.autoSizeColumn(cellIndex);
				
					cellIndex++;
				}
				
				rowIndex++;
			}
		}
	}
	
	public String generateExcelSheet(String filePath, IExcelDataReport... reports) throws IOException
	{
		Workbook wb = new HSSFWorkbook();
		 
		for(IExcelDataReport report: reports)
		{
			addReport(wb, report);
		}

		if(!filePath.endsWith(".xlsx"))
		{
			filePath += ".xlsx";
		}
		
		FileOutputStream fos = new FileOutputStream(filePath);
		
		wb.write(fos);
		fos.flush();
		
		fos.close();
		
		return filePath;
	}
}
