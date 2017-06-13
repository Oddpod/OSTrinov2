package com.example.odd.ostrinofragnavdrawer;


import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static android.R.id.input;

public class Ost {
    private String title, show, tags, url, thumbnail;

    private int id;

    Ost(){}

    Ost(String title, String show, String tags, String url) {
        this.title = title;
        this.show = show;
        this.tags = tags;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Ost{title='" + title + '\'' +
                ", show='" + show + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }

    String getTags() {
        return tags;
    }

    void setTags(String tags) {
        this.tags = tags;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    String getUrl() {
        return url;
    }
    void setUrl(String url){
        this.url = url;
    }

    String getSearchString(){
        return title + show + tags;
    }
}
