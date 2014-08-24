package com.gamalocus.sgs.profile.viewer.task_panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.gamalocus.sgs.profile.listener.report.RawLogRecord;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.viewer.ProfileViewer;
import com.gamalocus.sgs.profile.viewer.util.DateUtil;
import com.gamalocus.sgs.profile.viewer.util.TypedTableModel;

public class TaskPanel extends JPanel implements ListSelectionListener
{
	private static final long serialVersionUID = -2129977721230364170L;
	private LogRecordTable taskTable;
	private TaskDetailPanel taskDetails;
	private TaskLogRecordView taskLogRecords;

	public TaskPanel()
	{
		setLayout(new GridLayout(1, 1));
		
		JSplitPane split_pane;
		split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane leftPane;
		split_pane.setTopComponent(leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
		leftPane.setTopComponent(new JScrollPane(taskTable = new LogRecordTable()));
		leftPane.setBottomComponent(taskLogRecords = new TaskLogRecordView());
		split_pane.setBottomComponent(taskDetails = new TaskDetailPanel());
		split_pane.setResizeWeight(0.5);
		add(split_pane);
		
		// Listen to selection changes
		taskTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskTable.getSelectionModel().addListSelectionListener(this);
		
		// Ensure the list will be quite big.
		split_pane.setResizeWeight(1.0);
		leftPane.setResizeWeight(0.9);
	}
	
	class LogRecordTable extends JTable
	{
		private DefaultTableCellRenderer right_aligned;

		LogRecordTable()
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
			TableCellRenderer retVal = (column == 1) ? super.getCellRenderer(row, column) : right_aligned;
			RawProfileReport r = (RawProfileReport) getValueAt(row, 1);
			//System.out.println("r:"+r.wasSuccessful);
			if(retVal instanceof JLabel)
			{
				((JLabel)retVal).setForeground(r.wasSuccessful ? Color.black : Color.red);
			}
			else
			{
				//System.out.println(retVal.getClass());
			}
			return retVal;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	public void onNewDataLoaded()
	{
		Collection<RawProfileReport> reports = new ArrayList<RawProfileReport>();
		
		for (RawProfileReport report : ProfileViewer.getInstance().getReports(true))
		{
			//if(report.logRecords != null)
			{
				reports.add(report);
			}
		}

		// Sort and display
		RawProfileReport[] reportsArr = reports.toArray(new RawProfileReport[0]);
		Object[][] table_data = new Object[reports.size()][4];
		for(int i = 0; i < reports.size(); i++)
		{
			RawProfileReport p = reportsArr[i];
			table_data[i][0] = DateUtil.getTime(p.getStartTime());
			table_data[i][1] = p;
			table_data[i][2] = p.logRecords != null ? p.logRecords.size() : 0;
			table_data[i][3] = p.wasSuccessful ? "Y" : "N";
		}
		
		taskTable.setModel(new TypedTableModel(table_data, new Object[] { "Time", "Task", "Log Records", "Success" }, new Class<?>[] { String.class, RawProfileReport.class, Integer.class, String.class}));
		
		// Set widths
		taskTable.getColumnModel().getColumn(0).setMaxWidth(120);
		taskTable.getColumnModel().getColumn(2).setMaxWidth(90);
		taskTable.getColumnModel().getColumn(3).setMaxWidth(90);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		Object obj = e.getSource();
		if (obj instanceof ListSelectionModel)
		{
			ListSelectionModel list = (ListSelectionModel) obj;
			if (list == taskTable.getSelectionModel())
			{
				int i = taskTable.getSelectedRow();
				if(i != -1)
				{
					//failed_task_filters.add(((FailedTask) failed_task_list.getModel().getValueAt(i, 0)).name);
					RawProfileReport report = (RawProfileReport) taskTable.getValueAt(i, 1);
					if(report != null)
					{
						taskDetails.showProfileReport(report);
						taskLogRecords.showProfileReport(report);
					}
				}
			}
		}
	}

}
