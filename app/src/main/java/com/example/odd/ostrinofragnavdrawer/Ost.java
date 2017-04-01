package com.example.odd.ostrinofragnavdrawer;


public class Ost {
    private String title, show, tags, url;

    private int id;

    public Ost(){}

    public Ost(String title, String show, String tags, String url) {
        this.title = title;
        this.show = show;
        this.tags = tags;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Ost{title='" + title + '\'' +
                ", show='" + show + '\'' +
                '}';
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
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

    public String getUrl() {
        return url;
    }
    public void setUrl(String url){
        this.url = url;
    }
}
