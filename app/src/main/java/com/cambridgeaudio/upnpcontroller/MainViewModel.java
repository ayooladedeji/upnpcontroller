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
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

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

    Single<List<String>> getServers() {
        viewController.showProgressDialog(null, "Finding servers....");

        return upnpApi.getMediaServers().map(device -> device.getDetails().getFriendlyName()).toList();

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

        final boolean[] first = {true};
        Server server = new Server();

        long currentTime = System.currentTimeMillis();
        String directoryId = objectIdList.get(objectIdList.size() - 1);
        upnpApi.scan(directoryId)
                .timeout(15, TimeUnit.SECONDS, Flowable.create(e -> {
                    Log.d(TAG, "Cache complete");
                    viewController.dismissProgressDialog();
                    long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - currentTime);

                    Crashlytics.log("Cache time: " + time + System.lineSeparator() + "Tracks added: " + appDatabase.trackDao().getAll().size());
                }, BackpressureStrategy.BUFFER))
                .subscribe(didlObject -> {

                    if (first[0]) {
                        String serverName = upnpApi.getSelectedMediaServer().getDetails().getFriendlyName();
                        String serverAddress = upnpApi.getSelectedMediaServer().getDetails().getBaseURL().toString();
                        server.setName(serverName);
                        server.setAddress(serverAddress);
                        server.setWifi(getSSID());
                        appDatabase.serverDao().insert(server);
                        Log.d(TAG, "added Server: " + server.toString());
                        first[0] = false;
                    }

                    appDatabase.trackDao().insert(Track.create(didlObject, server.getName(), 0, 0));
                    Log.d(TAG, "Added Track to database: " + didlObject.getTitle());

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
    private void sendReport(String message) {

    }

    public interface ViewController {
        void showProgressDialog(String title, String message);

        void dismissProgressDialog();
    }
}
