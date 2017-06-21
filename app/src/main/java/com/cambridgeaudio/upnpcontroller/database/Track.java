package com.cambridgeaudio.upnpcontroller.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Ayo on 01/06/2017.
 */

@Entity(tableName = "tracks")
public class Track {

    //@PrimaryKey(autoGenerate = true)
    //private int id;

    @ColumnInfo(name = "artist")
    private String artist;

//    @ColumnInfo(name = "artists")
//    private String artists;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "genre")
    private String genre;

    @ColumnInfo(name = "genres")
    private String genres;

    @ColumnInfo(name = "track_number")
    private int trackNumber;

    @ColumnInfo(name = "date")
    private String date;

    @PrimaryKey
    @ColumnInfo(name = "media_path")
    private String mediaPath;

    @ColumnInfo(name = "track_title")
    private String trackTitle;


//    public int getId() {
//        return id;
//    }

//    public void setId(int id) {
//        this.id = id;
//    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }
}
