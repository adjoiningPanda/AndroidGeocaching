package net.acira.gcutils.dataproviders;

import java.util.List;

import net.acira.gcutils.Geocache;

public interface DataProvider {
	
	/**
	 * Returns at any time whether requests have access to private resources
	 * @return The current authentication state
	 */
	public Boolean isAuthenticated();
	
	/**
	 * 
	 * @param credentials
	 * @return
	 */
	public Boolean authenticate(Object credentials);
	
	/**
	 * 
	 * @return
	 */
	public Boolean revokeAuthentication();
	
	public Geocache getCacheDetails(String identifier);

	public List<Geocache> getCacheDetails(List<String> identifiers);
	
}
