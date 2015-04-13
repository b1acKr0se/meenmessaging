package io.wyrmise.meen;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SendActivity extends ActionBarActivity {
    ImageButton sendBtn;
    EditText phone_edt, msg_edt;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    private Toolbar toolbar;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.hasKitKat()) {
            if (!Utils.isDefaultApp(this)) {
                sendBtn.setVisibility(Button.INVISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isNightMode){
            setTheme(R.style.NightActionBar);
        } else {
            getActionBarColor();
        }
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean key = pref.getBoolean(SettingsActivity.KEY_SWIPE_BACK, true);
        if (key)
            SwipeBack.attach(this, Position.LEFT).setContentView(R.layout.activity_send).setSwipeBackView(R.layout.swipeback_default);
        else
            setContentView(R.layout.activity_send);
        initNavigationDrawer();
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
        SharedPreferences colorPref = getSharedPreferences("colors", MODE_PRIVATE);
        int color = colorPref.getInt("color",-1);
        switch(color){
            case 1:
                setTheme(R.style.Green);
                break;
            case 2:
                setTheme(R.style.LightGreen);
                break;
            case 3:
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
            Message message = new Message();
            message.messageNumber=phone_num;
            message.originalAddress=phone_num;
            message.messageContent=msg;
            SimpleDateFormat hours = new SimpleDateFormat("h:mm a",
                    Locale.US);
            message.messageDate=hours.format(new Date());

            Intent deliveredIntent = new Intent(DELIVERED);
            deliveredIntent.putExtra("Phone",message.originalAddress);
            deliveredIntent.putExtra("Message",message.messageContent);

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    deliveredIntent, 0);

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
            MainActivity.instance().updateList(message);
            finish();
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
        } else if (id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNavigationDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        onToolbarColorChanged();
        setSupportActionBar(toolbar);
    }

    private void onToolbarColorChanged(){
        SharedPreferences colorPref = getSharedPreferences("colors", MODE_PRIVATE);
        int color = colorPref.getInt("color",-1);
        switch (color){
            case 1:
                toolbar.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case 2:
                toolbar.setBackgroundColor(getResources().getColor(R.color.light_green));
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
}
