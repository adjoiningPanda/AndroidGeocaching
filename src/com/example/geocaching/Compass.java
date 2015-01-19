package com.example.geocaching;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This will open a new activity with a compass. 
 * The centered location is the user's location and
 * the destination will be shown
 */

public class Compass extends ILocation implements SensorEventListener {

	public static int SHOW_CAMERA_METERS = 500;
	public static int PICTURE_RESULT = 0;
	float distance;
	float heading;
	float bearingToDest;
	float accuracy;
	float tempBearing;
	private Location locationGlobal;
	GeomagneticField geoField;
	CompassView cv;
	TextView tv;
	Location l;
	GPSLocation gpsLocation;
	ExpandableListView expListView;
	Bitmap picture;
	Uri pictureUri;
	Menu menu;
	SensorManager mSensorManager;
	Sensor mOrientation;
	private boolean isFlashOn = false;
	private Camera camera;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * GUI variable and other variable initialization
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compass);

		if (savedInstanceState == null) {

		}

		cv = new CompassView(this);
		expListView = (ExpandableListView) findViewById(R.id.lvExp2);
		expListView.setBackgroundColor(Color.TRANSPARENT);

		RelativeLayout ll = (RelativeLayout) findViewById(R.id.compass_layout);
		ll.addView(cv);
		ll.bringChildToFront(expListView);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		gpsLocation = new GPSLocation(this);
		gpsLocation.obtainLocation();

		l = new Location("any string");

		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize((float) getDPFromPixels(10));
		ll.addView(tv);

		createExpandableHint();

	}

	/*
	 * Calculates the density-pixels from an amount of pixels. Useful when
	 * dealing with multiple platforms with different screen sizes
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

	/*
	 * Method that will create a directory if non-existent in Android
	 */
	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e("TravellerLog :: ", "Problem creating Image folder");
				ret = false;
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 * 
	 * The SensorManager listener is registered and more variables are
	 * initialized that are dependent on the sensor
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mOrientation,
				SensorManager.SENSOR_DELAY_NORMAL);

		double lat = getIntent().getExtras().getDouble("lat");
		double longi = getIntent().getExtras().getDouble("long");
		String name = getIntent().getExtras().getString("name");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle(name);
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		}

		l.setLatitude(lat);
		l.setLongitude(longi);
		accuracy = l.getAccuracy();
		tempBearing = locationGlobal.bearingTo(l);
		float distance = locationGlobal.distanceTo(l);

		tv.setText("Distance: " + distance + " m");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent) Obtains azimuth and uses these values to calculate north
	 * and other variables related to the cache in respect to current position
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {

		heading = event.values[0];

		if (geoField != null) {
			heading += geoField.getDeclination();
			heading = -heading;
			tempBearing = locationGlobal.bearingTo(l);
			bearingToDest = (tempBearing + heading);

			float distance = locationGlobal.distanceTo(l);
			double finalDistance = Math.round(distance * 100.0) / 100.0;
			this.distance = (float) finalDistance;
			tv.setText("Distance: " + finalDistance + " m");

			if (menu != null) {
				if (distance < SHOW_CAMERA_METERS) {
					MenuItem item = menu.findItem(R.id.menu_item_camera);
					item.setVisible(true);

				} else {
					MenuItem item = menu.findItem(R.id.menu_item_camera);
					item.setVisible(false);
				}
			}
		}

		cv.invalidate();

		float[] mGravity = new float[3];
		float[] mGeomagnetic = new float[3];
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0];
				System.out.println("azimut: " + azimut);
			}

		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	/*
	 * Create expandable hint and set adapter
	 */
	public void createExpandableHint() {
		List<String> hintName = new ArrayList<String>();
		hintName.add("\tHint");

		String hint = getIntent().getExtras().getString("hint").trim();
		List<String> hints = new ArrayList<String>();
		hints.add(hint);
		HashMap<String, List<String>> hintDetails = new HashMap<String, List<String>>();
		hintDetails.put("Hint", hints);

		ExpandableListAdapter ad = new ExpandableListAdapter(this, hintName,
				hintDetails, expListView);

		// setting list adapter
		expListView.setAdapter(ad);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent) If a picture is obtained, the share button
	 * becomes visible.
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			picture = obtainPicture(data);
			MenuItem item = menu.findItem(R.id.menu_item_share);
			item.setVisible(true);
		} else if (resultCode == Activity.RESULT_CANCELED) {

		}

	}

	/*
	 * Stores the picture that was saved on the sdcard into a Bitmap variable to
	 * be used
	 */

	protected Bitmap obtainPicture(Intent data) {
		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(pictureUri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bitmap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu) Menu is
	 * created as well as flash button set to visible
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.action_bar_share_menu, menu);
		MenuItem item = menu.findItem(R.id.menu_item_flash);
		item.setVisible(true);
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * Executes the correct method when respective menu buttons are selected
	 */

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_flash: {
			flash();
			return true;
		}

		case R.id.menu_item_camera: {
			takeAPicture();
			return true;
		}
		case R.id.menu_item_share: {
			shareItem(item);
			return true;
		}

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop() Camera closes when app is not the main
	 * focus
	 */

	@Override
	protected void onStop() {
		super.onStop();
		if (camera != null) {
			camera.release();
		}
	}

	/*
	 * When the flash button is pressed, torch is either turned on or off
	 * depending on the status
	 */
	public void flash() {
		// Retrieve application packages that are currently installed
		// on the device which includes camera, GPS etc.
		PackageManager pm = getPackageManager();
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Log.e("err", "Device has no camera!");
			// Toast a message to let the user know that camera is not
			// installed in the device
			Toast.makeText(getApplicationContext(),
					"Your device doesn't have camera!", Toast.LENGTH_SHORT)
					.show();
			// Return from the method, do nothing after this code block
			return;
		}

		if (isFlashOn) {
			Parameters p = camera.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(p);
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
			isFlashOn = false;
		} else {
			Parameters p = null;
			camera = Camera.open();
			p = camera.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(p);
			camera.startPreview();
			isFlashOn = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause() The SensorManager variable
	 * unregisters to avoid unnecessary calculation. Camera also closes.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		isFlashOn = false;
	}

	/*
	 * Starts the Share intent
	 */
	public void shareItem(MenuItem item) {
		startActivity(Intent.createChooser(share(), "Share via..."));
	}

	/*
	 * Opens the Camera activity. The app waits for its response
	 */
	public void takeAPicture() {
		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/GeocacheMoney/";
		createDirIfNotExists(path);
		pictureUri = Uri.fromFile(new File(path));
		camera.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
		startActivityForResult(camera, PICTURE_RESULT);
	}

	/*
	 * Obtains the already taken picture and returns the share Intent with the
	 * picture set
	 */
	public Intent share() {
		Bitmap icon = picture;
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "geocache.jpg");
		try {
			f.createNewFile();
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		share.putExtra(Intent.EXTRA_STREAM,
				Uri.parse("file:///sdcard/geocache.jpg"));
		return share;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.geocaching.ILocation#setLocation(java.lang.Object[])
	 * Implemented interface method. Sets the location and geomagnetic field
	 */
	@Override
	public void setLocation(Object... locationObjects) {
		// TODO Auto-generated method stub
		this.locationGlobal = (Location) locationObjects[0];
		this.geoField = (GeomagneticField) locationObjects[1];

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.example.geocaching.ILocation#getLocation() Returns the current
	 * location
	 */

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return locationGlobal;
	}

}
