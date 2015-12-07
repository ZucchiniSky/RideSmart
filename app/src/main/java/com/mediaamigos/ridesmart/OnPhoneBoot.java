package com.mediaamigos.ridesmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by nghiavo on 11/1/15.
 */
public class OnPhoneBoot extends BroadcastReceiver {
    public static final String TAG = "OnPhoneBoot";

    public interface ActivityDetectionListener {
        void onActivityDetected();
    }
    public static Subscription detectedActivitySubscription = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("Running on boot", TAG);
        //initializeRideSmart(context);
    }

    public static void initializeRideSmart(final Context context) {
        Log.d("Initializing RideSmart", TAG);
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
                    Utility.sendText(context.getApplicationContext(), incomingNumber);
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context.getApplicationContext());
        detectedActivitySubscription = locationProvider.getDetectedActivity(1000*60*5) // detectionIntervalMillis
                .subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult detectedActivity) {
                        Utility.onActivityCheck(Utility.getSharedPreferences(context.getApplicationContext()).getBoolean(Utility.ENABLED, false) &&
                                detectedActivity.getMostProbableActivity().getType() == DetectedActivity.IN_VEHICLE, context);
                    }
                });
    }
}
