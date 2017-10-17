package com.odd.ostrinov2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.RemoteViews;

import com.odd.ostrinov2.tools.DBHandler;
import com.odd.ostrinov2.tools.UtilMeths;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_AUTO_UPDATE = "AUTO_UPDATE";
    public Ost ostOfTheDay;
    public int ostNum;
    private boolean update = false;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
                if(update) {
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                            R.layout.widget_layout);
                    remoteViews.setTextViewText(R.id.tvOstoftheDay, ostOfTheDay.getTitle());
                    File tnFile = new File(Environment.getExternalStorageDirectory()
                            + "/OSTthumbnails/" + UtilMeths.INSTANCE.urlToId(ostOfTheDay.getUrl()) + ".jpg");
                    Picasso.with(context).load(tnFile).resize(500, 200).onlyScaleDown()
                            .into(remoteViews, R.id.widgetThumbnail, appWidgetIds);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(context.getString(R.string.label_ost_of_the_day), ostNum);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context,
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent);
                    remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent);
                    update = false;
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        System.out.println(intent.getAction());
        if(ACTION_AUTO_UPDATE.equals(intent.getAction())){
            System.out.println("updating");
            update = true;

            DBHandler dbHandler = new DBHandler(context);

            List<Ost> ostList = dbHandler.getAllOsts();
            ostNum = new Random().nextInt(ostList.size());
            ostOfTheDay = ostList.get(ostNum);

            Bundle extras = intent.getExtras();
            if(extras!=null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

                onUpdate(context, appWidgetManager, appWidgetIds);
            }

        }
    }
}
