// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity.jdbcstub;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Extend this class to create your own custom Connection class.<br>
 * You will need to write a constructor which connects to the source in any way that's appropriate.
 */
public abstract class ConnectionStub
	implements java.sql.Connection
{
	/**
	 *	Override this method to define your custom logic which returns a {@link JsonBackedStatement} for the supplied SQL.
	 *	This is where you will need to write most of your custom code.
	 */
	public abstract PreparedStatement prepareStatementImpl(String sql)
		throws SQLException;

	/**
	 *	Override this method with your custom logic to return a {@link DatabaseMetaData} for your driver
	 */
	public abstract DatabaseMetaData getMetaData()
		throws SQLException;

	/**
	 *	Override this method to return a schema name appropriate for your driver
	 */
	public abstract String getSchema()
		throws SQLException;

	/**
	 *	Override this method to return a catalog name appropriate for your driver
	 */
	public abstract String getCatalog()
		throws SQLException;

	@Override
	public void abort(Executor executor)
	{
	}

	@Override
	public void clearWarnings()
	{
	}

	@Override
	public void close()
	{
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method does nothing
	 */
	@Override
	public void commit()
	{
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Blob createBlob()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Clob createClob()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public NClob createNClob()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public SQLXML createSQLXML()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Statement createStatement()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean getAutoCommit()
	{
		return(true);
	}

	@Override
	public Properties getClientInfo()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getClientInfo(String name)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getHoldability()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getNetworkTimeout()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getTransactionIsolation()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Map<String,Class<?>> getTypeMap()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public SQLWarning getWarnings()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isClosed()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isReadOnly()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean isValid(int timeout)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String nativeSQL(String sql)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public CallableStatement prepareCall(String sql)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	/**
	 *	This method uses your custom {@link #prepareStatementImpl} method
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
		throws SQLException
	{
		return(prepareStatementImpl(sql));
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public void rollback()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public void rollback(Savepoint savepoint)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public void setAutoCommit(boolean autoCommit)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public void setCatalog(String catalog)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public void setClientInfo(Properties properties)
		throws SQLClientInfoException
	{
	}

	@Override
	public void setClientInfo(String name, String value)
		throws SQLClientInfoException
	{
	}

	@Override
	public void setHoldability(int holdability)
		throws SQLException
	{
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public void setReadOnly(boolean readOnly)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public Savepoint setSavepoint()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public Savepoint setSavepoint(String name)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public void setSchema(String schema)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	In this implementation TCL statements are meaningless, so this method throws an exception
	 */
	@Override
	public void setTransactionIsolation(int level)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public void setTypeMap(Map<String,Class<?>> map)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
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

