package com.cambridgeaudio.upnpcontroller.viewmodels.itemviews;

import android.databinding.BaseObservable;

import com.cambridgeaudio.upnpcontroller.database.model.Artist;

/**
 * Created by Ayo on 27/06/2017.
 */

public class ArtistViewModel extends BaseObservable {

    private final Artist model;

    public ArtistViewModel(Artist model) {
        this.model = model;
    }

    public Artist getModel(){return model;}

    public String getName(){return model.getName();}

    public long getId(){return model.getId();}
}
