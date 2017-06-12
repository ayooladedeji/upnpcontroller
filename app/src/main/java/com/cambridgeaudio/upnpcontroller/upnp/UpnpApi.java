package com.cambridgeaudio.upnpcontroller.upnp;

import android.content.ServiceConnection;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by Ayo on 12/06/2017.
 */

public interface UpnpApi {

    ServiceConnection getServiceConnection();

    Flowable<DIDLObject> browse(String id);

    Flowable<DIDLObject> recursiveScan(String id);

    void scan(String id);

    Observable<ArrayList<Device>> getMediaServers();

    void selectMediaServer();

    Device getSelectedDevice();

    void setSelectedDevice(Device device);

    void destroy();




}
