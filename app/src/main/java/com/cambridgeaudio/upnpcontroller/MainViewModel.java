package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private UpnpApi upnpApi;
    private AppDatabase appDatabase;
    private ArrayList<String> objectIdList = new ArrayList<>();
    private CompositeDisposable disposables = new CompositeDisposable();

    @Bindable
    public ObservableArrayList<DidlViewModel> didlList = new ObservableArrayList<>();

    MainViewModel(UpnpApi upnpApi, AppDatabase appDatabase) {
        this.upnpApi = upnpApi;
        this.appDatabase = appDatabase;
    }


    Observable<ArrayList<Device>> getMediaServers() {
        return upnpApi.getMediaServers().observeOn(AndroidSchedulers.mainThread());
    }

    ServiceConnection getServiceConnection() {
        return upnpApi.getServiceConnection();
    }


    void selectMediaServer(String name) {
        getMediaServers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(devices -> {
                    devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d));
                    browse("0");
                });

    }

    void browse(String id) {
        objectIdList.add(id);
        this.didlList.clear();
        upnpApi.browse(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(didlObject -> didlList.add(new DidlViewModel(didlObject)));
    }

    void goBack(){
        if(objectIdList.size() > 1){
            objectIdList.remove(objectIdList.size() - 1);
            browse(objectIdList.get(objectIdList.size() - 1));
        }
    }

    boolean isAtRoot(){
        return objectIdList.size() == 1;
    }

    void cacheCurrentDirectory(){
        disposables.add(upnpApi.scan(objectIdList.get(objectIdList.size() - 1)));
    }
}
