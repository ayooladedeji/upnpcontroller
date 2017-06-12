package com.cambridgeaudio.upnpcontroller.upnp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Ayo on 12/06/2017.
 */

public class UpnpApiImpl implements UpnpApi {

    private final String TAG = "UpnpApiImpl";

    //upnp objects
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    private ControlPoint controlPoint;
    private Device selectedDevice = null;
    private ArrayList<Device> mediaServers = new ArrayList<>();

    //rx objects
    private CompositeDisposable disposables = new CompositeDisposable();
    private BehaviorSubject<ArrayList<Device>> mediaServersSubject = BehaviorSubject.create();

    private ServiceConnection serviceConnection  = null;

    @Override
    public ServiceConnection getServiceConnection() {
        if(serviceConnection == null){
            serviceConnection = new ServiceConnection() {

                public void onServiceConnected(ComponentName className, IBinder service) {
                    upnpService = (AndroidUpnpService) service;

                    // Get ready for future device advertisements
                    upnpService.getRegistry().addListener(registryListener);

                    // Now add all devices to the list we already know about
                    for (Device device : upnpService.getRegistry().getDevices()) {
                        registryListener.deviceAdded(device);
                    }

                    // Search asynchronously for all devices, they will respond soon
                    upnpService.getControlPoint().search();
                    controlPoint = upnpService.getControlPoint();
                }

                public void onServiceDisconnected(ComponentName className) {
                    upnpService = null;
                }
            };
        }

        return serviceConnection;
    }

    @Override
    public Flowable<DIDLObject> browse(String id) {
        return null;
    }

    @Override
    public Flowable<DIDLObject> recursiveScan(String id) {
        return null;
    }

    @Override
    public void scan(String id) {

    }

    @Override
    public Observable<ArrayList<Device>> getMediaServers() {
        return mediaServersSubject.observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void selectMediaServer() {

    }

    @Override
    public Device getSelectedDevice() {
        return null;
    }

    @Override
    public void setSelectedDevice(Device device) {
        Log.d(TAG, "device selected: "+ device.getDetails().getFriendlyName());
        selectedDevice = device;
    }

    @Override
    public void destroy() {
        upnpService.getRegistry().removeListener(registryListener);
        disposables.clear();

    }

    private class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            Log.d(TAG,
                    "Discovery failed of '" + device.getDisplayString() + "': "
                            + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
            deviceRemoved(device);
        }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        void deviceAdded(final Device device) {
            if (Objects.equals(device.getType().getType(), "MediaServer")) {
                Log.d(TAG, "Discovered device: " + device.getDetails().getFriendlyName());
                for (Iterator<Device> iterator = mediaServers.iterator(); iterator.hasNext(); ) {
                    Device d = iterator.next();
                    if (d.getDetails().getFriendlyName().equals(device.getDetails().getFriendlyName())) {
                        iterator.remove();
                    }
                }
                mediaServers.add(device);
                mediaServersSubject.onNext(mediaServers);
            }
        }

        void deviceRemoved(final Device device) {
            if (Objects.equals(device.getType().getType(), "MediaServer")) {
                Log.d(TAG, "Removed device: " + device.getDetails().getFriendlyName());
                mediaServers.remove(device);
                mediaServersSubject.onNext(mediaServers);
            }

        }
    }

}
