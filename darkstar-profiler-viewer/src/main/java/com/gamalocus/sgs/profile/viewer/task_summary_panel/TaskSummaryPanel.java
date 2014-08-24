package com.gamalocus.sgs.profile.viewer.task_summary_panel;

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

public class TaskSummaryPanel extends JScrollPane
{
	private static final long serialVersionUID = -2129977721230364170L;
	private TaskTable table;

	public TaskSummaryPanel()
	{
		super();
		setViewportView(table = new TaskTable());
	}
	
	class TaskTable extends JTable
	{
		private DefaultTableCellRenderer right_aligned;

		TaskTable()
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
			setAutoCreateRowSorter(true);
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
			Summary summary = getSummary(summaries, report.baseTaskType);
			Summary total = getSummary(summaries, "Total");
			if(report.wasSuccessful)
			{
				summary.succeeded++;
				total.succeeded++;
			}
			else
			{
				summary.failed++;
				total.failed++;
			}
			summary.accessed_min = Math.min(summary.accessed_min, report.getNumberOfAccessedObjects());
			summary.accessed_max = Math.max(summary.accessed_max, report.getNumberOfAccessedObjects());
			summary.accessed_total += report.getNumberOfAccessedObjects();
		}
		
		// Sort and display
		Object[] sorted = summaries.values().toArray();
		Object[][] table_data = new Object[sorted.length][4];
		Arrays.sort(sorted);
		for(int i = 0; i < sorted.length; i++)
		{
			Summary s = (Summary) sorted[i];
			table_data[i][0] = s.taskClassName;
			table_data[i][1] = s.succeeded;
			table_data[i][2] = s.failed;
			if(s.accessed_min != Integer.MAX_VALUE)
			{
				table_data[i][3] = "("+s.accessed_min+"/"+(s.accessed_total/s.getTotal())+"/"+s.accessed_max+")";
			}
			else
			{
				table_data[i][3] = "-";
			}	
		}
		
		table.setModel(new TypedTableModel(
				table_data, 
				new Object[] { "Task Class", "Succeeded", "Failed", "Accessed Object" },
				new Class<?>[] { String.class, Integer.class, Integer.class, String.class}));
		
		// Set widths
		table.getColumnModel().getColumn(1).setMaxWidth(90);
		table.getColumnModel().getColumn(2).setMaxWidth(90);
		table.getColumnModel().getColumn(3).setMaxWidth(120);
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
		int succeeded;
		int failed;
		int accessed_min = Integer.MAX_VALUE;
		int accessed_max = 0;
		int accessed_total = 0;
		
		String taskClassName;
		public Summary(String taskClassName)
		{
			this.taskClassName = taskClassName;
		}
		
		public int getTotal()
		{
			return succeeded+failed;
		}

		public int compareTo(Summary o)
		{
			int res = o.getTotal()-getTotal();
			return res != 0 ? res : o.taskClassName.compareTo(taskClassName);
		}
	}
}
