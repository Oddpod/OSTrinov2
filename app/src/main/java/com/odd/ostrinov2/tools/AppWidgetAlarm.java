package com.odd.ostrinov2.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.odd.ostrinov2.WidgetProvider;

public class AppWidgetAlarm {
    private final int ALARM_ID = 0;

    private Context mContext;


    public AppWidgetAlarm(Context context)
    {
        mContext = context;
    }

    public void startAlarm()
    {
        Intent alarmIntent = new Intent(WidgetProvider.ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);

        if(pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            // RTC does not wake the device up
            alarmManager.setRepeating(AlarmManager.RTC, java.util.Calendar.getInstance().getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }


    public void stopAlarm()
    {
        Intent alarmIntent = new Intent(WidgetProvider.ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}