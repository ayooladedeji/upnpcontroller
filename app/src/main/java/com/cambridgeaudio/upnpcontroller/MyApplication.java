package com.cambridgeaudio.upnpcontroller;

import android.app.Activity;
import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by Ayo on 28/06/2017.
 */

public class MyApplication extends MultiDexApplication implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    private AppDatabase appDatabase = null;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerMyApplicationComponent.create().inject(this);
    }

    public  AppDatabase getAppDatabase(){
        if(appDatabase == null){
            appDatabase = AppDatabase.getAppDatabase(this);
            return appDatabase;
        }else{
            return appDatabase;
        }
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
