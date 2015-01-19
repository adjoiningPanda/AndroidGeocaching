package net.acira.gcutils.http;

import java.util.List;

public interface HttpClient {
	
	public String getRequest(String baseUrl, String... params);
	
	public String postRequest(String baseUrl, List<PostParam> postParams, String... urlParams);

}
