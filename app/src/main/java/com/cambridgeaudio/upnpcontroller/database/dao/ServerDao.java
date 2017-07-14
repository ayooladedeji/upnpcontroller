package com.cambridgeaudio.upnpcontroller.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.relations.ServerWithTracks;

import java.util.List;

import io.reactivex.Flowable;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Ayo on 26/06/2017.
 */

@Dao
public interface ServerDao {

    @Insert(onConflict = IGNORE)
    long[] insert(Server... servers);

    @Query("select * from servers where name =:name")
    Server getByName(String name);

    @Query("select * from servers where name =:name")
    Flowable<List<ServerWithTracks>> getTracksFromServer(String name);

}
