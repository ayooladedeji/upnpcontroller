package com.cambridgeaudio.upnpcontroller.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.MyApplication;
import com.cambridgeaudio.upnpcontroller.BR;
import com.cambridgeaudio.upnpcontroller.R;
import com.cambridgeaudio.upnpcontroller.databinding.ActivityBrowseBinding;
import com.cambridgeaudio.upnpcontroller.dialogs.SimpleDialog;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.WrapContentLinearLayoutManager;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.ClickHandler;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.CompositeItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.adapter.binder.ItemBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.binder.AlbumObjectBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.binder.ArtistObjectBinder;
import com.cambridgeaudio.upnpcontroller.recyclerbinding.binder.TrackObjectBinder;
import com.cambridgeaudio.upnpcontroller.viewmodels.BrowseViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.AlbumViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.ArtistViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.TrackViewModel;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ayo on 27/06/2017.
 */

public class BrowseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BrowseViewModel.ViewController {


    private BrowseViewModel browseViewModel;
    private ActivityBrowseBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        browseViewModel =
                new BrowseViewModel(
                        this,
                        ((MyApplication) this.getApplication()).getUpnpApi(),
                        ((MyApplication) this.getApplication()).getAppDatabase(),
                        this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse);
        binding.setBrowseViewModel(browseViewModel);
        binding.setBrowseView(this);
        binding.trackList.setLayoutManager(new WrapContentLinearLayoutManager(this));

        setSupportActionBar(binding.toolbarBrowse);
        showTrackList();
        hideAlbumList();
        hideArtistList();
        browseViewModel.getInitialList();
        setUpNavMenu();
        setUpDrawerLayout();
        setTitle("Browse");
    }
    private void setUpNavMenu(){
        Menu menu = binding.navViewBrowse.getMenu();
        menu.add("Servers");
        SubMenu subMenu = menu.addSubMenu(0, 1, Menu.NONE, "Media Renderers");
        populateMediaRenderers(subMenu);

    }

    private void populateMediaRenderers(SubMenu subMenu) {
        browseViewModel.getMediaRenderers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(device -> device.getDetails().getFriendlyName())
                .distinct()
                .subscribe(subMenu::add);
    }
    private void setUpDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayoutBrowse, binding.toolbarBrowse, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayoutBrowse.addDrawerListener(toggle);
        toggle.syncState();
        binding.navViewBrowse.setNavigationItemSelectedListener(this);
        binding.drawerLayoutBrowse.openDrawer(GravityCompat.START);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayoutBrowse;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_browse, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) BrowseActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(BrowseActivity.this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    final SearchView sv = (SearchView) searchItem.getActionView();
                    browseViewModel.registerSearchView(sv);
                    return true;
                }
            });

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String itemClicked = item.getTitle().toString();
        switch(itemClicked) {
            case "Servers":
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(BrowseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }, 250);
                break;
            default:
                browseViewModel.selectMediaRenderer(item.getTitle().toString());
                break;
        }
        DrawerLayout drawer = binding.drawerLayoutBrowse;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showTrackList() {
        this.runOnUiThread(() -> binding.tracksView.setVisibility(View.VISIBLE));
    }

    @Override
    public void showAlbumList() {
        this.runOnUiThread(() -> binding.albumsView.setVisibility(View.VISIBLE));
    }

    @Override
    public void showArtistList() {
        this.runOnUiThread(() -> binding.artistsView.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideTrackList() {
        this.runOnUiThread(() -> binding.tracksView.setVisibility(View.GONE));
    }

    @Override
    public void hideAlbumList() {
        this.runOnUiThread(() -> binding.albumsView.setVisibility(View.GONE));
    }

    @Override
    public void hideArtistList() {
        this.runOnUiThread(() -> binding.artistsView.setVisibility(View.GONE));
    }

    @Override
    public void showDialog(String title, String message, boolean cancelable) {
        this.runOnUiThread(() -> SimpleDialog.show(this, title, message, false));
    }

    @Override
    public void dismissDialog() {
        SimpleDialog.dismiss();
    }

    public ClickHandler<TrackViewModel> trackClickHandler() {
        return trackViewModel -> {
            browseViewModel.play(trackViewModel.getModel());
            Toast.makeText(BrowseActivity.this, trackViewModel.getTitle(), Toast.LENGTH_SHORT).show();
        };
    }

    public ItemBinder<TrackViewModel> trackItemViewBinder() {
        return new CompositeItemBinder<>(
                new TrackObjectBinder(BR.track, R.layout.item_track));
    }

    public ClickHandler<AlbumViewModel> albumClickHandler() {
        return albumViewModel -> {
            Toast.makeText(BrowseActivity.this, albumViewModel.getTitle(), Toast.LENGTH_SHORT).show();
            browseViewModel.onContainerClick(albumViewModel.getId(), "album");
        };
    }

    public ItemBinder<AlbumViewModel> albumItemViewBinder() {
        return new CompositeItemBinder<>(
                new AlbumObjectBinder(BR.album, R.layout.item_album));
    }

    public ClickHandler<ArtistViewModel> artistClickHandler() {
        return artistViewModel -> {
            Toast.makeText(BrowseActivity.this, artistViewModel.getName(), Toast.LENGTH_SHORT).show();
            browseViewModel.onContainerClick(artistViewModel.getId(), "artist");
        };
    }

    public ItemBinder<ArtistViewModel> artistItemViewBinder() {
        return new CompositeItemBinder<>(
                new ArtistObjectBinder(BR.artist, R.layout.item_artist));
    }

    @Override
    protected void onDestroy() {
        browseViewModel.onDestroy();
        super.onDestroy();
    }


}
