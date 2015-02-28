package com.ruptech.chinatalk.map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;

public class MyLocation implements LocationListener, BDLocationListener {

	public static abstract class LocationResult {
		public abstract void updateLocation(Location location, String city);
	}

	public static void refreshMyLocation(Context context) {
		if (instance == null)
			instance = new MyLocation(context);
		try {
			instance.start();
		} catch (Exception e) {
		}
	}

	public static void stop() {
		if (instance != null) {
			instance.mLocationClient.stop();
		}
	}

	public static int LOCATION_REFRESH_INTERVAL = 15 * 60 * 1000; // 15 mins
	private static final String TAG = Utils.CATEGORY
			+ MyLocation.class.getSimpleName();

	LocationManager mLocationManager;
	private final Context context;
	public static Location recentLocation;

	public static String address;

	public static MyLocation instance;

	public LocationClient mLocationClient;

	public MyLocation(Context context) {
		this.context = context;

		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mLocationClient = new LocationClient(context);
		mLocationClient.registerLocationListener(this);
		initLocation();
	}

	private void getAddress(final Location location) {

		if (location == null) {
			address = null;
		} else {
			AsyncTask<Void, Void, String> mAddressTask;
			mAddressTask = new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					address = Utils.getCity(context, location.getLatitude(),
							location.getLongitude());
					Log.v(TAG, "address:" + address);
					if (context != null)
						CommonUtilities.broadcastAddressUpdate(context);
					return address;
				}

				@Override
				protected void onPostExecute(String result) {

				}

				@Override
				protected void onPreExecute() {
					address = null;
				}
			};
			mAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);
		option.setCoorType("gcj02");
		int span = LOCATION_REFRESH_INTERVAL;
		option.setScanSpan(span);
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "onLocationChanged:");
		if (recentLocation == null) {
			recentLocation = location;
			getAddress(recentLocation);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "onProviderDisabled:" + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, "onProviderEnabled:" + provider);
	}

	@Override
	public void onReceiveLocation(BDLocation bdLocation) {
		Location location = new Location("Baidu");
		location.setLatitude(bdLocation.getLatitude());
		location.setLongitude(bdLocation.getLongitude());
		recentLocation = location;
		String city = bdLocation.getCity();
		if (city != null && city.length() > 0) {
			address = city;
		} else {
			getAddress(recentLocation);
		}

		Log.i(TAG,
				String.format("Baidu location =(%f, %f)",
						recentLocation.getLatitude(),
						recentLocation.getLongitude()));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private void start() {
		if (mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			recentLocation = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER,
					LOCATION_REFRESH_INTERVAL, 0, this);
		}

		mLocationClient.start();

	}
}
