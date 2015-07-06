/**
 * 
 */
package com.fw.utils.rest;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * A utility over the Apache http client which eases the invocation of Rest APIs
 * @author akiran
 */
public class RestClient
{
	private static Logger logger = LogManager.getLogger(RestClient.class);
	
	static class RestResultHandler implements ResponseHandler<RestResult<String>>
	{
		public RestResult<String> handleResponse(HttpResponse response) throws ClientProtocolException,IOException
		{
			int status = response.getStatusLine().getStatusCode();
			String value = null;
	
			logger.debug("Got response-status as {}", status);
			
			//if status is OK
			if(status >= 200 && status < 300)
			{
				HttpEntity entity = response.getEntity();
				value = entity != null? EntityUtils.toString(entity): null;
				
				logger.debug("Got response body: {}", value);
				
				if(StringUtils.isBlank(value))
				{
					value = null;
				}
			}
			
			return new RestResult<String>(value, status);
		}

	}
	
	/**
	 * Base url of the API server
	 */
	private String baseUrl;

	/**
	 * Apache http client to invoke rest requests
	 */
	CloseableHttpClient httpclient;

	/**
	 * Object mapper that will be used to parse json responses
	 */
	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * A base url of the API server (eg: http://localhost:8080/test), which will get prepended to each request being invoked
	 * @param baseUrl
	 */
	public RestClient(String baseUrl)
	{
		int len = baseUrl.length();

		//remove ending "/" if any
		if(baseUrl.endsWith("/"))
		{
			baseUrl = baseUrl.substring(0, len - 1);
		}

		this.baseUrl = baseUrl;
		httpclient = HttpClientFactory.getInstance().newHttpClient();
	}

	/**
	 * Gets value of objectMapper 
	 * @return the objectMapper
	 */
	public ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	/**
	 * Sets value for objectMapper
	 * @param objectMapper the objectMapper to set
	 */
	public void setObjectMapper(ObjectMapper objectMapper)
	{
		if(objectMapper == null)
		{
			throw new NullPointerException("Object mapper can not be null");
		}
		
		this.objectMapper = objectMapper;
	}

	/**
	 * Gets value of baseUrl 
	 * @return the baseUrl
	 */
	public String getBaseUrl()
	{
		return baseUrl;
	}

	/**
	 * Invokes the provided request. The response obtained should be in json format which
	 * will be converted to "expectedResponseType" instance and will be returned.
	 * @param request Request to be invoked
	 * @param expectedResponseType Expected response type, which will be used to convert json response
	 * @return Response (json) converted object as part of {@link RestResult}
	 */
	public <T> RestResult<T> invokeJsonRequest(RestRequest<?> request, final Class<T> expectedResponseType)
	{
		return invokeJsonRequest(request, TypeFactory.defaultInstance().uncheckedSimpleType(expectedResponseType));
	}

	/**
	 * Invokes the provided request. The response obtained should be in json format which
	 * will be converted to "expectedResponseType" instance and will be returned.
	 * @param request Request to be invoked
	 * @param expectedResponseType Expected response type, which will be used to convert json response
	 * @return Response (json) converted object as part of {@link RestResult}
	 */
	public <T> RestResult<T> invokeJsonRequest(RestRequest<?> request, final JavaType expectedResponseType)
	{
		//set the content type on request
		request.setContentType("application/json");
		
		//invoked the request
		RestResult<String> stringResult = invokeRequest(request);
		T resultValue = null;

		//if response has body
		if(stringResult.getValue() != null)
		{
			try
			{
				logger.debug("Got response as {}", stringResult.getValue());
				//convert the response json into required object
				resultValue = objectMapper.readValue(stringResult.getValue(), expectedResponseType);
			}catch(Exception ex)
			{
				logger.error("An error occurred while parsing json response", ex);
				throw new RestInvocationException("An error occurred while parsing json response", ex);
			}
		}
		
		//return final result
		return new RestResult<T>(resultValue, stringResult.getStatusCode());
	}

	/**
	 * Invokes the specified request and returns the response code and body as result.
	 * @param request Request to be invoked
	 * @return Response body and status as {@link RestResult}
	 */
	public RestResult<String> invokeRequest(RestRequest<?> request)
	{
		try
		{
			logger.trace("Invoking request [Base Url - {}]: {}", baseUrl, request);
			
			//build http client request
			HttpRequestBase convertedRequest = request.toHttpRequestBase(baseUrl);
			
			//invoke the request and capture the response
			RestResult<String> result = httpclient.execute(convertedRequest, new RestResultHandler());

			return result;
		}catch(Exception ex)
		{
			logger.error("An error occurred while invoking request: " + request.getUri(), ex);
			throw new RestInvocationException("An error occurred while invoking request: " + request.getUri(), ex);
		}
	}

	/**
	 * Closes underlying http client
	 */
	public void close()
	{
		try
		{
			httpclient.close();
		}catch(IOException ex)
		{
			throw new IllegalStateException("An error occurred while closing http-client", ex);
		}
	}
}
