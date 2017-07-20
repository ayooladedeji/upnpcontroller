package com.cambridgeaudio.upnpcontroller.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.cambridgeaudio.upnpcontroller.database.model.Track;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Ayo on 02/06/2017.
 */

@Dao
public interface TrackDao {

    @Query("select * from tracks")
    Flowable<List<Track>> getAll();

    @Query("select * from tracks limit :limit")
    Flowable<List<Track>> getAllWithLimit(int limit);

    @Query("select * from tracks where track_title  like :trackTitle")
    Flowable<List<Track>> getAllByTitle(String trackTitle);

    @Query("select * from tracks where album_id = :id")
    Flowable<List<Track>> getAllByAlbumId(long id);

    @Query("select * from tracks where artist_id = :id")
    Flowable<List<Track>> getAllByArtistId(long id);

    @Insert(onConflict = IGNORE)
    long[] insert(Track... tracks);
}
