package com.odd.ostrino;

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
        final int count = appWidgetIds.length;
        DBHandler dbHandler = new DBHandler(context);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            List<Ost> ostList = dbHandler.getAllOsts();
            Ost ost = ostList.get(new Random().nextInt(ostList.size()));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.tvOstoftheDay, ost.getTitle());
            File tnFile = new File(Environment.getExternalStorageDirectory()
                    + "/OSTthumbnails/" + UtilMeths.urlToId(ost.getUrl()) + ".jpg");
            Picasso.with(context).load(tnFile).resize(500, 200).onlyScaleDown()
                    .into(remoteViews, R.id.widgetThumbnail, appWidgetIds);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction("start ost");
            intent.putExtra("Ost of the Day", ost.getId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
