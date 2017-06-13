package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;

import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private Context context;
    private UpnpApi upnpApi;

    public MainViewModel(Context context, UpnpApi upnpApi){
        this.context = context;
        this.upnpApi = upnpApi;
    }


    public Observable<ArrayList<Device>> getMediaServers(){
        return upnpApi.getMediaServers().observeOn(AndroidSchedulers.mainThread());
    }

    public ServiceConnection getServiceConnection(){
       return upnpApi.getServiceConnection();
    }


    public void setSelectedDevice(String name){
        getMediaServers().subscribe(devices -> devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d)));
    }
}
