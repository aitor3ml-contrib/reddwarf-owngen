package com.gamalocus.sgs.profile.viewer.contention_panel;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class ContentionJTable extends JTable
{
	private DefaultTableCellRenderer right_aligned;

	public ContentionJTable(TableModel tableModel)
	{
		super(tableModel);
		right_aligned = new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				setHorizontalAlignment(RIGHT);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		if (column == 0)
			return super.getCellRenderer(row, column);
		return right_aligned;
	}
	
	@Override
	public void setModel(TableModel dataModel)
	{
		super.setModel(dataModel);
		if(getColumnModel().getColumnCount() == 3)
		{
			getColumnModel().getColumn(1).setMaxWidth(70);
			getColumnModel().getColumn(2).setMaxWidth(70);
		}
		else if(getColumnModel().getColumnCount() == 2)
		{
			getColumnModel().getColumn(1).setMaxWidth(70);
		}
	}
}
