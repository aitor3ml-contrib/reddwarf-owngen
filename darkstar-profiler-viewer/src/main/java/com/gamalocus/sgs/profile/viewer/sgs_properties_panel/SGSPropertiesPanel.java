package com.gamalocus.sgs.profile.viewer.sgs_properties_panel;

import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.gamalocus.sgs.profile.viewer.ProfileViewer;
import com.gamalocus.sgs.profile.viewer.util.TypedTableModel;

public class SGSPropertiesPanel extends JScrollPane
{
	private static final long serialVersionUID = -2129977721230364170L;
	private JTable table;

	public SGSPropertiesPanel()
	{
		super();
		setViewportView(table = new JTable());
		table.setAutoCreateRowSorter(true);
	}

	public void onNewDataLoaded()
	{
		
		Set<Entry<Object, Object>> entrySet = ProfileViewer.getInstance().getSGSProperties().entrySet();
		Object[][] table_data = new Object[entrySet.size()][2];
		int i = 0;
		for (Entry<Object, Object> prop : entrySet)
		{
			table_data[i][0] = prop.getKey();
			table_data[i][1] = prop.getValue();
			i++;
		}
		
		table.setModel(new TypedTableModel(table_data, new Object[] { "Key", "Value" }, new Class<?>[]{ String.class, String.class }));
	}
}
