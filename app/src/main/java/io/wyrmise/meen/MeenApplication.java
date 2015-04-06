package io.wyrmise.meen;

import android.app.Application;
import android.util.Log;

/**
 * Created by wyrmise on 3/20/2015.
 */
public class MeenApplication extends Application {
    public MeenApplication() {
        // this method fires only once per application start.
        // getApplicationContext returns null here

        Log.i("main", "Constructor fired");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Utils.hasKitKat()) {

            if (!Utils.isDefaultApp(this)) {

                Utils.setDefault(this);
            }
        }
        registerActivityLifecycleCallbacks(new LifeCycleHandler());
    }
}