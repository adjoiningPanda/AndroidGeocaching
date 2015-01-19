package com.example.geocaching;

/*
 * This activity will list the names of the fist 10 caches found
 * and will have expandable details available giving the user the option
 * to navigate to the cache via directions or compass
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import net.acira.gcutils.Geocache;
import net.acira.gcutils.dataproviders.geocaching.GeocachingCredentials;
import net.acira.gcutils.dataproviders.geocaching.GeocachingProvider;
import net.acira.gcutils.dataproviders.geocaching.SearchResult;
import net.acira.gcutils.http.ApacheHttpClient;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CacheList extends ILocation {

	private Location locationGlobal;
	ArrayList<Geocache> geocacheSpecifics;
	public Semaphore mutex;
	Handler handler;
	HelperFunctions helperFunctions;
	TextView detailTextResults;
	ExpandableListView expListView;
	private Geocache selectedGeocache;
	private ExpandableListAdapter ad;
	GPSLocation gpsLocation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 * Initializes the necessary variables used in this Activity, GPS location
	 * is obtained, and the network thread starts to obtain the caches in the
	 * area
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cache_list);

		if (savedInstanceState == null) {

		}

		geocacheSpecifics = new ArrayList<Geocache>();
		mutex = new Semaphore(1);

		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		detailTextResults = (TextView) findViewById(R.id.detailTestResults);
		detailTextResults.setText("");
		expListView = (ExpandableListView) findViewById(R.id.lvExp);

		defineHandler();

		gpsLocation = new GPSLocation(this);
		helperFunctions = new HelperFunctions(this);
		gpsLocation.obtainLocation();

		Thread serverThread = initServerThread();
		serverThread.start();

	}

	/*
	 * Returns the handler for public use
	 */

	public Handler getHandler() {
		return handler;
	}

	/*
	 * Returns the instance of the HelperFunction class
	 */
	public HelperFunctions getHelperFunction() {
		return helperFunctions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 * 
	 * Turns GPS off since app will be closing
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		gpsLocation.turnGPSOff();
	}

	/*
	 * Defines and starts the network thread. All caches are obtained in this
	 * thread.
	 */
	private Thread initServerThread() {
		Thread initServerThread = new Thread(new CustomRunnable(this) {
			public void run() {

				getHelperFunction().sendMessageToHandler("Running tests...");
				try {
					runTests(this.getParentContext());
					getHelperFunction().sendMessageToHandler("PASSED");
				} catch (Exception e) {
					e.printStackTrace();
					getHelperFunction().sendMessageToHandler("FAILED");

				}

				GeocachingCredentials credentials = new GeocachingCredentials(
						"fzayek", "password");
				GeocachingProvider gc = new GeocachingProvider(
						new ApacheHttpClient());

				getHelperFunction().sendMessageToHandler("Signing in...");

				gc.authenticate(credentials);

				

				try {
					mutex.acquire();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				SearchResult searchResult = gc.getCachesByCoordinates(""
						+ getLocation().getLatitude(), ""
						+ getLocation().getLongitude(), "5");
				List<String> identifiers = searchResult.getIDs();

				mutex.release();

				int listSize = 10;
				for (int i = 0; i < listSize; i++) {
					String identifier = "";
					try {
						identifier = identifiers.get(i);
					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
						if (i == 0) {
							getHelperFunction()
									.sendToastToHandler(
											"An internet connection is required to run CacheMoney");
							getParentContext().finish();
						}
						break;
					}

					getHelperFunction().sendMessageToHandler(
							"Signed In. Obtaining Cache " + (i+1) + " of 10");
					
					Geocache g = gc.getCacheDetails2(identifier);

					if (g == null || g.displayName == null
							|| g.displayName.equals("")) {
						identifiers.remove(i);
						i--;
						continue;
					}

					try {
						mutex.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					geocacheSpecifics.add(g);
					mutex.release();
				}

				getHelperFunction().sendMessageToHandler("finished");

			}
		});
		return initServerThread;
	}

	/*
	 * An easy method for notifying the user of small messages
	 */
	public void toast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
	}

	/*
	 * The handler is defined here. It is used to write to the UI when the UI
	 * needs to be updated from a separate thread from the UI thread.
	 */
	public void defineHandler() {
		final RelativeLayout rLayout = (RelativeLayout) findViewById(R.id.containerRL);
		final LinearLayout layout = (LinearLayout) findViewById(R.id.container);
		final ProgressBar loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
		final TextView loadingText = (TextView) findViewById(R.id.loadingText);
		final TextView testResults = (TextView) findViewById(R.id.testResults);

		// Define the Handler that receives messages from the thread and update
		// the progress
		handler = new Handler() {

			public void handleMessage(Message msg) {

				String aResponse = "";
				String test = "";
				String intent = "";
				String toast = "";
				intent = msg.getData().getString("intent");
				aResponse = msg.getData().getString("message");
				test = msg.getData().getString("test");
				toast = msg.getData().getString("toast");

				if (toast != null && !toast.equals("")) {
					toast(toast);

				} else if (aResponse != null && !aResponse.equals("finished")
						&& loadingText != null) {
					if (aResponse.equals("PASSED")) {
						testResults.append(Html
								.fromHtml("<font color=#006400>PASSED</font>"));
					} else if (aResponse.equals("FAILED")) {
						testResults.append("FAILED");
					}
					loadingText.setText(aResponse);
				} else if (test != null && !test.equals("")) {
					if (test.startsWith("<font"))
						detailTextResults.append(Html.fromHtml(test));
					else
						detailTextResults.append(test);
				} else if (intent != null && intent.equals("gotit")) {
				} else {

					layout.removeView(testResults);
					layout.removeView(loadingBar);
					layout.removeView(loadingText);
					layout.removeView(detailTextResults);
					rLayout.setBackgroundColor(Color.WHITE);

					listResults();

				}

			}
		};

	}

	/*
	 * Launches Google Maps with the parameters of source coordinates, dest
	 * coordinates, and the walking option
	 */
	public void launchDirections(View v) {
		setSelectedGeocache(geocacheSpecifics.get(ad.getLastExpanded()));

		double sourceLat = getLocation().getLatitude();
		double sourceLong = getLocation().getLongitude();

		double destLat = getSelectedGeocache().latitude;
		double destLong = getSelectedGeocache().longitude;

		String googleMapsIntent = "http://maps.google.com/maps?saddr="
				+ sourceLat + "," + sourceLong + "&daddr=" + destLat + ","
				+ destLong + "&dirflg=w&start=true";

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(googleMapsIntent));
		startActivity(intent);
	}

	/*
	 * Launches the Compass activity
	 */
	public void launchCompass(View v) {
		setSelectedGeocache(geocacheSpecifics.get(ad.getLastExpanded()));
		startSearch();
	}

	/*
	 * Lists the cache results obtained from the network thread. Places results
	 * in an ExpandableListView
	 */
	public void listResults() {
		List<String> displayNames = new ArrayList<String>();

		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < geocacheSpecifics.size(); i++) {
			Geocache g = geocacheSpecifics.get(i);
			displayNames.add(g.displayName);
		}

		HashMap<String, List<String>> listDetails = new HashMap<String, List<String>>();

		for (Geocache g : geocacheSpecifics) {
			float[] results = new float[10];
			Location.distanceBetween(getLocation().getLatitude(), getLocation()
					.getLongitude(), g.latitude, g.longitude, results);

			double finalDistance = Math.round(results[0] * 100.0) / 100.0;

			List details = new ArrayList();
			details.add("Identifier: " + g.identifier);
			details.add("Distance: " + finalDistance + " meters");
			details.add("Difficulty: " + g.difficulty + " / 5");
			details.add("Size: " + g.size);
			details.add("Terrain: " + g.terrain + " / 5");
			details.add("Favorite Count: " + g.favoriteCount);
			details.add("Latitude: " + g.latitude);
			details.add("Longitude: " + g.longitude);
			details.add("Description: " + g.description);

			LinearLayout buttons = new LinearLayout(expListView.getContext());
			details.add("buttons");
			listDetails.put(g.displayName, details);
		}

		mutex.release();

		ad = new ExpandableListAdapter(this, displayNames, listDetails,
				expListView);

		// setting list adapter
		expListView.setAdapter(ad);
	}

	/*
	 * Only one cache can be expanded at a time. If a user chooses to launch
	 * directions or launch compass, then that cache's coordinates will be used
	 */
	public void setSelectedGeocache(Geocache g) {
		selectedGeocache = g;
	}

	/*
	 * Returns the currently expanded cache
	 */
	public Geocache getSelectedGeocache() {
		return selectedGeocache;
	}

	/*
	 * Opens the Compass activity
	 */
	public void startSearch() {
		// TODO Auto-generated method stub
		Intent compassIntent = new Intent(this, Compass.class);
		Bundle extras = new Bundle();
		extras.putDouble("lat", getSelectedGeocache().latitude);
		extras.putDouble("long", getSelectedGeocache().longitude);
		extras.putString("hint", getSelectedGeocache().hint);
		extras.putString("name", getSelectedGeocache().displayName);
		compassIntent.putExtras(extras);
		startActivityForResult(compassIntent, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * 
	 * Overwritten Menu Options function. Not used.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * Overwritten on item selected method. not used.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 * 
	 * If GPS is not enabled, the user will be prompted to enable it.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

		} else if (resultCode == Activity.RESULT_CANCELED) {
			if (!gpsLocation.isGPSOn()) {
				this.finish();

			}
		}

	}

	/*
	 * Runs the tests from the Tests class
	 */
	public void runTests(CacheList context) throws Exception {
		Tests tests = new Tests(this);
		tests.runTests();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.geocaching.ILocation#setLocation(java.lang.Object[])
	 * 
	 * Implemented method of the ILocation.java abstract class. Sets the
	 * Location details
	 */
	@Override
	public void setLocation(Object... locationObjects) {
		// TODO Auto-generated method stub
		locationGlobal = (Location) locationObjects[0];
		mutex.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.geocaching.ILocation#getLocation()
	 * 
	 * Implemented method of the ILocation.java abstract class. Gets the
	 * location
	 */

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return locationGlobal;
	}

	/*
	 * Calculates the density-pixels from an amount of pixels.
	 * Useful when dealing with multiple platforms with different
	 * screen sizes. This function in this class is solely used for testing
	 */
	public double getDPFromPixels(double pixels) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			pixels = pixels * 0.75;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			// pixels = pixels * 1;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			pixels = pixels * 1.5;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			pixels = pixels * 2.0;
			break;
		case DisplayMetrics.DENSITY_XXHIGH:
			pixels = pixels * 2.5;
			break;
		}
		return pixels;
	}

}
