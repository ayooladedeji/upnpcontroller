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

/**
 * Created by Ayo on 02/06/2017.
 */

@Database(entities = {Track.class, Server.class, Album.class, Artist.class}, version = 22)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TrackDao trackDao();
    public abstract ServerDao serverDao();
    public abstract ArtistDao artistDao();
    public abstract AlbumDao albumDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            //.allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}