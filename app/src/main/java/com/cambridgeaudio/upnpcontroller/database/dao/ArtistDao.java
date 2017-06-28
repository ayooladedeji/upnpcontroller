package com.cambridgeaudio.upnpcontroller.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.relations.ArtistWithAlbums;
import com.cambridgeaudio.upnpcontroller.database.model.relations.ArtistWithTracks;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Ayo on 26/06/2017.
 */

@Dao
public interface ArtistDao {

    @Query("select  * from artists where name = :name")
    Flowable<ArtistWithTracks> getTracksByName(String name);

    @Query("select  * from artists where id = :id")
    Flowable<ArtistWithTracks> getTracksById(int id);

    @Query("select  * from artists where id = :id")
    Flowable<ArtistWithAlbums> getAlbumsById(int id);

    @Query("select  * from artists where name = :name")
    Flowable<ArtistWithAlbums> getAlbumsName(String name);

    @Query("select * from artists")
    Flowable<Artist> getAll();

    @Query("select * from artists where name =:name")
    Flowable<Artist> getByName(String name);

    @Query("select * from artists where id =:id")
    Artist getById(int id);

    @Insert(onConflict = REPLACE)
    long[] insert(Artist... artists);

}
