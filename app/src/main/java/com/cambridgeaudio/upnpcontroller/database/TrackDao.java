package com.cambridgeaudio.upnpcontroller.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;
import static android.arch.persistence.room.OnConflictStrategy.ROLLBACK;

/**
 * Created by Ayo on 02/06/2017.
 */

@Dao
public interface TrackDao {

    @Query("select * from tracks")
    List<Track> getAll();

    @Insert(onConflict = ROLLBACK)
    void insert(Track... tracks);
}