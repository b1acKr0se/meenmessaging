package io.wyrmise.meen;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class LicenseActivity extends ActionBarActivity {

    private RelativeLayout meen,load_toast,dialog,progress,swipe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isNightMode){
            setTheme(R.style.NightActionBar);
        } else {
            getActionBarColor();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        meen = (RelativeLayout) findViewById(R.id.meen);
        meen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        load_toast = (RelativeLayout) findViewById(R.id.load_open);
        load_toast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        dialog = (RelativeLayout) findViewById(R.id.dialog);
        dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        progress = (RelativeLayout) findViewById(R.id.progress);
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        swipe = (RelativeLayout) findViewById(R.id.swipe_back);
        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

    }

    public void getActionBarColor(){
        switch(MainActivity.colorCode){
            case 1:
                setTheme(R.style.GreenActionBar);
                break;
            case 2:
                setTheme(R.style.LightGreenActionBar);
                break;
            case 3:
                break;
            case 4:
                setTheme(R.style.BlueActionBar);
                break;
            case 5:
                setTheme(R.style.CyanActionBar);
                break;
            case 6:
                setTheme(R.style.TealActionBar);
                break;
            case 7:
                setTheme(R.style.RedActionBar);
                break;
            case 8:
                setTheme(R.style.OrangeActionBar);
                break;
            case 9:
                break;
            case 10:
                setTheme(R.style.PurpleActionBar);
                break;
            case 11:
                setTheme(R.style.PinkActionBar);
                break;
            case 12:
                setTheme(R.style.BrownActionBar);
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}