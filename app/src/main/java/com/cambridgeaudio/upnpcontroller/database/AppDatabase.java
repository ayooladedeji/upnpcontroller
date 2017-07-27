package com.cambridgeaudio.upnpcontroller.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.cambridgeaudio.upnpcontroller.database.dao.AlbumDao;
import com.cambridgeaudio.upnpcontroller.database.dao.ArtistDao;
import com.cambridgeaudio.upnpcontroller.database.dao.ServerDao;
import com.cambridgeaudio.upnpcontroller.database.dao.TrackDao;
import com.cambridgeaudio.upnpcontroller.database.model.Album;
import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;

import javax.inject.Inject;

/**
 * Created by Ayo on 02/06/2017.
 */

@Database(entities = {Track.class, Server.class, Album.class, Artist.class}, version = 95, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TrackDao trackDao();
    public abstract ServerDao serverDao();
    public abstract ArtistDao artistDao();
    public abstract AlbumDao albumDao();


    public AppDatabase(){}

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}