package com.mediaamigos.ridesmart;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Scott on 12/6/2015.
 */
public class RideSmartServiceConnector {
    public boolean isServiceBound = false;
    public RideSmartService rideSmartService;
    public RideSmartService.RideSmartServiceBinder binder;

    public enum PendingNotificationState {
        NONE, START, CANCEL
    }
    public PendingNotificationState pendingNotificationState;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (RideSmartService.RideSmartServiceBinder) iBinder;
            rideSmartService = binder.getService();
            isServiceBound = true;

            if (pendingNotificationState == PendingNotificationState.START) {
                rideSmartService.startNotification();
            }
            if (pendingNotificationState == PendingNotificationState.CANCEL) {
                rideSmartService.cancelNotification();
            }
            pendingNotificationState = PendingNotificationState.NONE;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    public void startServiceAndBind(Context context) {
        if (isServiceBound) return;

        Intent intent = new Intent(context, RideSmartService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context) {
        context.unbindService(mServiceConnection);
    }
}