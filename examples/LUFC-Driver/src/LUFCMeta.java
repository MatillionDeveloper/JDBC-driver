// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity.lufc;

import java.sql.*;
import java.util.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

public class LUFCMeta
	extends DatabaseMetaDataStub
{
	protected final static int MAJOR_VERSION = 1;
	protected final static int MINOR_VERSION = 0;
	protected final static String CATALOG_TERM = "catalog";
	protected final static String SCHEMA_NAME = "public";

	protected static String[] TABLES;
	protected static TreeMap<String,String[]> COLNAMES;

	static
	{
		TABLES = new String[] {"results"};

		COLNAMES = new TreeMap<String,String[]>();
		COLNAMES.put("results",		new String[] {"matchdate", "home", "away", "result", "home_goals", "away_goals"});
	}

	private static LUFCMeta gMetadataSingleton = null;

	public String[] getTableNames()
	{
		return(TABLES);
	}

	public String[] getColumnNamesForTable(String pTableName)
		throws SQLException
	{
		String[] arrCols = COLNAMES.get(pTableName);

		if(null != arrCols)
		{
			return(arrCols);
		}

		throw(new SQLException(String.format("Unknown table %s", pTableName)));
	}

	public String getDriverName()
	{
		return("LUFC Driver");
	}

	public String getDatabaseProductName()
	{
		return("LUFC Results");
	}

	public String getCatalogTerm()
	{
		return(CATALOG_TERM);
	}

	public String getSchemaName()
	{
		return(SCHEMA_NAME);
	}

	public int getDriverMajorVersion()
	{
		return(MAJOR_VERSION);
	}

	public int getDriverMinorVersion()
	{
		return(MINOR_VERSION);
	}

	public static synchronized LUFCMeta getInstance()
	{
		if(null == gMetadataSingleton)
		{
			gMetadataSingleton = new LUFCMeta();
		}
		return(gMetadataSingleton);
	}
 
	// There's nothing to initiate, but keep the constructor private to ensure the class can't be instantiated elsewhere
	private LUFCMeta()
	{
	}
}

