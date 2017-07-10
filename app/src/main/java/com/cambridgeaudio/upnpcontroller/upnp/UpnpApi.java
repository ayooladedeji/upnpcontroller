package com.cambridgeaudio.upnpcontroller.upnp;

import android.content.ServiceConnection;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ayo on 12/06/2017.
 */

public interface UpnpApi {

    ServiceConnection getServiceConnection();

    Flowable<DIDLObject> browse(String id);

    Flowable<List<DIDLObject>> browse1(String id, long start, long count);

    Flowable<DIDLObject> recursiveScan(String id);

    Flowable<DIDLObject> recursiveScan1(String id, long start, long count);

    Flowable<DIDLObject> scan(String id);

    Flowable<DIDLObject> scan1(String id, long start, long count);

    Observable<ArrayList<Device>> getMediaServersAsList();

    Observable<ArrayList<Device>> getMediaRenderersAsList();

    void selectMediaRenderer(Device device);

    Device getSelectedMediaRenderer();

    Observable<Device> getMediaServers();

    Observable<Device> getMediaRenderers();

    Device getSelectedMediaServer();

    void selectMediaServer(Device device);

    void destroy();

    void playTrack(String uri);

    void stopTrack();

    void pauseTrack();



}
