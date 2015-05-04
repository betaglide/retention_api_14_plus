package com.betaglide.betaglide_android_wrapper;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.betaglide.betaglide_android.BGConfig;
import com.betaglide.betaglide_android.BGPushNotificationEvent;
import com.betaglide.betaglide_android.BGUtils;
import com.betaglide.betaglide_android.BetaglideAPI;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class BGGCMBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "BetaGlideBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		String registrationID = intent.getStringExtra("registration_id");
		if (registrationID != null) {
			BGUtils.BGLogDebug(TAG, "Registration ID:" + registrationID);

		} else {
			BGUtils.BGLogDebug(TAG, "Registration ID: null");

			Bundle extras = intent.getExtras();
			GoogleCloudMessaging gcm = null;

			try {
				gcm = GoogleCloudMessaging.getInstance(context);

				// The getMessageType() intent parameter must be the intent you
				// received
				// in your BroadcastReceiver.

				if (gcm != null && extras != null && !extras.isEmpty()) {
					String messageType = gcm.getMessageType(intent);
					String id = BetaglideAPI.getcurrenttimestamp();
					extras.putString(BGConfig.BG_PUSH_NOTIFICATION_ID, id);

					/*
					 * Filter messages based on message type. Since it is likely
					 * that GCM will be extended in the future with new message
					 * types, just ignore any message types you're not
					 * interested in, or that you don't recognize.
					 */

					if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
							.equals(messageType)) {
						BGUtils.BGLogDebug(TAG,
								"Send error: " + extras.toString());
					} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
							.equals(messageType)) {
						BGUtils.BGLogDebug(TAG, "Deleted messages on server: "
								+ extras.toString());

						// If it's a regular GCM message, do some work.
					} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
							.equals(messageType)) {

						if (extras.containsKey("is_bg_uninstall_check_message")) {
							abortBroadcast();

							BGUtils.BGLogDebug(TAG,
									"Received is_bg_uninstall_check_message");
						} else {
							JSONObject json = new JSONObject();
							Set<String> keys = extras.keySet();
							for (String key : keys) {

								try {

									json.put(key, extras.get(key));
								} catch (JSONException e) {
									// Handle exception here
								}
							}
							BGUtils.BGLogDebug(TAG, "push notification data"
									+ json);
							BGPushNotificationEvent event = new BGPushNotificationEvent(
									"pushnotification", json, "home screes",
									context);
							event.save();

							BGUtils.BGLogDebug(TAG,
									"Received MESSAGE_TYPE_MESSAGE: passed to other receiver ");
						}

					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}