package com.cambridgeaudio.upnpcontroller.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.util.Log;
import android.widget.SearchView;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.AlbumViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.ArtistViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.TrackViewModel;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;
import com.jakewharton.rxbinding2.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.SearchViewQueryTextEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
/**
 * Created by Ayo on 27/06/2017.
 */

public class BrowseViewModel extends BaseObservable {

    private final String TAG = "BrowseViewModel";
    @Bindable
    public ObservableArrayList<TrackViewModel> trackList = new ObservableArrayList<>();
    @Bindable
    public ObservableArrayList<AlbumViewModel> albumList = new ObservableArrayList<>();
    @Bindable
    public ObservableArrayList<ArtistViewModel> artistList = new ObservableArrayList<>();


    private Context context;
    private UpnpApi upnpApi;
    private AppDatabase appDatabase;
    private ViewController viewController;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BrowseViewModel(Context context, UpnpApi upnpApi, AppDatabase appDatabase, ViewController viewController) {
        this.context = context;
        this.upnpApi = upnpApi;
        this.appDatabase = appDatabase;
        this.viewController = viewController;
    }


    public void registerSearchView(SearchView searchView) {
        compositeDisposable.add(RxSearchView.queryTextChangeEvents(searchView)
                .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                .filter(changes -> !Objects.equals(changes.queryText().toString().trim(), ""))
                .subscribeWith(searchObserver()));

    }

    private DisposableObserver<SearchViewQueryTextEvent> searchObserver() {

        return new DisposableObserver<SearchViewQueryTextEvent>() {

            @Override
            public void onNext(SearchViewQueryTextEvent searchViewQueryTextEvent) {
                String query = searchViewQueryTextEvent.queryText().toString();

                trackList.clear();
                albumList.clear();
                artistList.clear();

                appDatabase
                        .trackDao()
                        .getAllByTitle(query)
                        .subscribeOn(Schedulers.io())
                        //.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                track -> trackList.add(new TrackViewModel(track)),
                                throwable -> {
                                    Log.d(TAG, throwable.getMessage());
                                    Crashlytics.logException(throwable);
                                },
                                BrowseViewModel.this::handleTrackListView);

                appDatabase
                        .albumDao()
                        .getByTitle(query)
                        //.subscribeOn(Schedulers.io())
                       // .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(album -> albumList.add(new AlbumViewModel(album)),
                                throwable -> {
                                    Log.d(TAG, throwable.getMessage());
                                    Crashlytics.logException(throwable);
                                }, BrowseViewModel.this::handleAlbumListView);

                appDatabase
                        .artistDao()
                        .getByName(query)
                        //.subscribeOn(Schedulers.io())
                        //.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(artist -> artistList.add(new ArtistViewModel(artist)),
                                throwable -> {
                                    Log.d(TAG, throwable.getMessage());
                                    Crashlytics.logException(throwable);
                                }, BrowseViewModel.this::handleArtistListView);

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
                Crashlytics.logException(e);
                viewController.showDialog( "Error", "Could not load music", true);
            }

            @Override
            public void onComplete() {
            }
        };
    }

    public void getTrackList(){
//        appDatabase.trackDao().getAll().subscribe(track -> {
//            Log.d(TAG, "found track "+ track.getTrackTitle());
//            trackList.add(new TrackViewModel(track));
//        });



        Disposable d =
                Observable.create(e -> {
                    for(Track track : appDatabase.trackDao().getAllList())
                        trackList.add(new TrackViewModel(track));
                }).subscribeOn(Schedulers.io()).subscribe();
        compositeDisposable.add(d);
    }
    private void handleTrackListView() {
        if (trackList.isEmpty())
            viewController.hideTrackList();
        else
            viewController.showTrackList();
    }

    private void handleAlbumListView() {
        if (albumList.isEmpty())
            viewController.hideAlbumList();
        else
            viewController.showAlbumList();
    }

    private void handleArtistListView() {
        if (artistList.isEmpty())
            viewController.hideArtistList();
        else
            viewController.showArtistList();
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

    public interface ViewController {

        void showTrackList();

        void showAlbumList();

        void showArtistList();

        void hideTrackList();

        void hideAlbumList();

        void hideArtistList();

        void showDialog(String title, String message, boolean cancelable);

        void dismissDialog();

    }
}
