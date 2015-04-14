package io.wyrmise.meen.Helper;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

/**
 * Created by wyrmise on 3/19/2015.
 */
public class Utils {

    public static boolean hasKitKat(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isDefaultApp(Context context){

        if(hasKitKat()) return context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context));
        return true;
    }

    public static void setDefault(Context context){
        if(hasKitKat()){
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,context.getPackageName());
            context.startActivity(intent);
        }
    }
}
