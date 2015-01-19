package net.acira.gcutils.dataproviders.cachewrapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;

import net.acira.gcutils.Geocache;
import net.acira.gcutils.dataproviders.DataProvider;

public class CachewrapperProvider implements DataProvider {

	static class Url {
		public static String cacheDetails = "http://cachewrapper.appspot.com/api/0.1/caches/%1$s";
		public static String cachesByUser = "http://cachewrapper.appspot.com/api/0.1/seek/user?username=%1$s&page=%2$s";
		public static String cachesByOrigin = "http://cachewrapper.appspot.com/api/0.1/seek/origin?latitude=%1$s&longitude=%2$s&distance=%3$s&page=%4$s";
	}
	
	static Gson gson = new Gson();
	
	private String getRequest(String baseUrl, String... params) {
		String response = "";
		String url = String.format(baseUrl, (Object[])params);
		try {
            URL request = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response = response + line;
            }
            reader.close();
        } catch (Exception e) {
            // TODO
        }
        return response;
	}
	
	@Override
	public Boolean isAuthenticated() {
		return false;
	}

	@Override
	public Boolean authenticate(Object credentials) {
		return true;
	}

	@Override
	public Boolean revokeAuthentication() {
		return true;
	}

	@Override
	public Geocache getCacheDetails(String identifier) {
		String json = getRequest(CachewrapperProvider.Url.cacheDetails, identifier);
		return gson.fromJson(json, Geocache.class);
	}
	
	public SearchResultSet getCachesByUser(String username, Integer page) {
		String json = getRequest(CachewrapperProvider.Url.cachesByUser, username, page.toString());
		return gson.fromJson(json, SearchResultSet.class);
	}
	
	public SearchResultSet getCachesByCoordinates(String latitude, String longitude, String distance, Integer page) {
		String json = getRequest(CachewrapperProvider.Url.cachesByOrigin, latitude, longitude, distance, page.toString());
		return gson.fromJson(json, SearchResultSet.class);
	}

	@Override
	public List<Geocache> getCacheDetails(List<String> identifiers) {
		// TODO Auto-generated method stub
		return null;
	}

}
