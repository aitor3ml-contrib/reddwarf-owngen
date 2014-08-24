package com.gamalocus.sgs.profile.viewer.contention_panel;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import com.gamalocus.sgs.profile.viewer.contention_panel.ContentionInfo.FailedTask;

class FailedTaskTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	private ContentionInfo info;
	private FailedTask array[] = new FailedTask[0];

	FailedTaskTableModel(ContentionInfo info)
	{
		this.info = info;
	}
	
	@Override
	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public int getRowCount()
	{
		return array.length;
	}
	
	@Override
	public void fireTableDataChanged()
	{
		array = info.failed_tasks.values().toArray(new FailedTask[0]);
		Arrays.sort(array);
		super.fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int column)
	{
		switch(column)
		{
		case 0:
			return "Failed Tasks";
		case 1:
			return "Objects";
		case 2:
			return "Count";
		}
		throw new RuntimeException("Invalid column:"+column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		FailedTask task = array[rowIndex];
		switch(columnIndex)
		{
		case 0:
			return task;
		case 1:
			return task.classes.size();
		case 2:
			return task.count;
		}
		throw new ArrayIndexOutOfBoundsException("No such element ("+rowIndex+","+columnIndex+")");
	}
}
