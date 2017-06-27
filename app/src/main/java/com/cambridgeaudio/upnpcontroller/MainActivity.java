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
import android.view.View;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.dialogs.LoadingDialog;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.ClickHandler;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.CompositeItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.binder.DidlObjectBinder;
import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApiImpl;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainViewModel.ViewController {

    private final String TAG = "MainActivity";
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        mainViewModel = new MainViewModel(this, new UpnpApiImpl(), AppDatabase.getAppDatabase(this), this);

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getMediaServers();
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    private void getMediaServers() {
        //todo this is not very stable
        Menu menu = binding.navView.getMenu();
        Set<String> menuItems = new HashSet<>();


        mainViewModel.getMediaServers()
                .timeout(3, TimeUnit.SECONDS, new Observable<Device>() {
                    @Override
                    protected void subscribeActual(Observer<? super Device> observer) {
                        MainActivity.this.runOnUiThread(() -> {
                            for (String s : menuItems) {
                                menu.add(s);
                            }
                            dismissProgressDialog();
                        });
                    }
                })
                .map(device -> device.getDetails().getFriendlyName())
                .subscribe(menuItems::add);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!mainViewModel.isAtRoot()) {
            mainViewModel.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mainViewModel.selectMediaServer(item.getTitle().toString());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public ClickHandler<DidlViewModel> clickHandler() {
        return didlViewModel -> {
            mainViewModel.browse(didlViewModel.getId());
            Toast.makeText(MainActivity.this, didlViewModel.getTitle(), Toast.LENGTH_SHORT).show();
        };
    }

    public ItemBinder<DidlViewModel> itemViewBinder() {
        return new CompositeItemBinder<>(
                new DidlObjectBinder(com.cambridgeaudio.upnpcontroller.BR.didl, R.layout.item_didl));
    }

    public void cacheDirectory(View view) {
        mainViewModel.cacheCurrentDirectory();
    }


    @Override
    public void showProgressDialog(String title, String message) {
        LoadingDialog.show(this, title, message, false);
    }

    @Override
    public void dismissProgressDialog() {
        LoadingDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        mainViewModel.onDestroy();
        super.onDestroy();

    }
}
