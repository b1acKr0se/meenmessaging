package io.wyrmise.meen;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends ActionBarActivity implements
        AdapterView.OnItemClickListener, TextView.OnEditorActionListener {

    String TITLES[] = {"Settings"};
    int ICONS[] = {R.drawable.ic_action_settings};
    String NAME = "Hai Nguyen";
    String ID = "zenith.wyrm@gmail.com";

    private Toolbar toolbar;                              // Declaring the Toolbar Object
    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawerLayout;                            // Declaring DrawerLayout
    ActionBarDrawerToggle mDrawerToggle;

    private TextView noMessage;

    private int notificationID = 100; //based notification id
    private static final int TYPE_INCOMING_MESSAGE = 1;
    private static final String LIST_STATE = "listState";
    public static int fontCode = -1; //initial value of the global font
    public static boolean isNightMode; //whether the night mode should be enabled

    // listens for changes in the preference when user goes back to main activity
    SharedPreferences.OnSharedPreferenceChangeListener myPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            //the background theme
            if(key.equals(SettingsActivity.KEY_PREF_THEME)) {
                onThemeChanged(prefs);
                Toast.makeText(getBaseContext(), "Background changed", Toast.LENGTH_SHORT).show();
            }
            //the toolbar color
            else if(key.equals(SettingsActivity.KEY_PREF_ACTION_BAR)){
                onActionBarColorChanged(prefs);
                Toast.makeText(getBaseContext(), "Theme changed", Toast.LENGTH_SHORT).show();
            }
            //the floating action button color
            else if(key.equals(SettingsActivity.KEY_FAB_THEME)){
            }
            //the night mode
            else if(key.equals(SettingsActivity.KEY_NIGHT_MODE)){
                if(isNightMode)
                    enableNightMode();
            }
            //the global font
            else if(key.equals(SettingsActivity.KEY_FONT_MODE)){
                onFontChanged();
            }
        }
    };
    public static boolean refreshOnDataChanged = false;
    static MainActivity instance; //the current instance of the Activity
    static HashMap<String, String> contacts; //array of contacts that is used accross the app
    static boolean hasBackground = false; // black or white background
    static int colorCode = 1; //initial value of toolbar's color
    static HashMap<String, Drawable> contactPictureID = new HashMap<String, Drawable>();
    final String SENT = "SMS_SENT";
    final String DELIVERED = "SMS_DELIVERED";
    SwipeMenuListView messageList; //custom listview for swiping
    MessageAdapter messageListAdapter; //custom adapter for the listview
    ArrayList<Message> recordsStored; //the array of message that is used for the adapter
    ArrayList<Message> listInboxMessages; //
    FloatingActionButton fab; // floating action button
    ProgressWheel progressWheel; //loading spinner
    NotificationManager mNotificationManager;
    SharedPreferences prefs; //preference of the app
    SharedPreferences.Editor editor; //editor for said preference
    private Parcelable mListState = null;  //holds the current position of the listview

    /**
     *
     * @return returns a static instance of the class
     */
    public static MainActivity instance() {
        return instance;
    }

    /**
     * get the current hour of the device
     * @return whether current time is night or day
     */
    public boolean isNightTime(){
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour > 21;
    }

    /**
     *
     * @return boolean on whether it is night and night mode is enabled
     */
    public boolean isNightModeEnable(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean getMode = sharedPreferences.getBoolean(SettingsActivity.KEY_NIGHT_MODE, false);
        return isNightMode = (getMode && (isNightTime()));
    }

    /**
     * change the application theme to night mode
     */
    public void enableNightMode(){
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        isNightMode = isNightModeEnable();
        if(isNightMode) {
            mainLayout.setBackgroundResource(R.color.night_background);
            toolbar.setBackgroundColor(getResources().getColor(R.color.night));
        } else return;
    }

    /**
     * @effects returns the current instance of the class when starting
     */
    @Override
    public void onStart() {
        super.onStart();
        instance = this;
    }

    public void onFontChanged(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String getFont = sharedPreferences.getString(SettingsActivity.KEY_FONT_MODE, null);
        int fontSelection = -1;
        if(getFont!=null) fontSelection = Integer.parseInt(getFont);
        switch(fontSelection){
            case 1:
                fontCode = 1;
                break;
            case 2:
                fontCode = 2;
                break;
        }
    }

    public void onThemeChanged(SharedPreferences sharedPreferences){
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String getTheme = sharedPreferences.getString(SettingsActivity.KEY_PREF_THEME, null);
            int themeSelection = -1;
            if (getTheme != null) themeSelection = Integer.parseInt(getTheme);
            Log.d("selection: ", "" + themeSelection);
            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
            switch (themeSelection) {
                case 1:
                    mainLayout.setBackgroundResource(R.color.white);
                    hasBackground = false;
                    break;
                case 2:
                    mainLayout.setBackgroundResource(R.color.night);
                    hasBackground = true;
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void onFabColorChanged(FloatingActionButton fab){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String getColor = prefs.getString(SettingsActivity.KEY_FAB_THEME, null);
        int colorSelection = -1;
        if (getColor != null) colorSelection = Integer.parseInt(getColor);
        switch (colorSelection) {
            case -1:
                break;
            case 1:
                fab.setColorNormalResId(R.color.green);
                fab.setColorPressedResId(R.color.dark_green);
                break;
            case 2:
                fab.setColorNormalResId(R.color.light_green);
                fab.setColorPressedResId(R.color.dark_light_green);
                break;
            case 3:
                fab.setColorNormalResId(R.color.lime);
                fab.setColorPressedResId(R.color.dark_lime);
                break;
            case 4:
                fab.setColorNormalResId(R.color.light_blue);
                fab.setColorPressedResId(R.color.dark_blue);
                break;
            case 5:
                fab.setColorNormalResId(R.color.cyan);
                fab.setColorPressedResId(R.color.dark_cyan);
                break;
            case 6:
                fab.setColorNormalResId(R.color.teal);
                fab.setColorPressedResId(R.color.teal);
                break;
            case 7:
                fab.setColorNormalResId(R.color.red);
                fab.setColorPressedResId(R.color.dark_red);
                break;
            case 8:
                fab.setColorNormalResId(R.color.orange);
                fab.setColorPressedResId(R.color.dark_orange);
                break;
            case 9:
                fab.setColorNormalResId(R.color.amber);
                fab.setColorPressedResId(R.color.dark_amber);
                break;
            case 10:
                fab.setColorNormalResId(R.color.purple);
                fab.setColorPressedResId(R.color.dark_purple);
                break;
            case 11:
                fab.setColorNormalResId(R.color.pink);
                fab.setColorPressedResId(R.color.dark_pink);
                break;
            case 12:
                fab.setColorNormalResId(R.color.brown);
                fab.setColorPressedResId(R.color.dark_brown);
                break;
        }
    }

    public void onActionBarColorChanged(SharedPreferences sharedPreferences){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String getColor = sharedPreferences.getString(SettingsActivity.KEY_PREF_ACTION_BAR, null);
        int colorSelection = -1;
        if (getColor != null) colorSelection = Integer.parseInt(getColor);
        editor = getSharedPreferences("colors", MODE_PRIVATE).edit();
        switch (colorSelection) {
            case 1:
                setTheme(R.style.Green);
                colorCode = 1;
                recreate();
                break;
            case 2:
                setTheme(R.style.LightGreen);
                colorCode = 2;
                recreate();
                break;
            case 3:
                break;
            case 4:
                setTheme(R.style.Blue);
                colorCode = 4;
                recreate();
                break;
            case 5:
                setTheme(R.style.Cyan);
                colorCode = 5;
                recreate();
                break;
            case 6:
                setTheme(R.style.Teal);
                colorCode = 6;
                recreate();
                break;
            case 7:
                setTheme(R.style.Red);
                colorCode = 7;
                recreate();
                break;
            case 8:
                setTheme(R.style.Orange);
                colorCode = 8;
                recreate();
                break;
            case 9:
                break;
            case 10:
                setTheme(R.style.Purple);
                colorCode = 10;
                recreate();
                break;
            case 11:
                setTheme(R.style.Pink);
                colorCode = 11;
                recreate();
                break;
            case 12:
                setTheme(R.style.Brown);
                colorCode = 12;
                recreate();
                break;
        }
        editor.putInt("color",colorCode);
        editor.commit();
    }


    public void onCreateTheme(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String getColor = sharedPreferences.getString(SettingsActivity.KEY_PREF_ACTION_BAR, null);
        int colorSelection = -1;
        if (getColor != null) colorSelection = Integer.parseInt(getColor);
        editor = getSharedPreferences("colors", MODE_PRIVATE).edit();
        switch (colorSelection) {
            case 1:
                setTheme(R.style.Green);
                colorCode = 1;
                break;
            case 2:
                setTheme(R.style.LightGreen);
                colorCode = 2;
                break;
            case 3:
                break;
            case 4:
                setTheme(R.style.Blue);
                colorCode = 4;
                break;
            case 5:
                setTheme(R.style.Cyan);
                colorCode = 5;
                break;
            case 6:
                setTheme(R.style.Teal);
                colorCode = 6;
                break;
            case 7:
                setTheme(R.style.Red);
                colorCode = 7;
                break;
            case 8:
                setTheme(R.style.Orange);
                colorCode = 8;
                break;
            case 9:
                break;
            case 10:
                setTheme(R.style.Purple);
                colorCode = 10;
                break;
            case 11:
                setTheme(R.style.Pink);
                colorCode = 11;
                break;
            case 12:
                setTheme(R.style.Brown);
                colorCode = 12;
                break;
        }
        editor.putInt("color",colorCode);
        editor.commit();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* check the preference and enable and disable the night mode accordingly */
        if (this != null) {
            isNightMode = isNightModeEnable();
            if(isNightMode){
                setTheme(R.style.Night);
            } else {
                onCreateTheme();
            }
        }
        super.onCreate(savedInstanceState);
        /** get the current preference */
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            /** get the support action bar */
            /** apply the layout */
            setContentView(R.layout.activity_main);
            /** check the current position in the listview */
            if (mListState != null)
                messageList.onRestoreInstanceState(mListState);
            mListState = null;
            initNavigationDrawer();
            /** enable up button */
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            /** check the font preference */
            onFontChanged();

            isNightMode = isNightModeEnable();
            if(isNightMode){
                enableNightMode();
                toolbar.setBackgroundColor(getResources().getColor(R.color.night));
            } else {
                onThemeChanged(prefs);
            }
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onToolbarColorChanged(){
        switch (colorCode){
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

    private void initNavigationDrawer(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        onToolbarColorChanged();
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavAdapter(TITLES,ICONS,NAME,ID);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);        // drawerLayout object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        }; // drawerLayout Toggle Object Made
        drawerLayout.setDrawerListener(mDrawerToggle); // drawerLayout Listener set to the drawerLayout toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        switch (position){
                            case 1:
                                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                                startActivity(intent);
                        }
                    }
                })
        );
    }


    /**
     * @effects refreshes the layout for any possible data changes when resuming
     */
    @Override
    public void onResume() {
        super.onResume();
        if(refreshOnDataChanged){
            populateMessageList();
            messageListAdapter.notifyDataSetChanged();
            refreshOnDataChanged=false;
            Log.d("notify data changed", "true");
        }
        if(isNightMode)
            enableNightMode();
        prefs.registerOnSharedPreferenceChangeListener(myPrefListener);
        if (mListState != null)
            messageList.onRestoreInstanceState(mListState);
        mListState = null;
    }

    /**
     * @effects initialises a number of graphic components and retrieve the sms
     *          message
     */
    private void initViews() {
        recordsStored = new ArrayList<Message>();
        messageList = (SwipeMenuListView) findViewById(R.id.inbox_list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        noMessage = (TextView) findViewById(R.id.no_message);
        onFabColorChanged(fab);
        fab.attachToListView(messageList, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
            }
            @Override
            public void onScrollUp() {
            }
        }, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),
                        SendActivity.class);
                startActivity(intent);
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "delete" item
                SwipeMenuItem markUnreadItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                markUnreadItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                markUnreadItem.setWidth(dp2px(60));
                // set a icon
                markUnreadItem.setTitle("Unread");
                markUnreadItem.setTitleSize(12);
                // set item title font color
                markUnreadItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(markUnreadItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.argb(200,0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(40));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_action_delete);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };
        messageList.setMenuCreator(creator);

        messageList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Message message = (Message) messageList.getAdapter().getItem(position);
                        if (message.readState == 1) {
                            message.readState = 0;
                            NewSmsBroadcastReceiver.markSmsAsUnread(getBaseContext(), message.originalAddress, message.messageContent);
                            messageListAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 1:
                        final MaterialDialog mMaterialDialog = new MaterialDialog(MainActivity.this);
                        mMaterialDialog.setTitle("Confirm Action")
                                .setMessage("Do you really want to delete this conversation?")
                                .setPositiveButton("Delete", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                    }
                                });

                        mMaterialDialog.show();
                        break;
                }
                return false;
            }
        });

        messageList.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        messageList.setOnItemClickListener(this);
        messageList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        if (mListState != null)
            messageList.onRestoreInstanceState(mListState);
        mListState = null;
        new Task().execute();

    }

    /**
     * @effects calls method to retrieve the sms message and set the adapter for
     *          the current ListView
     */
    public void populateMessageList() {
        messageListAdapter = new MessageAdapter(this,
                R.layout.list_view_main, recordsStored);
        messageList.setAdapter(messageListAdapter);

        messageList.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = messageList.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                messageListAdapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.activity_main, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                toolbar.setVisibility(Toolbar.VISIBLE);
                messageListAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                toolbar.setVisibility(Toolbar.GONE);
                return false;
            }
        });

        }

    /**
     * @effects starts a thread to fetch the message if there is no data in the
     *          array, otherwise set the array to the adapter to notify change
     */
    private void fetchInboxMessages() {
        if (listInboxMessages == null) {
            recordsStored = fetchInboxSms(TYPE_INCOMING_MESSAGE);
            listInboxMessages = recordsStored;
            contactPictureID = RetrieveContactPicture();
        } else {
            recordsStored = listInboxMessages;
            messageListAdapter.setArrayList(recordsStored);
        }
    }

    public HashMap<String, String> getContacts() {
        HashMap<String, String> getContacts = new HashMap<String, String>();
        editor = getSharedPreferences("contacts", MODE_PRIVATE).edit();
        Cursor managedCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER },
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (managedCursor != null) {
            managedCursor.moveToLast();
            if (managedCursor.getCount() > 0) {
                if (managedCursor.moveToFirst()) {
                    do {
                        String value = managedCursor.getString(managedCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String key = managedCursor.getString(managedCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (key != null) {
                            key = key.replace(" ", "").replace("-", "");
                            if (key.startsWith("+")) {
                                key = key.substring(3);
                                key = "0" + key;
                            }
                            getContacts.put(key, value);
                            editor.putString(key, value);
                        }
                    } while (managedCursor.moveToNext());
                }
            }
        }
        managedCursor.close();
        editor.commit();
        return getContacts;
    }

    public ArrayList<Message> fetchInboxSms(int type) {
        ArrayList<Message> smsInbox = new ArrayList<Message>();

        Uri uriSms = Uri.parse("content://sms/");

        contacts = getContacts();

        Cursor cursor = this.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body",
                                "type", "read" }, null, null,
                        "date" + " COLLATE LOCALIZED");
        if (cursor != null) {
            cursor.moveToLast();
            if (cursor.getCount() > 0) {
                do {
                    Message message = new Message();
                    message.messageID = cursor.getString(cursor
                            .getColumnIndex("_id"));
                    message.messageNumber = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.originalAddress = cursor.getString(cursor
                            .getColumnIndex("address"));
                    message.messageContent = cursor.getString(cursor
                            .getColumnIndex("body"));
                    long milliSeconds = cursor.getLong(cursor
                            .getColumnIndex("date"));
                    message.readState = cursor.getInt(cursor.getColumnIndex("read"));
                    SimpleDateFormat initFormat = new SimpleDateFormat(
                            "MMM dd", Locale.US);
                    SimpleDateFormat hours = new SimpleDateFormat("h:mm aa",
                            Locale.US);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(milliSeconds);
                    String finalDateString = initFormat.format(calendar
                            .getTime());
                    Date now = new Date();
                    String strDate = initFormat.format(now);

                    if (finalDateString.equals(strDate)) {
                        finalDateString = hours.format(calendar.getTime());
                        message.messageDate = finalDateString;
                    } else {
                        finalDateString = initFormat.format(calendar.getTime());
                        message.messageDate = finalDateString;
                    }
                    String number = message.messageNumber.replace(" ", "")
                            .replace("-", "");
                    if (number.startsWith("+")) {
                        number = number.substring(3);
                        number = "0" + number;
                    }

                    if (contacts.containsKey(number))
                        message.messageNumber = contacts.get(number);
                    else
                        message.messageNumber = message.messageNumber.replace(
                                "-", "");
                    smsInbox.add(message);
                } while (cursor.moveToPrevious());
                cursor.close();
            }
        }

        ArrayList<Message> getThreadSms = new ArrayList<>();
        try {
            for (int i = 0; i < smsInbox.size(); i++) {
                Message msg = smsInbox.get(i);
                if (getThreadSms.size() == 0)
                    getThreadSms.add(msg);
                else {
                    for (int j = 0; j < getThreadSms.size(); j++) {
                        Message msg1 = getThreadSms.get(j);
                        if (msg.messageNumber.equals(msg1.messageNumber))
                            break;
                        if (j == getThreadSms.size() - 1)
                            if (!msg.messageNumber.equals(msg1.messageNumber))
                                getThreadSms.add(msg);
                    }
                }
            }
            for (int i = 0; i < getThreadSms.size() - 1; i++) {
                if (getThreadSms.get(i).messageNumber.equals(getThreadSms.get(i + 1).messageNumber)) {
                    getThreadSms.remove(i+1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getThreadSms;
    }

    public void pushNotification(final Message message) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this);
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String number = message.messageNumber.replace(" ", "").replace("-", "");
        if (number.startsWith("+84")) {
            number = number.substring(3);
            number = "0" + number;
        }
        if (contacts.containsKey(number))
            message.messageNumber = contacts.get(number);
        Toast.makeText(this, "SMS from " + message.messageNumber,
                Toast.LENGTH_SHORT).show();
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(message.messageNumber)
                .setContentText(message.messageContent)
                .setAutoCancel(true)
                .setSound(soundUri);
        Intent resultIntent = new Intent(this, ThreadActivity.class);
        resultIntent.putExtra("Phone", message.messageNumber);
        resultIntent.putExtra("originalAddress",message.originalAddress);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ThreadActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_LIGHTS;

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());
        getSystemService(Context.AUDIO_SERVICE);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wl.acquire(8000);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean getMode = prefs.getBoolean(SettingsActivity.KEY_POPUP_MODE, true);

        if(getMode && !LifeCycleHandler.isForegrounded()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.activity_dialog, null);
            dialogBuilder.setView(dialogView);
            TextView messageView = (TextView) dialogView.findViewById(R.id.messageDialog);

            final ImageButton reply = (ImageButton) dialogView.findViewById(R.id.reply);

            final EditText editText = (EditText) dialogView.findViewById(R.id.compose_message);

            final TextView charCount = (TextView) dialogView.findViewById(R.id.charCount);
            editText.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (reply.getVisibility() == ImageButton.INVISIBLE)
                        reply.setVisibility(ImageButton.VISIBLE);
                    if (charCount.getVisibility() == TextView.INVISIBLE)
                        charCount.setVisibility(TextView.VISIBLE);
                }
            });
            messageView.setText(message.messageContent);

            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.setTitle("Message from " + message.messageNumber);

            reply.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (editText.getText().toString().length() > 0 && editText.getText() != null) {
                        alertDialog.dismiss();
                    } else
                        Toast.makeText(getBaseContext(), "You haven't entered the message!", Toast.LENGTH_SHORT).show();
                }
            });
            messageView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(getBaseContext(), ThreadActivity.class);
                    String phone = message.messageNumber;
                    intent.putExtra("Phone", phone);
                    startActivity(intent);
                }
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() <= 160) {
                        if (s.length() >= 140) charCount.setTextColor(Color.RED);
                        else charCount.setTextColor(Color.GREEN);
                        charCount.setText(String.valueOf(160 - s.length()));
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            Window window = alertDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
    }

    public void updateList(Message message) {
        try {
            if(Character.isDigit(message.messageNumber.charAt(0)) || message.messageNumber.startsWith("+")) {
                String number = message.messageNumber.replace(" ", "").replace("-",
                        "");
                if (number.startsWith("+")) {
                    number = number.substring(3);
                    number = "0" + number;
                }
                Log.d("phone number: ", number);
                if (contacts.containsKey(number))
                    message.messageNumber = contacts.get(number);
                Log.d("phone number: ", contacts.get(number));
            }
            messageListAdapter.addItem(message);
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mListState = state.getParcelable(LIST_STATE);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = messageList.onSaveInstanceState();
        state.putParcelable(LIST_STATE, mListState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notif.cancelAll();

        Intent intent = new Intent(this, ThreadActivity.class);

        Message msg = (Message) messageList.getAdapter().getItem(position);
        String phone = msg.messageNumber;
        if (msg.readState == 0) {
            msg.readState = 1;
            NewSmsBroadcastReceiver.markSmsAsRead(this, msg.originalAddress, msg.messageContent);
            messageListAdapter.notifyDataSetChanged();
        }
        intent.putExtra("Phone", phone);
        intent.putExtra("originalAddress",msg.originalAddress);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(mRecyclerView)) {
                drawerLayout.closeDrawer(mRecyclerView);
            } else {
                drawerLayout.openDrawer(mRecyclerView);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        try {
            super.onPostCreate(savedInstanceState);
            mDrawerToggle.syncState();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO
        return false;
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public HashMap<String, Drawable> RetrieveContactPicture(){
        HashMap hm = new HashMap();
        for(int i =0; i<recordsStored.size();i++){
            Message message = recordsStored.get(i);
            Bitmap bmp = getPhoto(this,message.originalAddress);
            if(bmp!=null){
                Drawable d = getRoundedBitmap(getResources(),bmp);
                hm.put(message.messageNumber,d);
            }
        }
        return hm;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(Gravity.START|Gravity.LEFT)){
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            progressWheel.setVisibility(ProgressWheel.VISIBLE);
            messageList.setVisibility(ListView.GONE);
            fab.setVisibility(FloatingActionButton.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            populateMessageList();
            if(recordsStored.size()>0) {
                progressWheel.setVisibility(ProgressWheel.GONE);
                messageList.setVisibility(ListView.VISIBLE);
                fab.setVisibility(FloatingActionButton.VISIBLE);
                noMessage.setVisibility(TextView.GONE);
            } else {
                noMessage.setVisibility(TextView.VISIBLE);
                fab.setVisibility(FloatingActionButton.VISIBLE);
            }

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            fetchInboxMessages();
            return null;
        }
    }

    /**
     * initialise the receiver depends on whether the device's API is Kitkat and later
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utils.hasKitKat()) {
            ComponentName receiver = new ComponentName(this,
                    SmsBroadcastReceiver.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public static Bitmap getPhoto(Context ctx, String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Uri photoUri;
        ContentResolver cr = ctx.getContentResolver();
        Cursor contact = cr.query(phoneUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null);
        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
        } else {
            return null;
        }
        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri,true);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        } else {
            return null;
        }
        return null;
    }

    public static RoundedBitmapDrawable getRoundedBitmap(Resources res, Bitmap bitmap){
        RoundedBitmapDrawable roundBitMap = RoundedBitmapDrawableFactory.create(res, bitmap);
        roundBitMap.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 1.25f);
        roundBitMap.setAntiAlias(true);
        return roundBitMap;
    }

}
