package net.acira.gcutils.dataproviders.geocaching;

import java.util.Iterator;

import net.acira.gcutils.Geocache;

public class SearchResultIterator implements Iterator<Geocache> {

	private SearchResult list;
	private int n, i;
	
	public SearchResultIterator(SearchResult searchResult) {
		list = searchResult;
	    n = list.size();
	    i = 0;
	}

	public boolean hasNext() {
	    return i < n;
	}

	public Geocache next() {
	    return list.get(i++);
	}

	public void remove() {
       throw new UnsupportedOperationException();
   }
}