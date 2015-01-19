package com.example.geocaching;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.acira.gcutils.Geocache;
import net.acira.gcutils.dataproviders.geocaching.GeocachingCredentials;
import net.acira.gcutils.dataproviders.geocaching.GeocachingProvider;
import net.acira.gcutils.dataproviders.geocaching.SearchResult;
import net.acira.gcutils.http.ApacheHttpClient;
import android.annotation.SuppressLint;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ExpandableListView;

/*
 * Testing
 */
public class Tests {
	
	private CacheList context;
	
	/*
	 * Initialize context and location
	 */
	public Tests(CacheList context){
		this.context = context;
	}
	
	/*
	 * Test if the obtainLocation function is obtaining the correct
	 * location.
	 */
	public void testObtainLocation() throws Exception {
		context.mutex.acquire();
		Location l = context.getLocation();
		context.mutex.release();
		double lowAUlat = 32.5;
		double highAULat = 32.7;
		double lowAUlong = -85.37;
		double highAUlong = -85.57;
		double lat = l.getLatitude();
		double longi = l.getLongitude();

		assertTrue(lowAUlat < lat && lat < highAULat);
		assertTrue(highAUlong < longi && longi < lowAUlong);
	}

	/*
	 * Test to see if we login correctly
	 */
	public void testLogin() throws Exception {
		GeocachingCredentials credentials = new GeocachingCredentials("fzayek",
				"password");
		GeocachingProvider gc = new GeocachingProvider(new ApacheHttpClient());

		assertTrue(gc.authenticate(credentials));
	}

	/*
	 * Tests to see if same geocaches are obtained given the same location
	 */
	@SuppressLint("Assert")
	public void testGeocacheID() throws Exception {
		GeocachingCredentials credentials = new GeocachingCredentials("fzayek",
				"password");
		GeocachingProvider gc = new GeocachingProvider(new ApacheHttpClient());

		assertTrue(gc.authenticate(credentials));

		SearchResult searchResult = gc.getCachesByCoordinates("32.547971",
				"-85.555000", "5");
		List<String> identifiers = searchResult.getIDs();
		List<String> mockIdentifiers = new ArrayList<String>();
		mockIdentifiers.add("GC169G4");
		mockIdentifiers.add("GC52DW1");
		mockIdentifiers.add("GC1J0TA");
		mockIdentifiers.add("GC48BD0");
		mockIdentifiers.add("GC3J5HW");

		assert (identifiers.contains(mockIdentifiers.get(0)));
		assert (identifiers.contains(mockIdentifiers.get(1)));
		assert (identifiers.contains(mockIdentifiers.get(2)));
		assert (identifiers.contains(mockIdentifiers.get(3)));
		assert (identifiers.contains(mockIdentifiers.get(4)));

	}
	
	/*
	 * Tests to see if same details are given when the same Geocache
	 * ID is requested
	 */

	public void testGeocacheDetails() throws Exception {
		GeocachingCredentials credentials = new GeocachingCredentials("fzayek",
				"password");
		GeocachingProvider gc = new GeocachingProvider(new ApacheHttpClient());

		assertTrue(gc.authenticate(credentials));

		Geocache g = gc.getCacheDetails2("GC169G4");

		g.displayName.contains("K4 Kids Kache #1");
		assertTrue(g.difficulty.equals("2"));
		g.terrain.equals("2");
		g.size.equals("small");

	}
	
	/*
	 * Tests the Compass bearing in respect to north is correct for
	 * a hardcoded value
	 */

	public void testCompassBearing() throws Exception {
		GeomagneticField geoField = new GeomagneticField((float) 32.547971,
				(float) -85.555000, 179, (long) (1.41115038 * Math.pow(10, 12)));
		float declination = geoField.getDeclination();

		assertTrue(declination == (float) -3.9226203);
	}

