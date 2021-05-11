// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.regex.*;
import java.sql.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

/**
 *	Connection implementation for the Matillion ETL JDBC driver
 */
public class METLConnection
	extends ConnectionStub
{
	private String gUserName;
	private String gPassword;

	private static TreeMap<String,String> tmKnownGoodCreds = null;
	private static TreeMap<String,LocalDateTime> tmKnownBadCreds = null;

	public METLConnection(String url, Properties info)
		throws SQLException
	{
		if(null == tmKnownGoodCreds)
		{
			tmKnownGoodCreds = new TreeMap<String,String>();
		}

		if(null == tmKnownBadCreds)
		{
			tmKnownBadCreds = new TreeMap<String,LocalDateTime>();
		}

		gUserName = null;
		gPassword = null;

		for(Iterator<String> itKeys = new TreeSet<String>(info.stringPropertyNames()).iterator(); itKeys.hasNext(); )
		{
			String sKey = itKeys.next();

			if(sKey.equals("user"))
			{
				gUserName = info.getProperty(sKey);
			}
			if(sKey.equals("password"))
			{
				gPassword = info.getProperty(sKey);
			}
		}

		if(null != gUserName && null != gPassword && null != tmKnownGoodCreds)
		{
			try
			{
				if(tmKnownGoodCreds.get(gUserName).equals(gPassword))
				{
					// We know these credentials are valid
					return;
				}
			}
			catch(NullPointerException nx) {}
		}

		// Expire any known bad credentials that are > 10 seconds old
		if(null != gUserName && null != tmKnownBadCreds)
		{
			LocalDateTime dtNow = LocalDateTime.now();
			for(Iterator<String> itBads = tmKnownBadCreds.keySet().iterator(); itBads.hasNext(); )
			{
				String sBadUser = itBads.next();
				LocalDateTime dtBadTime = tmKnownBadCreds.get(sBadUser);

				if(dtNow.isAfter(dtBadTime.plusSeconds(10L)))
				{
					tmKnownBadCreds.remove(sBadUser);
				}
			}

			// Matillion tries to connect at least 10 times during normal operation
			// So if the API connection fails once it will fail again second time
			// Don't needlessly batter the API if this happens
			for(Iterator<String> itBads = tmKnownBadCreds.keySet().iterator(); itBads.hasNext(); )
			{
				String sBadUser = itBads.next();
				if(sBadUser.equals(gUserName))
				{
					throw(new SQLException("Bad username or password: please wait 10 seconds before retrying"));
				}
			}
		}

		try
		{
			APIv1.tryLogin(gUserName, gPassword);
		}
		catch(SQLException sx)
		{
			if(null != gUserName && null != tmKnownGoodCreds)
			{
				tmKnownGoodCreds.remove(gUserName);
			}

			if(null != gUserName && null != tmKnownBadCreds)
			{
				tmKnownBadCreds.put(gUserName, LocalDateTime.now());
			}

			throw(sx);
		}

		if(null != gUserName && null != gPassword && null != tmKnownGoodCreds)
		{
			tmKnownGoodCreds.put(gUserName, gPassword);
		}
	}

	@Override
	public PreparedStatement prepareStatementImpl(String sql)
		throws SQLException
	{
		Pattern p;
		Matcher m;

		boolean bIsCount = false;	// Is this a SELECT COUNT(*) FROM ( ...
		Integer iRowLimit = null;		// For the optional LIMIT clause

		p = Pattern.compile("SELECT\\s+COUNT\\(\\*\\)\\s+", Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		bIsCount = m.find();

		if(false == bIsCount)
		{
			p = Pattern.compile("LIMIT\\s(\\d+)", Pattern.MULTILINE | Pattern.DOTALL);
			m = p.matcher(sql);
			if(m.find())
			{
				if(1 == m.groupCount())
				{
					try
					{
						iRowLimit = Integer.parseInt(m.group(1));
					}
					catch(Throwable t)
					{
						iRowLimit = null;
					}
				}
			}
		}

		// Instance is backed by Java calls on the VM
		p = Pattern.compile("SELECT.*FROM.*\"instance\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(LinuxUtils.getInstanceQueryStatement(bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Joblaunchstats is backed by Java calls on the VM
		p = Pattern.compile("SELECT.*FROM.*\"joblaunchstats\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(LinuxUtils.getJobLaunchStatsQueryStatement(bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Group is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+group\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getGroupQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Project is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+project\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getProjectQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Schedule is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+schedule\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getScheduleQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Environment is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+environment\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getEnvironmentQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Version is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+version\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getVersionQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Runningjob is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+runningjob\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getRunningJobQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// Job is backed by an API call
		p = Pattern.compile("SELECT.*FROM[\\s\"]+job\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(APIv1.getAllJobNamesQueryStatement(gUserName, gPassword, bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// If reach here, the SQL is not acceptable
		throw(new SQLException("Syntax error"));
	}

	@Override
	public DatabaseMetaData getMetaData()
		throws SQLException
	{
		return(METLMetaData.getInstance());
	}

	@Override
	public String getSchema()
		throws SQLException
	{
		return(com.thereisnogravity.Driver.METL_SCHEMA_NAME);
	}

	@Override
	public String getCatalog()
		throws SQLException
	{
		return(com.thereisnogravity.Driver.CATALOG_TERM);
	}
}

