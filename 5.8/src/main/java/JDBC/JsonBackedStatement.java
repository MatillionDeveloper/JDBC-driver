// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity.jdbcstub;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.sql.*;

import com.google.gson.*;

/**
 *	This is a PreparedStatement implementation which is backed by a JSON object.<br>
 */
public class JsonBackedStatement
	implements PreparedStatement
{
	private JsonObject gjObj;
	private String gCatalogName;
	private String gSchemaName;

	/**
	 *	Constructs a new {@link JsonBackedStatement} with the supplied data as JSON
	 *	@param pObj The data
	 *	@param pCatalogName The catalog term, as per {@link DatabaseMetaDataStub#getCatalogTerm}
	 *	@param pSchemaName The schema name, as per {@link DatabaseMetaDataStub#getSchemaName}
	 */
	public JsonBackedStatement(JsonObject pObj, String pCatalogName, String pSchemaName)
	{
		this.gjObj = pObj;
		this.gCatalogName = pCatalogName;
		this.gSchemaName = pSchemaName;
	}

	/**
	 *	This method performs a virtual query execution
	 *	@return ResultSet A {@link JsonBackedResultSet} with the supplied data
	 */
	@Override
	public ResultSet executeQuery()
		throws SQLException
	{
		return(new JsonBackedResultSet(gjObj));
	}

	/**
	 *	This method returns the query metadata
	 *	@return ResultSetMetaData A {@link JsonBackedResultSetMetadata} for the query
	 */
	@Override
	public ResultSetMetaData getMetaData()
		throws SQLException
	{
		return(new JsonBackedResultSetMetadata(gjObj, gCatalogName, gSchemaName));
	}

	// ... otherwise the answer is "no", "don't ask" or "don't do that again"

	@Override
	public void addBatch()
	{
	}

	@Override
	public void clearParameters()
	{
	}

	@Override
	public boolean execute()
		throws SQLException
	{
		return(true);
	}

	@Override
	public long executeLargeUpdate()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public ResultSet executeQuery(String sql)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int executeUpdate()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public ParameterMetaData getParameterMetaData()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setArray(int parameterIndex, Array x)
	{
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x)
	{
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length)
	{
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length)
	{
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x)
	{
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x)
	{
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
	{
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
	{
	}

	@Override
	public void setBlob(int parameterIndex, Blob x)
	{
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream)
	{
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
	{
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x)
	{
	}

	@Override
	public void setByte(int parameterIndex, byte x)
	{
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x)
	{
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader)
	{
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length)
	{
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length)
	{
	}

	@Override
	public void setClob(int parameterIndex, Clob x)
	{
	}

	@Override
	public void setClob(int parameterIndex, Reader reader)
	{
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length)
	{
	}

	@Override
	public void setDate(int parameterIndex, java.sql.Date x)
	{
	}

	@Override
	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
	{
	}

	@Override
	public void setDouble(int parameterIndex, double x)
	{
	}

	@Override
	public void setFloat(int parameterIndex, float x)
	{
	}

	@Override
	public void setInt(int parameterIndex, int x)
	{
	}

	@Override
	public void setLong(int parameterIndex, long x)
	{
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value)
	{
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length)
	{
	}

	@Override
	public void setNClob(int parameterIndex, NClob value)
	{
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader)
	{
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
	{
	}

	@Override
	public void setNString(int parameterIndex, String value)
	{
	}

	@Override
	public void setNull(int parameterIndex, int sqlType)
	{
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
	{
	}

	@Override
	public void setObject(int parameterIndex, Object x)
	{
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
	{
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
	{
	}

	@Override
	public void setObject(int parameterIndex, Object x, SQLType targetSqlType)
	{
	}

	@Override
	public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength)
	{
	}

	@Override
	public void setRef(int parameterIndex, Ref x)
	{
	}

	@Override
	public void setRowId(int parameterIndex, RowId x)
	{
	}

	@Override
	public void setShort(int parameterIndex, short x)
	{
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
	{
	}

	@Override
	public void setString(int parameterIndex, String x)
	{
	}

	@Override
	public void setTime(int parameterIndex, Time x)
	{
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal)
	{
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x)
	{
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
	{
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
	{
	}

	@Override
	public void setURL(int parameterIndex, URL x)
	{
	}

	@Override
	// For Statement
	public boolean isCloseOnCompletion()
		throws SQLException
	{
		return(true);
	}

	@Override
	public void closeOnCompletion()
                throws SQLException
	{
	}

	@Override
	public boolean isPoolable()
            throws SQLException
	{
		return(false);
	}

	@Override
	public void setPoolable(boolean poolable)
          throws SQLException
	{
	}

	@Override
	public boolean isClosed()
          throws SQLException
	{
		return(true);
	}

	@Override
	public int getResultSetHoldability()
                     throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean execute(String sql, String[] columnNames)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public ResultSet getGeneratedKeys()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public boolean getMoreResults(int current)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public Connection getConnection()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int[] executeBatch()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void clearBatch()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void addBatch(String sql)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int getResultSetType()
	{
		return(ResultSet.TYPE_FORWARD_ONLY);
	}

	@Override
	public int getResultSetConcurrency()
	{
		return(ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	public int getFetchSize()
		throws SQLException
	{
		return(1);
	}

	@Override
	public void setFetchSize(int rows)
		throws SQLException
	{
	}

	@Override
	public int getFetchDirection()
	{
		return(ResultSet.FETCH_FORWARD);
	}

	@Override
	public void setFetchDirection(int direction)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public boolean getMoreResults()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int getUpdateCount()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public ResultSet getResultSet()
	{
		return(new JsonBackedResultSet(gjObj));
	}

	@Override
	public boolean execute(String sql)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setCursorName(String name)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void clearWarnings()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public SQLWarning getWarnings()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void cancel()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setQueryTimeout(int seconds)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int getQueryTimeout()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setEscapeProcessing(boolean enable)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setMaxRows(int max)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int getMaxRows()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void setMaxFieldSize(int max)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public int getMaxFieldSize()
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
	}

	@Override
	public void close()
		throws SQLException
	{
	}

	@Override
	public int executeUpdate(String sql)
		throws SQLException
	{
		throw(new SQLException("Unsupported"));
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

