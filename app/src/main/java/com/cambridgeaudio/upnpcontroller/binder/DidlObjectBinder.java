package com.cambridgeaudio.upnpcontroller.binder;

import com.cambridgeaudio.upnpcontroller.DidlViewModel;
import com.cambridgeaudio.upnpcontroller.adapter.binder.ConditionalDataBinder;

public class DidlObjectBinder extends ConditionalDataBinder<DidlViewModel> {
    public DidlObjectBinder(int bindingVariable, int layoutId) {
        super(bindingVariable, layoutId);
    }

    @Override
    public boolean canHandle(DidlViewModel model) {
        return true;
    }
}
