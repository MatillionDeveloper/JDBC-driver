// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.sql.*;
import java.util.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

/**
 * METLMetaData is the DatabaseMetaData implementation for the Matillion ETL JDBC driver.
 * The database metadata is fixed, so this class implements a singleton pattern.
 */
public class METLMetaData
	extends DatabaseMetaDataStub
{
	protected final static int MAJOR_VERSION = 5;
	protected final static int MINOR_VERSION = 8;

	protected static String[] METL_TABLES;
	protected static TreeMap<String,String[]> METL_COLNAMES;

	static
	{
		METL_TABLES = new String[] {"instance", "group", "project", "schedule", "environment", "version", "job", "runningjob", "joblaunchstats"};

		METL_COLNAMES = new TreeMap<String,String[]>();
		METL_COLNAMES.put("instance",		new String[] {"provider", "cdw", "version", "timezone", "diskused", "cpuused", "netifname", "netrxbytespersec", "nettxbytespersec", "memoryused"});
		METL_COLNAMES.put("group",			new String[] {"groupname"});
		METL_COLNAMES.put("project",		new String[] {"groupname", "projectname"});
		METL_COLNAMES.put("schedule",		new String[] {"groupname", "projectname", "schedulename"});
		METL_COLNAMES.put("environment",	new String[] {"groupname", "projectname", "environmentname"});
		METL_COLNAMES.put("version",		new String[] {"groupname", "projectname", "versionname"});
		METL_COLNAMES.put("job",			new String[] {"groupname", "projectname", "versionname", "jobname"});
		METL_COLNAMES.put("runningjob",		new String[] {"groupname", "projectname", "versionname", "id", "jobname", "starttime"});
		METL_COLNAMES.put("joblaunchstats",	new String[] {"hour", "totaljobs", "totaldelaysecs", "meanlatencysecs", "maxlatencysecs"});
	}

	private static METLMetaData gMetadataSingleton = null;

	public String[] getTableNames()
	{
		return(METL_TABLES);
	}

	public String[] getColumnNamesForTable(String pTableName)
		throws SQLException
	{
		String[] arrCols = METL_COLNAMES.get(pTableName);

		if(null != arrCols)
		{
			return(arrCols);
		}

		throw(new SQLException(String.format("Unknown table %s", pTableName)));
	}

	/**
	 *	This JDBC driver is named METL
	 */
	public String getDriverName()
	{
		return("METL");
	}

	/**
	 *	This database product is named METL
	 */
	public String getDatabaseProductName()
	{
		return("METL");
	}

	/**
	 *	The catalog term is fixed
	 */
	public String getCatalogTerm()
	{
		return(com.thereisnogravity.Driver.CATALOG_TERM);
	}

	/**
	 *	The schema name is fixed
	 */
	public String getSchemaName()
	{
		return(com.thereisnogravity.Driver.METL_SCHEMA_NAME);
	}

	public int getDriverMajorVersion()
	{
		return(METLMetaData.MAJOR_VERSION);
	}

	public int getDriverMinorVersion()
	{
		return(METLMetaData.MINOR_VERSION);
	}

	public static synchronized METLMetaData getInstance()
	{
		if(null == gMetadataSingleton)
		{
			gMetadataSingleton = new METLMetaData();
		}
		return(gMetadataSingleton);
	}
 
	// There's nothing to initiate, but keep the constructor private to ensure the class can't be instantiated elsewhere
	private METLMetaData()
	{
	}
}

