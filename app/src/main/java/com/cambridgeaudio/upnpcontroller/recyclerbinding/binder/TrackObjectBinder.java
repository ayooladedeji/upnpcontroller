package com.cambridgeaudio.upnpcontroller.recyclerbinding.binder;

import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ConditionalDataBinder;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.DidlViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.TrackViewModel;

public class TrackObjectBinder extends ConditionalDataBinder<TrackViewModel> {
    public TrackObjectBinder(int bindingVariable, int layoutId) {
        super(bindingVariable, layoutId);
    }

    @Override
    public boolean canHandle(TrackViewModel model) {
        return true;
    }
}
