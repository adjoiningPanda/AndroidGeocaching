package net.acira.gcutils.http;

import java.util.List;

public class BasicHttpCache extends HttpCache {

	public BasicHttpCache(HttpClient client) {
		super(client);
	}

	@Override
	public String getRequest(String baseUrl, String... params) {
		return client.getRequest(baseUrl, params);
	}

	@Override
	public String postRequest(String baseUrl, List<PostParam> postParams, String... urlParams) {
		return client.postRequest(baseUrl, postParams, urlParams);
	}

}
