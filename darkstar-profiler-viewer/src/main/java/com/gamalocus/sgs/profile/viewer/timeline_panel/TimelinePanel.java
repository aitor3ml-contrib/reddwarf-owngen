package com.gamalocus.sgs.profile.viewer.timeline_panel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.util.EnumSet;

import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.ui.RectangleEdge;

import com.gamalocus.sgs.profile.listener.report.RawAccessType;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.viewer.ProfileViewer;
import com.gamalocus.sgs.profile.viewer.ProfileViewer.ProfileReportKey;

public class TimelinePanel extends JScrollPane
{
	private static final long serialVersionUID = -2129977721230364170L;
	private JFreeChart chart;
	private ChartPanel chart_panel;
	
	private static final Color DARK_GREEN = new Color(0.0f, 0.6f, 0.0f);
	private static final Color DARK_RED = new Color(0.8f, 0.0f, 0.0f);
	private static final Color DARK_BLUE = new Color(0.0f, 0.0f, 0.8f);
	

	public TimelinePanel()
	{
		super();
		setViewportView(chart_panel = new ChartPanel(null));
	}

	public void onNewDataLoaded()
	{
		
		TimelineTimeTableXYDataset taskDataset = new TimelineTimeTableXYDataset();
		TimelineTimeTableXYDataset objectDataset = new TimelineTimeTableXYDataset();
		
		taskDataset.addRunningSeries(new RunningTimeSeries(taskDataset, "Successfull tasks", DARK_GREEN)
		{
			@Override
			protected int getCount(RawProfileReport report)
			{
				return report.wasSuccessful ? 1 : 0;
			}
		});
		taskDataset.addRunningSeries(new RunningTimeSeries (taskDataset, "Failed Tasks", DARK_RED)
		{
			@Override
			protected int getCount(RawProfileReport report)
			{
				return !report.wasSuccessful ? 1 : 0;
			}
		});
		objectDataset.addRunningSeries(new RunningTimeSeries (objectDataset, "READ Access", DARK_BLUE)
		{
			@Override
			protected int getCount(RawProfileReport report)
			{
				return report.getNumberOfAccessedObjects() - report.getNumberOfAccessedObjects(EnumSet.of(RawAccessType.WRITE));
			}
		});
		objectDataset.addRunningSeries(new RunningTimeSeries (objectDataset, "WRITE Access", DARK_RED)
		{
			@Override
			protected int getCount(RawProfileReport report)
			{
				return report.getNumberOfAccessedObjects(EnumSet.of(RawAccessType.WRITE));
			}
		});
		
		// Iterate all start/end of reports.
		for(ProfileReportKey event : ProfileViewer.getInstance().getReportsRaw())
		{
			if(event.isStart())
			{
				taskDataset.reportBegin(event.getReport());
				objectDataset.reportBegin(event.getReport());
			}
			else
			{
				taskDataset.reportEnd(event.getReport());
				objectDataset.reportEnd(event.getReport());
			}
		}
		
		// Create the taskPlot
		XYPlot taskPlot = createPlot(taskDataset, "Tasks", AxisLocation.TOP_OR_LEFT);
		
		// Create the objectAccessPlot
		XYPlot objectAccessPlot = createPlot(objectDataset, "Object Access", AxisLocation.TOP_OR_LEFT);
		
		// Create the combined plot
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
		plot.setGap(10.0);
		plot.add(taskPlot, 1);
		plot.add(objectAccessPlot, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);
		
		chart = new JFreeChart("Timeline", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart_panel.setChart(chart);

		repaint();
	}

	private XYPlot createPlot(
			TimelineTimeTableXYDataset dataset, 
			String title, 
			AxisLocation axisLocation)
	{
		StackedXYBarRenderer renderer = new StackedXYBarRenderer();
		StandardXYBarPainter barPainter = new StandardXYBarPainter()
		{
			@Override
			public void paintBarShadow(Graphics2D g2, XYBarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base, boolean pegShadow)
			{
				// NO SHADOWS
			}
		};
		renderer.setBarPainter(barPainter);
		
		// Colors
		for(int i = 0; i < dataset.getSeriesCount(); i++)
		{
			renderer.setSeriesPaint(i, dataset.getColor(i));
			renderer.setSeriesToolTipGenerator(i, dataset.getTooltipGenerator(i));
			//System.out.println("Tooltip:"+renderer.getSeriesToolTipGenerator(i));
		}
		/*
		renderer.setBaseToolTipGenerator(new XYToolTipGenerator()
		{

			@Override
			public String generateToolTip(XYDataset dataset, int series, int item)
			{
				TimelineTimeTableXYDataset timeDataset = ((TimelineTimeTableXYDataset)dataset);
				RunningTimeSeries s = timeDataset.series.get((String)timeDataset.getSeriesKey(series));
				return s.tooltips.get(item);
			}
		});
		*/
		
		XYPlot xyPlot = new XYPlot(dataset, null, new NumberAxis(title), renderer);
		xyPlot.setRangeAxisLocation(axisLocation);
		return xyPlot;
	}
}
