package net.acira.gcutils;

import java.util.List;

public class Geocache {

	/**
	 * The title of the cache
	 */
	public String displayName;

	/**
	 * The GC/OC Code or similar
	 */
	public String identifier;

	/**
	 * A list of the cache attributes
	 */
	public List<String> attributes;

	/**
	 * A list of the log entries belonging to the cache
	 */
	public List<Log> logs;

	/**
	 * Indicates the type of the cache (Traditional, Mystery, etc)
	 */
	public String type;

	/**
	 * The difficulty rating of the cache
	 */
	public String difficulty;

	/**
	 * The terrain rating of the cache
	 */
	public String terrain;

	/**
	 * Indicates the size of the cache container
	 */
	public String size;

	/**
	 * Indicates weather a cache has been archived or not
	 */
	public boolean archived;

	/**
	 * Indicates weather the cache is temporary disabled
	 */
	public boolean disabled;

	/**
	 * The number of times this cache has been marked as favorite
	 */
	public int favoriteCount;
	
	/**
	 * Indicates whether the cache is available to premium members only
	 */
	public boolean premiumMemberOnly;
	
	/**
	 * The the UTM location where this cache can be found (as WGS84-Datum)
	 */
	public String location;
	
	/**
	 * Indicates whether the cache was already found by the user
	 */
	public boolean alreadyFound;
	
	public double latitude;
	
	public double longitude;
	
	public String url;
	
	public String description;
	
	public String hint;
	
	
	
}