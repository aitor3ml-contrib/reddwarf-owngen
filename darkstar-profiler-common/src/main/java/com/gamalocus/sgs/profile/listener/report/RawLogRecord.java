package com.gamalocus.sgs.profile.listener.report;

import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class RawLogRecord implements Serializable 
{
	private static final long serialVersionUID = -4053878015555655420L;

    public Level level;
    public long sequenceNumber;
    public String sourceClassName;
    public String sourceMethodName;
    public String message;
    public int threadID;
    public long millis;
    public RawThrowable thrown;
    public String loggerName;
    public String resourceBundleName;
    
    
}
