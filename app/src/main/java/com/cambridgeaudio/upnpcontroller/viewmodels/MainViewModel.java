package com.cambridgeaudio.upnpcontroller.viewmodels;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
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

    public Observable<Device> getMediaServers() {
        return upnpApi
                .getMediaServers();
    }

    public ServiceConnection getServiceConnection() {
        return upnpApi.getServiceConnection();
    }


    public void selectMediaServer(String name) {
        upnpApi.getMediaServersAsList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(devices -> {
                    Log.d("SERVER", "CLICKED");
                    for (Device d : devices) {
                        if (name.equals(d.getDetails().getFriendlyName()))
                            upnpApi.selectMediaServer(d);
                    }
                    browse("0");
                });

    }

    public void browse(String id) {
        this.didlList.clear();

        objectIdList.add(id);

        for (DIDLObject didlObject : browse1(id))
            didlList.add(new DidlViewModel(didlObject));
    }

    public List<DIDLObject> browse1(String id) {
        return upnpApi.browse(id, 0L, 50L).retry()
                .blockingFirst();
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

    private String getBaseURL(String s) {
        String[] parts = s.split("/");
        return parts[0] + "//" + parts[2] + "/";
    }

    public void wipe(){
    }

    public void cacheCurrentDirectory() {
        long currentTime = System.currentTimeMillis();
        String directoryId = objectIdList.get(objectIdList.size() - 1);
        viewController.showProgressDialog(null, "Caching directory...", false);
        Log.d(TAG, "Cache started");
        final int[] counter = {0};
        final boolean[] isFirst = {true};
        upnpApi.scan(directoryId, 0, 1000)
                .retry()
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
                                server = null;
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

                            appDatabase.trackDao().insert(Track.create(didlObject, upnpApi.getSelectedMediaServer().getDetails().getFriendlyName(), albumId, artistId));
                            Log.d(TAG, "Added Track to database: " + didlObject.getTitle());
                            counter[0]++;
                            Log.d(TAG, "Count: " + counter[0]);


                        }, throwable -> {
                            Log.d(TAG, throwable.getMessage());
                            Crashlytics.logException(throwable);
                            viewController.dismissProgressDialog();
                            viewController.showDialog("Error", "Failed to cache your directory", true, null);
                        }
                        , () -> {
                            Log.d(TAG, "Cache complete");
                            viewController.dismissProgressDialog();
                            String dialogMessage = "Indexing is now complete, after clicking okay an email client will appear please click send so that we can analyse the results of the scan";
                            viewController.showDialog("Complete", dialogMessage, true,
                                    sendEmailClickListener(generateReport(
                                            System.currentTimeMillis() - currentTime, counter[0],
                                            upnpApi.getSelectedMediaServer().getDetails().getFriendlyName() + ", " + upnpApi.getSelectedMediaServer().getDetails().getModelDetails().getModelName())));

                        });
    }

    private String getSSID() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    private int getRSSI() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
    }

    private String generateReport(long duration, int tracksScanned, String serverInfo) {

        return String.format(Locale.getDefault(),
                "Upnp/Dlna Server : %s \n" +
                        "Tracks scanned : %d \n" +
                        "Time elapsed : %d \n" +
                        "Scans per second : %d \n" +
                        "Device model : %s \n" +
                        "Device version : %d \n" +
                        "Wifi strength : %d \n" +
                        "Database size (bytes) : %d \n",
                serverInfo, tracksScanned, TimeUnit.MILLISECONDS.toSeconds(duration), tracksScanned == 0 ? 0 : tracksScanned / TimeUnit.MILLISECONDS.toSeconds(duration),
                Build.MODEL, Build.VERSION.SDK_INT, getRSSI(), appDatabase.getOpenHelper().getReadableDatabase().getPath().length());

    }


    private DialogInterface.OnClickListener sendEmailClickListener(String report) {
        return (dialogInterface, i) -> {
            dialogInterface.cancel();
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"ayo.adedeji@cambridgeaudio.com"});
            Email.putExtra(Intent.EXTRA_SUBJECT, "BetaTesting");
            Email.putExtra(Intent.EXTRA_TEXT, report);
            context.startActivity(Intent.createChooser(Email, "Send Feedback:"));

        };
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

    public interface ViewController {
        void showProgressDialog(String title, String message, boolean cancelable);

        void setDialogMessage(String s);

        void dismissProgressDialog();

        void showDialog(String title, String message, boolean cancelable, DialogInterface.OnClickListener listener);

    }
}
