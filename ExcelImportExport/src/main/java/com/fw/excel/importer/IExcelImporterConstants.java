package com.fw.excel.importer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public interface IExcelImporterConstants
{
	public SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public DecimalFormat DEFAULT_NUMBER_FORMAT = new DecimalFormat("##.##");

}
