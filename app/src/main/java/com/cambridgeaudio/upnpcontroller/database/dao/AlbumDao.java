package com.cambridgeaudio.upnpcontroller.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.cambridgeaudio.upnpcontroller.database.model.Album;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.database.model.relations.AlbumWithTracks;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Ayo on 26/06/2017.
 */

@Dao
public interface AlbumDao {

    @Query("select  * from albums where title = :title")
    Flowable<List<AlbumWithTracks>> getTracksByTitle(String title);

    @Query("select  * from albums where id = :id")
    Flowable<List<AlbumWithTracks>> getTracksById(int id);

    @Query("select * from albums")
    Flowable<List<Album>> getAll();

    @Query("select * from albums where title like :title")
    Flowable<List<Album>> getByTitle(String title);

    @Query("select * from albums where id = :id")
    Album getById(int id);

    @Query("select * from albums limit :limit")
    Flowable<List<Album>> getAllWithLimit(int limit);

    @Insert(onConflict = REPLACE)
    long[] insert(Album... albums);

}
