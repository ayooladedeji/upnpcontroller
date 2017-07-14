package com.cambridgeaudio.upnpcontroller.viewmodels;

import android.content.Context;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.database.model.Album;
import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.DidlViewModel;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Math.toIntExact;

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

    public Observable<Device> getMediaServers() {
        //viewController.showProgressDialog(null, "Finding servers....");
        return upnpApi
                .getMediaServers();
    }

    public Observable<ArrayList<Device>> getMediaServersAsList() {
        return upnpApi.getMediaServersAsList();
    }

    public ServiceConnection getServiceConnection() {
        return upnpApi.getServiceConnection();
    }


    public void selectMediaServer(String name) {

//        upnpApi.getMediaServers()
//                .distinct()
//                .filter(d -> name.equals(d.getDetails().getFriendlyName()))
//                .subscribe(device -> {
//                    Log.d("SERVER", "CLICKED");
//                    upnpApi.selectMediaServer(device);
//                    browse("0");
//                });
        upnpApi.getMediaServersAsList()
                .observeOn(AndroidSchedulers.mainThread())
                //.subscribeOn(Schedulers.io())
                .subscribe(devices -> {
                    Log.d("SERVER", "CLICKED");
                    for (Device d : devices) {
                        if (name.equals(d.getDetails().getFriendlyName()))
                            upnpApi.selectMediaServer(d);
                    }
                    //devices.stream().filter(d -> name.equals(d.getDetails().getFriendlyName())).forEach(d -> upnpApi.selectMediaServer(d));
                    browse("0");
                });

    }

    public void browse(String id) {
        //viewController.showProgressDialog(null, "loading directory...");
        objectIdList.add(id);
        this.didlList.clear();
        upnpApi.browse(id)
                .timeout(2, TimeUnit.SECONDS, Flowable.create(e -> {
                    //viewController.dismissProgressDialog();
                }, BackpressureStrategy.BUFFER))
                //.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(didlObject -> didlList.add(new DidlViewModel(didlObject)));
    }

    public void goBack() {
        if (objectIdList.size() > 1) {
            objectIdList.remove(objectIdList.size() - 1);
            browse(objectIdList.get(objectIdList.size() - 1));
        }
    }

    public boolean isAtRoot() {
        return objectIdList.size() == 1;
    }

    private void addServer() {
        Disposable d =
                Observable.create(e -> {
                    Server server = new Server();
                    String serverName = upnpApi.getSelectedMediaServer().getDetails().getFriendlyName();
                    String serverAddress = upnpApi.getSelectedMediaServer().getDetails().getBaseURL().toString();
                    server.setName(serverName);
                    server.setAddress(serverAddress);
                    server.setWifi(getSSID());
                    appDatabase.serverDao().insert(server);
                    Log.d(TAG, "added Server: " + server.getName());
                }).subscribeOn(Schedulers.io()).subscribe();
        compositeDisposable.add(d);
    }

    private String getBaseURL(String s) {
        String[] parts = s.split("/");
        return parts[0] + "//" + parts[2] + "/";
    }

    public void cacheCurrentDirectory() {
        long currentTime = System.currentTimeMillis();
        String directoryId = objectIdList.get(objectIdList.size() - 1);
        viewController.showProgressDialog(null, "Caching directory...", false);
        Log.d(TAG, "Cache started");
        final int[] counter = {0};
        final boolean[] isFirst = {true};
        upnpApi.scan1(directoryId, 0, 1000)
                .retry()
                .timeout(4, TimeUnit.SECONDS, Flowable.create(e -> {
                    Log.d(TAG, "Cache complete");
                    sendReport(System.currentTimeMillis() - currentTime, counter[0], upnpApi.getSelectedMediaServer().getDetails().getFriendlyName() +", " +upnpApi.getSelectedMediaServer().getDetails().getModelDetails().toString());
                    viewController.dismissProgressDialog();
                    viewController.showDialog("Complete", "your chosen directory has now been indexed, you can view these files in the browse activity ", true);

                }, BackpressureStrategy.BUFFER))

                .subscribe(didlObject -> {

                    viewController.setDialogMessage(didlObject.getTitle());
                    if (isFirst[0]) {
                        Server server = new Server();
                        String serverName = upnpApi.getSelectedMediaServer().getDetails().getFriendlyName();
                        String serverAddress = getBaseURL(didlObject.getFirstResource().getValue());
                        server.setName(serverName);
                        server.setAddress(serverAddress);
                        server.setWifi(getSSID());
                        appDatabase.serverDao().insert(server);
                        Log.d(TAG, "added Server: " + server.getName());
                        isFirst[0] = false;
                    }

                    Log.d(TAG, "didlObject isntance of" + (didlObject instanceof MusicTrack ? "MusicTrack" : "AudioItem"));

                    long albumId, artistId;
                    albumId = artistId = -1L;

                    if (didlObject instanceof MusicTrack) {
                        String artistName = "unknown";

                        if (((MusicTrack) didlObject).getFirstArtist() != null)
                            if (((MusicTrack) didlObject).getFirstArtist().getName() != null)
                                artistName = ((MusicTrack) didlObject).getFirstArtist().getName();

                        artistId = appDatabase.artistDao().insert(new Artist(artistName))[0];
                        Log.d(TAG, "Added Artist to database: " + artistName);

                        if (((MusicTrack) didlObject).getAlbum() != null) {
                            albumId = appDatabase.albumDao().insert(new Album(((MusicTrack) didlObject).getAlbum(), artistId))[0];
                            Log.d(TAG, "Added Album to database: " + ((MusicTrack) didlObject).getAlbum());
                        }
                    }

                    long [] row = appDatabase.trackDao().insert(Track.create(didlObject, upnpApi.getSelectedMediaServer().getDetails().getFriendlyName(), albumId, artistId));
                    //if(row[0] >= 0){
                        Log.d(TAG, "Added Track to database: " + didlObject.getTitle());
                        counter[0]++;
                        Log.d(TAG, "Count: " + counter[0]);
                   //}


                }, throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Crashlytics.logException(throwable);
                    viewController.dismissProgressDialog();
                    viewController.showDialog("Error", "Failed to cache your directory", true);
                });
    }


    //todo handle cases where wifi is not connected?
    private String getSSID() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    private int getRSSI(){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
    }

    private void sendReport(long duration, int tracksScanned, String serverInfo) {

        Answers.getInstance().logCustom(new CustomEvent("INDEXING REPORT")
                .putCustomAttribute("Upnp/Dlna Server", serverInfo)
                .putCustomAttribute("Tracks scanned", String.valueOf(tracksScanned))
                .putCustomAttribute("Time elapsed", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration)))
                .putCustomAttribute("Scans per second", String.valueOf(tracksScanned / TimeUnit.MILLISECONDS.toSeconds(duration)))
                .putCustomAttribute("Device model", Build.MODEL)
                .putCustomAttribute("Device version", String.valueOf(Build.VERSION.SDK_INT))
                .putCustomAttribute("Wifi strength", String.valueOf(getRSSI()))
                .putCustomAttribute("Database size (bytes):", String.valueOf(appDatabase.getOpenHelper().getReadableDatabase().getPath().length()))
        );
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

    public interface ViewController {
        void showProgressDialog(String title, String message, boolean cancelable);

        void setDialogMessage(String s);

        void dismissProgressDialog();

        void showDialog(String title, String message, boolean cancelable);
    }
}
