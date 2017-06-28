package com.cambridgeaudio.upnpcontroller.recyclerbinding.binder;

import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ConditionalDataBinder;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.ArtistViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.DidlViewModel;

public class ArtistObjectBinder extends ConditionalDataBinder<ArtistViewModel> {
    public ArtistObjectBinder(int bindingVariable, int layoutId) {
        super(bindingVariable, layoutId);
    }

    @Override
    public boolean canHandle(ArtistViewModel model) {
        return true;
    }
}
