package net.acira.gcutils.dataproviders.geocaching;

import java.util.ListIterator;

import net.acira.gcutils.Geocache;

public class SearchResultListIterator implements ListIterator<Geocache> {

	private SearchResult list;
	private int n, i;
	
	public SearchResultListIterator(SearchResult searchResult) {
		list = searchResult;
		n = searchResult.size();
		i = 0;
	}
	
	public SearchResultListIterator(SearchResult searchResult, int index) {
		list = searchResult;
		n = searchResult.size();
		i = index;
	}
	
	@Override
	public void add(Geocache arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return i < n - 1;
	}

	@Override
	public boolean hasPrevious() {
		return i > 0;
	}

	@Override
	public Geocache next() {
		return list.get(i++);
	}

	@Override
	public int nextIndex() {
		return i;
	}

	@Override
	public Geocache previous() {
		return list.get(--i);
	}

	@Override
	public int previousIndex() {
		return i - 1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Geocache arg0) {
		throw new UnsupportedOperationException();
	}

}