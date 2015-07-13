package com.fw.excel.exporter.data;

import java.util.List;

public interface IExcelDataReport
{
	public String getName();
	public String[] headings();
	public List<List<Cell>> rows();
}
