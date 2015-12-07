package com.mediaamigos.ridesmart;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Scott on 12/6/2015.
 */
public class NotificationResponseService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utility.disableThisTrip(this);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
