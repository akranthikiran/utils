/**
 * 
 */
package com.fw.utils.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author akiran
 *
 */
public class HttpClientFactory
{
	private static Logger logger = LogManager.getLogger(HttpClientFactory.class);

	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 500;
	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;
	private static final Pattern PROXY_PATTERN = Pattern.compile("([\\w\\.\\-]+)\\:(\\d+)");
	
	public static final String PROP_PROXY = "http.proxy";
	
	
	private static HttpClientFactory instance;

	PoolingHttpClientConnectionManager connectionManager = null;
	ConnectionKeepAliveStrategy keepAliveStrategy;
	HttpHost proxyHost;

	HttpClientFactory()
	{
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
		connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

		keepAliveStrategy = new ConnectionKeepAliveStrategy()
		{
			public long getKeepAliveDuration(HttpResponse response, HttpContext context)
			{
				HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

				while(it.hasNext())
				{
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					
					if(StringUtils.isNotBlank(value) && param.equalsIgnoreCase("timeout"))
					{
						return Long.parseLong(value) * 1000;
					}
				}

				return 5 * 1000;
			}
		};

		String proxy = System.getProperty(PROP_PROXY);

		if(StringUtils.isNotBlank(proxy))
		{
			Matcher matcher = PROXY_PATTERN.matcher(proxy);

			if(!matcher.matches())
			{
				throw new IllegalStateException(String.format("Invalid proxy specified - %s. Expected pattern - proxy-host:port", proxy));
			}

			String host = matcher.group(1);
			Integer port = Integer.parseInt(matcher.group(2));
			proxyHost = new HttpHost(host, port);

			logger.debug("Using http proxy {}:{}", host, port);
		}
	}
	
	/**
	 * @return the instance
	 */
	public static synchronized HttpClientFactory getInstance()
	{
		if(instance == null)
		{
			instance = new HttpClientFactory();
		}
		
		return instance;
	}
	
	HttpClientBuilder newClientBuilder()
	{
		return HttpClients.custom();
	}
	
	DefaultProxyRoutePlanner newDefaultProxyRoutePlanner(HttpHost host)
	{
		return new DefaultProxyRoutePlanner(host);
	}

	public CloseableHttpClient newHttpClient()
	{
		HttpClientBuilder clientBuilder = newClientBuilder();
		clientBuilder.setConnectionManager(connectionManager);
		clientBuilder.setKeepAliveStrategy(keepAliveStrategy);
		clientBuilder.setConnectionManagerShared(true);
		
		if(proxyHost != null)
		{
			clientBuilder.setRoutePlanner(newDefaultProxyRoutePlanner(proxyHost));
		}

		return clientBuilder.build();
	}
	
	public static synchronized void reset()
	{
		if(instance == null)
		{
			return;
		}
		
		instance.connectionManager.close();
		instance = null;
	}
}
