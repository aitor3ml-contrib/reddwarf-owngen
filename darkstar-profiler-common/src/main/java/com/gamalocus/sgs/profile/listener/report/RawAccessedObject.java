/**
 * 
 */
package com.gamalocus.sgs.profile.listener.report;

import java.io.Serializable;

public class RawAccessedObject implements Serializable
{
	private static final long serialVersionUID = -491384165362777818L;
	public String source;
	public RawAccessType access_type;
	public String class_name;
	public String to_string;
	public long oid;
	
	RawAccessedObject()
	{
	}

	public String getSource()
	{
		return source;
	}

	public RawAccessType getAccessType()
	{
		return access_type;
	}
}