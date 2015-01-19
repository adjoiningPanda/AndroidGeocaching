package net.acira.gcutils.http;

public abstract class HttpCache implements HttpClient {
	
	protected HttpClient client;
	
	public HttpCache(HttpClient client) {
		this.client = client;
	}
		
}
