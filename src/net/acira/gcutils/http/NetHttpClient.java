package net.acira.gcutils.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class NetHttpClient implements HttpClient {

	@Override
	public String getRequest(String baseUrl, String... params) {
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
	public String postRequest(String baseUrl, List<PostParam> postParams, String... urlParams) {
		String response = "";
		try {
            URL url = new URL(String.format(baseUrl, (Object[])urlParams));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            String raw = "";
            for (NameValuePair<String, String> pair : postParams) {
            	raw = raw + pair.name + "=" + URLEncoder.encode(pair.value, "UTF-8") + "&";
            }
            writer.write(raw.substring(0, raw.length() - 1));
            writer.close();
    
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response = response + line;
                }
                reader.close();
            } else {
                // TODO Server returned HTTP error code.
            }
        } catch (Exception e) {
            // TODO
        }
        return response;
	}

}
