package com.gamalocus.sgs.profile.viewer.util;

import java.util.Calendar;

public class DateUtil {
	private static Calendar cal = Calendar.getInstance();
	public static String getTime(long time)
	{
		cal.setTimeInMillis(time);
		return String.format("%1$tH:%1$tM.%1$tL", cal);
	}
}
