package com.sunitha.rssfeedaggregator.net;

/**
 * Created by Sunitha Premjee on 4/18/2016.
 */
public class Entry {
    public final String id;
    public final String title;
    public final String link;
    public long published;

    public Entry(String id, String title, String link, long published) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.published = published;
    }
}
