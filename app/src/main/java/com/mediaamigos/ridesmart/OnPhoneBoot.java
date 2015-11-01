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

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by nghiavo on 11/1/15.
 */
public class OnPhoneBoot extends BroadcastReceiver {
    public static Subscription detectedActivitySubscription = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
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
        Subscription subscription = locationProvider.getDetectedActivity(1000*60*5) // detectionIntervalMillis
        .subscribe(new Action1<ActivityRecognitionResult>() {
            @Override
            public void call(ActivityRecognitionResult detectedActivity) {
                if (detectedActivity.getMostProbableActivity().getType() == DetectedActivity.IN_VEHICLE) {
                    // silence phone
                    AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(Utility.SILENCING, true);
                    editor.putInt(Utility.RINGER_STATE, audioManager.getRingerMode());
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    editor.commit();

                } else {
                    // unsilence phone
                    AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(sharedPrefs.getInt(Utility.RINGER_STATE, AudioManager.RINGER_MODE_NORMAL));

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(Utility.SILENCING, false);
                    editor.commit();
                }
            }
        });
        detectedActivitySubscription = subscription;
    }
}
