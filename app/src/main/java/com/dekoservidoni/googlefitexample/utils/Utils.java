package com.dekoservidoni.googlefitexample.utils;

import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Responsible to provide the common methods of the application
 *
 * Created by DeKo on 05/11/2015.
 */
public class Utils {

    /**
     * Print the message on the UI
     *
     * @param view
     *          TextView to show the message
     * @param msg
     *          Message string
     */
    public static void printMessage(TextView view, String msg) {
        String text = view.getText().toString() + "\n" + msg;
        view.setText(text);
    }

    /**
     * Parse the data point
     */
    public static String describeDataPoint(DataPoint dp) {
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String msg = "dataPoint: "
                + "type: " + dp.getDataType().getName() +"\n"
                + ", range: [" + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS))
                + "-" + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + "]\n"
                + ", fields: [";

        for(Field field : dp.getDataType().getFields()) {
            msg += field.getName() + "=" + dp.getValue(field) + " ";
        }

        msg += "]";

        return msg;
    }
}
