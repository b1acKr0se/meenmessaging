package io.wyrmise.meen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by wyrmise on 3/23/2015.
 */
public class SendBroadcastReceiver extends BroadcastReceiver{

    private static final String SMS_SENT = "SMS_SENT";

    public void onReceive(Context context, Intent intent){
        Log.d("Receive","Working on it");
        String action = intent.getAction();
        if(action.equals(SMS_SENT)){
            Log.d("ResultCode",""+getResultCode());
            Log.d("ResultCode","RESULT_OK"+Activity.RESULT_OK);
            Log.d("ResultCode","RESULT_ERROR_GENERIC_FAILURE"+SmsManager.RESULT_ERROR_GENERIC_FAILURE);
            Log.d("ResultCode","RESULT_ERROR_NO_SERVICE"+SmsManager.RESULT_ERROR_NO_SERVICE);
            Log.d("ResultCode","RESULT_ERROR_NULL_PDU"+SmsManager.RESULT_ERROR_NULL_PDU);
            Log.d("ResultCode","SmsManager.RESULT_ERROR_RADIO_OFF"+SmsManager.RESULT_ERROR_RADIO_OFF);
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Sent",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "Generic failure",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
