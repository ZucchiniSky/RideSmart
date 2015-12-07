package com.mediaamigos.ridesmart;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by nghiavo on 11/1/15.
 */
public class Utility {
    public static final String TAG = "Utility";

    public static final String SHARED_PREFS_NAME = "RIDESMART";

    public static final String ENABLED = "ENABLED";
    public static final String SILENCING = "SILENCING";
    public static final String LAST_CHECKED = "LAST_CHECKED";
    public static final String RINGER_STATE = "RINGER_STATE";
    public static final String TEXT_RESPONSE_ENABLED = "TEXT_RESPONSE_ENABLED";
    public static final String TEXT_RESPONSE_BODY = "TEXT_RESPONSE_BODY";

    public static final String TEXT_RESPONSE_BODY_DEFAULT = "HEY, I'M DRIVING!!!";

    public static final int NOTIFICATION_ID = 109;

    private static boolean mIsNotificationBound = false;
    private static NotificationService mNotificationService;
    private static NotificationService.NotificationBinder mBinder;
    private static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (NotificationService.NotificationBinder) iBinder;
            mNotificationService = mBinder.getService();
            mIsNotificationBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsNotificationBound = false;
        }
    };

    public interface ActivityDetectionListener {
        void onActivityDetected();
    }
    public static ActivityDetectionListener listener = null;
    public static void setActivityDetectionListener(ActivityDetectionListener detectionListener) {
        listener = detectionListener;
    }

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

        if (!sharedPrefs.contains(ENABLED)) {
            editor.putBoolean(ENABLED, true);
        }
        editor.putBoolean(SILENCING, false);
        editor.putLong(LAST_CHECKED, System.currentTimeMillis());
        editor.putInt(RINGER_STATE, audioManager.getRingerMode());

        if (!sharedPrefs.contains(TEXT_RESPONSE_ENABLED)) {
            editor.putBoolean(TEXT_RESPONSE_ENABLED, true);
        }

        if (!sharedPrefs.contains(TEXT_RESPONSE_BODY)) {
            editor.putString(TEXT_RESPONSE_BODY, TEXT_RESPONSE_BODY_DEFAULT);
        }

        editor.apply();
    }

    public static void sendText(Context context, String phoneNumber) {
        Log.d("Checking to send text", TAG);
        SharedPreferences sharedPrefs = getSharedPreferences(context);
        if (sharedPrefs.getBoolean(SILENCING, false) && sharedPrefs.getBoolean(TEXT_RESPONSE_ENABLED, false)) {
            Log.d("Sending text", TAG);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, sharedPrefs.getString(TEXT_RESPONSE_BODY, TEXT_RESPONSE_BODY_DEFAULT), null, null);
        } else {
            Log.d("Not sending text", TAG);
        }
    }

    public static void onActivityCheck(boolean isDriving, Context context) {
        SharedPreferences sharedPrefs = Utility.getSharedPreferences(context);
        Log.d("Checking activity", TAG);

        Boolean wasSilencing = sharedPrefs.getBoolean(Utility.SILENCING, false);

        SharedPreferences.Editor editor = sharedPrefs.edit();
        Log.d("enabled is " + sharedPrefs.getBoolean(Utility.ENABLED, false), TAG);
        if (isDriving) {
            Log.d("Silencing phone", TAG);
            // silence phone
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            editor.putBoolean(Utility.SILENCING, true);
            editor.putInt(Utility.RINGER_STATE, audioManager.getRingerMode());
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        } else {
            Log.d("Unsilencing phone", TAG);
            // unsilence phone
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(sharedPrefs.getInt(Utility.RINGER_STATE, AudioManager.RINGER_MODE_NORMAL));

            editor.putBoolean(Utility.SILENCING, false);
        }
        editor.putLong(Utility.LAST_CHECKED, System.currentTimeMillis());
        editor.apply();

        if (listener != null) {
            listener.onActivityDetected();
        }

        Boolean isSilencing = sharedPrefs.getBoolean(Utility.SILENCING, false);

        if (wasSilencing != isSilencing) {
            if (!mIsNotificationBound) {
                Intent intent = new Intent(context, NotificationService.class);
                context.startService(intent);
                context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
            if (isSilencing) {
                mNotificationService.startNotification();
            } else {
                mNotificationService.cancelNotification();
            }
        }
    }

    public static Notification buildNotification(Context context) {
        Notification.Builder builder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.common_signin_btn_text_light)
                        .setContentTitle("RideSmart is running")
                        .setContentText("Focus on the road!")
                        .setOngoing(true);
        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        return builder.build();
    }
}
