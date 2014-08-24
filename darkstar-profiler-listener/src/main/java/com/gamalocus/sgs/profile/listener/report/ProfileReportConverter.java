package com.gamalocus.sgs.profile.listener.report;

import java.beans.PropertyChangeEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.sun.sgs.impl.service.task.HackPendingTask;
import com.sun.sgs.kernel.AccessedObject;
import com.sun.sgs.kernel.AccessReporter.AccessType;
import com.sun.sgs.profile.AccessedObjectsDetail;
import com.sun.sgs.profile.ProfileReport;
import com.sun.sgs.profile.AccessedObjectsDetail.ConflictType;

public class ProfileReportConverter {

	public static RawProfileReport convert(ProfileReport report) {
		RawProfileReport raw_report = new RawProfileReport();
		raw_report.wasSuccessful = report.wasTaskSuccessful();
		raw_report.wasTransactional = report.wasTaskTransactional();
		raw_report.txnId = raw_report.wasTransactional ? new RawTransactionId(report.getTransactionId()) : null;
		raw_report.runningTime = report.getRunningTime();
		raw_report.startTime = report.getActualStartTime();
		raw_report.failureCause = convert(report.getFailureCause());
		raw_report.baseTaskType = report.getTask().getBaseTaskType();
		raw_report.taskToString = report.getTask().toString();
		raw_report.taskClassName = report.getTask().getClass().getCanonicalName();
		
		if(report.getAccessedObjectsDetail() != null)
		{
			raw_report.accessedObjectDetail = convert(report.getAccessedObjectsDetail());
		}
		else
		{
			raw_report.accessedObjectDetail = null;
		}

		return raw_report;
	}

	private static RawThrowable convert(Throwable throwable) {
		if(throwable == null)
			return null;
		RawThrowable raw_throwable = new RawThrowable();
		raw_throwable.throwableClassName = throwable.getClass().getCanonicalName();
		raw_throwable.message = throwable.getMessage();
		raw_throwable.localized_message = throwable.getLocalizedMessage();
		raw_throwable.stack_trace = throwable.getStackTrace();
		
		return raw_throwable;
	}

	public static RawAccessedObjectsDetail convert(AccessedObjectsDetail details) {
		RawAccessedObjectsDetail raw_details = new RawAccessedObjectsDetail();
		raw_details.conflictType = convert(details.getConflictType());
		raw_details.conflictingId = details.getConflictingId() != null ? new RawTransactionId(details.getConflictingId()) : null;

		if(details.getAccessedObjects() != null)
		{
			raw_details.accessedObjects = new RawAccessedObject[details.getAccessedObjects().size()];
			int i = 0;
			for(AccessedObject obj : details.getAccessedObjects())
			{
				raw_details.accessedObjects[i++] = convert(obj);
			}
		}
		else
		{
			raw_details.accessedObjects = new RawAccessedObject[0];
		}
		
		return raw_details;
	}
	
	
	private static RawAccessedObject convert(AccessedObject obj) {
		RawAccessedObject raw_obj = new RawAccessedObject();
		raw_obj.source = obj.getSource();
		raw_obj.access_type = convert(obj.getAccessType());
		
		// Copy a class_name and toString
		{
			String class_name;
			String to_string;
			Object description = obj.getDescription();
			if(description != null)
			{
				class_name = description.getClass().getCanonicalName();
				if(class_name.equals("com.sun.sgs.impl.service.task.PendingTask"))
				{
					String alt_cl = HackPendingTask.getBaseTaskTypeFromPendingTask(description);
					class_name = alt_cl != null ? alt_cl : class_name;
				}
			}
			else
			{
				class_name = obj.getSource()+":"+obj.getObjectId();
			}
			try
			{
				if(description != null)
				{
					to_string = description.toString();
				}
				else
				{
					to_string = null;
				}
			}
			catch(Throwable t)
			{
				to_string = class_name+"@"+obj.getObjectId();
				//System.out.println("to_string:"+to_string);
			}
			
			raw_obj.class_name = class_name;
			raw_obj.to_string = to_string;
		}
		
		Object oid = obj.getObjectId();
		if(oid instanceof BigInteger)
		{
			raw_obj.oid = ((BigInteger)oid).longValue();
		}
		else if(oid instanceof Long)
		{
			raw_obj.oid = ((Long)oid);
		}
		else if(oid instanceof Integer)
		{
			raw_obj.oid = ((Integer)oid);
		}
		else
		{
			raw_obj.oid = -1;
		}
		
		return raw_obj;
	}

	private static RawAccessType convert(AccessType accessType) {
		switch(accessType)
		{
		case READ:
			return RawAccessType.READ;
		case WRITE:
			return RawAccessType.WRITE;
		}
		throw new RuntimeException("Unknown AccessType: "+accessType);
	}
	
	private static RawConflictType convert(ConflictType conflictType) {
		switch(conflictType)
		{
		case ACCESS_NOT_GRANTED:
			return RawConflictType.ACCESS_NOT_GRANTED;
		case DEADLOCK:
			return RawConflictType.DEADLOCK;
		case NONE:
			return RawConflictType.NONE;
		case UNKNOWN:
			return RawConflictType.UNKNOWN;
		}
		throw new RuntimeException("Unknown ConflictType: "+conflictType);
	}

	public static Object convert(PropertyChangeEvent event) {
		RawPropertyChangeEvent raw_event = new RawPropertyChangeEvent();
		raw_event.time = System.currentTimeMillis();
		if(event.getSource() != null)
		{
			raw_event.sourceToString = event.getSource().toString();
			raw_event.sourceClass = event.getSource().getClass().toString();
		}
		raw_event.propertyName = event.getPropertyName();
		if(event.getOldValue() != null)
		{
			raw_event.oldValueToString = event.getOldValue().toString();
			raw_event.oldValueClass = event.getOldValue().getClass().toString();
		}
		if(event.getNewValue() != null)
		{
			raw_event.newValueToString = event.getNewValue().toString();
			raw_event.newValueClass = event.getNewValue().getClass().toString();
		}
		return raw_event;
	}

	public static Collection<RawLogRecord> convert(Collection<LogRecord> records) {
		if(records == null)
			return null;
		Collection<RawLogRecord> raw_records = new ArrayList<RawLogRecord>();
		for(LogRecord r : records)
		{
			raw_records.add(convert(r));
		}
		return raw_records;
	}

	private static RawLogRecord convert(LogRecord record) {
		RawLogRecord raw_record = new RawLogRecord();
	    raw_record.level = record.getLevel();
	    raw_record.sequenceNumber = record.getSequenceNumber();
	    raw_record.sourceClassName = record.getSourceClassName();
	    raw_record.sourceMethodName = record.getSourceMethodName();
	    raw_record.message = record.getMessage();
	    raw_record.threadID = record.getThreadID();
	    raw_record.millis = record.getMillis();
	    raw_record.thrown = convert(record.getThrown());
	    raw_record.loggerName = record.getLoggerName();
	    raw_record.resourceBundleName = record.getResourceBundleName();
		
		return raw_record;
	}

}
