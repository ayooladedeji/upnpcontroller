package com.cambridgeaudio.upnpcontroller;

/**
 * Created by Ayo on 20/07/2017.
 */

import com.cambridgeaudio.upnpcontroller.activities.BrowseActivity;
import com.cambridgeaudio.upnpcontroller.activities.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MyApplicationModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivityInjector();
    @ContributesAndroidInjector
    abstract BrowseActivity contributeBrowseActivityInjector();

}

