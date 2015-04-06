package io.wyrmise.meen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String address = "";
            String smsBody = "";

            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage
                        .createFromPdu((byte[]) sms[i]);
                smsBody = smsMessage.getMessageBody().toString();
                address = smsMessage.getOriginatingAddress();
            }

            Message msg = new Message();
            msg.messageNumber = address;
            msg.messageContent = smsBody;
            SimpleDateFormat hours = new SimpleDateFormat("h:mm a", Locale.US);
            msg.messageDate = hours.format(new Date());
            MainActivity inst = MainActivity.instance();
            inst.pushNotification(msg);
            inst.updateList(msg);
        }
    }
}
