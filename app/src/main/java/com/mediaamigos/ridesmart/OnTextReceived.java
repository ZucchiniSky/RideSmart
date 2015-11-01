package com.mediaamigos.ridesmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by nghiavo on 11/1/15.
 */
public class OnTextReceived extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        final SmsMessage[] messages = new SmsMessage[pdus.length];
        messages[0] = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Utility.sendText(context, messages[0].getOriginatingAddress());
    }
}
