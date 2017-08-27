package com.odd.ostrinov2;

public class Ost {
    private String title, show, tags, url;
    private boolean isPlaying;

    private int id;

    Ost(){}

    Ost(String title, String show, String tags, String url) {
        this.title = title;
        this.show = show;
        this.tags = tags;
        this.url = url;
        isPlaying = false;
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
        return title + ", " + show + ", " + tags;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isPlaying(){
        return  isPlaying;
    }
}
