package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;

import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private UpnpApi upnpApi;

    //@Bindable
   // public ObservableArrayList<DidlViewModel> didlList;

    public MainViewModel(UpnpApi upnpApi){
        this.upnpApi = upnpApi;
      //  this.didlList = new ObservableArrayList<>();
    }


    public Observable<ArrayList<Device>> getMediaServers(){
        return upnpApi.getMediaServers().observeOn(AndroidSchedulers.mainThread());
    }

    public ServiceConnection getServiceConnection(){
        return upnpApi.getServiceConnection();
    }


    public void selectMediaServer(String name){
        getMediaServers().subscribe(devices -> {
            devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d));
            browse("0");
        });
    }

    public void browse(String id) {
       // this.didlList.clear();
        //upnpApi.browse(id).subscribe(didlObject -> didlList.add(new DidlViewModel(didlObject)));
    }
}
