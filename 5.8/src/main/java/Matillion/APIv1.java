// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import com.google.gson.*;

import com.thereisnogravity.jdbcstub.*;

public class APIv1
{
	private final static String AUTH_ERROR_MSG = "Invalid username or password, or not privileged to use the API";

	// Return a PreparedStatement which contains all the Groups
	protected static JsonBackedStatement getGroupQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		// Add the column name(s) from the metadata
		for(String s: METLMetaData.METL_COLNAMES.get("group"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jRaw;
		JsonObject jRow;

		jRaw = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jRaw.iterator(); ji.hasNext(); )
		{
			jRet.getAsJsonArray("data").add(jRow = new JsonObject());
			jRow.addProperty("groupname", ji.next().getAsString());

			if(0 == --iRowstop)
			{
				break rowstop;
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getGroups(String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing groups";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword, "/group");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Projects
	protected static JsonBackedStatement getProjectQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("project"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects;
		JsonObject jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();

				jRet.getAsJsonArray("data").add(jRow = new JsonObject());
				jRow.addProperty("groupname", sGroupName);
				jRow.addProperty("projectname", sProjectName);

				if(0 == --iRowstop)
				{
					break rowstop;
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getProjects(String pGroupName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing projects";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project",
						makeUrlSafe(pGroupName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Schedules
	protected static JsonBackedStatement getScheduleQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("schedule"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects, jSchedules;
		JsonObject jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();

				jSchedules = new JsonParser().parse(APIv1.getSchedules(sGroupName, sProjectName, sUser, sPassword)).getAsJsonArray();
				for(Iterator<JsonElement> js = jSchedules.iterator(); js.hasNext(); )
				{
					String sScheduleName = js.next().getAsString();

					jRet.getAsJsonArray("data").add(jRow = new JsonObject());
					jRow.addProperty("groupname", sGroupName);
					jRow.addProperty("projectname", sProjectName);
					jRow.addProperty("schedulename", sScheduleName);

					if(0 == --iRowstop)
					{
						break rowstop;
					}
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project/name/Demo/schedule
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getSchedules(String pGroupName, String pProjectName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing schedules";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project/name/%s/schedule",
						makeUrlSafe(pGroupName),
						makeUrlSafe(pProjectName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Environments
	protected static JsonBackedStatement getEnvironmentQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("environment"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects, jEnvironments;
		JsonObject jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();

				jEnvironments = new JsonParser().parse(APIv1.getEnvironments(sGroupName, sProjectName, sUser, sPassword)).getAsJsonArray();
				for(Iterator<JsonElement> je = jEnvironments.iterator(); je.hasNext(); )
				{
					String sEnvironmentName = je.next().getAsString();

					jRet.getAsJsonArray("data").add(jRow = new JsonObject());
					jRow.addProperty("groupname", sGroupName);
					jRow.addProperty("projectname", sProjectName);
					jRow.addProperty("environmentname", sEnvironmentName);

					if(0 == --iRowstop)
					{
						break rowstop;
					}
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project/name/Demo/environment
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getEnvironments(String pGroupName, String pProjectName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing environments";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project/name/%s/environment",
						makeUrlSafe(pGroupName),
						makeUrlSafe(pProjectName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Versions
	protected static JsonBackedStatement getVersionQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("version"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects, jVersions;
		JsonObject jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();

				jVersions = new JsonParser().parse(APIv1.getVersions(sGroupName, sProjectName, sUser, sPassword)).getAsJsonArray();
				for(Iterator<JsonElement> je = jVersions.iterator(); je.hasNext(); )
				{
					String sVersionName = je.next().getAsString();

					jRet.getAsJsonArray("data").add(jRow = new JsonObject());
					jRow.addProperty("groupname", sGroupName);
					jRow.addProperty("projectname", sProjectName);
					jRow.addProperty("versionname", sVersionName);

					if(0 == --iRowstop)
					{
						break rowstop;
					}
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project/name/Demo/version
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getVersions(String pGroupName, String pProjectName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing versions";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project/name/%s/version",
						makeUrlSafe(pGroupName),
						makeUrlSafe(pProjectName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Job Names on the instance
	protected static JsonBackedStatement getAllJobNamesQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("job"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects, jVersions, jJobs;
		JsonObject jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();

				jVersions = new JsonParser().parse(APIv1.getVersions(sGroupName, sProjectName, sUser, sPassword)).getAsJsonArray();
				for(Iterator<JsonElement> je = jVersions.iterator(); je.hasNext(); )
				{
					String sVersionName = je.next().getAsString();

					jJobs = new JsonParser().parse(APIv1.getJobs(sGroupName, sProjectName, sVersionName, sUser, sPassword)).getAsJsonArray();
					for(Iterator<JsonElement> jj = jJobs.iterator(); jj.hasNext(); )
					{
						String sJobName = jj.next().getAsString();

						jRet.getAsJsonArray("data").add(jRow = new JsonObject());
						jRow.addProperty("groupname", sGroupName);
						jRow.addProperty("projectname", sProjectName);
						jRow.addProperty("versionname", sVersionName);
						jRow.addProperty("jobname", sJobName);

						if(0 == --iRowstop)
						{
							break rowstop;
						}
					}
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project/name/Demo/version/name/default/job
	// Returns a JSON Array, e.g. [ "Blah", "Blah Blah" ]
	private static String getJobs(String pGroupName, String pProjectName, String pVersionName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing jobs";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project/name/%s/version/name/%s/job",
						makeUrlSafe(pGroupName),
						makeUrlSafe(pProjectName),
						makeUrlSafe(pVersionName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(u.getResponse());
	}

	// Return a PreparedStatement which contains all the Running Jobs
	protected static JsonBackedStatement getRunningJobQueryStatement(String sUser, String sPassword, boolean pIsCount, Integer pRowLimit)
		throws Exception
	{
		int iRowstop = null == pRowLimit ? -1 : pRowLimit.intValue();

		JsonObject jRet = METLMetaData.getBaseJson();
		for(String s: METLMetaData.METL_COLNAMES.get("runningjob"))
		{
			jRet.getAsJsonObject("metadata").getAsJsonArray("names").add(s);
		}

		JsonArray jGroups, jProjects, jRunningJobs;
		JsonObject jJob, jRow;

		jGroups = new JsonParser().parse(APIv1.getGroups(sUser, sPassword)).getAsJsonArray();

		rowstop:
		for(Iterator<JsonElement> ji = jGroups.iterator(); ji.hasNext(); )
		{
			String sGroupName = ji.next().getAsString();

			jProjects = new JsonParser().parse(APIv1.getProjects(sGroupName, sUser, sPassword)).getAsJsonArray();
			for(Iterator<JsonElement> jp = jProjects.iterator(); jp.hasNext(); )
			{
				String sProjectName = jp.next().getAsString();
				String sJobState;

				jRunningJobs = APIv1.getRunningJobs(sGroupName, sProjectName, sUser, sPassword);

				for(Iterator<JsonElement> jj = jRunningJobs.iterator(); jj.hasNext(); )
				{
					jJob = jj.next().getAsJsonObject();

					jRet.getAsJsonArray("data").add(jRow = new JsonObject());
					jRow.addProperty("groupname", sGroupName);
					jRow.addProperty("projectname", sProjectName);
					jRow.addProperty("versionname", jJob.get("versionName").getAsString());
					jRow.addProperty("id", jJob.get("id").getAsString());
					jRow.addProperty("jobname", jJob.get("jobName").getAsString());

					// Queued jobs also show up here
					sJobState = jJob.get("state").getAsString();
					if(sJobState.equals("RUNNING"))
					{
						java.util.Date d = new java.util.Date(jJob.get("startTime").getAsLong());
						LocalDateTime ldt = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						jRow.addProperty("starttime", ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
					}
					else
					{
						jRow.addProperty("starttime", sJobState);
					}

					if(0 == --iRowstop)
					{
						break rowstop;
					}
				}
			}
		}

		if(pIsCount)
		{
			return(METLMetaData.getRowcountStatement(jRet.getAsJsonArray("data").size(), com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
		}

		return(new JsonBackedStatement(jRet, com.thereisnogravity.Driver.CATALOG_TERM, com.thereisnogravity.Driver.METL_SCHEMA_NAME));
	}

	// http://localhost:8080/rest/v1/group/name/Matillion/project/name/Demo/task/running
	// Returns a JSON array of JSON Objects
	private static JsonArray getRunningJobs(String pGroupName, String pProjectName, String sUser, String sPassword)
		throws Exception
	{
		String sErrorText = "Error listing running jobs";
		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword,
					String.format("/group/name/%s/project/name/%s/task/running",
						makeUrlSafe(pGroupName),
						makeUrlSafe(pProjectName)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(sErrorText));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(sErrorText));
		}

		return(new JsonParser().parse(u.getResponse()).getAsJsonArray());
	}

	// http://localhost:8080/rest/v1/userconfig/export
	// This is used during connect()
	protected static void tryLogin(String sUser, String sPassword)
		throws SQLException
	{
		int iRespCode;
		StringBuffer sbData = new StringBuffer();

		HttpHelper u = null;

		try
		{
			u = new HttpHelper(sUser, sPassword, "");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw(new SQLException(AUTH_ERROR_MSG));
		}

		if(200 != u.getCode())
		{
			throw(new SQLException(AUTH_ERROR_MSG));
		}
	}

	// URLEncoder.encode converts to application/x-www-form-urlencoded where a space is a + sign
	// We need a %20 for those
	private static String makeUrlSafe(String sIn)
		throws Exception
	{
		return(URLEncoder.encode(sIn, "UTF-8").replaceAll("\\+", "%20"));
	}

}

