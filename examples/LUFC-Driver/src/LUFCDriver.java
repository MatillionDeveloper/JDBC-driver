package com.thereisnogravity.lufc;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

public class LUFCDriver
	implements java.sql.Driver
{
	private static final Driver gDriverInstance = new LUFCDriver();
	private static boolean registered = false;

	private DriverPropertyInfo[] gInfo = null;

	static
	{
		registerSelf();
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

	public boolean acceptsURL(String url)
	{
		if(null != url)
		{
			return(url.startsWith("jdbc:lufc://"));
		}
		return(false);
	}

	public Connection connect(String url, String pUser, String pPassword)
		throws SQLException
	{
		return(new LUFCConnection(url, new Properties()));
	}

	public Connection connect(String url, Properties info)
		throws SQLException
	{
		if(acceptsURL(url))
		{
			return(new LUFCConnection(url, info));
		}
		return(null);
	}

	public int getMajorVersion()
	{
		return(LUFCMeta.MAJOR_VERSION);
	}

	public int getMinorVersion()
	{
		return(LUFCMeta.MINOR_VERSION);
	}

	public Logger getParentLogger()
	{
		return(null);
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
	{
		if(null == gInfo)
		{
			gInfo = new DriverPropertyInfo[] {};
		}
		return(gInfo);
	}

	public boolean jdbcCompliant()
	{
		return(false);
	}
}

