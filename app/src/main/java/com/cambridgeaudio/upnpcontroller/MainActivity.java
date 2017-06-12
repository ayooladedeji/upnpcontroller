package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingComponent;
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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
//    @BindView(R.id.nav_view)
//    NavigationView navigationView;
//    @BindView(R.id.drawer_layout)
//    DrawerLayout drawer;




    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        viewModel = new MainViewModel(this, new UpnpApiImpl());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainViewModel(viewModel);
        binding.setView(this);
        binding.didlList.setLayoutManager(new LinearLayoutManager(this));
        binding.didlList.setAdapter(new DidlRecyclerViewAdapter(this, new ArrayList<>()));
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                viewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );

        setSupportActionBar(binding.includedAppBar.test);
        setUpDrawerLayout();


    }

    private void setUpDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getMediaServers();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void getMediaServers() {
        Menu menu = binding.navView.getMenu();
        viewModel.getMediaServers().subscribe(list -> {
            menu.clear();
            for (Device mediaServer : list) {
                menu.add(mediaServer.getDetails().getFriendlyName());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
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
        viewModel.setSelectedDevice(item.getTitle().toString());
        viewModel.browse("0");
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
