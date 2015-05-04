package com.betaglide.betaglide_android_wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.betaglide.betaglide_android.BGConfig;
import com.betaglide.betaglide_android.BGUtils;
import com.betaglide.betaglide_android.BetaglideAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GCMHelper {
	private static final String TAG = "BG GCMHelper";

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public static boolean checkPlayServices(Activity ctx) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(ctx);
		if (resultCode != ConnectionResult.SUCCESS) {
			
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 * @throws Exception
	 */
	public static String getRegistrationId(Context context) throws Exception {

		
		String registrationId = BetaglideAPI.getSharedPreferences(context)
				.getString(BGConfig.GCM_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.

		int registeredVersion = BetaglideAPI.getSharedPreferences(context)
				.getInt(BGConfig.APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 * @throws Exception
	 */
	public static void storeRegistrationId(Context context, String regId)
			throws Exception {

		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		BetaGlideAPIWrapper.getInstance();
		SharedPreferences.Editor editor = BetaglideAPI.getSharedPreferences(
				context).edit();
		editor.putString(BGConfig.GCM_REG_ID, regId);
		editor.putInt(BGConfig.APP_VERSION, appVersion);
		editor.commit();
	}
}
