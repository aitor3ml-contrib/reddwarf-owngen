package com.gamalocus.sgs.profile.viewer.task_panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import com.gamalocus.sgs.profile.listener.report.RawAccessedObject;
import com.gamalocus.sgs.profile.listener.report.RawLogRecord;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.viewer.util.TypedTableModel;

class TaskDetailPanel extends JPanel {
	
	private JTable objectAccessTable;
	private JTextArea exceptionStackTrace;
	private JTable taskDetailTable;

	TaskDetailPanel()
	{
		setLayout(new GridLayout(3, 1));
		setPreferredSize(new Dimension(300, -1));
		setMinimumSize(new Dimension(100, 100));
		
		// The details
		{
			add(new JScrollPane(taskDetailTable = new JTable(10, 2)));
		}
		
		// The object access
		{
			add(new JScrollPane(objectAccessTable = new JTable()));
		}

		// Exception
		{
			add(new JScrollPane(exceptionStackTrace = new JTextArea())); 
		}
	}
	
	void showProfileReport(RawProfileReport report)
	{
		// Fill in details
		{
			// Color?
			taskDetailTable.setForeground(report.wasSuccessful ? Color.black : Color.red);
			
			// Values
			taskDetailTable.setValueAt("Task Type:", 0, 0);
			taskDetailTable.setValueAt(report.baseTaskType, 0, 1);
			
			taskDetailTable.setValueAt("ClassName:", 1, 0);
			taskDetailTable.setValueAt(report.taskClassName, 1, 1);

			taskDetailTable.setValueAt("Trans:", 2, 0);
			taskDetailTable.setValueAt(report.wasTransactional ? "Yes" : "No", 2, 1);
		}
		
		// Fill in object access
		{
			String[] header = new String[] { "Managed Object", "Class", "oid", "Type" };
			if(report.accessedObjectDetail != null && report.accessedObjectDetail.accessedObjects != null)
			{
				Object data[][] = new Object[report.accessedObjectDetail.accessedObjects.length][4];
				int i = 0;
				for(RawAccessedObject o : report.accessedObjectDetail.accessedObjects)
				{
					data[i][0] = o.to_string;
					data[i][1] = o.class_name;
					data[i][2] = o.oid;
					data[i][3] = o.access_type;
					
					i++;
				}
				
				objectAccessTable.setModel(new DefaultTableModel(data, header));
			}
			else
			{
				objectAccessTable.setModel(new DefaultTableModel(new Object[0][0], header));
			}
			objectAccessTable.getColumnModel().getColumn(2).setMaxWidth(30);
			objectAccessTable.getColumnModel().getColumn(3).setMaxWidth(40);
		}
		
		// Fill in exception
		{
			if(report.failureCause != null)
			{
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        report.failureCause.printStackTrace(pw);
		        pw.close();
		        
				exceptionStackTrace.setText(sw.toString());
			}
			else
			{
				exceptionStackTrace.setText("no exception");
			}
			exceptionStackTrace.setCaretPosition(0);
		}
	}
}
