package com.gamalocus.sgs.profile.listener.report;

import java.io.Serializable;

public class RawPropertyChangeEvent implements Serializable {
	private static final long serialVersionUID = -7814665108204618865L;
	public long time;
	public String sourceToString;
	public String sourceClass;
	public String propertyName;
	public String oldValueToString;
	public String oldValueClass;
	public String newValueToString;
	public String newValueClass;
	
	@Override
	public String toString() {
		return sourceToString+"["+sourceClass+"]."+propertyName+": "+oldValueToString+"["+oldValueClass+"] => "+newValueToString+"["+newValueClass+"]";
	}
}
