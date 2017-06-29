package com.cambridgeaudio.upnpcontroller.database.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.MusicTrack;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Ayo on 01/06/2017.
 */

@Entity(tableName = "tracks"//,
//        foreignKeys = {
//                @ForeignKey(entity = Artist.class,
//                        parentColumns = "id",
//                        childColumns = "artist_id",
//                        onDelete = CASCADE,
//                        onUpdate = CASCADE),
//                @ForeignKey(entity = Album.class,
//                        parentColumns = "id",
//                        childColumns = "album_id",
//                        onDelete = CASCADE,
//                        onUpdate = CASCADE),
//                @ForeignKey(entity = Server.class,
//                        parentColumns = "name",
//                        childColumns = "server_name",
//                        onDelete = CASCADE,
//                        onUpdate = CASCADE)
//        },
//        indices = {@Index(value = "media_path", unique = true), @Index(value = {"artist_id", "album_id", "server_name"}, unique = true)}
)
public class Track {

    @ColumnInfo(name = "id")
    private String id;

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

    @ColumnInfo(name = "duration")
    private String duration;

    @ColumnInfo(name = "parent_id")
    private String parentId;

    @ColumnInfo(name = "artist_id")
    private long artistId;

    @ColumnInfo(name = "album_id")
    private long albumId;

    @ColumnInfo(name = "server_name")
    private String serverName;


    private static String createMediaPath(String s){
        String regex = "http?://\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b:\\d{1,4}/";
        String[] parts = s.split(regex);
        return parts[1];
    }

    public static Track create(DIDLObject didlObject, String serverName, long albumId, long artistId) {
        Track t = new Track();

        t.setServerName(serverName);
        t.setAlbumId(albumId);
        t.setArtistId(artistId);
        t.setParentId(didlObject.getParentID());

        t.setTrackTitle(didlObject.getTitle() != null ? didlObject.getTitle() : "");
        t.setGenre(((AudioItem) didlObject).getFirstGenre() != null ? ((AudioItem) didlObject).getFirstGenre() : "");
        t.setDuration(didlObject.getFirstResource().getDuration());
        t.setMediaPath(didlObject.getFirstResource().getValue() != null ? createMediaPath(didlObject.getFirstResource().getValue()) : "");

        if (didlObject instanceof MusicTrack) {
            t.setGenre(((MusicTrack) didlObject).getFirstGenre() != null ? ((MusicTrack) didlObject).getFirstGenre() : "");
            t.setTrackNumber(((MusicTrack) didlObject).getOriginalTrackNumber() != null ? ((MusicTrack) didlObject).getOriginalTrackNumber() : 0);
            t.setDate(((MusicTrack) didlObject).getDate() != null ? ((MusicTrack) didlObject).getDate() : "");
        }

        return t;
    }

    public Track() {
    }

    @Ignore
    public Track(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
