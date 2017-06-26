package com.cambridgeaudio.upnpcontroller.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.cambridgeaudio.upnpcontroller.database.model.Track;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Ayo on 02/06/2017.
 */

@Dao
public interface TrackDao {

    @Query("select * from tracks")
    List<Track> getAll();

    @Insert(onConflict = REPLACE)
    void insert(Track... tracks);
}
