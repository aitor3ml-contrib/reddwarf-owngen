package com.gamalocus.sgs.profile.viewer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.listener.report.RawPropertyChangeEvent;
import com.gamalocus.sgs.profile.listener.report.RawTransactionId;
import com.gamalocus.sgs.profile.viewer.contention_panel.ContentionAggregationPanel;
import com.gamalocus.sgs.profile.viewer.data_loader.DataLoader;
import com.gamalocus.sgs.profile.viewer.exception_panel.ExceptionPanel;
import com.gamalocus.sgs.profile.viewer.sgs_properties_panel.SGSPropertiesPanel;
import com.gamalocus.sgs.profile.viewer.task_panel.TaskPanel;
import com.gamalocus.sgs.profile.viewer.task_summary_panel.TaskSummaryPanel;
import com.gamalocus.sgs.profile.viewer.timeline_panel.TimelinePanel;

public class ProfileViewer extends JFrame
{
	private static final long serialVersionUID = 1L;
	static Properties defaultProperties = new Properties();
	static
	{
		// defaultProperties.put("output_directory", null);
	}
	private Properties appProperties = new Properties(defaultProperties);
	private File appPropertiesFile = new File("ProfileViewer.properties");
	private static ProfileViewer instance;
	private ConcurrentSkipListSet<ProfileReportKey> reports = new ConcurrentSkipListSet<ProfileReportKey>();
	private Hashtable<RawTransactionId, RawProfileReport> backlog = new Hashtable<RawTransactionId, RawProfileReport>();
	private TopMenuBar menubar;
	private JTabbedPane tabs;
	private ContentionAggregationPanel contention_panel;
	private TaskSummaryPanel task_panel;
	private ExceptionPanel exception_panel;
	private File outputDirectory;
	private Properties sgsProperties;
	private SGSPropertiesPanel sgs_properties_panel;
	private long beginning_of_time;
	private long end_of_time;
	private TimelinePanel timeline_panel;
	private TaskPanel log_panel;
	/**
	 * This represents a start or end time in the reports.
	 * 
	 * @author emanuel
	 */
	public static class ProfileReportKey implements Comparable<ProfileReportKey>
	{
		final long time;
		final boolean start;
		final int profileHash;
		final RawProfileReport report;

		public ProfileReportKey(long time, boolean start, RawProfileReport report)
		{
			this.time = time;
			this.start = start;
			this.report = report;
			this.profileHash = System.identityHashCode(report);
		}

		@Override
		public int compareTo(ProfileReportKey o)
		{
			if (o.time != time)
			{
				return time < o.time ? -1 : 1;
			}
			if (o.start != start)
			{
				return start ? -1 : 1; // Start is before end
			}
			// Fall back on object identity.
			return profileHash - o.profileHash;
		}

		@Override
		public String toString()
		{
			return "[" + (start ? "start" : "ended") + ":" + time + ", " + (report != null ? report.baseTaskType : "dummy") + "]";
		}

		public boolean isStart()
		{
			return start;
		}

		public RawProfileReport getReport()
		{
			return report;
		}
	}

	public ProfileViewer()
	{
		super("Darkstar Profile Viewer");
		instance = this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		// Create the top-menu
		setJMenuBar(menubar = new TopMenuBar());
		// Create a tabbed pane
		add(tabs = new JTabbedPane());
		tabs.addTab("Timeline", timeline_panel = new TimelinePanel());
		tabs.addTab("Contention", contention_panel = new ContentionAggregationPanel());
		tabs.addTab("Task Summary", task_panel = new TaskSummaryPanel());
		tabs.addTab("Exceptions", exception_panel = new ExceptionPanel());
		tabs.addTab("SGS Properties", sgs_properties_panel = new SGSPropertiesPanel());
		tabs.addTab("All Tasks", log_panel = new TaskPanel());
		pack();
		setVisible(true);
		// Do we have sane properties?
		loadAppProperties();
	}

