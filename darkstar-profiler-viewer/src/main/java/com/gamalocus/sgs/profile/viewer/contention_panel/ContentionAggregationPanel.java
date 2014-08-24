package com.gamalocus.sgs.profile.viewer.contention_panel;

import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.gamalocus.sgs.profile.listener.report.RawAccessType;
import com.gamalocus.sgs.profile.listener.report.RawAccessedObject;
import com.gamalocus.sgs.profile.listener.report.RawConflictType;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.viewer.ProfileViewer;
import com.gamalocus.sgs.profile.viewer.contention_panel.ContentionInfo.ContendedClass;
import com.gamalocus.sgs.profile.viewer.contention_panel.ContentionInfo.FailedTask;

public class ContentionAggregationPanel extends JPanel implements ListSelectionListener
{
	private JSplitPane outer_split_pane;
	private JTable class_list;
	private JTable failed_task_list;
	private JTable conflicting_task_list;
	private Set<String> class_filters = new HashSet<String>();
	private Set<String> failed_task_filters = new HashSet<String>();
	private final ContentionInfo info = new ContentionInfo();
	private boolean updating_lists;
	private JSplitPane inner_split_pane;
	private FailedTaskTableModel failed_task_model;
	private ContendedClassTableModel contended_class_model;
	private ConflictingTaskTableModel conflicting_task_model;
	static String test_data[][] = { { "com.gamalocus.task1", "com.gamalocus.data.class0", "com.gamalocus.data.class1" }, { "com.gamalocus.task1", "com.gamalocus.data.class0", "com.gamalocus.data.class1" }, { "com.gamalocus.task1", "com.gamalocus.data.class0" }, { "com.gamalocus.task2", "com.gamalocus.data.class1", "com.gamalocus.data.class4" }, { "com.gamalocus.task2", "com.gamalocus.data.class1", "com.gamalocus.data.class4" }, { "com.gamalocus.task2", "com.gamalocus.data.class1" }, { "com.gamalocus.task2", "com.gamalocus.data.class1" }, { "com.gamalocus.task3", "com.gamalocus.data.classX" }, { "com.gamalocus.task5", "com.gamalocus.data.class1", "com.gamalocus.data.classA", "com.gamalocus.data.class2", "com.gamalocus.data.class3" }, { "com.gamalocus.task5", "com.gamalocus.data.classA" }, { "com.gamalocus.task5", "com.gamalocus.data.classA" }, { "com.gamalocus.task5", "com.gamalocus.data.classA" }, { "com.gamalocus.task5", "com.gamalocus.data.classA" }, { "com.gamalocus.task5", "com.gamalocus.data.classA" }, };

