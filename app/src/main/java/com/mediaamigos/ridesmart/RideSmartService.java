package com.mediaamigos.ridesmart;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by Scott on 12/6/2015.
 */
public class RideSmartService extends Service {
    private static NotificationManager sNotificationManager;
    private static Subscription sDetectedActivitySubscription;

    // Binder given to clients
    private final IBinder mBinder = new RideSmartServiceBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class RideSmartServiceBinder extends Binder {
        RideSmartService getService() {
            return RideSmartService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sNotificationManager == null) {
            sNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void startNotification() {
        sNotificationManager.notify(Utility.NOTIFICATION_ID, Utility.buildNotification(this));
    }

    public void cancelNotification() {
        sNotificationManager.cancel(Utility.NOTIFICATION_ID);
    }

    public Subscription getSubscription() {
        return sDetectedActivitySubscription;
    }

    public void startSubscription(final Context context) {
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context.getApplicationContext());
        sDetectedActivitySubscription = locationProvider.getDetectedActivity(1000*60*5) // detectionIntervalMillis
                .subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult detectedActivity) {
                        Utility.onActivityCheck(Utility.getSharedPreferences(context.getApplicationContext()).getBoolean(Utility.ENABLED, false) &&
                                detectedActivity.getMostProbableActivity().getType() == DetectedActivity.IN_VEHICLE, context);
                    }
                });
    }
}