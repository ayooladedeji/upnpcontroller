package com.cambridgeaudio.upnpcontroller.adapter.binder;

public interface ItemBinder<T> {
    int getLayoutRes(T model);

    int getBindingVariable(T model);
}
