package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainActivity";
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new MainViewModel(new UpnpApiImpl());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainViewModel(mainViewModel);
        binding.setView(this);

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                mainViewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );

        setSupportActionBar(binding.toolbar);
        setUpDrawerLayout();


    }

    private void setUpDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getMediaServers();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void getMediaServers() {
        //todo can we clean this up?
        Menu menu = binding.navView.getMenu();
        mainViewModel.getMediaServers().subscribe(list -> {
                    for (Device mediaServer : list) {
                        ArrayList<String> menuItems = new ArrayList<>();
                        for (int x = 0; x < menu.size(); x++) {
                            menuItems.add(menu.getItem(x).getTitle().toString());
                        }
                        if (!menuItems.contains(mediaServer.getDetails().getFriendlyName()))
                            menu.add(mediaServer.getDetails().getFriendlyName());
                    }
                },
                throwable -> Log.e(TAG, throwable.getMessage()));
//        Menu menu = navigationView.getMenu();
//        mainViewModel.getMediaServers().subscribe(list -> {
//            menu.clear();
//            for (Device mediaServer : list) {
//                menu.add(mediaServer.getDetails().getFriendlyName());
//            }
//        });

//        Menu menu = binding.navView.getMenu();
//        mainViewModel.getMediaServers().subscribe(list -> {
//            for (Device mediaServer : list) {
//                ArrayList<String> menuItems = new ArrayList<>();
//                for (int x = 0; x < menu.size(); x++) {
//                    menuItems.add(menu.getItem(x).getTitle().toString());
//                }
//                if (!menuItems.contains(mediaServer.getDetails().getFriendlyName()))
//                    menu.add(mediaServer.getDetails().getFriendlyName());
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        mainViewModel.selectMediaServer(item.getTitle().toString());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
