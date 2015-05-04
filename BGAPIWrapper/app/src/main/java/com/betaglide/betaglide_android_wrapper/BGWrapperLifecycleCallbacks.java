package com.betaglide.betaglide_android_wrapper;

import java.io.IOException;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.betaglide.betaglide_android.BGConfig;
import com.betaglide.betaglide_android.BGUtils;
import com.betaglide.betaglide_android.BetaglideAPI;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class BGWrapperLifecycleCallbacks implements
		Application.ActivityLifecycleCallbacks {
	private static final String TAG = "BGWrapperLifecycleCallbacks";
	private GoogleCloudMessaging gcm;
	private BetaglideAPI mApi;
	private boolean shouldSetupGCM = true;

	public BGWrapperLifecycleCallbacks(BetaglideAPI api) {
		mApi = api;

	}

	public void dispatchTouchEvent(Activity activity, MotionEvent event) {

	}

	@Override
	public void onActivityCreated(final Activity act, Bundle arg1) {
		try {
			if (shouldSetupGCM) {
				setupGCM(act);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityDestroyed(Activity act) {

	}

	@Override
	public void onActivityPaused(Activity arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityResumed(Activity arg0) {
		// mCurrentActivity=arg0;

	}

	@Override
	public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityStarted(final Activity act) {

	}

	@Override
	public void onActivityStopped(Activity act) {

	}

	public void setupGCM(Activity ctx) throws Exception {

		if (GCMHelper.checkPlayServices(ctx)) {
			gcm = GoogleCloudMessaging.getInstance(ctx);

			if (GCMHelper.getRegistrationId(ctx.getApplicationContext())
					.isEmpty() && BetaglideAPI.isNetworkConnected(mApi)) {
				new GCMRegistrationTask().execute();

			} else {

				String gcmId = GCMHelper.getRegistrationId(BetaGlideAPIWrapper
						.getInstance().getTargetContext());
				if (gcmId != null && !gcmId.isEmpty()) {
					shouldSetupGCM = false;
				}
				BGUtils.BGLogError(TAG, "Lib GCM TOKEN Found " + gcmId);

			}

		} else {
			// take user to google play store to install google play services

			// throw new Exception("No valid Google Play Services APK found");
			Log.e(TAG, "No valid Google Play Services APK found.");
		}

	}

	private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {
		private String regId = "";

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "New GCM REGISTRATION ID " + result);

			if (result.isEmpty()) {

				Log.e(TAG, "GCM Registration Failed");

				try {
					if (GCMHelper.getRegistrationId(mApi.getTargetContext())
							.isEmpty() && BetaglideAPI.isNetworkConnected(mApi)) {
						Log.e(TAG, "Retrying GCM Registration ");
						new GCMRegistrationTask().execute();

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// Persist the regID - no need to register again.
				try {
					GCMHelper.storeRegistrationId(mApi.getTargetContext(),
							result);
					mApi.getUser().addDataField(
							BGConfig.GCM_REG_ID_KEY_TO_SEND, result);
					if (result != null && !result.isEmpty()) {
						shouldSetupGCM = false;
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		@Override
		protected String doInBackground(Void... params) {

			try {

				if (gcm == null) {
					try {
						gcm = GoogleCloudMessaging.getInstance(mApi
								.getTargetContext());
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
				regId = gcm.register(BGConfig.GCM_SENDER_ID);

			} catch (IOException ex) {
				ex.printStackTrace();
				BGUtils.BGLogDebug(TAG, ex.getMessage());
				// If there is an error, don't just keep trying to register.
				// Require the user to click a button again, or perform
				// exponential back-off.
			}
			return regId;
		}
	}
}
