// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity.jdbcstub;

import java.sql.*;
import java.util.*;

import com.google.gson.*;

/**
 *	Extend this class to create your own custom DatabaseMetaData class.<br>
 *	The main customizing work is deciding what tables and columns are available, by writing the {@link #getTableNames} and {@link #getColumnNamesForTable} methods.
 */
public abstract class DatabaseMetaDataStub
	implements java.sql.DatabaseMetaData
{
	/**
	 *	Override to return your table names
	 *	@return String[] An array of String table names
	 */
	public abstract String[] getTableNames();

	/**
	 *	Override this method to return the column names that are expected for the supplied table
	 *	@param pTableName The table name
	 *	@return String[] An array of String column names
	 *	@throws SQLException If the supplied table name is not known
	 */
	public abstract String[] getColumnNamesForTable(String pTableName)
		throws SQLException;

	/**
	 *	Override to return the name of your JDBC driver
	 */
	public abstract String getDriverName();

	/**
	 *	Override to return the name of your database product
	 */
	public abstract String getDatabaseProductName();

	/**
	 *	Override to return your catalog term
	 */
	public abstract String getCatalogTerm();

	/**
	 *	Override to return your schema name
	 */
	public abstract String getSchemaName();

	/**
	 *	Override to return your major version number
	 */
	public abstract int getDriverMajorVersion();

	/**
	 *	Override to return your minor version number
	 */
	public abstract int getDriverMinorVersion();

	/**
	 *	This convenience method returns a JsonBackedStatement suitable for a COUNT(*) query.<br>
	 *	The output column is named "counter"
	 *	@param pCount The rowcount to return
	 *	@param pCatalogName The catalog term, as per {@link #getCatalogTerm}
	 *	@param pSchemaName The schema name, as per {@link #getSchemaName}
	 *	@return JsonBackedStatement A {@link JsonBackedStatement} which will function as the return of a COUNT(*) query
	 */
	public static JsonBackedStatement getRowcountStatement(int pCount, String pCatalogName, String pSchemaName)
	{
		JsonObject jRet = DatabaseMetaDataStub.getBaseJson();
		jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("counter");
		JsonObject jRow;
		jRet.getAsJsonArray("data").add(jRow = new JsonObject());
		jRow.addProperty("counter", String.format("%d", pCount));
		return(new JsonBackedStatement(jRet, pCatalogName, pSchemaName));
	}

	/**
	 *	This convenience method returns a JsonObject skeleton to which you can add columns and values
	 *	@return JsonObject A skeleton JsonObject for building a query result
	 */
	public static JsonObject getBaseJson()
	{
		JsonObject jRet = new JsonObject();

		JsonObject jMetaData;
		JsonArray jColumnNames, jData;

		jRet.add("metadata", (jMetaData = new JsonObject()));
		jMetaData.add("names", (jColumnNames = new JsonArray()));

		jRet.add("data", (jData = new JsonArray()));

		return(jRet);
	}

	/**
	 *	This implementation returns a list containing just one schema, as defined by your implementation of {@link #getSchemaName}
	 */
	public ResultSet getSchemas()
	{
		JsonObject jRet = DatabaseMetaDataStub.getBaseJson();
		jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("name");

		JsonObject row0;
		jRet.getAsJsonArray("data").add(row0 = new JsonObject());
		row0.addProperty("name", getSchemaName());

		return(new JsonBackedResultSet(jRet));
	}

	/**
	 *	This implementation returns a list containing just one schema, as defined by your implementation of {@link #getSchemaName}
	 *	@param catalog This parameter is ignored
	 *	@param schemaPattern This parameter is ignored
	 */
	public ResultSet getSchemas(String catalog, String schemaPattern)
		throws SQLException
	{
		return(getSchemas());
	}

	/**
	 *	This is a metadata query which returns the columns in a named table.<br>
	 *	Each record includes the following columns:<br>
	 *	<ul>
	 *  <li>TABLE_CAT String => table catalog, set to the output of {@link #getCatalogTerm}</li>
	 *  <li>TABLE_SCHEM String => table schema, set to the output of {@link #getSchemaName}</li>
	 *  <li>TABLE_NAME String => table name</li>
	 *  <li>COLUMN_NAME String => column name</li>
	 *  <li>DATA_TYPE int => SQL type from java.sql.Types, and we are always making a VARCHAR</li>
	 *  <li>TYPE_NAME String => Data source dependent type name, which is always VARCHAR</li>
	 *  <li>COLUMN_SIZE int => column size (fixed at 80)</li>
	 *	</ul>
	 *	There are supposed to be more metadata columns, but we are just ignoring those
	 *	@param catalog This parameter is ignored
	 *	@param schemaPattern This parameter is ignored
	 *	@param tableNamePattern Expected to be one of the tables in {@link #getTableNames}
	 *	@param columnNamePattern This parameter is ignored
	 */
	public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
		throws SQLException
	{
		if(null != tableNamePattern)
		{
			JsonObject jRet = DatabaseMetaDataStub.getBaseJson();

			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_CAT");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_SCHEM");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_NAME");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("COLUMN_NAME");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("DATA_TYPE");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TYPE_NAME");
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("COLUMN_SIZE");

			String[] tabColArr = getColumnNamesForTable(tableNamePattern);

			if(null != tabColArr)
			{
				for(String s: tabColArr)
				{
					JsonObject jrow;
					jRet.getAsJsonArray("data").add(jrow = new JsonObject());
					jrow.addProperty("TABLE_CAT", getCatalogTerm());
					jrow.addProperty("TABLE_SCHEM", getSchemaName());
					jrow.addProperty("TABLE_NAME", tableNamePattern);
					jrow.addProperty("COLUMN_NAME", s);
					jrow.addProperty("DATA_TYPE", java.sql.Types.VARCHAR);
					jrow.addProperty("TYPE_NAME", "VARCHAR");
					jrow.addProperty("COLUMN_SIZE", 80);
				}
				return(new JsonBackedResultSet(jRet));
			}
		}

		throw(new SQLFeatureNotSupportedException());
	}

	/**
	 *	This is a metadata query which returns the table names.<br>
	 *	Each record includes the following columns:<br>
	 *	<ul>
	 *  <li>TABLE_CAT String => table catalog, set to the output of {@link #getCatalogTerm}</li>
	 *  <li>TABLE_SCHEM String => table schema, set to the output of {@link #getSchemaName}</li>
	 *  <li>TABLE_NAME String => table name</li>
	 *	</ul>
	 *	There are supposed to be more metadata columns (including TABLE_TYPE, REMARKS, TYPE_CAT, TYPE_SCHEM, TYPE_NAME, SELF_REFERENCING_COL_NAME and REF_GENERATION), but we are just ignoring those
	 *	@param catalog This parameter is ignored
	 *	@param schemaPattern This parameter is ignored
	 *	@param tableNamePattern Expected to be one of the tables in {@link #getTableNames}
	 *	@param types This parameter is ignored
	 */
	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
		throws SQLException
	{
		JsonObject jRet = DatabaseMetaDataStub.getBaseJson();

		jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_CAT");
		jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_SCHEM");
		jRet.getAsJsonObject("metadata").getAsJsonArray("names").add("TABLE_NAME");

		for(String s : getTableNames())
		{
			JsonObject jrow;

			jRet.getAsJsonArray("data").add(jrow = new JsonObject());
			jrow.addProperty("TABLE_CAT", getCatalogTerm());
			jrow.addProperty("TABLE_SCHEM", getSchemaName());
			jrow.addProperty("TABLE_NAME", s);
		}

		return(new JsonBackedResultSet(jRet));
	}

	// Everywhere else, the answer is either "no" or "don't ask"

	@Override
	public boolean allProceduresAreCallable()
	{
		return(false);
	}

	@Override
	public boolean allTablesAreSelectable()
	{
		return(true);
	}

	@Override
	public boolean autoCommitFailureClosesAllResultSets()
	{
		return(true);
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit()
	{
		return(true);
	}

	@Override
	public boolean dataDefinitionIgnoredInTransactions()
	{
		return(true);
	}

	@Override
	public boolean deletesAreDetected(int type)
	{
		return(true);
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs()
	{
		return(true);
	}

	@Override
	public boolean generatedKeyAlwaysReturned()
	{
		return(false);
	}

	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getCatalogs()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getCatalogSeparator()
	{
		return(".");
	}

	@Override
	public ResultSet getClientInfoProperties()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public Connection getConnection()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getDatabaseMajorVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getDatabaseMinorVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getDatabaseProductVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getDefaultTransactionIsolation()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getDriverVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getExportedKeys(String catalog, String schema, String table)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getExtraNameCharacters()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getIdentifierQuoteString()
		throws SQLException
	{
		return(" ");
	}

	@Override
	public ResultSet getImportedKeys(String catalog, String schema, String table)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getJDBCMajorVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getJDBCMinorVersion()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxBinaryLiteralLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxCatalogNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxCharLiteralLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnsInGroupBy()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnsInIndex()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnsInOrderBy()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnsInSelect()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxColumnsInTable()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxConnections()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxCursorNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxIndexLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public long getMaxLogicalLobSize()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxProcedureNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxRowSize()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxSchemaNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxStatementLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxStatements()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxTableNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxTablesInSelect()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getMaxUserNameLength()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getNumericFunctions()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getProcedureTerm()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getResultSetHoldability()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public RowIdLifetime getRowIdLifetime()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getSchemaTerm()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getSearchStringEscape()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getSQLKeywords()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public int getSQLStateType()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getStringFunctions()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getSystemFunctions()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getTableTypes()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getTimeDateFunctions()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getTypeInfo()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getURL()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public String getUserName()
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public ResultSet getVersionColumns(String catalog, String schema, String table)
		throws SQLException
	{
		throw(new SQLFeatureNotSupportedException());
	}

	@Override
	public boolean insertsAreDetected(int type)
	{
		return(false);
	}

	@Override
	public boolean isCatalogAtStart()
	{
		return(false);
	}

	@Override
	public boolean isReadOnly()
	{
		return(false);
	}

	@Override
	public boolean locatorsUpdateCopy()
	{
		return(false);
	}

	@Override
	public boolean nullPlusNonNullIsNull()
	{
		return(false);
	}

	@Override
	public boolean nullsAreSortedAtEnd()
	{
		return(false);
	}

	@Override
	public boolean nullsAreSortedAtStart()
	{
		return(false);
	}

	@Override
	public boolean nullsAreSortedHigh()
	{
		return(false);
	}

	@Override
	public boolean nullsAreSortedLow()
	{
		return(false);
	}

	@Override
	public boolean othersDeletesAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean othersInsertsAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean othersUpdatesAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean ownDeletesAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean ownInsertsAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean ownUpdatesAreVisible(int type)
	{
		return(false);
	}

	@Override
	public boolean storesLowerCaseIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean storesMixedCaseIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean storesUpperCaseIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean supportsAlterTableWithAddColumn()
	{
		return(false);
	}

	@Override
	public boolean supportsAlterTableWithDropColumn()
	{
		return(false);
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL()
	{
		return(false);
	}

	@Override
	public boolean supportsANSI92FullSQL()
	{
		return(false);
	}

	@Override
	public boolean supportsANSI92IntermediateSQL()
	{
		return(false);
	}

	@Override
	public boolean supportsBatchUpdates()
	{
		return(false);
	}

	@Override
	public boolean supportsCatalogsInDataManipulation()
	{
		return(false);
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls()
	{
		return(false);
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsColumnAliasing()
	{
		return(false);
	}

	@Override
	public boolean supportsConvert()
	{
		return(false);
	}

	@Override
	public boolean supportsConvert(int fromType, int toType)
	{
		return(false);
	}

	@Override
	public boolean supportsCoreSQLGrammar()
	{
		return(false);
	}

	@Override
	public boolean supportsCorrelatedSubqueries()
	{
		return(false);
	}

	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions()
	{
		return(false);
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly()
	{
		return(false);
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames()
	{
		return(false);
	}

	@Override
	public boolean supportsExpressionsInOrderBy()
	{
		return(false);
	}

	@Override
	public boolean supportsExtendedSQLGrammar()
	{
		return(false);
	}

	@Override
	public boolean supportsFullOuterJoins()
	{
		return(false);
	}

	@Override
	public boolean supportsGetGeneratedKeys()
	{
		return(false);
	}

	@Override
	public boolean supportsGroupBy()
	{
		return(false);
	}

	@Override
	public boolean supportsGroupByBeyondSelect()
	{
		return(false);
	}

	@Override
	public boolean supportsGroupByUnrelated()
	{
		return(false);
	}

	@Override
	public boolean supportsIntegrityEnhancementFacility()
	{
		return(false);
	}

	@Override
	public boolean supportsLikeEscapeClause()
	{
		return(false);
	}

	@Override
	public boolean supportsLimitedOuterJoins()
	{
		return(false);
	}

	@Override
	public boolean supportsMinimumSQLGrammar()
	{
		return(false);
	}

	@Override
	public boolean supportsMixedCaseIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers()
	{
		return(false);
	}

	@Override
	public boolean supportsMultipleOpenResults()
	{
		return(false);
	}

	@Override
	public boolean supportsMultipleResultSets()
	{
		return(false);
	}

	@Override
	public boolean supportsMultipleTransactions()
	{
		return(false);
	}

	@Override
	public boolean supportsNamedParameters()
	{
		return(false);
	}

	@Override
	public boolean supportsNonNullableColumns()
	{
		return(false);
	}

	@Override
	public boolean supportsOpenCursorsAcrossCommit()
	{
		return(false);
	}

	@Override
	public boolean supportsOpenCursorsAcrossRollback()
	{
		return(false);
	}

	@Override
	public boolean supportsOpenStatementsAcrossCommit()
	{
		return(false);
	}

	@Override
	public boolean supportsOpenStatementsAcrossRollback()
	{
		return(false);
	}

	@Override
	public boolean supportsOrderByUnrelated()
	{
		return(false);
	}

	@Override
	public boolean supportsOuterJoins()
	{
		return(false);
	}

	@Override
	public boolean supportsPositionedDelete()
	{
		return(false);
	}

	@Override
	public boolean supportsPositionedUpdate()
	{
		return(false);
	}

	@Override
	public boolean supportsRefCursors()
	{
		return(false);
	}

	@Override
	public boolean supportsResultSetConcurrency(int type, int concurrency)
	{
		return(false);
	}

	@Override
	public boolean supportsResultSetHoldability(int holdability)
	{
		return(false);
	}

	@Override
	public boolean supportsResultSetType(int type)
	{
		return(false);
	}

	@Override
	public boolean supportsSavepoints()
	{
		return(false);
	}

	@Override
	public boolean supportsSchemasInDataManipulation()
	{
		return(false);
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsSchemasInProcedureCalls()
	{
		return(false);
	}

	@Override
	public boolean supportsSchemasInTableDefinitions()
	{
		return(false);
	}

	@Override
	public boolean supportsSelectForUpdate()
	{
		return(false);
	}

	@Override
	public boolean supportsStatementPooling()
	{
		return(false);
	}

	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax()
	{
		return(false);
	}

	@Override
	public boolean supportsStoredProcedures()
	{
		return(false);
	}

	@Override
	public boolean supportsSubqueriesInComparisons()
	{
		return(false);
	}

	@Override
	public boolean supportsSubqueriesInExists()
	{
		return(false);
	}

	@Override
	public boolean supportsSubqueriesInIns()
	{
		return(false);
	}

	@Override
	public boolean supportsSubqueriesInQuantifieds()
	{
		return(false);
	}

	@Override
	public boolean supportsTableCorrelationNames()
	{
		return(false);
	}

	@Override
	public boolean supportsTransactionIsolationLevel(int level)
	{
		return(false);
	}

	@Override
	public boolean supportsTransactions()
	{
		return(false);
	}

	@Override
	public boolean supportsUnion()
	{
		return(false);
	}

	@Override
	public boolean supportsUnionAll()
	{
		return(false);
	}

	@Override
	public boolean updatesAreDetected(int type)
	{
		return(false);
	}

	@Override
	public boolean usesLocalFilePerTable()
	{
		return(false);
	}

	@Override
	public boolean usesLocalFiles()
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

