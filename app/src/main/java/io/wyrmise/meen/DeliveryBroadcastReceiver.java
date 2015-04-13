package io.wyrmise.meen;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wyrmise on 3/23/2015.
 */
public class DeliveryBroadcastReceiver extends BroadcastReceiver{

    private static final String SMS_DELIVERED = "SMS_DELIVERED";

    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        Log.d("ResultCode","delivery "+getResultCode());
        if(action.equals(SMS_DELIVERED)){
            String phone = intent.getStringExtra("Phone");
            String content = intent.getStringExtra("Message");
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Delivered",
                            Toast.LENGTH_SHORT).show();
                    NewSmsBroadcastReceiver.markSmsAsDelivered(context,phone,content);
                    Log.d("SMS Delivery","RESULT_OK");
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
