package com.cambridgeaudio.upnpcontroller;

import android.databinding.BaseObservable;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Created by Ayo on 14/06/2017.
 */

public class DidlViewModel extends BaseObservable {


    private final DIDLObject model;

    public DidlViewModel(DIDLObject model) {
        this.model = model;
    }

    public DIDLObject getModel(){return model;}

    public String getTitle(){return model.getTitle();}

    public String getId(){return model.getId();}
}
