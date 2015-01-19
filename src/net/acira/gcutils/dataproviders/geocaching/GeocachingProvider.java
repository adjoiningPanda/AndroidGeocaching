package net.acira.gcutils.dataproviders.geocaching;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.acira.gcutils.Geocache;
import net.acira.gcutils.Log;
import net.acira.gcutils.dataproviders.DataProvider;
import net.acira.gcutils.http.HttpClient;
import net.acira.gcutils.http.PostParam;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class LogBuilder {

	private static Log parseLog(Element log) {
		Log result = new Log();
		result.username = log.select("strong").select("a").text();
		result.type = log.select("strong").select("img").attr("title");
		log.select("strong").remove();
		log.select("a[title=View log]").remove();
		String[] split = log.text().split("\\)", 2);
		result.text = split[Math.max(0, split.length - 1)];
		return result;
	}

	public static List<Log> convertFrom(Element logsTable) {
		List<Log> result = new ArrayList<Log>();
		Elements logs = logsTable.select("td.Nothing, td.AlternatingRow");
		for (Element log : logs) {
			result.add(parseLog(log));
		}
		return result;
	}
}

class CacheBuilder {

	private static List<String> parseAttributes(Element attributesContainer) {
		List<String> attributeList = new ArrayList<String>();
		Elements attributes = attributesContainer.select("img");
		for (Element attribute : attributes) {
			attributeList.add(attribute.attr("alt"));
		}
		attributeList.removeAll(Collections.singleton("blank"));
		return attributeList;
	}

