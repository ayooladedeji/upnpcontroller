package com.cambridgeaudio.upnpcontroller.recyclerbinding.binder;

import com.cambridgeaudio.upnpcontroller.DidlViewModel;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ConditionalDataBinder;

public class DidlObjectBinder extends ConditionalDataBinder<DidlViewModel> {
    public DidlObjectBinder(int bindingVariable, int layoutId) {
        super(bindingVariable, layoutId);
    }

    @Override
    public boolean canHandle(DidlViewModel model) {
        return true;
    }
}