	public ContentionAggregationPanel()
	{
		setLayout(new GridLayout(1, 1));
		
		// Create the splitpane with the two scrollable areas
		add(outer_split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
		outer_split_pane.setTopComponent(new JScrollPane(failed_task_list = new ContentionJTable(failed_task_model = new FailedTaskTableModel(info)), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		outer_split_pane.setBottomComponent(inner_split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT));
		inner_split_pane.setTopComponent(new JScrollPane(class_list = new ContentionJTable(contended_class_model = new ContendedClassTableModel(info)), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		inner_split_pane.setBottomComponent(new JScrollPane(conflicting_task_list = new ContentionJTable(conflicting_task_model = new ConflictingTaskTableModel(info)), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		// Listen to the two interesting tables.
		class_list.getSelectionModel().addListSelectionListener(this);
		failed_task_list.getSelectionModel().addListSelectionListener(this);
		
		// And set split-panel
		//outer_split_pane.setDividerLocation(0.5);
		//outer_split_pane.setDividerLocation(280);
		//outer_split_pane.setDividerSize(5);
		outer_split_pane.setResizeWeight(0.5);
		inner_split_pane.setResizeWeight(0.5);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (updating_lists)
			return;
		Object obj = e.getSource();
		if (obj instanceof ListSelectionModel)
		{
			ListSelectionModel list = (ListSelectionModel) obj;
			if (list == failed_task_list.getSelectionModel())
			{
				failed_task_filters.clear();
				for (int i : failed_task_list.getSelectedRows())
				{
					failed_task_filters.add(((FailedTask) failed_task_list.getValueAt(i, 0)).name);
				}
				updateInfo();
				contended_class_model.fireTableDataChanged();
				conflicting_task_model.fireTableDataChanged();
			}
			else if (list == class_list.getSelectionModel())
			{
				class_filters.clear();
				for (int i : class_list.getSelectedRows())
				{
					class_filters.add(((ContendedClass) class_list.getValueAt(i, 0)).name);
				}
				updateInfo();
				conflicting_task_model.fireTableDataChanged();
			}
		}
	}

	public void onNewDataLoaded()
	{
		class_filters.clear();
		failed_task_filters.clear();
		updateLists();
	}

	public void updateLists()
	{
		// Update the info
		updateInfo();
		
		// update the tables.
		updating_lists = true;
		failed_task_model.fireTableDataChanged();
		contended_class_model.fireTableDataChanged();
		conflicting_task_model.fireTableDataChanged();
		updating_lists = false;
	}

	private void updateInfo()
	{
		info.clear();
		// Fetch info the reports
		ProfileViewer profileViewer = ProfileViewer.getInstance();
		for (RawProfileReport report : profileViewer.getReports(true))
		{
			if (report.wasTransactional)
			{
				// Look at the situation
				if (!report.wasSuccessful && report.accessedObjectDetail != null && report.accessedObjectDetail.conflictType != RawConflictType.NONE)
				{
					if (failed_task_filters.size() == 0 || failed_task_filters.contains(report.baseTaskType))
					{
						if (report.accessedObjectDetail.conflictingId != null)
						{
							RawProfileReport conflictingReport = profileViewer.getReport(report.accessedObjectDetail.conflictingId);
							if(findConflictingObjects(report, conflictingReport))
							{
								//System.out.println("Found easy conflicts! ("+report.baseTaskType+" vs. "+conflictingReport.baseTaskType+")");
							}
						}
						else
						{
							// ok, then we have to loop the backlog
							Collection<RawProfileReport> headMap = profileViewer.getReportsThatOverlap(report.getStartTime(), report.getEndTime());
							for (RawProfileReport conflictingReport : headMap)
							{
								if (conflictingReport != report)
								{
									if(findConflictingObjects(report, conflictingReport))
									{
										//System.out.println("Found a conflict, lets stop here  ("+report.baseTaskType+" vs. "+conflictingReport.baseTaskType+")");
										//break;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean findConflictingObjects(RawProfileReport report, RawProfileReport conflictingReport)
	{
		Map<Long, RawAccessedObject> conflicts = new Hashtable<Long, RawAccessedObject>();
		for (RawAccessedObject obj : report.accessedObjectDetail.accessedObjects)
		{
			if(conflictingReport.accessedObjectDetail != null)
			{
				for (RawAccessedObject conflictobj : conflictingReport.accessedObjectDetail.accessedObjects)
				{
					// System.out.println(conflictobj.class_name+".equals("+obj.class_name+")");
					//if (conflictobj.oid == obj.oid)
					if(conflicts(obj, conflictobj))
					{
						if(!conflicts.containsKey(obj.oid))
						{
							//System.out.println("Conflict:"+obj.oid+" ("+obj.getAccessType()+"/"+conflictobj.getAccessType()+"):"+obj.class_name);
							if(class_filters.size() == 0 || class_filters.contains(obj.class_name))
							{
								info.addContentionPair(report.baseTaskType, obj.class_name, conflictingReport.baseTaskType);
								conflicts.put(obj.oid, obj);
							}
						}
					}
				}
			}
		}
		
		return conflicts.size() > 0;
	}

	private static boolean conflicts(RawAccessedObject o1, RawAccessedObject o2)
	{
		if (o1.oid != -1 && o1.oid == o2.oid)
		{
			RawAccessType a1 = o1.getAccessType();
			RawAccessType a2 = o2.getAccessType();
			return (a1 != RawAccessType.READ) || (a2 != RawAccessType.READ);
		}
		return false;
	}
}

