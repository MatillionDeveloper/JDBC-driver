// (c) 2021 Ian Funnell Matillion Ltd
// https://www.matillion.com/
// https://github.com/MatillionDeveloper/JDBC-driver
// This code is licensed under the MIT license (see LICENSE.txt for details)

package com.thereisnogravity;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;

/**
 * HttpHelper makes it simpler to interact with Matillion's REST API, trying HTTPS first, and silently falling back to HTTP if necessary.
 */
public class HttpHelper
{
	private int gRespCode;
	private String gResponse;

	private static Boolean gUseTLS = null;
	private static TrustManager[] gTrustMgr = null;
	private static HostnameVerifier gHostVerif = null;

	private final static String APIHOST = "localhost";
	private final static String TLS_SPEC = "TLSv1.2";
	private final static int TIMEOUT_MS = 1500;

	protected int getCode()
	{
		return(gRespCode);
	}

	protected String getResponse()
	{
		return(gResponse);
	}

	private void initHostnameVerifier()
	{
		gHostVerif = new HostnameVerifier()
		{
			@Override
			public boolean verify(String hostname, SSLSession session)
			{
				return(hostname.equals(APIHOST));
			}
		};
	}

	private void initTrustManager()
	{
		gTrustMgr = new TrustManager[]
		{ 
			new X509TrustManager()
			{     
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{ 
					return new X509Certificate[0];
				}
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
				} 
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
				}
			} 
		};
	}

	private void doGet(String sUser, String sPassword, String pPath, boolean bSecure)
		throws Exception
	{
		URL uIn = new URL(String.format("%s://%s:%d/rest/v1/%s",
			bSecure ? "https" : "http",
			APIHOST,
			bSecure ? 8443 : 8080,
			pPath.startsWith("/") ? pPath.substring(1) : pPath));

		URLConnection ucIn = uIn.openConnection();

		ucIn.setConnectTimeout(TIMEOUT_MS);

		HttpURLConnection hIn = (HttpURLConnection) ucIn;

		hIn.setRequestMethod("GET");
		if(null != sUser && null != sPassword)
		{
			hIn.setRequestProperty("Authorization",
					"Basic "+Base64.getEncoder().encodeToString((sUser+":"+sPassword)
						.getBytes(StandardCharsets.UTF_8)));
		}

		if(hIn instanceof HttpsURLConnection)
		{
			HttpsURLConnection hsIn = (HttpsURLConnection) hIn;

			if(null == gTrustMgr)
			{
				initTrustManager();
			}
			if(null == gHostVerif)
			{
				initHostnameVerifier();
			}

			SSLContext scTrustAll12 = SSLContext.getInstance(TLS_SPEC);
			scTrustAll12.init(null, gTrustMgr, new java.security.SecureRandom());

			hsIn.setSSLSocketFactory(scTrustAll12.getSocketFactory());
			hsIn.setHostnameVerifier(gHostVerif);
		}

		try
		{
			hIn.connect();

			gRespCode = hIn.getResponseCode();

			StringBuffer sbData = new StringBuffer();
			String sLine;
			try(BufferedReader bIn = new BufferedReader(new InputStreamReader(hIn.getInputStream())))
			{
				while(null != (sLine = bIn.readLine()))
				{
					sbData.append(sLine);
				}
			}

			gResponse = sbData.toString();
		}
		finally
		{
			try
			{
				hIn.disconnect();
			}
			catch(Throwable t) {}
			try
			{
				hIn.getInputStream().close();
			}
			catch(Throwable t) {}
			try
			{
				hIn.getErrorStream().close();
			}
			catch(Throwable t) {}
		}
	}

	protected HttpHelper(String sUser, String sPassword, String pPath)
		throws Exception
	{
		gResponse = null;
		gRespCode = 500;

		if(null == gUseTLS)
		{
			try	// Try HTTPS first
			{
				doGet(sUser, sPassword, pPath, true);
				gUseTLS = true;
			}
			catch(SocketTimeoutException sx)
			{
				doGet(sUser, sPassword, pPath, false);
				gUseTLS = false;
			}
		}
		else
		{
			if(gUseTLS.booleanValue())
			{
				doGet(sUser, sPassword, pPath, true);
			}
			else
			{
				doGet(sUser, sPassword, pPath, false);
			}
		}
	}
}

