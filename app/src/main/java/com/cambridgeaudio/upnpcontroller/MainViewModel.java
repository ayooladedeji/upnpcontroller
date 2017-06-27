package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.database.model.Album;
import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava2.async.DisposableFlowable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ayo on 12/06/2017.
 */

public class MainViewModel extends BaseObservable {

    private Context context;
    private final String TAG = "MainViewModel";
    private UpnpApi upnpApi;
    private AppDatabase appDatabase;
    private ArrayList<String> objectIdList = new ArrayList<>();
    private ViewController viewController;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Bindable
    public ObservableArrayList<DidlViewModel> didlList = new ObservableArrayList<>();

    public MainViewModel(Context context, UpnpApi upnpApi, AppDatabase appDatabase, ViewController viewController) {
        this.context = context;
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

    private void addServer() {
        Disposable d =
                Observable.create(e -> {
                    Server server = new Server();
                    String serverName = upnpApi.getSelectedMediaServer().getDetails().getFriendlyName();
                    String serverAddress = upnpApi.getSelectedMediaServer().getDetails().getPresentationURI().toString();
                    server.setName(serverName);
                    server.setAddress(serverAddress);
                    server.setWifi(getSSID());
                    appDatabase.serverDao().insert(server);
                    Log.d(TAG, "added Server: " + server.getName());
                }).subscribeOn(Schedulers.io()).subscribe();
        compositeDisposable.add(d);
    }

    void cacheCurrentDirectory() {
        long currentTime = System.currentTimeMillis();
        String directoryId = objectIdList.get(objectIdList.size() - 1);
        viewController.showProgressDialog(null, "Caching directory...");
        Log.d(TAG, "Cache started");
        final int[] counter = {0};
        addServer();
        upnpApi.scan(directoryId)
                .timeout(15, TimeUnit.SECONDS, Flowable.create(e -> {
                    Log.d(TAG, "Cache complete");
                    viewController.dismissProgressDialog();
                    sendReport(System.currentTimeMillis() - currentTime, counter[0]);
                }, BackpressureStrategy.BUFFER))
                .subscribe(didlObject -> {

                    Log.d(TAG, "didlObject isntance of" + (didlObject instanceof MusicTrack ? "MusicTrack" : "AudioItem"));

                    long albumId, artistId;
                    albumId = artistId = -1L;

                    if (didlObject instanceof MusicTrack) {
                        artistId = appDatabase.artistDao().insert(new Artist(((MusicTrack) didlObject).getFirstArtist().getName()))[0];
                        Log.d(TAG, "Added Artist to database: " + ((MusicTrack) didlObject).getFirstArtist().getName());

                        if (((MusicTrack) didlObject).getAlbum() != null) {
                            albumId = appDatabase.albumDao().insert(new Album(((MusicTrack) didlObject).getAlbum(), artistId))[0];
                            Log.d(TAG, "Added Album to database: " + ((MusicTrack) didlObject).getAlbum());
                        }
                    }

                    appDatabase.trackDao().insert(Track.create(didlObject, upnpApi.getSelectedMediaServer().getDetails().getFriendlyName(), albumId, artistId));
                    Log.d(TAG, "Added Track to database: " + didlObject.getTitle());
                    counter[0]++;

                }, throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Crashlytics.logException(throwable);
                });
    }


    //todo handle cases where wifi is not connected?
    private String getSSID() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    //todo
    private void sendReport(long duration, int tracksScanned) {
        Disposable d =
                Observable.create(e -> Answers.getInstance().logCustom(new CustomEvent("Cache Report")
                        .putCustomAttribute("Tracks scanned", tracksScanned)
                        .putCustomAttribute("Time elapsed", TimeUnit.MILLISECONDS.toSeconds(duration)))).subscribeOn(Schedulers.io()).subscribe();
        compositeDisposable.add(d);

    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

    public interface ViewController {
        void showProgressDialog(String title, String message);

        void dismissProgressDialog();
    }
}
