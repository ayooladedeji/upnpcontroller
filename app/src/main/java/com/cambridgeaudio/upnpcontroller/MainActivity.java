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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.adapter.ClickHandler;
import com.cambridgeaudio.upnpcontroller.adapter.binder.CompositeItemBinder;
import com.cambridgeaudio.upnpcontroller.adapter.binder.ItemBinder;
import com.cambridgeaudio.upnpcontroller.binder.DidlObjectBinder;
import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new MainViewModel(new UpnpApiImpl());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainViewModel(mainViewModel);
        binding.setView(this);
        binding.didlList.setLayoutManager(new LinearLayoutManager(this));
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                mainViewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );

        setSupportActionBar(binding.toolbar);
        setUpDrawerLayout();


    }

    private void setUpDrawerLayout() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getMediaServers();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void getMediaServers() {
        Menu menu = binding.navView.getMenu();
        mainViewModel.getMediaServers().subscribe(list -> {
            menu.clear();
            for (Device mediaServer : list) {
                menu.add(mediaServer.getDetails().getFriendlyName());
            }
        });
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
        DrawerLayout drawer = binding.drawerLayout;
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
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public ClickHandler<DidlViewModel> clickHandler() {
        return didlViewModel -> Toast.makeText(MainActivity.this, didlViewModel.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public ItemBinder<DidlViewModel> itemViewBinder() {
        return new CompositeItemBinder<>(
                new DidlObjectBinder(com.cambridgeaudio.upnpcontroller.BR.didl, R.layout.item_didl)
        );
    }
}