	/*
	 * Tests if the correct values are set after a list is expanded
	 */
	public void testListExpansion() throws Exception {
		ExpandableListView expListView = new ExpandableListView(context);
		
		List<String> names = new ArrayList<String>();
		names.add("cache1");
		names.add("cache2");
		names.add("cache3");
		names.add("cache4");
		names.add("cache5");
		
		List<String> list1 = new ArrayList<String>();
		list1.add("list1");
		
		List<String> list2 = new ArrayList<String>();
		list1.add("list2");
		
		List<String> list3 = new ArrayList<String>();
		list1.add("list3");
		
		List<String> list4 = new ArrayList<String>();
		list1.add("list4");
		
		List<String> list5 = new ArrayList<String>();
		list1.add("list5");

		
		HashMap<String, List<String>> hm = new HashMap<String, List<String>>();
		hm.put(names.get(0), list1);
		hm.put(names.get(1), list2);
		hm.put(names.get(2), list3);
		hm.put(names.get(3), list4);
		hm.put(names.get(4), list5);
		
		ExpandableListAdapter expListAdapter = new ExpandableListAdapter(context, names, hm, expListView);
		
		expListAdapter.onGroupExpanded(0);
		assertTrue(expListAdapter.getLastExpanded() == 0);

	}
	
	/*
	 * Checks if List values are correct given a certain input
	 */
	public void testListView() throws Exception {
		ExpandableListView expListView = new ExpandableListView(context);
		
		List<String> names = new ArrayList<String>();
		names.add("cache1");
		names.add("cache2");
		names.add("cache3");
		names.add("cache4");
		names.add("cache5");
		
		List<String> list1 = new ArrayList<String>();
		list1.add("list1");
		
		List<String> list2 = new ArrayList<String>();
		list1.add("list2");
		
		List<String> list3 = new ArrayList<String>();
		list1.add("list3");
		
		List<String> list4 = new ArrayList<String>();
		list1.add("list4");
		
		List<String> list5 = new ArrayList<String>();
		list1.add("list5");

		
		HashMap<String, List<String>> hm = new HashMap<String, List<String>>();
		hm.put(names.get(0), list1);
		hm.put(names.get(1), list2);
		hm.put(names.get(2), list3);
		hm.put(names.get(3), list4);
		hm.put(names.get(4), list5);
		
		ExpandableListAdapter expListAdapter = new ExpandableListAdapter(context, names, hm, expListView);
		
		String str = (String)expListAdapter.getChild(0, 0);
		assertTrue(str.equals("list1"));

	}


	/*
	 * Tests to see if a path that does not exist make the path
	 */
	public void testPathCreation() throws Exception {
		String path = Environment.getExternalStorageDirectory() + "/GeocacheTesting/";
		File file = new File(Environment.getExternalStorageDirectory(), path);
		file.delete();
		Compass.createDirIfNotExists(path);
		
		assertTrue(file.exists());
	}
	
	/*
	 * Tests to see if a path is not deleted when already exists
	 */
	public void testPathMaintenance() throws Exception {
		String path = Environment.getExternalStorageDirectory() + "/GeocacheTesting/";
		File file = new File(Environment.getExternalStorageDirectory(), path);
		Compass.createDirIfNotExists(path);
		
		assertTrue(file.exists());
		file.delete();
	}
	
	/*
	 * Tests to see if given an amount of pixels, will
	 * this method correctly return the density pixels.
	 * The S3 has a pixel density of 2.0 and that value is used
	 * in conjunction to the regular pixel size.
	 */
	public void testDPtoPixels() throws Exception {
		int pixels = 30;
		double dpPixels = context.getDPFromPixels(pixels);
		double dpPixels2 = 2.0 * pixels;
		
		assertTrue(dpPixels == dpPixels2);
	}
	
	/*
	 * Runs all the tests
	 */
	public void runTests() throws Exception {

		HelperFunctions helperFunctions = new HelperFunctions(context);
		String passed = "<font color=#006400>Passed</font>";
		
		helperFunctions.sendTestMessageToHandler("\nTest Obtain Location: ");
		testObtainLocation();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Login: ");
		testLogin();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Geocache ID: ");
		testGeocacheID();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Geocache Details: ");
		testGeocacheDetails();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Compass Bearing: ");
		testCompassBearing();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest List Expansion: ");
		testListExpansion();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest List View: ");
		testListView();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Path Creation: ");
		testPathCreation();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Path Maintenance: ");
		testPathMaintenance();
		helperFunctions.sendTestMessageToHandler(passed);
		
		helperFunctions.sendTestMessageToHandler("\nTest Density-Pixels from Pixels: ");
		testDPtoPixels();
		helperFunctions.sendTestMessageToHandler(passed);
		
		Thread.sleep(1000);

	}

	public void assertTrue(boolean bool) throws Exception {
		if (!bool)
			throw new Exception("Test Failed");
	}


}
