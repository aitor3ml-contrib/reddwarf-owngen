package com.sun.sgs.impl.service.task;

public class HackPendingTask
{
	public static String getBaseTaskTypeFromPendingTask(Object pendingTask)
	{
		PendingTask pt = (PendingTask) pendingTask;
		return pt.getBaseTaskType();
	}
}
