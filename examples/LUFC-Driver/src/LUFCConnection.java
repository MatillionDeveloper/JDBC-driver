package com.thereisnogravity.lufc;

import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.regex.*;
import java.sql.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

public class LUFCConnection
	extends ConnectionStub
{
	// This is a simplistic implementation so this random sample of results is hardcoded
	private final static String[][] gResultData = {
				{"2021-05-08", "Leeds United", "Tottenham Hotspur", "Home Win", "3", "1"},
				{"2021-04-10", "Manchester City", "Leeds United", "Away Win", "1", "2"},
				{"2010-04-10", "Manchester United", "Leeds United", "Away Win", "0", "1"},
				{"2001-04-13", "Liverpool", "Leeds United", "Away Win", "1", "2"},
				{"2002-12-27", "Leeds United", "Chelsea", "Home Win", "2", "0"},
				{"2003-05-03", "Arsenal", "Leeds United", "Away Win", "2", "3"}
			};

	// We're not actually connecting to anything at all here, so this method just succeeds every time
	public LUFCConnection(String url, Properties info)
		throws SQLException
	{
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

		// Check if this is a query against the "results" table
		p = Pattern.compile("SELECT.*FROM.*\"results\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		m = p.matcher(sql);
		if(m.find())
		{
			try
			{
				return(getResultsQueryStatement(bIsCount, iRowLimit));
			}
			catch(Throwable t)
			{
				throw(new SQLException(t.getMessage()));
			}
		}

		// If reach here, the SQL is not acceptable
		throw(new SQLException("Syntax error"));
	}

	private static JsonBackedStatement getResultsQueryStatement(boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = LUFCMeta.getBaseJson();

		// Add the column name(s) from the metadata
		for(String s: LUFCMeta.COLNAMES.get("results"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonObject jRow;

		rowstop:
		for(String[] sMatch : gResultData)
		{
			jRet.getAsJsonArray("data").add(jRow = new JsonObject());
			jRow.addProperty("matchdate", sMatch[0]);
			jRow.addProperty("home", sMatch[1]);
			jRow.addProperty("away", sMatch[2]);
			jRow.addProperty("result", sMatch[3]);
			jRow.addProperty("home_goals", sMatch[4]);
			jRow.addProperty("away_goals", sMatch[5]);

			if(0 == --iRowstop)
			{
				break rowstop;
			}
		}

		if(pIsCount)
		{
			return(LUFCMeta.getRowcountStatement(jRet.getAsJsonArray("data").size(), LUFCMeta.CATALOG_TERM, LUFCMeta.SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, LUFCMeta.CATALOG_TERM, LUFCMeta.SCHEMA_NAME));
	}

	@Override
	public DatabaseMetaData getMetaData()
		throws SQLException
	{
		return(LUFCMeta.getInstance());
	}

	@Override
	public String getSchema()
		throws SQLException
	{
		return(LUFCMeta.SCHEMA_NAME);
	}

	@Override
	public String getCatalog()
		throws SQLException
	{
		return(LUFCMeta.CATALOG_TERM);
	}
}

