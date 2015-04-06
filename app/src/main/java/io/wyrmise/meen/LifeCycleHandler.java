package io.wyrmise.meen;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by wyrmise on 3/20/2015.
 */
public class LifeCycleHandler implements Application.ActivityLifecycleCallbacks{
    private static int resumed;
    private static int stopped;

    public static boolean isForegrounded(){
        return resumed>stopped;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState){

    }

    public void onActivityDestroyed(Activity activity){

    }

    public void onActivityResumed(Activity activity){
        ++resumed;
    }

    public void onActivityPaused(Activity activity) {

    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    public void onActivityStarted(Activity activity) {

    }

    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

}
