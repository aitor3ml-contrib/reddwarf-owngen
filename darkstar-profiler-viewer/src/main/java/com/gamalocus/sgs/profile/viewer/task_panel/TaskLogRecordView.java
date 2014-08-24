package com.gamalocus.sgs.profile.viewer.task_panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.gamalocus.sgs.profile.listener.report.RawLogRecord;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;

public class TaskLogRecordView extends JPanel {

	private JTextArea logOutput;

	TaskLogRecordView()
	{
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(-1, 100));
		setMinimumSize(new Dimension(20, 20));
		
		add(new JScrollPane(logOutput = new JTextArea()), BorderLayout.CENTER);
	}
	
	void showProfileReport(RawProfileReport report)
	{
		// Fill in the logRecords
		StringBuffer details = new StringBuffer();
		if(report.logRecords != null)
		{
			for(RawLogRecord lr : report.logRecords)
			{
				details.append(format(lr)+"\n");
			}
		}
		//taskDetails.setText("");
		logOutput.setText(details.toString());
		
		logOutput.setCaretPosition(0);
	}

	
	private String format(RawLogRecord record) {
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		Date dat = new Date(record.millis);
		sb.append(dat);
		sb.append(" ");
		if (record.sourceClassName != null) {	
		    sb.append(record.sourceClassName);
		} else {
		    sb.append(record.loggerName);
		}
		if (record.sourceMethodName != null) {	
		    sb.append(" ");
		    sb.append(record.sourceMethodName);
		}
		sb.append("\n");
		String message = record.message;
		sb.append(record.level.getLocalizedName());
		sb.append(": ");
		sb.append(message);
		sb.append("\n");
		if (record.thrown != null) {
		    try {
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        record.thrown.printStackTrace(pw);
		        pw.close();
			sb.append(sw.toString());
		    } catch (Exception ex) {
		    }
		}
		return sb.toString();
	}
	
	
}
