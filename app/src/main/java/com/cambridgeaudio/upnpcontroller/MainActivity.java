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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;
    //private DidlListAdapter adapter;
    //private ArrayList<DIDLObject> didlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new MainViewModel(this, new UpnpApiImpl());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainViewModel(viewModel);
        binding.setView(this);
        binding.executePendingBindings();
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                viewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );
        setSupportActionBar(binding.includedAppBar.toolbar);

       // RecyclerView recyclerView = binding.includedAppBar.includedContentMain.recyclerView;
        //didlList = new ArrayList<>();
       // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //adapter = new DidlListAdapter(this, didlList);
        //recyclerView.setAdapter(adapter);
        setUpDrawerLayout();


    }

    private void setUpDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.includedAppBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getMediaServers();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void getMediaServers() {
        Menu menu = binding.navView.getMenu();
        viewModel.getMediaServers().subscribe(list -> {
            for (Device mediaServer : list) {
                ArrayList<String> menuItems = new ArrayList<>();
                for (int x = 0; x < menu.size(); x++){
                    menuItems.add(menu.getItem(x).getTitle().toString());
                }
                if (!menuItems.contains(mediaServer.getDetails().getFriendlyName()))
                    menu.add(mediaServer.getDetails().getFriendlyName());
            }


        });


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

        viewModel.selectMediaServer(item.getTitle().toString());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
