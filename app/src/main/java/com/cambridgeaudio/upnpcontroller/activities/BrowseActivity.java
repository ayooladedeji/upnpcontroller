package com.cambridgeaudio.upnpcontroller.activities;

import android.app.SearchManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.cambridgeaudio.upnpcontroller.Application;
import com.cambridgeaudio.upnpcontroller.BR;
import com.cambridgeaudio.upnpcontroller.R;
import com.cambridgeaudio.upnpcontroller.databinding.ActivityBrowseBinding;
import com.cambridgeaudio.upnpcontroller.dialogs.SimpleDialog;
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

/**
 * Created by Ayo on 27/06/2017.
 */

public class BrowseActivity extends AppCompatActivity implements BrowseViewModel.ViewController {


    private BrowseViewModel browseViewModel;
    private ActivityBrowseBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        browseViewModel =
                new BrowseViewModel(
                        this,
                        ((Application) this.getApplication()).getUpnpApi(),
                        ((Application) this.getApplication()).getAppDatabase(),
                        this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse);
        binding.setBrowseViewModel(browseViewModel);
        binding.setBrowseView(this);
        binding.albumList.setLayoutManager(new LinearLayoutManager(this));
        binding.trackList.setLayoutManager(new LinearLayoutManager(this));
        binding.artistList.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.toolbarBrowse);
        showTrackList();
        browseViewModel.getTrackList();
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
            browseViewModel.registerSearchView(searchView);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showTrackList() {
        binding.trackList.setVisibility(View.VISIBLE);
    }

    @Override
    public void showAlbumList() {
        binding.albumList.setVisibility(View.VISIBLE);
    }

    @Override
    public void showArtistList() {
        binding.artistList.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTrackList() {
        binding.trackList.setVisibility(View.GONE);
    }

    @Override
    public void hideAlbumList() {
        binding.albumList.setVisibility(View.GONE);
    }

    @Override
    public void hideArtistList() {
        binding.artistList.setVisibility(View.GONE);
    }

    @Override
    public void showDialog(String title, String message, boolean cancelable) {
        SimpleDialog.show(this, title, message, false);
    }

    @Override
    public void dismissDialog() {
        SimpleDialog.dismiss();
    }

    public ClickHandler<TrackViewModel> trackClickHandler() {
        return trackViewModel -> {
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
        };
    }

    public ItemBinder<AlbumViewModel> albumItemViewBinder() {
        return new CompositeItemBinder<>(
                new AlbumObjectBinder(BR.album, R.layout.item_album));
    }

    public ClickHandler<ArtistViewModel> artistClickHandler() {
        return artistViewModel -> {
            Toast.makeText(BrowseActivity.this, artistViewModel.getName(), Toast.LENGTH_SHORT).show();
        };
    }

    public ItemBinder<ArtistViewModel> artistItemViewBinder() {
        return new CompositeItemBinder<>(
                new ArtistObjectBinder(BR.artist, R.layout.item_artist));
    }

}
