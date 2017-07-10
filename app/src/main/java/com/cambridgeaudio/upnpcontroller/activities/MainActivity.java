package com.cambridgeaudio.upnpcontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Handler;
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
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.MyApplication;
import com.cambridgeaudio.upnpcontroller.R;
import com.cambridgeaudio.upnpcontroller.databinding.ActivityMainBinding;
import com.cambridgeaudio.upnpcontroller.dialogs.LoadingDialog;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.WrapContentLinearLayoutManager;
import com.cambridgeaudio.upnpcontroller.viewmodels.MainViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.DidlViewModel;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.ClickHandler;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.CompositeItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.binder.DidlObjectBinder;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainViewModel.ViewController {

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        mainViewModel =
                new MainViewModel(
                        this,
                        ((MyApplication) this.getApplication()).getUpnpApi(),
                        ((MyApplication) this.getApplication()).getAppDatabase(),
                        this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMainViewModel(mainViewModel);
        binding.setView(this);
        binding.didlList.setLayoutManager(new WrapContentLinearLayoutManager(this));

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                mainViewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );

        setSupportActionBar(binding.toolbar);
        setUpNavMenu();
        setUpDrawerLayout();

    }

    private void setUpNavMenu(){
        Menu menu = binding.navView.getMenu();
        menu.add("Browse");
        SubMenu subMenu = menu.addSubMenu(0, 1, Menu.NONE, "Media Servers");
        populateMediaServers(subMenu);

    }
    private void populateMediaServers(SubMenu subMenu) {
        mainViewModel.getMediaServers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(device -> device.getDetails().getFriendlyName())
                .distinct()
                .subscribe(subMenu::add);
    }

    private void setUpDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawerLayout.openDrawer(GravityCompat.START);
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
        Log.d("CLICKED", "onNavigationItemSelected");

        String itemClicked = item.getTitle().toString();
        switch(itemClicked) {
            case "Browse":
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, BrowseActivity.class);
                    startActivity(intent);
                    finish();
                }, 250);
                break;
            default:
                mainViewModel.selectMediaServer(item.getTitle().toString());
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
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
    public void showProgressDialog(String title, String message, boolean cancelable) {
        LoadingDialog.show(this, title, message, cancelable);
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
