package com.mediaamigos.ridesmart;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by Scott on 12/6/2015.
 */
public class NotificationService extends Service {
    private static NotificationManager sNotificationManager;

    // Binder given to clients
    private final IBinder mBinder = new NotificationBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class NotificationBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startNotification() {
        sNotificationManager.notify(Utility.NOTIFICATION_ID, Utility.buildNotification(this));
    }

    public void cancelNotification() {
        sNotificationManager.cancel(Utility.NOTIFICATION_ID);
    }
}