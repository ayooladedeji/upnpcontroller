package com.cambridgeaudio.upnpcontroller.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Ayo on 26/06/2017.
 */

@Entity(tableName = "albums",
        foreignKeys =
        @ForeignKey(entity = Artist.class,
                parentColumns = "id",
                childColumns = "artist_id"))
public class Album {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist_id")
    private int artistId;

    public Album(int id, String title, int artistId) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
