package com.gamalocus.sgs.profile.viewer.util;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public class TypedTableModel extends AbstractTableModel 
{

	private Class<?>[] columnClasses;
	private Object[][] data;
	private Object[] columnNames;

	public TypedTableModel(Object[][] data, Object[] columnNames, Class<?>[] columnClasses) {
		if(columnClasses.length != columnNames.length)
		{
			throw new IllegalArgumentException("The number of column names and classes must match");
		}
		if(data.length > 0)
		{
			if(data[0].length < columnClasses.length)
			{
				throw new IllegalArgumentException("There is not enough data compaired to the number of columns");
			}
		}
		
		// Accept the input
		this.data = data;
		this.columnNames = columnNames;
		this.columnClasses = columnClasses;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column].toString();
	}

	@Override
	public int getColumnCount() {
		return columnClasses.length;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}
	
}
