package com.cambridgeaudio.upnpcontroller.upnp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Switch;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private Device selectedMediaRenderer = null;

    private ArrayList<Device> mediaServers = new ArrayList<>();
    private ArrayList<Device> mediaRenderers = new ArrayList<>();

    //rx objects
    private BehaviorSubject<ArrayList<Device>> mediaServerListSubject = BehaviorSubject.create();
    private BehaviorSubject<ArrayList<Device>> mediaRendererListSubject = BehaviorSubject.create();

    private ReplaySubject<Device> mediaServerSubject = ReplaySubject.create();
    private ReplaySubject<Device> mediaRendererSubject = ReplaySubject.create();

    private ServiceConnection serviceConnection = null;

    @Override
    public ServiceConnection getServiceConnection() {
        if (serviceConnection == null) {
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
        return Flowable.create(e -> controlPoint.execute(new Browse(selectedMediaServer.findService(new UDAServiceType("ContentDirectory")), id, BrowseFlag.DIRECT_CHILDREN, "*", 0, 50L, new SortCriterion(true, "dc:title")) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                for(DIDLObject didlObject : didl.getContainers())
                    e.onNext(didlObject);

                for(DIDLObject didlObject : didl.getItems())
                    e.onNext(didlObject);

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
    }

    @Override
    public Observable<ArrayList<Device>> getMediaServersAsList() {
        return mediaServerListSubject;
    }

    @Override
    public Observable<ArrayList<Device>> getMediaRenderersAsList() {
        return mediaRendererListSubject;
    }

    @Override
    public void selectMediaRenderer(Device device) {
        Log.d(TAG, "Renderer selected: " + device.getDetails().getFriendlyName());
        selectedMediaRenderer = device;

    }

    @Override
    public Device getSelectedMediaRenderer() {
        return selectedMediaRenderer;

    }


    @Override
    public Observable<Device> getMediaServers() {
        return mediaServerSubject;
    }

    @Override
    public Observable<Device> getMediaRenderers() {
        return mediaRendererSubject;
    }


    public Device getSelectedMediaServer() {
        return selectedMediaServer;
    }

    @Override
    public void selectMediaServer(Device device) {
        Log.d(TAG, "device selected: " + device.getDetails().getFriendlyName());
        selectedMediaServer = device;
    }

    @Override
    public void destroy() {
        upnpService.getRegistry().removeListener(registryListener);

    }

    @Override
    public void playTrack(String uri) {

        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Stop(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                Log.v(TAG, "Success stopping ! ");
                callback();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Log.w(TAG, "Fail to stop ! " + arg2);
                callback();
            }

            public void callback() {
                setURI(uri);
            }
        });
    }

    private void setURI(String uri) {
        Log.i(TAG, "Set uri to " + uri);

        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.i(TAG, "URI successfully set !");
                commandPlay();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Log.w(TAG, "Fail to set URI ! " + arg2);
            }
        });
    }

    private void commandPlay() {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Play(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                Log.v(TAG, "Success playing ! ");
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Log.w(TAG, "Fail to play ! " + arg2);
            }
        });
    }

    @Override
    public void stopTrack() {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Stop(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation) {
                Log.v(TAG, "Success stopping ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Log.w(TAG, "Fail to stop ! " + arg2);
            }
        });
    }

    @Override
    public void pauseTrack() {

    }

    private Service getAVTransportService() {
        if (selectedMediaRenderer == null) {
            Log.d(TAG, "media renderer is null");
            return null;
        }
        return selectedMediaRenderer.findService(new UDAServiceType("AVTransport"));

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

            String deviceType = device.getType().getType();
            String deviceName = device.getDetails().getFriendlyName();

            if (deviceType.equals("MediaServer")) {
                Log.d(TAG, "Discovered device: " + deviceName);
                mediaServerSubject.onNext(device);
                mediaServers.add(device);
                mediaServerListSubject.onNext(mediaServers);
            }
            else if (deviceType.equals("MediaRenderer")) {
                Log.d(TAG, "Discovered MediaRenderer: " + deviceName);
                mediaRendererSubject.onNext(device);
                mediaRenderers.add(device);
                mediaRendererListSubject.onNext(mediaRenderers);
            }

        }

        void deviceRemoved(final Device device) {
            if (device.getType().getType() == "MediaServer") {
                Log.d(TAG, "Removed device: " + device.getDetails().getFriendlyName());
                mediaServers.remove(device);
                mediaServerListSubject.onNext(mediaServers);
            }
            else if (device.getType().getType() == "MediaRenderer") {
                mediaRenderers.remove(device);
                mediaRendererListSubject.onNext(mediaRenderers);
            }


        }
    }

}
