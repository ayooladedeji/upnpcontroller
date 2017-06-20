package com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder;

public interface ItemBinder<T> {
    int getLayoutRes(T model);

    int getBindingVariable(T model);
}
