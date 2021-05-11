// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity.jdbcstub;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

import com.google.gson.*;

/**
 *	This is a ResultSetMetaData implementation which is backed by a JSON object.<br>
 */
public class JsonBackedResultSetMetadata
	implements ResultSetMetaData
{
	private JsonObject gjObj;
	private String gCatalogName;
	private String gSchemaName;

	/**
	 *	Constructs a new {@link JsonBackedResultSetMetadata} with the supplied data as JSON
	 *	@param pObj The data
	 *	@param pCatalogName The catalog term, as per {@link DatabaseMetaDataStub#getCatalogTerm}
	 *	@param pSchemaName The schema name, as per {@link DatabaseMetaDataStub#getSchemaName}
	 */
	public JsonBackedResultSetMetadata(JsonObject pObj, String pCatalogName, String pSchemaName)
	{
		this.gjObj = pObj;
		this.gCatalogName = pCatalogName;
		this.gSchemaName = pSchemaName;
	}

	@Override
	public String getCatalogName(int column)
		throws SQLException
	{
		return(gCatalogName);
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return String Fixed to java.lang.String
	 */
	@Override
	public String getColumnClassName(int column)
	{
		return("java.lang.String");
	}

	@Override
	public int getColumnCount()
		throws SQLException
	{
		return(gjObj.getAsJsonObject("metadata").getAsJsonArray("names").size());
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return int Fixed to 80
	 */
	@Override
	public int getColumnDisplaySize(int column)
	{
		return(80);
	}

	@Override
	public String getColumnLabel(int column)
		throws SQLException
	{
		return(gjObj.getAsJsonObject("metadata").getAsJsonArray("names").get(column-1).getAsString());
	}

	@Override
	public String getColumnName(int column)
		throws SQLException
	{
		return(gjObj.getAsJsonObject("metadata").getAsJsonArray("names").get(column-1).getAsString());
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return int Fixed to java.sql.Types.VARCHAR
	 */
	@Override
	public int getColumnType(int column)
		throws SQLException
	{
		return(java.sql.Types.VARCHAR);
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return String Fixed to VARCHAR
	 */
	@Override
	public String getColumnTypeName(int column)
		throws SQLException
	{
		return("VARCHAR");
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return int Fixed to 80
	 */
	@Override
	public int getPrecision(int column)
	{
		return(80);
	}

	/**
	 *	In this implementation, every column is a VARCHAR(80)
	 *	@return int Fixed to 0
	 */
	@Override
	public int getScale(int column)
	{
		return(0);
	}

	@Override
	public String getSchemaName(int column)
		throws SQLException
	{
		return(gSchemaName);
	}

	@Override
	public String getTableName(int column)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isAutoIncrement(int column)
	{
		return(false);
	}

	@Override
	public boolean isCaseSensitive(int column)
	{
		return(false);
	}

	@Override
	public boolean isCurrency(int column)
	{
		return(false);
	}

	@Override
	public boolean isDefinitelyWritable(int column)
	{
		return(false);
	}

	@Override
	public int isNullable(int column)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isReadOnly(int column)
	{
		return(true);
	}

	@Override
	public boolean isSearchable(int column)
	{
		return(false);
	}

	@Override
	public boolean isSigned(int column)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isWritable(int column)
	{
		return(false);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface)
	{
		return(false);
	}

	@Override
	public <T> T unwrap(Class<T> iface)
      throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}
}

