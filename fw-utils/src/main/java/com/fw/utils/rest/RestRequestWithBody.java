/**
 * 
 */
package com.fw.utils.rest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents Rest request method with body
 * @author akiran
 */
public abstract class RestRequestWithBody<T extends RestRequestWithBody<T>> extends RestRequest<T>
{
	private String requestBody;
	
	/**
	 * Object mapper that will be used to convert objects to json
	 */
	private ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * HTTP method represented by this request
	 */
	private String method;
	
	/**
	 * @param uri
	 */
	public RestRequestWithBody(String uri, String method)
	{
		super(uri);
		this.method = method;
	}
	
	/**
	 * Sets the request body.
	 * Note: params and request body can not be used on same request
	 * @param body
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T setBody(String body)
	{
		if(!super.params.isEmpty())
		{
			throw new IllegalStateException("Both params and body can not be set on a single request. Param(s) were already set");
		}
		
		this.requestBody = body;
		return (T)this;
	}

	/**
	 * Converts specified object into json and sets it as request body.
	 * Note: Params and request body can not be used on same request
	 * @param object
	 * @return
	 */
	public T setJsonBody(Object object)
	{
		try
		{
			return setBody(objectMapper.writeValueAsString(object));
		}catch(JsonProcessingException ex)
		{
			throw new IllegalArgumentException("Failed to format specified object as json - " + object, ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.yodlee.common.rest.RestRequest#addParam(java.lang.String, java.lang.String)
	 */
	@Override
	public T addParam(String name, String value)
	{
		if(requestBody != null)
		{
			throw new IllegalStateException("Both params and body can not be set on a single request. Request body was already set");
		}
		
		return super.addParam(name, value);
	}

	/* (non-Javadoc)
	 * @see com.yodlee.common.rest.RestRequest#toHttpRequestBase(java.lang.String)
	 */
	@Override
	public HttpRequestBase toHttpRequestBase(String baseUrl) throws UnsupportedEncodingException
	{
		HttpEntityEnclosingRequestBase postRequest = newRequest(baseUrl);

		//set the content type if specified
		if(super.getContentType() != null)
		{
			postRequest.setHeader(HttpHeaders.CONTENT_TYPE, super.getContentType());
		}

		//if params are present set the request body in the http FORM format
		if(!super.params.isEmpty())
		{
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>(super.params.size());
			
			for(String paramName : super.params.keySet())
			{
				urlParameters.add(new BasicNameValuePair(paramName, super.params.get(paramName)));
			}
			
			postRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
		}
		
		//if no params are present but request body is present, set body as simple string
		if(requestBody != null)
		{
			postRequest.setEntity(new StringEntity(requestBody));
		}
		
		return postRequest;
	}
	
	/**
	 * Factory method. Child classes are expected to override this method and provide new request object.
	 * @param baseUrl
	 * @return
	 */
	protected abstract HttpEntityEnclosingRequestBase newRequest(String baseUrl);
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[");

		builder.append("\n\tRequest Type: ").append(method);
		builder.append("\n\t").append("Resolved URI: ").append(super.getResolvedUri());
		builder.append("\n\t").append("Params: ").append(super.params);
		builder.append("\n\t").append("Body: ").append(requestBody);

		builder.append("\n]");
		return builder.toString();
	}
}
