package com.gamalocus.sgs.profile.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.sun.sgs.impl.profile.HackProfileCollectorImpl;
import com.sun.sgs.impl.profile.ProfileCollectorImpl;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.profile.ProfileCollector;
import com.sun.sgs.profile.ProfileReport;

public class ProfileLogHandler extends Handler 
{
	private static ProfileCollector profileCollector;
	private static Map<ProfileReport, Collection<LogRecord>> logRecords = new IdentityHashMap<ProfileReport, Collection<LogRecord>>();

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord record) {
		if(profileCollector != null)
		{
			if(profileCollector instanceof ProfileCollectorImpl)
			{
				for(ProfileReport report : HackProfileCollectorImpl.getCurrentReportStack((ProfileCollectorImpl)profileCollector))
				{
					Collection<LogRecord> records = getLogRecords(report, false);
					records.add(record);
				}
			}
			else
			{
				// ARGH!!
			}
		}
	}

	public static Collection<LogRecord> getLogRecords(ProfileReport report, boolean remove) 
	{
		synchronized (logRecords)
		{
			if(remove)
			{
				return logRecords.remove(report);
			}
			else
			{
				Collection<LogRecord> retVal = logRecords.get(report);
				if(retVal == null)
				{
					retVal = new ArrayList<LogRecord>();
					logRecords.put(report, retVal);
				}
				return retVal;
			}
		}
	}

	public static void setProfileCollector(ProfileCollector collector)
	{
		profileCollector = collector;
	}
}
