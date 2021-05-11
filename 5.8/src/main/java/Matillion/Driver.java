// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

public class Driver
	implements java.sql.Driver
{
	private static boolean registered = false;
	private static final com.thereisnogravity.Driver gDriverInstance = new com.thereisnogravity.Driver();

	private DriverPropertyInfo[] gInfo = null;

	protected final static String CATALOG_TERM = "catalog";
	protected final static String METL_SCHEMA_NAME = "public";

	static {
		registerSelf();
		com.thereisnogravity.LinuxUtils.runVMMonitor();
		com.thereisnogravity.LinuxUtils.runProviderGuesser();
	}

	public static synchronized Driver registerSelf()
	{
		try
		{
            if(!registered)
			{
                registered = true;
                DriverManager.registerDriver(gDriverInstance);
            }
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return(gDriverInstance);
	}

	@Override
	public boolean acceptsURL(String url)
	{
		if(null != url)
		{
			return(url.startsWith("jdbc:metl://"));
		}
		return(false);
	}

	public Connection connect(String url, String pUser, String pPassword)
		throws SQLException
	{
		Properties pCreds = new Properties();
		pCreds.put("user", pUser);
		pCreds.put("password", pPassword);
		return(new METLConnection(url, pCreds));
	}

	@Override
	public Connection connect(String url, Properties info)
		throws SQLException
	{
		if(acceptsURL(url))
		{
			return(new METLConnection(url, info));
		}
		return(null);
	}

	@Override
	public int getMajorVersion()
	{
		return(METLMetaData.MAJOR_VERSION);
	}

	@Override
	public int getMinorVersion()
	{
		return(METLMetaData.MINOR_VERSION);
	}

	@Override
	public Logger getParentLogger()
	{
		return(null);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
	{
		if(null == gInfo)
		{
			gInfo = new DriverPropertyInfo[] {};
		}
		return(gInfo);
	}

	@Override
	public boolean jdbcCompliant()
	{
		return(false);
	}
}

