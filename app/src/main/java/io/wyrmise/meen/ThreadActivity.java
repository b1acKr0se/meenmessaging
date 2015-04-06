package io.wyrmise.meen;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ThreadActivity extends ActionBarActivity implements
        OnItemClickListener {

    private Toolbar toolbar;

    ImageView sendBtn;
    EditText msg_edit;
    ListView listView;
    ArrayList<Message> message = new ArrayList<Message>();
    ThreadAdapter msgAdapter;
    HashMap<String, String> contacts = MainActivity.contacts;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    TextView dateView;
    ProgressWheel progressWheel, delaySendProgressWheel;
    ProgressBar sendProgressBar;
    TextView charTextView;
    CountDownTimer mCountDownTimer;
    private String phoneNum;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            new sendMessageAsyncTask().execute();
        }
    };

    public static String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_thread, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int color = intent.getIntExtra("Color", -1);
        if (MainActivity.isNightMode) {
            setTheme(R.style.Night);
        } else {
            switch (color) {
                case -1:
                    this.setTheme(R.style.AppTheme);
                    break;
                case 0:
                    this.setTheme(R.style.Green);
                    break;
                case 1:
                    this.setTheme(R.style.Blue);
                    break;
                case 2:
                    this.setTheme(R.style.Orange);
                    break;
            }
        }
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean key = pref.getBoolean(SettingsActivity.KEY_SWIPE_BACK,true);
        if(key)
            SwipeBack.attach(this,Position.LEFT).setContentView(R.layout.activity_thread).setSwipeBackView(R.layout.swipeback_default);
        else
            setContentView(R.layout.activity_thread);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (MainActivity.isNightMode) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.night));
        } else {
            switch (color) {
                case 0:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.green));
                    break;
                case 1:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    break;
                case 2:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.deep_orange));
                    break;
            }
        }
        setSupportActionBar(toolbar);

        msg_edit = (EditText) findViewById(R.id.edit_msg);
        getBackgroundPicture();
        sendProgressBar = (ProgressBar) findViewById(R.id.sendingProgressBar);

        phoneNum = intent.getStringExtra("Phone");
        setTitle(phoneNum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        delaySendProgressWheel = (ProgressWheel) findViewById(R.id.send_delay);
        delaySendProgressWheel.setLinearProgress(true);

        delaySendProgressWheel.setOnClickListener(new ProgressWheel.OnClickListener() {
            public void onClick(View v) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
                sendBtn.setVisibility(ImageView.VISIBLE);
                delaySendProgressWheel.setVisibility(ProgressWheel.GONE);
            }
        });

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/helveticaneuelight.ttf");
        msg_edit.setTypeface(face);
        sendBtn = (ImageView) findViewById(R.id.Btnsend);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean getMode = prefs.getBoolean(SettingsActivity.KEY_DELAY_MODE, true);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(msg_edit.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"You must enter the message!",Toast.LENGTH_LONG).show();
                    Log.d("No char","edt");
                } else {
                    if (getMode) {
                        delaySendProgressWheel.setProgress(0);
                        delaySendProgressWheel.setSpinSpeed(0.25f);
                        sendBtn.setVisibility(ImageView.GONE);
                        delaySendProgressWheel.setVisibility(ProgressWheel.VISIBLE);

                        mCountDownTimer = new CountDownTimer(5000, 1000) {
                            float i = 0;
                            Handler mHandler = new Handler();

                            @Override
                            public void onTick(long millisUntilFinished) {
                                i += 0.25f;
                                delaySendProgressWheel.setProgress(i);
                            }

                            @Override
                            public void onFinish() {
                                mHandler.postDelayed(mRunnable, 200);
                                delaySendProgressWheel.setVisibility(ProgressWheel.GONE);
                            }
                        };
                        mCountDownTimer.start();
                    } else sendSMS();
                }
            }
        });
        charTextView = (TextView) findViewById(R.id.charCountThread);
        msg_edit = (EditText) findViewById(R.id.edit_msg);
        msg_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 160) {
                    if (s.length() >= 140) charTextView.setTextColor(Color.RED);
                    else charTextView.setTextColor(getResources().getColor(R.color.dark_green));
                    charTextView.setText(String.valueOf(160 - s.length()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        listView = (ListView) findViewById(R.id.thread_view);
        listView.setOnItemClickListener(this);

        if(!MainActivity.cacheThread.containsKey(phoneNum)){
            new getThreadMessageTask().execute();
        } else {
            this.message = MainActivity.cacheThread.get(phoneNum);
            msgAdapter = new ThreadAdapter(this, R.layout.list_view_thread, message);
            msgAdapter.setArrayList(this.message);
            if (message.size() == 1)
                getSupportActionBar().setSubtitle(message.size() + " message");
            else
                getSupportActionBar().setSubtitle(message.size() + " messages");
            listView.setAdapter(msgAdapter);
        }
    }

    public void populateThread() {
        msgAdapter = new ThreadAdapter(this, R.layout.list_view_thread, message);
        msgAdapter.setArrayList(this.message);
        MainActivity.cacheThread.put(phoneNum,this.message);
        listView.setAdapter(msgAdapter);
    }

    private void getBackgroundPicture() {
        RelativeLayout threadLayout = (RelativeLayout) findViewById(R.id.threadLayout);
        if (MainActivity.isNightMode) {
            threadLayout.setBackgroundResource(R.color.night_background);
            msg_edit.setTextColor(getResources().getColor(R.color.white));
        } else {
            if (MainActivity.hasBackground) {
                threadLayout.setBackgroundResource(R.color.night_background);
                msg_edit.setTextColor(getResources().getColor(R.color.white));
            } else {
                threadLayout.setBackgroundResource(R.color.white);
                msg_edit.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    private void getConversation() {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone");
        String actualPhone = intent.getStringExtra("originalAddress");
        Uri uriSms = Uri.parse("content://sms/");
        Cursor cursor = this.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body",
                                "type", "read" }, "address='"+actualPhone+"' OR address='"+actualPhone.replace(" ", "")+"'", null,
                        "date" + " asc");
        System.out.println("Actual phone: " +actualPhone);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Message message = new Message();
                    message.messageNumber = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.originalAddress = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.messageContent = cursor.getString(cursor
                            .getColumnIndex("body"));
                    long milliSeconds = cursor.getLong(cursor
                            .getColumnIndex("date"));
                    message.readState = cursor.getInt(cursor.getColumnIndex("read"));
                    SimpleDateFormat initFormat = new SimpleDateFormat("MMM dd",Locale.US);
                    SimpleDateFormat hours = new SimpleDateFormat("h:mm a",Locale.US);

                    Calendar calendar = Calendar.getInstance();

                    calendar.setTimeInMillis(milliSeconds);

                    String finalDateString = initFormat.format(calendar.getTime());
                    Date now = new Date();
                    String strDate = initFormat.format(now);

                    if(finalDateString.equals(strDate)) {
                        finalDateString = hours.format(calendar.getTime());
                        message.messageDate = finalDateString;
                    }else {
                        finalDateString = initFormat.format(calendar.getTime());
                        message.messageDate = finalDateString;
                    }
                    String number = message.messageNumber.replace(" ", "")
                            .replace("-", "");
                    if (number.startsWith("+")) {
                        number = number.substring(3);
                        number = "0" + number;
                    }
                    if (contacts.containsKey(number)) {
                        message.messageNumber = contacts.get(number);
                    }
                    if (message.messageNumber.equals(phoneNum)) {
                        message.readState = 1;
                        NewSmsBroadcastReceiver.markSmsAsRead(this,message.originalAddress,message.messageContent);
                        String type = cursor.getString(cursor
                                .getColumnIndex("type"));
                        int checkType = Integer.parseInt(type);
                        if (checkType == 2)
                            message.messageNumber = "Me";
                        this.message.add(message);
                    } else {
                        System.out.println("not equal : "+ message.messageNumber + " "+phoneNum);
                    }
                } while (cursor.moveToNext());
                cursor.close();

            }
        }
    }

    public void onStop() {
        super.onStop();
    }

    protected Message sendFakeSMS() {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone");
        String msg = msg_edit.getText().toString();

        String phoneNumber = getPhoneNumber(phoneNum, this);
        if(phoneNumber==null) phoneNumber = phoneNum;
        if (phoneNumber.length() > 0 && msg != null && msg.length() > 0) {
            try {
                Message message = new Message();
                message.messageNumber = "Me";
                message.readState = 1;
                message.messageContent = msg;
                Long date = System.currentTimeMillis();

                SimpleDateFormat initFormat = new SimpleDateFormat("MMM dd", Locale.US);
                SimpleDateFormat hours = new SimpleDateFormat("h:mm a", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("MMM dd h:mm a", Locale.US);

                Calendar calendar = Calendar.getInstance();

                calendar.setTimeInMillis(date);

                String finalDateString = initFormat.format(calendar.getTime());
                Date now = new Date();
                String strDate = initFormat.format(now);

                if (finalDateString.equals(strDate)) {
                    finalDateString = hours.format(calendar.getTime());
                    message.messageDate = finalDateString;
                } else {
                    finalDateString = formatter.format(calendar.getTime());
                    message.messageDate = finalDateString;
                }
                msgAdapter.addItem(message);
                listView.smoothScrollToPosition(msgAdapter.getCount() - 1);
                Message message1 = (Message) message.clone();
                message1.messageNumber = phoneNumber;
                MainActivity.instance().updateList(message1);
                return message;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "You must enter the message!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    protected void sendActualSMS(Message message) {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone");
        String msg = msg_edit.getText().toString();

        String phoneNumber = getPhoneNumber(phoneNum, this);
        try {
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(DELIVERED), 0);

            SmsManager smsMan = SmsManager.getDefault();
            smsMan.sendTextMessage(phoneNumber, null, msg, sentPI, deliveredPI);

            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("date", System.currentTimeMillis() + "");
            values.put("read", "1");
            values.put("type", "2");
            values.put("body", msg);
            Uri uri = Uri.parse("content://sms/");
            getContentResolver().insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendSMS() {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone");
        String msg = msg_edit.getText().toString();

        String phoneNumber = getPhoneNumber(phoneNum, this);

        if (phoneNumber.length() > 0 && msg != null && msg.length() > 0) {
            try {
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(DELIVERED), 0);

                SmsManager smsMan = SmsManager.getDefault();
                smsMan.sendTextMessage(phoneNumber, null, msg, sentPI, deliveredPI);

                ContentValues values = new ContentValues();
                values.put("address", phoneNumber);
                values.put("date", System.currentTimeMillis() + "");
                values.put("read", "1");
                values.put("type", "2");
                values.put("body", msg);
                Uri uri = Uri.parse("content://sms/");
                getContentResolver().insert(uri, values);
                Log.d("phone num", phoneNumber);
                Message message = new Message();
                message.messageNumber = "Me";
                message.readState = 1;
                message.messageContent = msg;
                Long date = (values.getAsLong("date"));

                SimpleDateFormat initFormat = new SimpleDateFormat("MMM dd", Locale.US);
                SimpleDateFormat hours = new SimpleDateFormat("h:mm a", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("MMM dd h:mm a", Locale.US);

                Calendar calendar = Calendar.getInstance();

                calendar.setTimeInMillis(date);

                String finalDateString = initFormat.format(calendar.getTime());
                Date now = new Date();
                String strDate = initFormat.format(now);

                if (finalDateString.equals(strDate)) {
                    finalDateString = hours.format(calendar.getTime());
                    message.messageDate = finalDateString;
                } else {
                    finalDateString = formatter.format(calendar.getTime());
                    message.messageDate = finalDateString;
                }
                this.message.add(message);
                Message message1 = (Message) message.clone();
                message1.messageNumber = phoneNumber;
                MainActivity.instance().updateList(message1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "You must enter the message!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.hasKitKat()) {
            if (!Utils.isDefaultApp(this)) {
                sendBtn.setVisibility(ImageView.INVISIBLE);
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            dateView = (TextView) findViewById(R.id.threadDate);
            dateView.setVisibility(TextView.GONE);
        } catch (NullPointerException e) {
            e.getLocalizedMessage();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_call:
                String phone = getPhoneNumber(phoneNum, this);
                if(phone==null) phone = phoneNum;
                String uri = "tel:" + phone.trim() ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    class getThreadMessageTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            sendBtn.setVisibility(ImageView.GONE);
            charTextView.setVisibility(TextView.GONE);
            msg_edit.setVisibility(EditText.GONE);
            progressWheel.setVisibility(ProgressWheel.VISIBLE);
            listView.setVisibility(ListView.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sendBtn.setVisibility(ImageView.VISIBLE);
            charTextView.setVisibility(TextView.VISIBLE);
            msg_edit.setVisibility(EditText.VISIBLE);
            progressWheel.setVisibility(ProgressWheel.GONE);
            listView.setVisibility(ListView.VISIBLE);
            populateThread();
            if (message.size() == 1)
                getSupportActionBar().setSubtitle(message.size() + " message");
            else
                getSupportActionBar().setSubtitle(message.size() + " messages");
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            getConversation();
            return null;
        }
    }

    class sendMessageAsyncTask extends AsyncTask<String, Integer, Boolean> {
        Message message;

        @Override
        protected void onPreExecute() {
            sendProgressBar.setVisibility(ProgressBar.VISIBLE);
            Toast.makeText(getApplicationContext(),"Sending your message, please wait...",Toast.LENGTH_SHORT).show();
            message = sendFakeSMS();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            sendActualSMS(message);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sendProgressBar.setVisibility(ProgressBar.GONE);
            MainActivity.refreshOnDataChanged = true;
            msg_edit.setText("");
            sendBtn.setVisibility(ImageView.VISIBLE);
        }
    }
}
