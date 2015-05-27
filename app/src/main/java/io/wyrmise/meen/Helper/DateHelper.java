package io.wyrmise.meen.Helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wyrmise on 4/14/2015.
 */
public class DateHelper {
    public static String format(long date){
        SimpleDateFormat initFormat = new SimpleDateFormat(
                "MMM dd", Locale.US);
        SimpleDateFormat hours = new SimpleDateFormat("HH:mm",
                Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        String finalDateString = initFormat.format(calendar
                .getTime());
        Date now = new Date();
        String strDate = initFormat.format(now);
        if (finalDateString.equals(strDate)) {
            finalDateString = hours.format(calendar.getTime());
        } else {
            finalDateString = initFormat.format(calendar.getTime());
        }
        return finalDateString;
    }

    public static String detailedFormat(long date){
        SimpleDateFormat initFormat = new SimpleDateFormat(
                "HH:mm dd/MMM", Locale.US);
        SimpleDateFormat hours = new SimpleDateFormat("HH:mm",
                Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        String finalDateString = initFormat.format(calendar
                .getTime());
        Date now = new Date();
        String strDate = initFormat.format(now);
        if (finalDateString.equals(strDate)) {
            finalDateString = hours.format(calendar.getTime());
        } else {
            finalDateString = initFormat.format(calendar.getTime());
        }
        return finalDateString;
    }
}
