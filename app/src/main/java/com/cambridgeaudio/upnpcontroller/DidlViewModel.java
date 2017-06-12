package com.cambridgeaudio.upnpcontroller;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Created by Ayo on 12/06/2017.
 */

public class DidlViewModel extends BaseObservable{

    private final DIDLObject model;

    public DidlViewModel(DIDLObject model){
        this.model = model;
    }

    public String getName(){
        return model.getTitle();
    }
}
