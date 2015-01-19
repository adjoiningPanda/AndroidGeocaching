package net.acira.gcutils.http;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class ApacheHttpClient implements HttpClient {

	private org.apache.http.client.HttpClient client = new DefaultHttpClient();

	private List<NameValuePair> postParamsToNameValuePairs(
			List<PostParam> postParams) {
		List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
		for (PostParam postParam : postParams) {
			nameValuePairs.add(new BasicNameValuePair(postParam.name,
					postParam.value));
		}
		return nameValuePairs;
	}

	@Override
	public String getRequest(String baseUrl, String... params) {
		ResponseHandler<String> responseHandler = new BasicResponseHandler();		
		HttpGet httpget = new HttpGet(String.format(baseUrl, (Object[]) params));
		try {
			String responseBody = this.client.execute(httpget, responseHandler);
			return responseBody;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	@Override
	public String postRequest(String baseUrl, List<PostParam> postParams,
			String... urlParams) {

		String responseBody = null;
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpPost httppost = new HttpPost(String.format(baseUrl,
				(Object[]) urlParams));
		UrlEncodedFormEntity entity = null;
		try {

			entity = new UrlEncodedFormEntity(
					postParamsToNameValuePairs(postParams), "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httppost.setEntity(entity);
		try {
			responseBody = this.client.execute(httppost, responseHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseBody;
	}

	
}
