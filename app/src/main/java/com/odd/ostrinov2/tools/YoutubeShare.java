package com.odd.ostrinov2.tools;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.odd.ostrinov2.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeShare {

    private String youtube, url;
    private Activity activity;
    private DBHandler db;

    public YoutubeShare(Activity activity, String url, DBHandler db){
        this.db = db;
        this.activity = activity;
        this.url = url;
        YoutubeGetInfo youtubeGetInfo = new YoutubeGetInfo();
        youtubeGetInfo.execute();
    }
    public String getTitle(){
        return youtube;
    }

    private class YoutubeGetInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonUrl = "https://www.youtube.com/oembed?format=json&amp;url=" + url
                    + "&key=" + Constants.YDATA_API_TOKEN;

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(jsonUrl);

            //  Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    System.out.println(jsonObj);
                    youtube = jsonObj.get("title").toString();
                    UtilMeths.INSTANCE.parseAddOst(youtube, db, url);

                } catch (final JSONException e) {
                    // Log.e(TAG, "Json parsing error: " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity,
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                //Log.e(TAG, "Couldn't get json from server.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}