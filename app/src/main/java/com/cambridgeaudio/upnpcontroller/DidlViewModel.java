package com.cambridgeaudio.upnpcontroller;

import android.databinding.BaseObservable;

import org.fourthline.cling.support.model.DIDLObject;


/**
 * Created by Ayo on 14/06/2017.
 */

public class DidlViewModel extends BaseObservable{

    private DIDLObject model;


    public DidlViewModel(DIDLObject didlObject){
        this.model = didlObject;
    }

    public DIDLObject getModel(){return model;}

    public String getId(){
        return model.getId();
    }

    public String getTitle(){
        return model.getTitle();
    }
}
