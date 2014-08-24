package com.gamalocus.sgs.profile.viewer.exception_panel;

import java.awt.Component;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.viewer.ProfileViewer;
import com.gamalocus.sgs.profile.viewer.util.TypedTableModel;

public class ExceptionPanel extends JScrollPane
{
	private static final long serialVersionUID = -2129977721230364170L;
	private ExceptionTable table;

	public ExceptionPanel()
	{
		super();
		setViewportView(table = new ExceptionTable());
		table.setAutoCreateRowSorter(true);
	}
	
	class ExceptionTable extends JTable
	{
		private DefaultTableCellRenderer right_aligned;

		ExceptionTable()
		{
			right_aligned = new DefaultTableCellRenderer()
			{
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					setHorizontalAlignment(RIGHT);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			};
			//getColumnModel().getColumn(1).setMaxWidth(70);
			//getColumnModel().getColumn(2).setMaxWidth(70);
		}
	
		@Override
		public TableCellRenderer getCellRenderer(int row, int column)
		{
			if (column == 0)
				return super.getCellRenderer(row, column);
			return right_aligned;
		}		
	}

	public void onNewDataLoaded()
	{
		Hashtable<String, Summary> summaries = new Hashtable<String, Summary>();
		
		for (RawProfileReport report : ProfileViewer.getInstance().getReports(true))
		{
			if(!report.wasSuccessful)
			{
				Summary summary = getSummary(summaries, report.getFailureCauseStack());
				summary.count++;
			}
		}
		
		// Sort and display
		Object[] sorted = summaries.values().toArray();
		Object[][] table_data = new Object[sorted.length][2];
		Arrays.sort(sorted);
		for(int i = 0; i < sorted.length; i++)
		{
			Summary s = (Summary) sorted[i];
			table_data[i][0] = s.exceptionClassName;
			table_data[i][1] = s.count;
		}
		
		table.setModel(new TypedTableModel(table_data, new Object[] { "Exception Class", "Count" }, new Class<?>[]{ String.class, Integer.class }));
		
		//((JLabel)table.getColumnModel().getColumn(1).getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
	}
	
	private Summary getSummary(Hashtable<String, Summary> summaries, String taskClassName)
	{
		Summary s = summaries.get(taskClassName);
		if(s == null)
		{
			s = new Summary(taskClassName);
			summaries.put(taskClassName, s);
		}
		return s;
	}

	class Summary implements Comparable<Summary>
	{
		int count;
		String exceptionClassName;
		public Summary(String taskClassName)
		{
			this.exceptionClassName = taskClassName;
		}
		
		public int compareTo(Summary o)
		{
			int res = o.count-count;
			return res != 0 ? res : o.exceptionClassName.compareTo(exceptionClassName);
		}
	}
}
