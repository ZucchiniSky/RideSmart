package com.mediaamigos.ridesmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

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
    public interface ActivityDetectionListener {
        void onActivityDetected();
    }

    public static Subscription detectedActivitySubscription = null;
    public static ActivityDetectionListener listener = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        initializeRideSmart(context);
    }

    public static void initializeRideSmart(final Context context) {
        if (detectedActivitySubscription != null && !detectedActivitySubscription.isUnsubscribed()) {
            return;
        }

        Utility.initializePrefsOnBoot(context);

        final SharedPreferences sharedPrefs = Utility.getSharedPreferences(context);

        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Utility.sendText(context, incomingNumber);
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        // TODO check if app enabled

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        detectedActivitySubscription = locationProvider.getDetectedActivity(1000*60*5) // detectionIntervalMillis
                .subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult detectedActivity) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        if (sharedPrefs.getBoolean(Utility.ENABLED, false) && detectedActivity.getMostProbableActivity().getType() == DetectedActivity.IN_VEHICLE) {
                            // silence phone
                            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                            editor.putBoolean(Utility.SILENCING, true);
                            editor.putInt(Utility.RINGER_STATE, audioManager.getRingerMode());
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                        } else {
                            // unsilence phone
                            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                            audioManager.setRingerMode(sharedPrefs.getInt(Utility.RINGER_STATE, AudioManager.RINGER_MODE_NORMAL));

                            editor.putBoolean(Utility.SILENCING, false);
                        }
                        editor.putLong(Utility.LAST_CHECKED, System.currentTimeMillis());
                        editor.apply();

                        listener.onActivityDetected();
                    }
                });
    }

    public static void setActivityDetectionListener(ActivityDetectionListener detectionListener) {
        listener = detectionListener;
    }
}