	private void chooseOutputDirectory() throws FileNotFoundException, IOException
	{
		String outDirProp = appProperties.getProperty("output_directory");
		File outDir = outDirProp != null ? new File(outDirProp) : null;
		do
		{
			JFileChooser fc = new JFileChooser(outDir);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(false);
			int result = fc.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				outDir = fc.getSelectedFile();
			}
			else
			{
				//JOptionPane.showMessageDialog(this, "You have to select the output folder");
				return; // cancelled
			}
		}
		while (!trySetNewOutputDirectory(outDir, false));
		// Save for later
		appProperties.put("output_directory", outDir.getAbsolutePath());
		appProperties.store(new FileOutputStream(appPropertiesFile, false), "Saved " + new Date());
	}

	private boolean trySetNewOutputDirectory(File outDir, boolean overwrite)
	{
		if(overwrite)
		{
			this.outputDirectory = null;
		}
		if(outDir == null)
		{
			JOptionPane.showMessageDialog(this, "The output directory given was null");
			return false;
		}
		if (!outDir.isDirectory())
		{
			JOptionPane.showMessageDialog(this, "The directory " + outDir + " does not exist.");
			return false;
		}
		if(!outDir.isDirectory())
		{
			JOptionPane.showMessageDialog(this, "The directory " + outDir + " is not a directory.");
			return false;
		}
		if(!new File(outDir, "sgs.properties").isFile())
		{
			JOptionPane.showMessageDialog(this, "The directory " + outDir + " does not contain an sgs.properties file.");
			return false;
		}
		if (!new File(outDir, "sgs.properties").isFile())
		{
			JOptionPane.showMessageDialog(this, "The directory " + outDir + " must contain a file called\nsgs.properties generated by the RawProfileListener.");
			return false;
		}
		this.outputDirectory = outDir;
		return true;
	}

	private void loadAppProperties()
	{
		if (appPropertiesFile.isFile())
		{
			try
			{
				appProperties.load(new FileInputStream(appPropertiesFile));
			}
			catch (FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(this, e.getMessage(), "An error occured", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(this, e.getMessage(), "An error occured", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		ProfileViewer app = new ProfileViewer();
		
		if(args.length == 1)
		{
			if(app.trySetNewOutputDirectory(new File(args[0]), true))
			{
				app.loadProfileData(false);
			}
		}
	}

	public static ProfileViewer getInstance()
	{
		return instance;
	}

	public void loadProfileData(boolean force_dialog)
	{
		if(outputDirectory == null || force_dialog)
		{
			try
			{
				chooseOutputDirectory();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Error:" + e.getLocalizedMessage(), e.getMessage() + "\n\nLook in the output for details", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
		if(outputDirectory == null)
			return;

		DataLoader loader = new DataLoader(this, outputDirectory);
		loader.setVisible(true);
		loader.dispose();
	}
	
	public void setNewData(ConcurrentSkipListSet<ProfileReportKey> reports, 
			Hashtable<RawTransactionId, RawProfileReport> backlog, 
			long beginning_of_time, 
			long end_of_time, 
			Properties sgsProperties, 
			int loadedFiles)
	{
		// Copy into the viewer.
		this.reports = reports;
		this.backlog = backlog;
		this.beginning_of_time = beginning_of_time;
		this.end_of_time = end_of_time;
		this.sgsProperties = sgsProperties;

		// New title
		setTitle("Darkstar Profile Viewer: Loaded " + (reports.size() / 2) + " reports from " + loadedFiles + " file"+(loadedFiles != 1 ? "s" : ""));

		// Update all panels
		timeline_panel.onNewDataLoaded();
		contention_panel.onNewDataLoaded();
		task_panel.onNewDataLoaded();
		exception_panel.onNewDataLoaded();
		sgs_properties_panel.onNewDataLoaded();
		log_panel.onNewDataLoaded();
	}

	/**
	 * Get the reports sorted by start or end time.
	 * 
	 * @return
	 */
	public Iterable<RawProfileReport> getReports(final boolean orderByStart)
	{
		final Iterator<ProfileReportKey> it = reports.iterator();
		return new Iterable<RawProfileReport>()
		{
			@Override
			public Iterator<RawProfileReport> iterator()
			{
				return new Iterator<RawProfileReport>()
				{
					ProfileReportKey next;

					@Override
					public boolean hasNext()
					{
						// Find the next "start"
						while (it.hasNext())
						{
							next = it.next();
							if (next.start == orderByStart)
								return true;
						}
						next = null;
						return false;
					}

					@Override
					public RawProfileReport next()
					{
						return (next != null ? next.report : null);
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException("This iterator does not support removal");
					}
				};
			}
		};
	}

	/**
	 * Get the reports that overlap the given interval, in no particular order.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Collection<RawProfileReport> getReportsThatOverlap(long startTime, long endTime)
	{
		Set<RawProfileReport> result = new HashSet<RawProfileReport>();
		/*
		 * int open = 0; int max = 0; for(ProfileKey k : reports) { open +=
		 * (k.start ? 1 : -1); if(open > max) { System.out.println("new
		 * max:"+max); max = open; } //System.out.println(open+":k:"+k); }
		 * if(true) throw new RuntimeException("sdjf");
		 */
		ProfileReportKey from = new ProfileReportKey(startTime, true, null);
		ProfileReportKey to = new ProfileReportKey(endTime, false, null);
		NavigableSet<ProfileReportKey> intersection = reports.subSet(from, true, to, true);
		// System.out.println("from: "+startTime+" - "+endTime+"
		// ("+(endTime-startTime)+"ms), times:"+intersection.size());
		// Iterate the map and add to result set
		for (ProfileReportKey rep : intersection)
		{
			result.add(rep.report);
		}
		return result;
	}

	public Properties getSGSProperties()
	{
		return sgsProperties;
	}

	public RawProfileReport getReport(RawTransactionId conflictingId)
	{
		return backlog.get(conflictingId);
	}

	public long getBeginningOfTime()
	{
		return beginning_of_time;
	}
	
	public long getEndOfTime()
	{
		return end_of_time;
	}

	public ConcurrentSkipListSet<ProfileReportKey> getReportsRaw()
	{
		return reports;
	}

	public File getOutputDirectory()
	{
		return outputDirectory;
	}
}
