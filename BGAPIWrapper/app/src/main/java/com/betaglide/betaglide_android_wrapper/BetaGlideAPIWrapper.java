package com.betaglide.betaglide_android_wrapper;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.betaglide.betaglide_android.APIConfig;
import com.betaglide.betaglide_android.BGConfig;
import com.betaglide.betaglide_android.BGUtils;
import com.betaglide.betaglide_android.BetaglideAPI;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class BetaGlideAPIWrapper {

	private static final String TAG = "BetaGlideAPIWrapper";
	private static GoogleCloudMessaging gcm;
	private static BetaglideAPI mApi;
	public static BetaglideAPI getInstance() throws Exception {

		return BetaglideAPI.getInstance();
	}

	public static void initialize(final Context context, String apikey,
			APIConfig apiConfig) throws Exception {
		Context mContext = context.getApplicationContext();

		WeakReference<Context> r = new WeakReference<Context>(mContext);
		BGUtils.BGLogDebug(TAG, "New Applunge is initializing with api key  "
				+ apikey + " and context " + r.get().toString());

		mApi = BetaglideAPI.initialize(mContext, apikey, apiConfig);

		if (BetaglideAPI.getInstance().getAPIConfig()
				.isUnInstallTrackingEnable()
				&& BetaglideAPI.hasPermission(mContext,
						".permission.C2D_MESSAGE")

				&& BetaglideAPI.hasPermission(mContext,
						"com.google.android.c2dm.permission.RECEIVE")
				&& BetaglideAPI.hasPermission(mContext,
						"android.permission.WAKE_LOCK")

		) {
			try {
			

				Application app = ((Application) getInstance()
						.getTargetContext());
				app.registerActivityLifecycleCallbacks(new BGWrapperLifecycleCallbacks(
						getInstance()));
				BGUtils.BGLogDebug(TAG, "uninstall tracking enabled");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			BGUtils.BGLogDebug(TAG, "uninstall tracking disabled");
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				addAdvertisingId(context);

			}
		}).start();

	}

	private static void addAdvertisingId(Context ctx) {

		Info adInfo = null;
		try {
			adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx);

			final String id = adInfo.getId();
			final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
			Log.d("Advertisement ", "advertisement id" + id
					+ "isLimitAdTrackingEnabled" + isLAT);

			try {
				BetaglideAPI.getInstance().getUser()
						.addDataField(BGConfig.ADVERTISEMENT_ID, id);
				BetaglideAPI
						.getInstance()
						.getUser()
						.addDataField(BGConfig.IS_LIMIT_AD_TRACKING_ENABLE,
								isLAT);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GooglePlayServicesRepairableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void setupGCM(Activity context) throws Exception {

		if (GCMHelper.checkPlayServices(context)) {
			gcm = GoogleCloudMessaging.getInstance(context);

			if (GCMHelper.getRegistrationId(mApi.getTargetContext()).isEmpty()
					&& BetaglideAPI.isNetworkConnected(mApi)) {
				new GCMRegistrationTask().execute();

			} else {

				String gcmId = GCMHelper.getRegistrationId(BetaGlideAPIWrapper
						.getInstance().getTargetContext());
				mApi.getUser().addDataField(BGConfig.GCM_REG_ID_KEY_TO_SEND,
						gcmId);
				BGUtils.BGLogError(TAG, "Lib GCM TOKEN Found " + gcmId);

			}

		} else {
			// take user to google play store to install google play services

			// throw new Exception("No valid Google Play Services APK found");
			Log.e(TAG, "No valid Google Play Services APK found.");
		}

	}

	private static class GCMRegistrationTask extends
			AsyncTask<Void, Void, String> {
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

	public static void addReferrer(String referrerString, Context ctx) {
		BetaglideAPI.addReferrer(referrerString, ctx);
	}

}
