package com.gamalocus.sgs.profile.listener;

import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Properties;

import com.gamalocus.sgs.profile.listener.report.ProfileReportConverter;
import com.gamalocus.sgs.profile.listener.report.RawProfileReport;
import com.gamalocus.sgs.profile.logger.ProfileLogHandler;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.profile.ProfileCollectorImpl;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.profile.ProfileCollector;
import com.sun.sgs.profile.ProfileListener;
import com.sun.sgs.profile.ProfileReport;

public class RawDumpListener implements ProfileListener
{
	//public static final File outFolder = new File("/tmp/darkstar_profile_output");
	public final static String OUPUT_FOLDER_PROPERTY = RawDumpListener.class.getName()+".output_folder";
	public final static String OUPUT_FOLDER_DEFAULT = "darkstar_profile_output";
	public final static String FILE_MAX_AGE_PROPERTY = RawDumpListener.class.getName()+".max_age";
	public final static long   FILE_MAX_AGE_DEFAULT  = 60000;
	public final static long   FILE_MAX_AGE_MIN      = 1000;
	private File outFolder;
	private long maxAge = FILE_MAX_AGE_DEFAULT;
	
	class OutputStreamProvider
	{
		private ObjectOutputStream currentOutputStream;
		private long currentOutputStreamTime;
		
		ObjectOutputStream getOutputStream()
		{
			if(currentOutputStream == null || System.currentTimeMillis() > currentOutputStreamTime+maxAge)
			{
				if(currentOutputStream != null)
				{
					try
					{
						currentOutputStream.flush();
						currentOutputStream.close();
					}
					catch(Exception e)
					{
						// Could not close old one... too bad.
					}
				}
				currentOutputStreamTime = (System.currentTimeMillis()/maxAge)*maxAge;
				try
				{
					currentOutputStream = new ObjectOutputStream(new FileOutputStream(outFolder+File.separator+"reports_"+currentOutputStreamTime+"_"+(currentOutputStreamTime+maxAge)+"_thread_"+Thread.currentThread().getId()+".output"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return currentOutputStream;
		}
	}
	
	private ThreadLocal<OutputStreamProvider> outStream = new ThreadLocal<OutputStreamProvider>()
	{
		@Override
		protected OutputStreamProvider initialValue()
		{
			return new OutputStreamProvider();
		}
	};
	
	public RawDumpListener(Properties properties, Identity identity, ComponentRegistry registry) throws FileNotFoundException, IOException
	{
		PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
		outFolder = new File(wrappedProps.getProperty(OUPUT_FOLDER_PROPERTY, OUPUT_FOLDER_DEFAULT));
		if(!outFolder.isDirectory() && (outFolder.exists() || !outFolder.mkdirs()))
		{
			throw new RuntimeException("Could not create folder: "+outFolder);
		}
		maxAge = wrappedProps.getLongProperty(FILE_MAX_AGE_PROPERTY, FILE_MAX_AGE_DEFAULT, FILE_MAX_AGE_MIN, Long.MAX_VALUE);
		
		ProfileCollector collector = 
			registry.getComponent(ProfileCollector.class);
		ProfileLogHandler.setProfileCollector(collector);
		
		// Echo the properties to a file there
		properties.store(new FileOutputStream(new File(outFolder, "sgs.properties"), false), "Echo from instantiation:" + new Date());
		
		// WRite the viewer script:
		String clientJar = "DarkstarProfileViewer-0.0.5.jar";
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFolder, "profiler_viewer.sh")));
		out.write("#!/bin/bash\n\n");
		
		out.write("cd `dirname $0`\n\n");
		
		out.write("if [ ! -f \""+clientJar+"\" ] ; then \n");
		out.write("\twget \"http://darkstar-profiler.googlecode.com/files/"+clientJar+"\" || exit -1\n");
		out.write("fi\n\n");
		
		out.write("java -Xmx512M -jar \""+clientJar+"\" .\n");
		out.flush();
		out.close();
	}

	public void propertyChange(PropertyChangeEvent event)
	{
		// Write a report to the file
		try
		{
			ObjectOutputStream out = outStream.get().getOutputStream();
			out.writeObject(ProfileReportConverter.convert(event));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void report(ProfileReport report)
	{
		// Write a report to the file
		try
		{
			ObjectOutputStream out = outStream.get().getOutputStream();
			RawProfileReport rawReport = ProfileReportConverter.convert(report);
			rawReport.logRecords = ProfileReportConverter.convert(ProfileLogHandler.getLogRecords(report, true));
			out.writeObject(rawReport);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void shutdown()
	{
		// TODO: close the open files (based on threads... how to do this :D)
	}
}
