/**
 * 
 */
package com.fw.utils.rest;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;

/**
 * Represents PUT request method
 * @author akiran
 */
public class PutRestRequest extends RestRequestWithBody<PutRestRequest>
{
	/**
	 * @param uri
	 */
	public PutRestRequest(String uri)
	{
		super(uri, "PUT");
	}
	
	/* (non-Javadoc)
	 * @see com.fw.utils.rest.RestRequestWithBody#newRequest(java.lang.String)
	 */
	@Override
	protected HttpEntityEnclosingRequestBase newRequest(String baseUrl) 
	{
		return new HttpPut(baseUrl + getResolvedUri());
	}
}
