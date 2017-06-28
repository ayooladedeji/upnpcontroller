package com.cambridgeaudio.upnpcontroller;

import android.support.multidex.MultiDexApplication;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

/**
 * Created by Ayo on 28/06/2017.
 */

public class Application extends MultiDexApplication {

    private UpnpApi upnpApi = null;
    private AppDatabase appDatabase = null;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public UpnpApi getUpnpApi(){
        if(upnpApi == null){
            upnpApi = new UpnpApiImpl();
            return upnpApi;
        }
        else
            return upnpApi;
    }

    public  AppDatabase getAppDatabase(){
        if(appDatabase == null){
            appDatabase = AppDatabase.getAppDatabase(this);
            return appDatabase;
        }else{
            return appDatabase;
        }
    }
}
