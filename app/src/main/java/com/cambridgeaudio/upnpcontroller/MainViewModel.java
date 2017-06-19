package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;

import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private UpnpApi upnpApi;


    public MainViewModel(UpnpApi upnpApi){
        this.upnpApi = upnpApi;
    }


    public Observable<ArrayList<Device>> getMediaServers(){
        return upnpApi.getMediaServers().subscribeOn(Schedulers.io());
    }

    public ServiceConnection getServiceConnection(){
        return upnpApi.getServiceConnection();
    }


    public void selectMediaServer(String name){
        getMediaServers().subscribe(devices ->{
            devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d));
            //browse("0");
        } );

    }

    public Flowable<DIDLObject> browse(String id) {
       return upnpApi.browse(id);
    }

    public void select(String name){
        getMediaServers().map(devices -> {
            for(Device d : devices){
                if (d.getDetails().getFriendlyName().equals(name))
                    return ;

            }
        })
    }

}
