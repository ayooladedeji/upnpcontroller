package com.cambridgeaudio.upnpcontroller.upnp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by Ayo on 12/06/2017.
 */

public class UpnpApiImpl implements UpnpApi {

    private final String TAG = "UpnpApiImpl";

    //upnp objects
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    private ControlPoint controlPoint;
    private Device selectedMediaServer = null;
    private ArrayList<Device> mediaServers = new ArrayList<>();

    //rx objects
    private BehaviorSubject<ArrayList<Device>> mediaServersSubject = BehaviorSubject.create();
    private ReplaySubject<Device> testDeviceSubject = ReplaySubject.create();

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
        return Flowable.create(e -> controlPoint.execute(new Browse(selectedMediaServer.findService(new UDAServiceType("ContentDirectory")), id, BrowseFlag.DIRECT_CHILDREN, "*", 0, null, new SortCriterion(true, "dc:title")) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                didl.getContainers().forEach(e::onNext);
                didl.getItems().forEach(e::onNext);
            }

            @Override
            public void updateStatus(Status status) {
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            }
        }), BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<DIDLObject> recursiveScan(String id) {
        return browse(id)
                .flatMap(didlObject -> {
                    if (didlObject instanceof Container) {
                        return recursiveScan(didlObject.getId());
                    } else {
                        return Flowable.just(didlObject);
                    }
                });
    }

    @Override
    public Flowable<DIDLObject> scan(String id) {

        return recursiveScan(id)
                .subscribeOn(Schedulers.io())
                .filter(didlObject -> didlObject instanceof AudioItem);

//        return recursiveScan(id)
//                .subscribeOn(Schedulers.io())
//                .subscribe(didlObject -> {
//                    if (didlObject instanceof AudioItem) {
//                        Log.d(TAG, didlObject.getTitle());
//                    }
//                });
    }

    @Override
    public Observable<ArrayList<Device>> getMediaServersAsList() {
        return mediaServersSubject.observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public Observable<Device> getMediaServers() {
        return testDeviceSubject.observeOn(AndroidSchedulers.mainThread());
    }


    public Device getSelectedMediaServer() {
        return selectedMediaServer;
    }

    @Override
    public void selectMediaServer(Device device) {
        Log.d(TAG, "device selected: "+ device.getDetails().getFriendlyName());
        selectedMediaServer = device;
    }

    @Override
    public void destroy() {
        upnpService.getRegistry().removeListener(registryListener);

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
                testDeviceSubject.onNext(device);

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
