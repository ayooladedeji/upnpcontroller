package com.cambridgeaudio.upnpcontroller.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Ayo on 26/06/2017.
 */

@Entity(tableName = "albums",
        foreignKeys =
        @ForeignKey(entity = Artist.class,
                parentColumns = "id",
                childColumns = "artist_id",
                onDelete = CASCADE,
                onUpdate = CASCADE),
        indices = {@Index("artist_id")})
public class Album {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist_id")
    private long artistId;

    public Album(String title, long artistId) {
        this.title = title;
        this.artistId = artistId;
    }

    @Ignore
    public Album(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
