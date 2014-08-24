package com.gamalocus.sgs.profile.viewer;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class TopMenuBar extends JMenuBar
{
	private JMenu files;
	private JMenuItem reloadAction;

	TopMenuBar()
	{
		add(files = new JMenu("Files"));
		files.add(reloadAction = new JMenuItem(new AbstractAction("Reload")
		{
			public void actionPerformed(ActionEvent e)
			{
				ProfileViewer.getInstance().loadProfileData(false);
			}
		}));
		files.add(new JMenuItem(new AbstractAction("Load...")
		{
			public void actionPerformed(ActionEvent e)
			{
				ProfileViewer.getInstance().loadProfileData(true);
			}
		}));
		
		files.addMenuListener(new MenuListener()
		{

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {
				reloadAction.setEnabled(ProfileViewer.getInstance().getOutputDirectory() != null);
			}
			
		});
	}
}
