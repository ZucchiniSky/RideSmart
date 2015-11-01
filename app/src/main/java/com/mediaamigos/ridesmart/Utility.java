package com.mediaamigos.ridesmart;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.SmsManager;

/**
 * Created by nghiavo on 11/1/15.
 */
public class Utility {
    public static final String SHARED_PREFS_NAME = "RIDESMART";

    public static final String ENABLED = "ENABLED";
    public static final String SILENCING = "SILENCING";
    public static final String RINGER_STATE = "RINGER_STATE";
    public static final String TEXT_RESPONSE_ENABLED = "TEXT_RESPONSE_ENABLED";
    public static final String TEXT_RESPONSE_BODY = "TEXT_RESPONSE_BODY";

    public static final String TEXT_RESPONSE_BODY_DEFAULT = "HEY, I'M DRIVING!!!";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void initializePrefsOnBoot(Context context) {
        SharedPreferences sharedPrefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        // if RideSmart was on before phone shut off, set it back to previous ringer state
        if (sharedPrefs.getBoolean(SILENCING, false)) {
            audioManager.setRingerMode(sharedPrefs.getInt(RINGER_STATE, AudioManager.RINGER_MODE_NORMAL));
        }

        editor.putBoolean(ENABLED, false);
        editor.putBoolean(SILENCING, false);
        editor.putInt(RINGER_STATE, audioManager.getRingerMode());

        if (!sharedPrefs.contains(TEXT_RESPONSE_ENABLED)) {
            editor.putBoolean(TEXT_RESPONSE_ENABLED, true);
        }

        if (!sharedPrefs.contains(TEXT_RESPONSE_BODY)) {
            editor.putString(TEXT_RESPONSE_BODY, TEXT_RESPONSE_BODY_DEFAULT);
        }

        editor.commit();
    }

    public static void sendText(Context context, String phoneNumber) {
        SharedPreferences sharedPrefs = getSharedPreferences(context);
        if (sharedPrefs.getBoolean(SILENCING, false) && sharedPrefs.getBoolean(TEXT_RESPONSE_ENABLED, false)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, sharedPrefs.getString(TEXT_RESPONSE_BODY, TEXT_RESPONSE_BODY_DEFAULT), null, null);
        }
    }
}
