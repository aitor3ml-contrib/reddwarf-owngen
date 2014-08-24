package com.sun.sgs.impl.profile;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

import com.sun.sgs.profile.ProfileReport;

public class HackProfileCollectorImpl {
	
	public static ProfileReport getCurrentReport(ProfileCollectorImpl collector) {
		try
		{
			return collector.getCurrentProfileReport();
		}
		catch(EmptyStackException e)
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static Iterable<ProfileReportImpl> getCurrentReportStack(
			ProfileCollectorImpl profileCollector) 
	{
		try {
			Field f = ProfileCollectorImpl.class.getDeclaredField("profileReports");
			f.setAccessible(true);
			ThreadLocal<Stack<ProfileReportImpl>> profileReports = (ThreadLocal<Stack<ProfileReportImpl>>)f.get(profileCollector);
			return profileReports.get();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
}
