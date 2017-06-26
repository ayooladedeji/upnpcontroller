package com.cambridgeaudio.upnpcontroller.database.model.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;

import java.util.List;

/**
 * Created by Ayo on 26/06/2017.
 */

public class ServerWithTracks {
    @Embedded
    public Server server;

    @Relation(parentColumn = "name", entityColumn = "server_name", entity = Track.class)
    public List<Track> tracks;
}
