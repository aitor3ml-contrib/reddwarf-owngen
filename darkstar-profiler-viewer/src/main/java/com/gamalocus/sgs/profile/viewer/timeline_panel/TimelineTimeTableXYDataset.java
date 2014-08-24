/**
 * 
 */
package com.gamalocus.sgs.profile.viewer.timeline_panel;

import java.awt.Paint;
import java.util.ArrayList;

import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.TimeTableXYDataset;

import com.gamalocus.sgs.profile.listener.report.RawProfileReport;

class TimelineTimeTableXYDataset extends TimeTableXYDataset
{
	private static final long serialVersionUID = 1249158950875606548L;
	ArrayList<RunningTimeSeries> series = new ArrayList<RunningTimeSeries>();
	
	public void addRunningSeries(RunningTimeSeries runningTimeSeries)
	{
		series.add(runningTimeSeries);
	}
	
	public XYToolTipGenerator getTooltipGenerator(int i)
	{
		String seriesName = (String) getSeriesKey(i);
		final RunningTimeSeries s = getSeries(seriesName);
		return new CustomXYToolTipGenerator()
		{
			@Override
			public String getToolTipText(int series, int item)
			{
				return s.tooltips.get(item);
			}
		};
	}

	private RunningTimeSeries getSeries(String seriesName)
	{
		for(RunningTimeSeries s : series)
		{
			if(s.title.equals(seriesName))
				return s;
		}
		throw new RuntimeException("Unknown series");
	}

	public Paint getColor(int i)
	{
		return getSeries((String) getSeriesKey(i)).color;
	}

	void reportBegin(RawProfileReport report)
	{
		for(RunningTimeSeries s : series)
		{
			s.reportBegin(report);
		}
	}
	
	void reportEnd(RawProfileReport report)
	{
		for(RunningTimeSeries s : series)
		{
			s.reportEnd(report);
		}
	}
}