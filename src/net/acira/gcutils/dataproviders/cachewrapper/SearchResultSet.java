package net.acira.gcutils.dataproviders.cachewrapper;

import java.util.ArrayList;
import java.util.List;

import net.acira.gcutils.Geocache;

public class SearchResultSet {
	
	public Integer page;
	public Integer pageCount;
	public Integer resultCount;
	public List<Geocache> results = new ArrayList<Geocache>();
	
}
