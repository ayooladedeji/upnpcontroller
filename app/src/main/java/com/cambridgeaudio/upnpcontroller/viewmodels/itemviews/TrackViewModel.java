package com.cambridgeaudio.upnpcontroller.viewmodels.itemviews;

import android.databinding.BaseObservable;

import com.cambridgeaudio.upnpcontroller.database.model.Track;

/**
 * Created by Ayo on 14/06/2017.
 */

public class TrackViewModel extends BaseObservable {


    private final Track model;

    public TrackViewModel(Track model) {
        this.model = model;
    }

    public Track getModel(){return model;}

    public String getTitle(){return model.getTrackTitle();}

    public String getId(){return model.getId();}
}
