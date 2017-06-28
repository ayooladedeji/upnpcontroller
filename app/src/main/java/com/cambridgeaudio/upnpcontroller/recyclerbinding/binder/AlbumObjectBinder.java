package com.cambridgeaudio.upnpcontroller.recyclerbinding.binder;

import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ConditionalDataBinder;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.AlbumViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.DidlViewModel;

public class AlbumObjectBinder extends ConditionalDataBinder<AlbumViewModel> {
    public AlbumObjectBinder(int bindingVariable, int layoutId) {
        super(bindingVariable, layoutId);
    }

    @Override
    public boolean canHandle(AlbumViewModel model) {
        return true;
    }
}
