// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

/**
 * LinuxUtils contains static functions which get information from the Linux operating system.
 */
public class LinuxUtils
{
	private final static int POLL_INTERVAL_SECS = 10;

	private static boolean monitorRunning = false;

	private static String platformName = "Unknown";
	private static String gCdwName = "Unknown";
	private static String gMetlVersion = "Unknown";

	private static String currProcStat = null;
	private static String prevProcStat = null;

	private static String currProcNetDev = null;
	private static String prevProcNetDev = null;

	// Return a PreparedStatement which contains the job launch stats
	protected static JsonBackedStatement getJobLaunchStatsQueryStatement(boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		// Add the column name(s) from the metadata
		// "hour", "totaljobs", "totaldelaysecs", "meanlatencysecs", "maxlatencysecs"
		for(String s: METLMetaData.METL_COLNAMES.get("joblaunchstats"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		TreeMap<String,LatencyMetrics> tmData = collectJobLatencyMetrics();

		JsonObject jRow;

		rowstop:
		for(Iterator<String> iKs = tmData.keySet().iterator(); iKs.hasNext() ;)
		{
			String sBucket = iKs.next();
			LatencyMetrics l = tmData.get(sBucket);

			jRet.getAsJsonArray("data").add(jRow = new JsonObject());
			jRow.addProperty("hour", String.format("%s:00:00", sBucket));
			jRow.addProperty("totaljobs", l.getTotalJobsAsString());
			jRow.addProperty("totaldelaysecs", l.getCumlSecsAsString());
			jRow.addProperty("meanlatencysecs", l.getMeanLatencyAsString());
			jRow.addProperty("maxlatencysecs", l.getLongestSecsAsString());

			if(0 == --iRowstop)
			{
				break rowstop;
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	private static TreeMap<String,LatencyMetrics> collectJobLatencyMetrics()
		throws Exception
	{
		TreeMap<String,String> tmTasks = new TreeMap<String,String>();	// Keyed by ID
		TreeMap<String,LatencyMetrics> tmLatency = new TreeMap<String,LatencyMetrics>();	// Keyed by YYYY-MM-DD HH
		DateTimeFormatter dtfCatalinaTimestamp = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
		DateTimeFormatter dtfBinFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

		String sLine;
		String sMarkTimestamp, sStartTimestamp;

		Pattern pEnqueue = Pattern.compile("Queueing taskbatch with ID \\[(\\d+)\\]");
		Pattern pDequeue = Pattern.compile("Starting taskbatch with ID \\[(\\d+)\\]");
		Pattern pTimestamp = Pattern.compile("([0-9]{2}-[a-z]{3}-[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}).[0-9]{3}\\sINFO", Pattern.CASE_INSENSITIVE);
		Matcher m;

		try(BufferedReader brLog = new BufferedReader(new FileReader("/var/log/tomcat8/catalina.out")))
		{
			while(null != (sLine = brLog.readLine()))
			{
				if(sLine.contains("taskbatch with ID"))
				{
					m = pTimestamp.matcher(sLine);

					if(m.find())
					{
						if(1 == m.groupCount())
						{
							sMarkTimestamp = m.group(1);

							m = pEnqueue.matcher(sLine);

							if(m.find())
							{
								tmTasks.put(m.group(1), sMarkTimestamp);
							}

							m = pDequeue.matcher(sLine);

							if(m.find())
							{
								sStartTimestamp = tmTasks.remove(m.group(1));
								if(null != sStartTimestamp)
								{
									registerLaunch(m.group(1), sStartTimestamp, sMarkTimestamp, dtfCatalinaTimestamp, dtfBinFormat, tmLatency);
								}
							}

						}
					}
				}
			}
		}

		return(tmLatency);
	}

	private static class LatencyMetrics
	{
		private int launchCount;
		private int cumulativeSeconds;
		private int longestDelaySeconds;

		public LatencyMetrics()
		{
			this.launchCount = 0;
			this.cumulativeSeconds = 0;
			this.longestDelaySeconds = 0;
		}

		public LatencyMetrics(int secs)
		{
			super();
			update(secs);
		}

		public void update(int secs)
		{
			this.launchCount++;
			this.cumulativeSeconds += secs;
			if(secs > this.longestDelaySeconds)
			{
				this.longestDelaySeconds = secs;
			}
		}

		public String getTotalJobsAsString()
		{
			return(String.format("%d", launchCount));
		}

		public String getCumlSecsAsString()
		{
			return(String.format("%d", cumulativeSeconds));
		}

		public String getLongestSecsAsString()
		{
			return(String.format("%d", longestDelaySeconds));
		}

		public String getMeanLatencyAsString()
		{
			if(0 >= cumulativeSeconds || 0 >= launchCount)
			{
				return("0");
			}

			double dRet = cumulativeSeconds;
			dRet /= launchCount;
			return(String.format("%.0f", dRet));
		}
	}

	private static void registerLaunch(String pID, String pEnqueueTS, String pDequeueTS, DateTimeFormatter pfmtCatalinaTimestamp, DateTimeFormatter pfmtBinFormat, TreeMap<String,LatencyMetrics> ptmLatency)
	{
		int iLagSecs;
		String sBucket = null;

		LocalDateTime ldtEnqueue = LocalDateTime.from(pfmtCatalinaTimestamp.parse(pEnqueueTS));
		sBucket = pfmtBinFormat.format(ldtEnqueue);

		if(pEnqueueTS.equals(pDequeueTS))
		{
			iLagSecs = 0;
		}
		else
		{
			LocalDateTime ldtDequeue = LocalDateTime.from(pfmtCatalinaTimestamp.parse(pDequeueTS));

			Duration dDiff = Duration.between(ldtEnqueue, ldtDequeue);
			iLagSecs = (int)dDiff.getSeconds();
		}

		LatencyMetrics l = ptmLatency.get(sBucket);

		if(null == l)
		{
			ptmLatency.put(sBucket, new LatencyMetrics(iLagSecs));
		}
		else
		{
			l.update(iLagSecs);
		}
	}

	// Return a PreparedStatement which contains the instance data
	protected static JsonBackedStatement getInstanceQueryStatement(boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(1, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		JsonObject jRet = METLMetaData.getBaseJson();
		// Add the column name(s) from the metadata
		for(String s: METLMetaData.METL_COLNAMES.get("instance"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonObject jRow;
		jRet.getAsJsonArray("data").add(jRow = new JsonObject());

		jRow.addProperty("provider", getProvider());
		jRow.addProperty("cdw", getCDW());
		jRow.addProperty("version", getVersion());
		jRow.addProperty("timezone", ZoneId.systemDefault().toString());
		jRow.addProperty("diskused", getDiskused());
		jRow.addProperty("cpuused", getCpuUsed());

		TreeMap<String,String> tmNetStats = getNetworkUsed();
		for(Iterator<String> iMapIt = tmNetStats.keySet().iterator(); iMapIt.hasNext() ;)
		{
			String sKey = iMapIt.next();
			jRow.addProperty(sKey, tmNetStats.get(sKey));
		}
		
		jRow.addProperty("memoryused", getMemoryused());

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// For instance.provider
	protected static String getProvider()
	{
		return(platformName);
	}

	// For instance.cdw
	protected static String getCDW()
	{
		return(gCdwName);
	}

	// For instance.version
	protected static String getVersion()
	{
		return(gMetlVersion);
	}

	// For instance.networkused
	protected static TreeMap<String,String> getNetworkUsed()
	{
		TreeMap<String,String> tmRet = new TreeMap<String,String>();
		tmRet.put("netifname", "?");
		tmRet.put("netrxbytespersec", "?");
		tmRet.put("nettxbytespersec", "?");

		if(null != currProcNetDev && null != prevProcNetDev)
		{
			String[] sPrevLine = prevProcNetDev.split(":");
			String[] sCurrLine = currProcNetDev.split(":");

			if(2 == sPrevLine.length && 2 == sCurrLine.length)
			{
				if(sPrevLine[0].replaceAll("\\s", "").equals( sCurrLine[0].replaceAll("\\s", "") ))
				{
					tmRet.put("netifname", sPrevLine[0].replaceAll("\\s", ""));

					String[] sPrevStats = sPrevLine[1].replaceAll("^\\s+", "").split("\\s+");
					String[] sCurrStats = sCurrLine[1].replaceAll("^\\s+", "").split("\\s+");

					if(0 == sPrevStats.length%2 && sPrevStats.length == sCurrStats.length)
					{
						long prevRxBytes = Long.parseLong(sPrevStats[0]);
						long prevTxBytes = Long.parseLong(sPrevStats[sPrevStats.length/2]);
						long currRxBytes = Long.parseLong(sCurrStats[0]);
						long currTxBytes = Long.parseLong(sCurrStats[sCurrStats.length/2]);

						if(prevRxBytes <= currRxBytes && prevTxBytes <= currTxBytes)
						{
							long intervalRxBytes = currRxBytes - prevRxBytes;
							long intervalTxBytes = currTxBytes - prevTxBytes;

							tmRet.put("netrxbytespersec", String.format("%d", intervalRxBytes/POLL_INTERVAL_SECS));
							tmRet.put("nettxbytespersec", String.format("%d", intervalTxBytes/POLL_INTERVAL_SECS));
						}
					}
				}
			}
		}

		return(tmRet);
	}

	// For instance.memoryused
	protected static String getMemoryused()
	{
		Runtime vRt = Runtime.getRuntime();

		long lMax = vRt.maxMemory();
		long lTot = vRt.totalMemory();
		//long lAvail = lMax - lTot;

		double dRet = lTot;
		dRet /= lMax;
		dRet *= 100;

		return(String.format("%.0f%%", dRet));
	}

	// For instance.cpuused
	protected static String getCpuUsed()
	{
		if(null != currProcStat && null != prevProcStat)
		{
			Pattern p = Pattern.compile("^cpu\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s.*");
			Matcher mPrev = p.matcher(prevProcStat);
			Matcher mCurr = p.matcher(currProcStat);
			if(mPrev.matches() && mCurr.matches())
			{
				if(5 == mPrev.groupCount() && 5 == mCurr.groupCount())
				{
					long prevUser = Long.parseLong(mPrev.group(1));
					long prevNice = Long.parseLong(mPrev.group(2));
					long prevSyst = Long.parseLong(mPrev.group(3));
					long prevIdle = Long.parseLong(mPrev.group(4));
					long prevIOwt = Long.parseLong(mPrev.group(5));
					long currUser = Long.parseLong(mCurr.group(1));
					long currNice = Long.parseLong(mCurr.group(2));
					long currSyst = Long.parseLong(mCurr.group(3));
					long currIdle = Long.parseLong(mCurr.group(4));
					long currIOwt = Long.parseLong(mCurr.group(5));

					double dTot = currUser + currNice + currSyst + currIdle + currIOwt - prevUser - prevNice - prevSyst - prevIdle - prevIOwt;
					double dIdle = currIdle - prevIdle;

					if(dTot > 0 && dIdle > 0 && dTot >= dIdle)
					{
						double dNonIdle = dTot - dIdle;
						return(String.format("%.0f%%", 100 * dNonIdle / dTot));
					}
				}
			}
		}
		return("?");
	}

	// For instance.diskused
	protected static String getDiskused()
	{
		Runtime vRt = Runtime.getRuntime();
		String sLine;
		StringBuffer sb = new StringBuffer();

		try
		{
			Process pCmd = vRt.exec(new String[] {"df", "/"});

			try(BufferedReader bIn = new BufferedReader(new InputStreamReader(pCmd.getInputStream())))
			{
				while(null != (sLine = bIn.readLine()))
				{
					sb.append(sLine);
				}
			}

			Pattern p = Pattern.compile("[0-9]+[%]");
			Matcher m = p.matcher(sb.toString());

			if(m.find())
			{
				return(m.group());
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}

		return("Unknown");
	}

	// Launched from Driver static
	protected static void runProviderGuesser()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				Runtime vRt = Runtime.getRuntime();
				String sLine;

				java.net.URL uTest;
				boolean bStillGuessing = true;

				// If this is Google, http://metadata.google.internal should respond
				// On other platforms it should fail with a java.net.UnknownHostException:
				HttpURLConnection gHttpConn = null;
				try
				{
					uTest = new java.net.URL("http://metadata.google.internal");
					gHttpConn = (HttpURLConnection)uTest.openConnection();
					gHttpConn.setRequestMethod("GET");
					gHttpConn.setConnectTimeout(1000);		// Allow 1000 milliseconds
					gHttpConn.connect();
					platformName = "GCP";
					bStillGuessing = false;
				}
				catch(Throwable t)
				{
				}
				finally
				{
					try
					{
						gHttpConn.disconnect();
					}
					catch(Throwable t)
					{
					}
				}

				if(bStillGuessing)
				{
					// If this is AWS, http://169.254.169.254 should respond successfully
					// Azure instances also respond to this address, but with an HTTP 400
					HttpURLConnection aHttpConn = null;
					try
					{
						uTest = new java.net.URL("http://169.254.169.254");
						aHttpConn = (HttpURLConnection)uTest.openConnection();
						aHttpConn.setRequestMethod("GET");
						aHttpConn.setConnectTimeout(1000);		// Allow 1000 milliseconds
						aHttpConn.connect();
						if(200 == aHttpConn.getResponseCode())
						{
							platformName = "AWS";
						}
						else
						{
							platformName = "Azure";
						}
					}
					catch(Throwable t)
					{
					}
					finally
					{
						try
						{
							aHttpConn.disconnect();
						}
						catch(Throwable t)
						{
						}
					}
				}

				// Guess the CDW
				try
				{
					Process pCmd = vRt.exec(new String[] {"zcat", "-f", "/var/log/tomcat8/catalina.out", "/var/log/tomcat8/catalina*.gz"});

					try(BufferedReader bIn = new BufferedReader(new InputStreamReader(pCmd.getInputStream())))
					{
						while(null != (sLine = bIn.readLine()))
						{
							if(sLine.contains("getRealModule"))
							{
								Pattern p = Pattern.compile("getRealModule Using Database Environment \\[([\\w\\s]+)\\]");
								Matcher m = p.matcher(sLine);

								if(m.find())
								{
									if(1 == m.groupCount())
									{
										gCdwName = new String(m.group(1));
										break;
									}
								}
							}
						}
					}
				}
				catch(Throwable t)
				{
				}

				// Guess the METL version
				try
				{
					Process pCmd = vRt.exec(new String[] {"/bin/bash", "-c", "ls /usr/share/emerald/WEB-INF/lib/emerald-1*.jar"});

					try(BufferedReader bIn = new BufferedReader(new InputStreamReader(pCmd.getInputStream())))
					{
						if(null != (sLine = bIn.readLine()))
						{
							Pattern p = Pattern.compile("emerald-(1\\.[0-9]+\\.[0-9]+).jar");
							Matcher m = p.matcher(sLine);

							if(m.find())
							{
								if(1 == m.groupCount())
								{
									gMetlVersion = new String(m.group(1));
								}
							}
						}
					}
				}
				catch(Throwable t)
				{
				}
			}
		}).start();
	}

	// Launched from Driver static
	protected static void runVMMonitor()
	{
		if(monitorRunning)
		{
			return;
		}
		else
		{
			monitorRunning = true;
			new Thread(new Runnable()
			{
				public void run()
				{
					boolean bContinue = true;

					while(bContinue)
					{
						String sLine;

						try(BufferedReader brProcStat = new BufferedReader(new FileReader("/proc/stat")))
						{
							if(null != (sLine = brProcStat.readLine()))
							{
								prevProcStat = currProcStat;
								currProcStat = sLine;
							}
						}
						catch(Throwable t)
						{
							currProcStat = null;
							prevProcStat = null;
							bContinue = false;
						}

						try(BufferedReader brProcStat = new BufferedReader(new FileReader("/proc/net/dev")))
						{
							while(null != (sLine = brProcStat.readLine()))
							{
								if(sLine.contains(":") && !sLine.contains("lo:"))
								{
									prevProcNetDev = currProcNetDev;
									currProcNetDev = sLine;
									break;
								}
							}
						}
						catch(Throwable t)
						{
							currProcNetDev = null;
							prevProcNetDev = null;
							bContinue = false;
						}

						try
						{
							Thread.sleep(POLL_INTERVAL_SECS * 1000);
						}
						catch(InterruptedException x)
						{
							bContinue = false;
						}
					}
				}
			}).start();
		}
	}

	// This class can not be instantiated
	private LinuxUtils()
	{
	}
}