	public static Geocache convertFrom(String html) {
		Geocache cache = new Geocache();
		/*
		 * Document doc = Jsoup.parse(html); cache.premiumMemberOnly =
		 * doc.select("p.PMCacheInfoSpacing").size() > 0; if
		 * (!(cache.premiumMemberOnly)) { cache.archived =
		 * doc.select("p.Warning").size() > 0; Element cacheDescription =
		 * doc.select("table").first(); // Element propertyContainer = //
		 * cacheDescription
		 * .select("table > tr:eq(0) > td:eq(0) > table").get(2);
		 * cache.identifier = doc.select("span#ctl00_uxWaypointName").text();
		 * cache.displayName = doc.select("span#ctl00_ContentBody_CacheName")
		 * .text(); String location = doc.select("span#uxLatLon").text();
		 * cache.location = location; /*cache.type = cacheDescription
		 * .select("a[href=/about/cache_types.aspx]").select("img")
		 * .attr("alt");
		 */
		/*
		 * cache.difficulty = Math.round((Float.parseFloat(doc
		 * .select("#ctl00_ContentBody_uxLegendScale").select("img")
		 * .attr("alt").split(" ")[0])) * 10); cache.terrain =
		 * Math.round((Float.parseFloat(doc
		 * .select("#ctl00_ContentBody_Localize12").select("img")
		 * .attr("alt").split(" ")[0])) * 10);
		 */
		/*
		 * cache.size = doc.select("span.minorCacheDetails").select("small")
		 * .text().replace("(", "").replace(")", ""); cache.disabled =
		 * doc.select("p.OldWarning").size() > 0; if
		 * (cacheDescription.select("div.CacheDetailNavigationWidget")
		 * .select("strong").isEmpty()) { // Element attributes = //
		 * cacheDescription.select("div.CacheDetailNavigationWidget"); //
		 * cache.attributes = parseAttributes(attributes); cache.attributes =
		 * new ArrayList<String>();
		 * 
		 * } else { cache.attributes = new ArrayList<String>(); } Element
		 * logsTable = doc.select("table.LogsTable").first(); cache.logs =
		 * LogBuilder.convertFrom(logsTable); Element location2 = doc.select(
		 * "span#ctl00_ContentBody_LocationSubPanel > small").first(); if
		 * (location != null) { // cache.location =
		 * location2.text().replace("UTM: ", ""); } }
		 */

		String sPremiumOnly = getMatch(html,
				"cache listing visible to Premium Members only(.)",
				Pattern.UNICODE_CASE | Pattern.DOTALL);
		if (!sPremiumOnly.equals("")) {
			return null;
		}

		// Difficulty
		cache.difficulty = getMatch(html,
				"Difficulty:.*\n.*\n.*?alt=\"(.*) out of", Pattern.UNICODE_CASE
						| Pattern.MULTILINE);

		cache.hint = getMatch(html,
				"<div id=\"div_hint\" class=\"span-8 WrapFix\">(.*?)</div>",
				Pattern.UNICODE_CASE | Pattern.DOTALL);

		// Terrain
		cache.terrain = getMatch(html, "Terrain:.*\n.*\n.*?alt=\"(.*) out of",
				Pattern.UNICODE_CASE | Pattern.MULTILINE);

		String lat = getMatch(html, "mapLatLng = .\"lat\":(.*?),",
				Pattern.UNICODE_CASE | Pattern.MULTILINE);
		if (!lat.equals("")) {
			cache.latitude = Double.parseDouble(lat);
		}

		String longitude = getMatch(html,
				"mapLatLng = .\"lat\".*?,\"lng\":(.*?),", Pattern.UNICODE_CASE
						| Pattern.MULTILINE);
		if (!longitude.equals("")) {
			cache.longitude = Double.parseDouble(longitude);
		}

		cache.displayName = getMatch(html,
				"mapLatLng = .\"lat\".*?,\"name\":\"(.*?)\"",
				Pattern.UNICODE_CASE | Pattern.MULTILINE);

		cache.size = getMatch(html,
				"class=\"minorCacheDetails\".*?alt=\"Size: (.*?)\"",
				Pattern.UNICODE_CASE | Pattern.MULTILINE);

		String favoriteCount = getMatch(html,
				"class=\"favorite-value\".*\n(.*)",
				Pattern.UNICODE_CASE | Pattern.MULTILINE).trim();
		if (!favoriteCount.equals("")) {
			cache.favoriteCount = Integer.parseInt(favoriteCount);
		}

		String descriptionShortBegin = "<span id=\"ctl00_ContentBody_ShortDescription";
		String descriptionShortEnd = "</span>";
		String descriptionLongBegin = "<span id=\"ctl00_ContentBody_LongDescription";
		String descriptionLongEnd = "</span>";
		Document doc1 = null;

		Pattern regexStart = Pattern.compile(descriptionShortBegin,
				Pattern.UNICODE_CASE | Pattern.MULTILINE);
		Matcher regexMatcherStart = regexStart.matcher(html);
		String text = "";
		if (regexMatcherStart.find()) {
			int start = regexMatcherStart.start();
			Pattern regexEnd = Pattern.compile(descriptionShortEnd,
					Pattern.UNICODE_CASE | Pattern.MULTILINE);
			Matcher regexMatcherEnd = regexEnd.matcher(html.substring(start));
			if (regexMatcherEnd.find()) {
				int end = start + regexMatcherEnd.end();
				String shortDescription = html.substring(start, end);
				doc1 = Jsoup.parse(shortDescription);
			}
			text = doc1.select("span#ctl00_ContentBody_ShortDescription")
					.text();
		}

		System.out.print(text);

		Document doc2 = null;

		Pattern regexStart2 = Pattern.compile(descriptionLongBegin,
				Pattern.UNICODE_CASE | Pattern.MULTILINE);
		Matcher regexMatcherStart2 = regexStart2.matcher(html);
		String text2 = "";
		if (regexMatcherStart2.find()) {
			int start = regexMatcherStart2.start();
			Pattern regexEnd2 = Pattern.compile(descriptionLongEnd,
					Pattern.UNICODE_CASE | Pattern.MULTILINE);
			Matcher regexMatcherEnd2 = regexEnd2.matcher(html.substring(start));
			if (regexMatcherEnd2.find()) {
				int end = start + regexMatcherEnd2.end();
				String shortDescription = html.substring(start, end);
				doc2 = Jsoup.parse(shortDescription);
			}
			text2 = doc2.select("span#ctl00_ContentBody_LongDescription")
					.text();
		}

		System.out.print(text2);

		cache.description = text + "\n\n" + text2;

		return cache;
	}

	private static String getMatch(String html, String pattern, int flags) {
		Pattern regex = Pattern.compile(pattern, flags);
		Matcher regexMatcher = regex.matcher(html);
		String matched = "";
		if (regexMatcher.find()) {
			matched = regexMatcher.group(1);
		}
		return matched;
	}

}

public class GeocachingProvider implements DataProvider {

	public static class Urls {

