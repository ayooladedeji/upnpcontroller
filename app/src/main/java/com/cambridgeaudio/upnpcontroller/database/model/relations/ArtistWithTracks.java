package com.cambridgeaudio.upnpcontroller.database.model.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.Track;

import java.util.List;

/**
 * Created by Ayo on 26/06/2017.
 */

public class ArtistWithTracks{

    @Embedded
    public Artist artist;

    @Relation(parentColumn = "id", entityColumn = "artist_id", entity = Track.class)
    public List<Track> tracks;

}
