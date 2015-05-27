package io.wyrmise.meen;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.isNightMode){
            setTheme(R.style.NightActionBar);
        } else {
            getActionBarColor();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView open_source = (TextView) findViewById(R.id.about_open_source);
        open_source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this,LicenseActivity.class);
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
