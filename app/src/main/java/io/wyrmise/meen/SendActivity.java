package io.wyrmise.meen;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SendActivity extends ActionBarActivity {
    private Toolbar toolbar;
    ImageButton sendBtn;
    EditText phone_edt, msg_edt;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;

    @Override
    protected void onPause() {
        super.onPause();
        if(sendBroadcastReceiver!=null && deliveryBroadcastReceiver!=null) {
            unregisterReceiver(sendBroadcastReceiver);
            unregisterReceiver(deliveryBroadcastReceiver);
            sendBroadcastReceiver = null;
            deliveryBroadcastReceiver = null;
        }
    }

    private void onToolbarColorChanged(){
        switch (MainActivity.colorCode){
            case 1:
                toolbar.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case 2:
                toolbar.setBackgroundColor(getResources().getColor(R.color.light_green));
                break;
            case 3:
                toolbar.setBackgroundColor(getResources().getColor(R.color.lime));
                break;
            case 4:
                toolbar.setBackgroundColor(getResources().getColor(R.color.light_blue));
                break;
            case 5:
                toolbar.setBackgroundColor(getResources().getColor(R.color.cyan));
                break;
            case 6:
                toolbar.setBackgroundColor(getResources().getColor(R.color.teal));
                break;
            case 7:
                toolbar.setBackgroundColor(getResources().getColor(R.color.red));
                break;
            case 8:
                toolbar.setBackgroundColor(getResources().getColor(R.color.orange));
                break;
            case 9:
                toolbar.setBackgroundColor(getResources().getColor(R.color.amber));
                break;
            case 10:
                toolbar.setBackgroundColor(getResources().getColor(R.color.purple));
                break;
            case 11:
                toolbar.setBackgroundColor(getResources().getColor(R.color.pink));
                break;
            case 12:
                toolbar.setBackgroundColor(getResources().getColor(R.color.brown));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.hasKitKat()) {
            if (!Utils.isDefaultApp(this)) {
                sendBtn.setVisibility(Button.INVISIBLE);
            }
        }
        sendBroadcastReceiver = new BroadcastReceiver() {

            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        deliveryBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Delivered",
                                Toast.LENGTH_SHORT).show();

                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        };
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isNightMode){
            setTheme(R.style.Night);
        } else {
            getActionBarColor();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(MainActivity.isNightMode){
            toolbar.setBackgroundColor(getResources().getColor(R.color.night));
        } else {
            onToolbarColorChanged();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        phone_edt = (EditText) findViewById(R.id.phone_edit);

        msg_edt = (EditText) findViewById(R.id.msg_edit);

        final TextView charCount = (TextView) findViewById(R.id.charCountSend);
        if(MainActivity.fontCode==2) {
            phone_edt.setTypeface(null);
            msg_edt.setTypeface(null);
        }else if(MainActivity.fontCode==1){
            phone_edt.setTypeface(null);
            msg_edt.setTypeface(null);
        }

        msg_edt.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() <= 160) {
                    if (s.length() >= 140) charCount.setTextColor(Color.RED);
                    else charCount.setTextColor(getResources().getColor(R.color.dark_green));
                    charCount.setText(String.valueOf(160 - s.length()));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }
            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        getBackgroundPicture();
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSMS();

            }
        });
    }

    public void getActionBarColor(){
        switch(MainActivity.colorCode){
            case 1:
                setTheme(R.style.Green);
                break;
            case 2:
                setTheme(R.style.LightGreen);
                break;
            case 3:
                setTheme(R.style.Lime);
                break;
            case 4:
                setTheme(R.style.Blue);
                break;
            case 5:
                setTheme(R.style.Cyan);
                break;
            case 6:
                setTheme(R.style.Teal);
                break;
            case 7:
                setTheme(R.style.Red);
                break;
            case 8:
                setTheme(R.style.Orange);
                break;
            case 9:
                setTheme(R.style.Amber);
                break;
            case 10:
                setTheme(R.style.Purple);
                break;
            case 11:
                setTheme(R.style.Pink);
                break;
            case 12:
                setTheme(R.style.Brown);
                break;
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    public void getBackgroundPicture(){
        RelativeLayout sendLayout = (RelativeLayout) findViewById(R.id.sendLayout);
        if(MainActivity.isNightMode){
            sendLayout.setBackgroundResource(R.color.night_background);
            phone_edt.setTextColor(getResources().getColor(R.color.white));
            msg_edt.setTextColor(getResources().getColor(R.color.white));
        } else {
            if (!MainActivity.hasBackground) {
                sendLayout.setBackgroundResource(R.color.white);
                phone_edt.setTextColor(getResources().getColor(R.color.black));
                msg_edt.setTextColor(getResources().getColor(R.color.black));
            } else {
                sendLayout.setBackgroundResource(R.color.night_background);
                phone_edt.setTextColor(getResources().getColor(R.color.white));
                msg_edt.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    protected void sendSMS() {
        String phone_num = phone_edt.getText().toString();
        String msg = msg_edt.getText().toString();
        try {
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            SmsManager smsMan = SmsManager.getDefault();
            smsMan.sendTextMessage(phone_num, null, msg, sentPI, deliveredPI);

            ContentValues values = new ContentValues();
            values.put("address", phone_num);
            values.put("date", System.currentTimeMillis()+"");
            values.put("read", "1");
            values.put("type", "2");
            values.put("body",msg);
            Uri uri = Uri.parse("content://sms/");
            getContentResolver().insert(uri,values);

            Message message = new Message();
            message.messageNumber=phone_num;
            message.messageContent=msg;
            SimpleDateFormat hours = new SimpleDateFormat("h:mm a",
                    Locale.US);
            message.messageDate=hours.format(new Date());

            MainActivity instance = MainActivity.instance();
            instance.updateList(message);

        } catch(Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
