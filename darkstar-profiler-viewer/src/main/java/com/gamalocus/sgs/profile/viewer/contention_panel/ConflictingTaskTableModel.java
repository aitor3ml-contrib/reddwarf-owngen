package com.gamalocus.sgs.profile.viewer.contention_panel;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import com.gamalocus.sgs.profile.viewer.contention_panel.ContentionInfo.ConflictingTask;

class ConflictingTaskTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	private ContentionInfo info;
	ConflictingTask array[] = new ConflictingTask[0]; 

	ConflictingTaskTableModel(ContentionInfo info)
	{
		this.info = info;
	}
	
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public int getRowCount()
	{
		return info.conflicting_tasks.size();
	}
	
	@Override
	public void fireTableDataChanged()
	{
		array = info.conflicting_tasks.values().toArray(new ConflictingTask[0]);
		Arrays.sort(array);
		super.fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int column)
	{
		switch(column)
		{
		case 0:
			return "Conflicting Task";
		case 1:
			return "Count";
		}
		throw new RuntimeException("Invalid column:"+column);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ConflictingTask task = array[rowIndex];
		switch(columnIndex)
		{
		case 0:
			return task;
		case 1:
			return task.count;
		}
		throw new ArrayIndexOutOfBoundsException("No such element ("+rowIndex+","+columnIndex+")");
	}
}
