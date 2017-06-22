package com.cambridgeaudio.upnpcontroller;

import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.util.Log;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.database.Track;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.services.common.Crash;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private final String TAG = "MainViewModel";
    private UpnpApi upnpApi;
    private AppDatabase appDatabase;
    private ArrayList<String> objectIdList = new ArrayList<>();
    private ViewController viewController;
    @Bindable
    public ObservableArrayList<DidlViewModel> didlList = new ObservableArrayList<>();

    MainViewModel(UpnpApi upnpApi, AppDatabase appDatabase, ViewController viewController) {
        this.upnpApi = upnpApi;
        this.appDatabase = appDatabase;
        this.viewController = viewController;
    }



    Observable<Device> getMediaServers() {
        viewController.showProgressDialog(null, "Finding servers....");
        return upnpApi
                .getMediaServers();
    }

    ServiceConnection getServiceConnection() {
        return upnpApi.getServiceConnection();
    }


    void selectMediaServer(String name) {

        upnpApi.getMediaServersAsList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(devices -> {
                    devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d));
                    browse("0");
                });

    }

    void browse(String id) {
        viewController.showProgressDialog(null, "loading directory...");
        objectIdList.add(id);
        this.didlList.clear();
        upnpApi.browse(id)
                .timeout(2, TimeUnit.SECONDS, Flowable.create(e -> {
                    viewController.dismissProgressDialog();
                }, BackpressureStrategy.BUFFER))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(didlObject -> didlList.add(new DidlViewModel(didlObject)));
    }

    void goBack() {
        if (objectIdList.size() > 1) {
            objectIdList.remove(objectIdList.size() - 1);
            browse(objectIdList.get(objectIdList.size() - 1));
        }
    }

    boolean isAtRoot() {
        return objectIdList.size() == 1;
    }

    void cacheCurrentDirectory() {
        viewController.showProgressDialog(null, "Caching directory...");
        Log.d(TAG, "Cache started");
        long currentTime = System.currentTimeMillis();
        String directoryId = objectIdList.get(objectIdList.size() - 1);
        upnpApi.scan(directoryId)
                .timeout(15, TimeUnit.SECONDS, Flowable.create(e -> {
                    Log.d(TAG, "Cache complete");
                    viewController.dismissProgressDialog();
                    long elapsedTime = System.currentTimeMillis() - currentTime;
                    Crashlytics.log("Cache time: " + elapsedTime);
                }, BackpressureStrategy.BUFFER))
                .subscribe(didlObject -> {
                    Log.d(TAG, "Added Track to database: " + didlObject.getTitle());
                    appDatabase.trackDao().insert(createTrackObject(didlObject));
                }, Crashlytics::logException);
    }


    private Track createTrackObject(DIDLObject didlObject) {

        Track t = new Track();

        t.setTrackTitle(didlObject.getTitle() != null ? didlObject.getTitle() : "");
        t.setGenre(((AudioItem) didlObject).getFirstGenre() != null ? ((AudioItem) didlObject).getFirstGenre() : "");
        t.setMediaPath(didlObject.getFirstResource().getValue() != null ? didlObject.getFirstResource().getValue() : "");

        if (didlObject instanceof MusicTrack) {
            t.setAlbum(((MusicTrack) didlObject).getAlbum() != null ? ((MusicTrack) didlObject).getAlbum() : "");
            t.setArtist(((MusicTrack) didlObject).getFirstArtist().getName() != null ? ((MusicTrack) didlObject).getFirstArtist().getName() : "");
            t.setGenre(((MusicTrack) didlObject).getFirstGenre() != null ? ((MusicTrack) didlObject).getFirstGenre() : "");
            t.setTrackNumber(((MusicTrack) didlObject).getOriginalTrackNumber() != null ? ((MusicTrack) didlObject).getOriginalTrackNumber() : 0);
            t.setDate(((MusicTrack) didlObject).getDate() != null ? ((MusicTrack) didlObject).getDate() : "");
        }

        return t;
    }

    public interface ViewController{
        void showProgressDialog(String title, String message);
        void dismissProgressDialog();
    }
}
