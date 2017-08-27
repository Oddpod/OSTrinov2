package com.odd.ostrinov2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DBHandler dbHandler = new DBHandler(context);

        for (int appWidgetId : appWidgetIds) {
            List<Ost> ostList = dbHandler.getAllOsts();
            int ostNum = new Random().nextInt(ostList.size());
            Ost ost = ostList.get(ostNum);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.tvOstoftheDay, ost.getTitle());
            File tnFile = new File(Environment.getExternalStorageDirectory()
                    + "/OSTthumbnails/" + UtilMeths.INSTANCE.urlToId(ost.getUrl()) + ".jpg");
            Picasso.with(context).load(tnFile).resize(500, 200).onlyScaleDown()
                    .into(remoteViews, R.id.widgetThumbnail, appWidgetIds);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(context.getString(R.string.label_ost_of_the_day), ostNum);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
