package com.example.geocaching;

import android.app.Activity;
import android.location.Location;

/*
 * Activity that defines location methods for CacheList and Compass
 * for a cleaner design
 */
public abstract class ILocation extends Activity {
	
	public abstract void setLocation(Object...locationObjects);
	
	public abstract Location getLocation();

}