		public static String login = "http://www.geocaching.com/login/default.aspx";
		public static String cacheDetailsByGc = "http://www.geocaching.com/seek/cache_details.aspx?wp=%1$s&decrypt=y";
		public static String cacheDetailsByGuid = "http://www.geocaching.com/seek/cache_details.aspx?guid=%1$s&decrypt=y";
		public static String cachesByUser = "http://www.geocaching.com/seek/nearest.aspx?ul=%1$s";
		public static String cachesByCoordinates = "http://www.geocaching.com/seek/nearest.aspx?origin_lat=%1$s&origin_long=%2$s&dist=%3$s&submit3=Search";

	}

	private HttpClient client;
	private Boolean isAuthenticated = false;
	private String username;

	public GeocachingProvider(HttpClient client) {
		this.client = client;
	}

	public SearchResult getCachesByUser(String username) {
		SearchResult result = new SearchResult(this.client,
				GeocachingProvider.Urls.cachesByUser, username);
		return result;
	}

	public SearchResult getCachesByCoordinates(String latitude,
			String longitude, String distance) {
		
		SearchResult result = new SearchResult(this.client,
				GeocachingProvider.Urls.cachesByCoordinates, latitude,
				longitude, distance);
		
		return result;
	}

	public Geocache getCacheDetails2(String identifier) {
		/*
		 * if (identifiers.startsWith("{")) { // GUID given String html =
		 * client.getRequest(GeocachingProvider.Urls.cacheDetailsByGuid,
		 * identifiers); this.doAuthenticationCheck(html); return
		 * CacheBuilder.convertFrom(html); } else if
		 * (identifiers.startsWith("GC")) {
		 */
		// GC Code given

		String html = client.getRequest(
				GeocachingProvider.Urls.cacheDetailsByGc, identifier);
		doAuthenticationCheck(html);
		Geocache g = CacheBuilder.convertFrom(html);
		
		if (g == null)
			return null;
		
		g.identifier = identifier;

		return g;
	}

	public String getHTML(String identifier) {
		String httpget = client.getRequest(String.format(
				GeocachingProvider.Urls.cacheDetailsByGc, identifier));
		return httpget;
	}

	private void doAuthenticationCheck(String htmlData) {
		boolean foundMatch = false;
		try {
			Pattern regex = Pattern.compile("You are signed in as.*"
					+ this.username, Pattern.DOTALL | Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE | Pattern.MULTILINE);
			Matcher regexMatcher = regex.matcher(htmlData);
			foundMatch = regexMatcher.find();
		} catch (Exception e) {
			// TODO Syntax error in the regular expression
		}
		this.isAuthenticated = foundMatch;
	}

	private String extractViewState(String htmlData, String suffix) {
		String viewState = null;
		try {
			Pattern regex = Pattern.compile(
					"<input type=\"hidden\" name=\"__VIEWSTATE" + suffix
							+ "\" id=\"__VIEWSTATE" + suffix
							+ "\" value=\"(.*?)\" />", Pattern.DOTALL
							| Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
							| Pattern.MULTILINE);
			Matcher regexMatcher = regex.matcher(htmlData);
			if (regexMatcher.find()) {
				viewState = regexMatcher.group(1);
			}
		} catch (Exception e) {
			return viewState;
		}
		return viewState;
	}

	@Override
	public Boolean authenticate(Object credentials) {
		GeocachingCredentials c = (GeocachingCredentials) credentials;
		this.username = c.username;
		String loginPage = client.getRequest(Urls.login);
		String viewState = extractViewState(loginPage, "");
		List<PostParam> postParams = new ArrayList<PostParam>();
		postParams.add(new PostParam("__EVENTARGUMENT", ""));
		postParams.add(new PostParam("__EVENTTARGET", ""));
		postParams.add(new PostParam("__VIEWSTATE", viewState));
		postParams.add(new PostParam("ctl00$ContentBody$btnSignIn", "true"));
		postParams.add(new PostParam("ctl00$ContentBody$cookie", "on"));
		postParams
				.add(new PostParam("ctl00$ContentBody$tbPassword", c.password));
		postParams
				.add(new PostParam("ctl00$ContentBody$tbUsername", c.username));
		String response = client.postRequest(Urls.login, postParams);
		doAuthenticationCheck(response);
		return isAuthenticated;
	}

	@Override
	public Boolean isAuthenticated() {
		return isAuthenticated;
	}

	@Override
	public Boolean revokeAuthentication() {
		// TODO
		return false;
	}

	@Override
	public Geocache getCacheDetails(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Geocache> getCacheDetails(List<String> identifiers) {
		// TODO Auto-generated method stub
		return null;
	}

}
