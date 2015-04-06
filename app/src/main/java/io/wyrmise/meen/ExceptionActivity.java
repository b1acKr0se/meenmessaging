package io.wyrmise.meen;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import me.drakeet.materialdialog.MaterialDialog;


public class ExceptionActivity extends ActionBarActivity implements View.OnClickListener {
    TextView mExceptionView;
    MaterialDialog mMaterialDialog;
    String error = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);
        mExceptionView = (TextView) findViewById(R.id.exceptionTextView);
        Intent intent = getIntent();
        error = intent.getStringExtra("error");
        mMaterialDialog = new MaterialDialog(ExceptionActivity.this);
        mMaterialDialog.setTitle("Error")
                .setMessage("Unfortunately, the program has caught an exception and needed to stop. Press OK to view details.")
                .setPositiveButton("OK", this);
        mMaterialDialog.show();
    }

    @Override
    public void onClick(View v) {
        mExceptionView.setText(error);
        mMaterialDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exception, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
