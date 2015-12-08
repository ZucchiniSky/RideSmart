package com.mediaamigos.ridesmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import rx.Subscription;

/**
 * Created by nghiavo on 11/1/15.
 */
public class OnPhoneBoot extends BroadcastReceiver {
    public static final String TAG = "OnPhoneBoot";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("Running on boot", TAG);
        //initializeRideSmart(context);
    }

    public static void initializeRideSmart(final Context context) {
        Log.d("Initializing RideSmart", TAG);
        Subscription detectedActivitySubscription = Utility.serviceConnector.rideSmartService.getSubscription();
        if (detectedActivitySubscription != null && !detectedActivitySubscription.isUnsubscribed()) {
            Log.d("Already initialized", TAG);
            return;
        }

        Utility.initializePrefsOnBoot(context.getApplicationContext());

        TelephonyManager telephony = (TelephonyManager)context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Log.d("Intercepting call", TAG);
                    Utility.sendAutoResponse(context.getApplicationContext(), incomingNumber);
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        Utility.serviceConnector.rideSmartService.startSubscription(context);
    }
}
