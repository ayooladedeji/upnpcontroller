package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.util.Log;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private final String TAG = "MainViewModel";

    private Context context;
    private UpnpApi upnpApi;
    public ObservableArrayList<DidlViewModel> items;

    private CompositeDisposable disposables = new CompositeDisposable();

    public MainViewModel(Context context, UpnpApi upnpApi){
        this.context = context;
        this.upnpApi = upnpApi;
        this.items = new ObservableArrayList<>();
    }


    public Observable<ArrayList<Device>> getMediaServers(){
        return upnpApi.getMediaServers().observeOn(AndroidSchedulers.mainThread());
    }

    public ServiceConnection getServiceConnection(){
       return upnpApi.getServiceConnection();
    }


    public void setSelectedDevice(String name){
        getMediaServers().subscribe(devices -> devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.setSelectedDevice(d)));
    }

    public void browse(String id){
        Toast.makeText(context, "browse", Toast.LENGTH_SHORT).show();
        Flowable<DIDLObject> obs = upnpApi.browse(id)
                .delaySubscription(50, TimeUnit.MILLISECONDS)
                .retry(10);

        Disposable d = obs.subscribe(
                didlObject -> items.add(new DidlViewModel(didlObject)),
                throwable -> Log.w(TAG, throwable)
        );

        disposables.add(d);
    }
}
