package com.gamalocus.sgs.profile.listener.report;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.logging.LogRecord;

public class RawProfileReport implements Serializable
{
	private static final long serialVersionUID = -6615410688672504524L;
	public RawTransactionId txnId;
	public boolean wasSuccessful;
	public boolean wasTransactional;
	public long runningTime;
	public long startTime;
	public RawThrowable failureCause;
	public String baseTaskType;
	public RawAccessedObjectsDetail accessedObjectDetail;
	public String taskToString;
	public String taskClassName;
	public Collection<RawLogRecord> logRecords;
	
	public RawProfileReport()
	{
	}
	
	
	@Override
	public String toString()
	{
		return baseTaskType;
	}


	public long getEndTime()
	{
		return startTime+runningTime;
	}


	public long getStartTime()
	{
		return startTime;
	}


	public RawThrowable getDeepestFailureCause()
	{
		RawThrowable t = failureCause;
		while(t.getCause() != null)
		{
			t = t.getCause();
		}
		return t;
	}


	public String getFailureCauseStack()
	{
		RawThrowable t = failureCause;
		StringBuffer b = new StringBuffer(""+t.throwableClassName);
		while(t.getCause() != null)
		{
			t = t.getCause();
			b.append(" -> "+t.throwableClassName);
		}
		return b.toString();
	}

	public int getNumberOfAccessedObjects()
	{
		return getNumberOfAccessedObjects(EnumSet.allOf(RawAccessType.class));
	}

	public int getNumberOfAccessedObjects(EnumSet<RawAccessType> access_types)
	{
		// Recalculate this only once.
		if(accessedObjectDetail != null)
		{
			return accessedObjectDetail.getNumberOfAccessedObjects(access_types);
		}
		return 0;
	}


	public String getSimpleClassName()
	{
		String[] parts = baseTaskType.split("\\.");
		return parts[parts.length-1];
	}
	
}
