package com.example.geocaching;

import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/*
 * This class contains the current GPS Location of the user.
 */
public class GPSLocation {

	private ILocation context;
	private static Location location;
	LocationManager locationManagerGps;
	static GeomagneticField geoField;
	boolean locationObtained = false;

	/*
	 * GPSLocation is set
	 */
	public void setLocation(Location location) {
		GPSLocation.location = location;
	}

	/*
	 * Returns the current location
	 */
	public Location getLocation() {
		return location;
	}

	/*
	 * Set the context
	 */
	public GPSLocation(ILocation context) {
		this.context = context;
	}

	/*
	 * NetworkLocation as well as GPSLocations are registered for updates 
	 * on the user's location. User's location will be updated as soon as one is 
	 * available
	 */
	public void obtainLocation() {
		if (this.context instanceof CacheList) {
			try {
				((CacheList) this.context).mutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		obtainNetworkLocation();
		obtainGPSLocation();
		updateContextLocation();
		if (!isGPSOn() && context instanceof CacheList) {
			context.startActivityForResult(new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
					0);
		} else if (!isGPSOn()) {
			Toast.makeText(
					context,
					"CacheMoney recommends an enabled GPS for a more pleasant geocaching experience",
					Toast.LENGTH_LONG).show();
			;
		}

	}

	/*
	 * Returns the geomagnetic field
	 */
	public GeomagneticField getGeoField() {
		return geoField;
	}
	
	/*
	 * Turns off GPS
	 */

	public void turnGPSOff() {
		if (isGPSOn()) {
			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
			intent.putExtra("enabled", false);
			context.sendBroadcast(intent);
		}
	}

	/*
	 * Simplified method using the Strategy design. Passes location objects
	 * to the contexts and the contexts manage this data themselves
	 */
	public void updateContextLocation() {
		if (getLocation() != null) {
			Object[] itemObjs = { getLocation(), getGeoField() };
			context.setLocation(itemObjs);
		}
	}

	/*
	 * Returns if GPS is on
	 */
	
	public boolean isGPSOn() {
		return locationManagerGps
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/*
	 * Registers a listener for the GPS Location
	 */
	public void obtainGPSLocation() {
		locationManagerGps = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				setLocation(location);
				geoField = new GeomagneticField(Double.valueOf(
						location.getLatitude()).floatValue(), Double.valueOf(
						location.getLongitude()).floatValue(), Double.valueOf(
						location.getAltitude()).floatValue(),
						System.currentTimeMillis());

				updateContextLocation();

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}
		};

		// Register the listener with the Location Manager to receive location
		// updates

		locationManagerGps.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, locationListener);
	}

	/*
	 * Registers a listener for the Network location
	 */
	public void obtainNetworkLocation() {
		LocationManager locationManagerNetwork = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// setLocation(locationManagerNetwork
		// .getLastKnownLocation(locationManagerNetwork.NETWORK_PROVIDER));

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				setLocation(location);
				geoField = new GeomagneticField(Double.valueOf(
						location.getLatitude()).floatValue(), Double.valueOf(
						location.getLongitude()).floatValue(), Double.valueOf(
						location.getAltitude()).floatValue(),
						System.currentTimeMillis());

				updateContextLocation();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManagerNetwork.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

	}

}
