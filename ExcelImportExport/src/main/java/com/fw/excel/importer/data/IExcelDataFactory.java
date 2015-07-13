package com.fw.excel.importer.data;

import java.util.Collection;
import java.util.Map;

public interface IExcelDataFactory<T>
{
	public Collection<? extends Column> getColumns();
	public Column getColumn(String name);
	public T newDataObject(Map<String, Object> valueMap);
}
