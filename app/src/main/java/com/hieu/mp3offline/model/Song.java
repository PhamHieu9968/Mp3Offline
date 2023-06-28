package com.hieu.mp3offline.model;

import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieu.mp3offline.R;

public class Song   {
    public String title, path, album, artist;
    public Uri cover;

    public Song(String title, String path, String album, String artist,Uri cover) {
        this.cover = cover;
        this.title = title;
        this.path = path;
        this.album = album;
        this.artist = artist;
    }

    @Override
    public String toString() {
        return title ;
    }


}
