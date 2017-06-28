package com.cambridgeaudio.upnpcontroller.viewmodels.itemviews;

import android.databinding.BaseObservable;

import com.cambridgeaudio.upnpcontroller.database.model.Album;

/**
 * Created by Ayo on 27/06/2017.
 */

public class AlbumViewModel extends BaseObservable {

    private final Album model;

    public AlbumViewModel(Album model) {
        this.model = model;
    }

    public Album getModel(){return model;}

    public String getTitle(){return model.getTitle();}

    public long getId(){return model.getId();}
}
