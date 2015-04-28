package io.wyrmise.meen;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.swipeback.Position;
import com.hannesdorfmann.swipeback.SwipeBack;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.HashMap;

import io.wyrmise.meen.BroadcastReceiver.NewSmsBroadcastReceiver;
import io.wyrmise.meen.Helper.DateHelper;
import io.wyrmise.meen.Helper.Utils;
import io.wyrmise.meen.Object.Message;

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
    TextView charTextView;
    CountDownTimer mCountDownTimer;
    private String phoneNum;
    int height = 0;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            new sendMessageAsyncTask(ThreadActivity.this).execute();
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
        SharedPreferences colorPref = getSharedPreferences("colors", MODE_PRIVATE);
        int color = colorPref.getInt("color", -1);
        String phone_num = intent.getStringExtra("address");
        if (MainActivity.isNightMode) {
            setTheme(R.style.Night);
        } else {
            switch (color) {
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
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean key = pref.getBoolean(SettingsActivity.KEY_SWIPE_BACK, true);
        if (key)
            SwipeBack.attach(this, Position.LEFT)
                    .setContentView(R.layout.activity_thread)
                    .setSwipeBackView(R.layout.swipeback_default);
        else
            setContentView(R.layout.activity_thread);

        TypedValue tv = new TypedValue();

        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        initNavigationDrawer();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean toolbar_pic = prefs.getBoolean(SettingsActivity.KEY_TOOLBAR_PICTURE, true);
        if(toolbar_pic) {
            Bitmap bmp = MainActivity.getPhoto(this, phone_num);
            if (bmp != null && bmp.getHeight() >= 500 && bmp.getWidth() >= 500) {

                Bitmap dstBmp;
                if (bmp.getWidth() >= bmp.getHeight() && bmp.getHeight() / 2 > height) {
                    dstBmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight() / 3, bmp.getWidth(), height);
                } else {
                    dstBmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight() / 3, bmp.getWidth(), bmp.getHeight() / 4);
                }
                BitmapDrawable background = new BitmapDrawable(getResources(), dstBmp);
                getSupportActionBar().setBackgroundDrawable(background);
            }
        }
        msg_edit = (EditText) findViewById(R.id.edit_msg);
        getBackgroundPicture();

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

        sendBtn = (ImageView) findViewById(R.id.Btnsend);

        final boolean getMode = prefs.getBoolean(SettingsActivity.KEY_DELAY_MODE, true);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msg_edit.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "You must enter the message!", Toast.LENGTH_LONG).show();
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
        new getThreadMessageTask().execute();
    }

    private void initNavigationDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        onToolbarColorChanged();
        setSupportActionBar(toolbar);
    }

    private void onToolbarColorChanged() {
        SharedPreferences colorPref = getSharedPreferences("colors", MODE_PRIVATE);
        int color = colorPref.getInt("color", -1);
        switch (color) {
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

    public void populateThread() {
        msgAdapter = new ThreadAdapter(this, R.layout.list_view_thread, message);
        msgAdapter.setArrayList(this.message);
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
        String actualPhone = intent.getStringExtra("address");
        Uri uriSms = Uri.parse("content://sms/");
        Cursor cursor = this.getContentResolver()
                .query(uriSms,new String[]{"_id", "address", "date", "body",
                                "type", "read","status"}, "address='" + actualPhone + "' OR address='" + actualPhone.replace(" ", "") + "'", null,
                        "date" + " asc");
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Message message = new Message();
                    message.id = cursor.getString(cursor
                            .getColumnIndex("_id"));
                    message.name = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.address = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.content = cursor.getString(cursor
                            .getColumnIndex("body"));
                    long milliSeconds = cursor.getLong(cursor
                            .getColumnIndex("date"));
                    message.read = cursor.getInt(cursor.getColumnIndex("read"));
                    message.delivery = cursor.getInt(cursor.getColumnIndex("status"));
                    message.date = DateHelper.detailedFormat(milliSeconds);
                    if (message.read == 0) {
                        message.read = 1;
                        NewSmsBroadcastReceiver.markSmsAsRead(this, message.address, message.content);
                    }
                    String type = cursor.getString(cursor.getColumnIndex("type"));
                    int checkType = Integer.parseInt(type);
                    if (checkType == 2)
                        message.name = "Me";
                    this.message.add(message);
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
        if (phoneNumber == null) phoneNumber = phoneNum;
        if (phoneNumber.length() > 0 && msg != null && msg.length() > 0) {
            try {
                Message message = new Message();
                message.name = "Me";
                message.address = phoneNumber;
                message.read = 1;
                message.content = msg;
                message.delivery = 1;
                Long date = System.currentTimeMillis();
                message.date = DateHelper.format(date);
                Message message1 = (Message) message.clone();
                message1.name = phoneNumber;
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

    void updateInterface(Message message){
        msgAdapter.addItem(message);
        listView.smoothScrollToPosition(msgAdapter.getCount() - 1);
    }

    protected void sendActualSMS(Message message) {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone");
        String msg = msg_edit.getText().toString();

        String phoneNumber = getPhoneNumber(phoneNum, this);
        try {
            Intent deliveredIntent = new Intent(DELIVERED);
            deliveredIntent.putExtra("Phone",message.address);
            deliveredIntent.putExtra("Message",message.content);
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                    deliveredIntent, 0);

            SmsManager smsMan = SmsManager.getDefault();
            smsMan.sendTextMessage(phoneNumber, null, msg, sentPI, deliveredPI);

            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("date", System.currentTimeMillis() + "");
            values.put("status","1");
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
                Message message = new Message();
                message.name = "Me";
                message.read = 1;
                message.content = msg;
                Long date = (values.getAsLong("date"));
                message.date = DateHelper.format(date);
                this.message.add(message);
                Message message1 = (Message) message.clone();
                message1.name = phoneNumber;
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
            case R.id.action_call:
                String phone = getPhoneNumber(phoneNum, this);
                if (phone == null) phone = phoneNum;
                String uri = "tel:" + phone.trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
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
        LoadToast lt;
        private Context context;

        public sendMessageAsyncTask(Context mContext){
            context = mContext;
        }

        @Override
        protected void onPreExecute() {
            lt = new LoadToast(context);
            lt.setText("Sending...");
            lt.setTextColor(Color.WHITE).setBackgroundColor(Color.BLACK);
            lt.setTranslationY(height+50);
            lt.show();
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
            lt.success();
            updateInterface(message);
            msg_edit.setText("");
            sendBtn.setVisibility(ImageView.VISIBLE);
        }
    }
}
