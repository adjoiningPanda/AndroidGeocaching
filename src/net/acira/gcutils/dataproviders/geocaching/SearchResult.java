package net.acira.gcutils.dataproviders.geocaching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.acira.gcutils.Geocache;
import net.acira.gcutils.http.HttpClient;
import net.acira.gcutils.http.PostParam;

class MiniCacheBuilder {

	public String page; 
	private static Pattern gcCodePattern = Pattern.compile("(GC.*?)_",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static String getGcCode(String tdText) {
		String ResultString = null;
		try {
			Matcher regexMatcher = gcCodePattern.matcher(tdText);
			if (regexMatcher.find()) {
				ResultString = regexMatcher.group(1);
			}
		} catch (Exception e) {
			// TODO Syntax error in the regular expression
		}
		return ResultString;
	}

	private static Geocache buildCacheFrom(Element tr) {
		Geocache gc = new Geocache();
		Elements tds = tr.select("td");

		gc.type = tds.get(4).select("img").attr("alt");
		gc.favoriteCount = Integer.parseInt(tds.get(2)
				.select("span.favorite-rank").text());
		gc.identifier = getGcCode(tds.get(4).toString());
		Elements url = tds.get(4).select("a");
		gc.url = getUrl(url.toString());
		gc.displayName = parseName(tds.get(5));
		gc.disabled = tds.get(4).select("a.Strike").size() > 0;
		gc.archived = tds.get(4).select("a.OldWarning").size() > 0;
		gc.premiumMemberOnly = !tds.get(5)
				.select("img[alt=Premium Member Only Cache]").isEmpty();
		gc.alreadyFound = !tds.get(3).select("img[title=Found It!]").isEmpty();
		return gc;
	}

	private static String getUrl(String url) {
		Pattern toMatch = Pattern.compile("\"(.*?)\"", Pattern.UNICODE_CASE);
		Matcher regexMatcher = toMatch.matcher(url);
		String name = "";
		if (regexMatcher.find()) {
			name = regexMatcher.group(1);
		}

		return name;
	}

	private static String parseName(Object toParse) {
		// private static Pattern gcCodePattern =
		// Pattern.compile("\\((GC[a-z0-9]*?)\\)", Pattern.CASE_INSENSITIVE |
		// Pattern.UNICODE_CASE);

		Pattern toMatch = Pattern.compile("<span>(.*?)</span>",
				Pattern.UNICODE_CASE);
		Matcher regexMatcher = toMatch.matcher(toParse.toString());
		String name = "";
		if (regexMatcher.find()) {
			name = regexMatcher.group(1);
		}

		return name;
	}

	public static List<String> getCachesFrom(String html) {
		List<String> result = new ArrayList<String>();
		Pattern regex = Pattern.compile("(GC.*?)_", Pattern.UNICODE_CASE);
		Matcher regexMatcher = regex.matcher(html);

		while (regexMatcher.find()) {
				String key = regexMatcher.group(1);
				if (!result.contains(key))
				{
					result.add(key);
				}
		}
		//Document doc = Jsoup.parse(html);
		//Elements trs = doc.select("table.SearchResultsTable").select("tr.Data");
		// Iterate over the table rows (that are caches)
		//for (Element tr : trs) {
			//result.add(buildCacheFrom(tr));
	//	}
		return result;
	}

}

public class SearchResult extends Object implements List<Geocache> {

	private HttpClient client;
	private String baseUrl;
	private String[] params;
	private Integer cacheCount = 0;
	private String page = "";
	private ArrayList<Geocache> results;

	private String extractViewState(String html) {
		return extractViewState(html, "");
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

	private List<PostParam> buildBasePostParams(String html) {
		List<PostParam> postParams = new ArrayList<PostParam>();
		postParams.add(new PostParam("__EVENTARGUMENT", ""));
		postParams.add(new PostParam("__VIEWSTATE", extractViewState(html)));
		String viewState1 = extractViewState(html, "1");
		if (viewState1 != null) {
			postParams.add(new PostParam("__VIEWSTATE1", viewState1));
			postParams.add(new PostParam("__VIEWSTATEFIELDCOUNT", "2"));
		}
		return postParams;
	}

	private String getPage(Integer pageIndex) {
		String page = client.getRequest(this.baseUrl, this.params);
		if (pageIndex <= 1)
			return page;
		List<PostParam> postParams = buildBasePostParams(page);
		// Make right-jumps (10 pages each) until we are inside the right page
		// range
		int currentPage = 1;
		while (currentPage + 10 < pageIndex) {
			postParams.add(new PostParam("__EVENTTARGET",
					"ctl00$ContentBody$pgrTop$ctl06"));
			page = client.postRequest(baseUrl, postParams, this.params);
			currentPage += 10;
			postParams = buildBasePostParams(page);
		}
		// Return last page, if target page is 11, 21, 31, ...
		if (pageIndex % 10 == 1)
			return page;
		// As soon as we are able to do so, request the target page
		postParams = buildBasePostParams(page);
		postParams.add(new PostParam("__EVENTTARGET",
				"ctl00$ContentBody$pgrBottom$lbGoToPage_" + pageIndex));
		page = client.postRequest(baseUrl, postParams, this.params);
		return page;
	}

	public SearchResult(HttpClient client, String url, String... urlParams) {
		this.client = client;
		this.baseUrl = url;
		this.params = urlParams;
		String page1 = getPage(1);
		this.page = page1;
		try {
			Pattern regex = Pattern
					.compile(
							"<td class=\"PageBuilderWidget\"><span>Total Records: <b>(\\d+)</b>",
							Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			Matcher regexMatcher = regex.matcher(page1);
			if (regexMatcher.find()) {
				cacheCount = Integer.parseInt(regexMatcher.group(1));
			}
		} catch (Exception e) {
			// Syntax error in the regular expression
		}
		this.results = new ArrayList<Geocache>(20);
		for (int i = 0; i < 20; i++) {
			results.add(null);
		}
	}

	@Override
	public boolean add(Geocache e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Geocache element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Geocache> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends Geocache> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public List<String> getIDs() {
		/*if (results.get(index) == null) {
			// Request the page and read out the 20 caches around the requested
			// one
			String page = getPage((int) Math.ceil((index + 1) / 20.0));
			List<String> caches = MiniCacheBuilder.getCachesFrom(page);
			Integer currentIndex = index - (index % 20);
			for (String gcIdentifier: caches) {
				results.set(currentIndex, gcIdentifier);
				currentIndex++;
			}
		}
		return results.get(index);*/
		
		String page = this.page;
		List<String> caches = MiniCacheBuilder.getCachesFrom(page);
		return caches;
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return cacheCount == 0;
	}

	@Override
	public Iterator<Geocache> iterator() {
		return new SearchResultIterator(this);
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Geocache> listIterator() {
		return new SearchResultListIterator(this);
	}

	@Override
	public ListIterator<Geocache> listIterator(int index) {
		return new SearchResultListIterator(this, index);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Geocache remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Geocache set(int index, Geocache element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return cacheCount;
	}

	@Override
	public List<Geocache> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Geocache get(int location) {
		// TODO Auto-generated method stub
		return null;
	}

}
