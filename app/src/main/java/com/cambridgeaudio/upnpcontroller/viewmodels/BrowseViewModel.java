package com.cambridgeaudio.upnpcontroller.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.util.AndroidException;
import android.util.Log;
import android.widget.SearchView;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.database.model.Album;
import com.cambridgeaudio.upnpcontroller.database.model.Artist;
import com.cambridgeaudio.upnpcontroller.database.model.Server;
import com.cambridgeaudio.upnpcontroller.database.model.Track;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.AlbumViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.ArtistViewModel;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.TrackViewModel;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.crashlytics.android.Crashlytics;
import com.jakewharton.rxbinding2.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.SearchViewQueryTextEvent;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;
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

                String query = "%%" + searchViewQueryTextEvent.queryText().toString() + "%%";

                trackList.clear();
                albumList.clear();
                artistList.clear();

                Disposable d =
                        Observable.create(e -> {
                            for (Track track : appDatabase.trackDao().getAllByTitle(query))
                                trackList.add(new TrackViewModel(track));

                            if (trackList.isEmpty())
                                viewController.hideTrackList();
                            else
                                viewController.showTrackList();

                        }).subscribeOn(Schedulers.io()).subscribe();

                Disposable d1 =
                        Observable.create(e -> {
                            for (Album album : appDatabase.albumDao().getByTitle(query))
                                albumList.add(new AlbumViewModel(album));

                            if (albumList.isEmpty())
                                viewController.hideAlbumList();
                            else
                                viewController.showAlbumList();

                        }).subscribeOn(Schedulers.io()).subscribe();

                Disposable d2 =
                        Observable.create(e -> {
                            for (Artist artist : appDatabase.artistDao().getAll())
                                artistList.add(new ArtistViewModel(artist));

                            if (artistList.isEmpty())
                                viewController.hideArtistList();
                            else
                                viewController.showArtistList();

                        }).subscribeOn(Schedulers.io()).subscribe();

                compositeDisposable.addAll(d, d1, d2);

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
                Crashlytics.logException(e);
                viewController.showDialog("Error", "Could not load music", true);
            }

            @Override
            public void onComplete() {
                if (trackList.isEmpty() && albumList.isEmpty() && artistList.isEmpty())
                    getInitialList();
            }
        };
    }

    public void getInitialList() {

        Disposable d =
                Observable.create(e -> {
                    for (Track track : appDatabase.trackDao().getAll())
                        trackList.add(new TrackViewModel(track));

                    if (trackList.isEmpty())
                        trackList.add(new TrackViewModel(new Track("EMPTY")));
                }).subscribeOn(Schedulers.io()).subscribe();

        Disposable d1 =
                Observable.create(e -> {
                    for (Album album : appDatabase.albumDao().getAll())
                        albumList.add(new AlbumViewModel(album));

                    if (albumList.isEmpty())
                        albumList.add(new AlbumViewModel(new Album("EMPTY")));
                }).subscribeOn(Schedulers.io()).subscribe();

        Disposable d2 =
                Observable.create(e -> {
                    for (Artist artist : appDatabase.artistDao().getAll())
                        artistList.add(new ArtistViewModel(artist));

                    if (artistList.isEmpty())
                        artistList.add(new ArtistViewModel(new Artist("EMPTY")));
                })
                        .subscribeOn(Schedulers.io())
                        //.observeOn(AndroidSchedulers.mainThread())
                        .subscribe();

        compositeDisposable.addAll(d, d1, d2);
    }

    public void onContainerClick(long id, String container) {
        Disposable d =
                Observable.create(e -> {

                    List<Track> tracks = new ArrayList<>();

                    switch (container) {
                        case "artist":
                            tracks = appDatabase.trackDao().getAllByArtistId(id);
                            break;
                        case "album":
                            tracks = appDatabase.trackDao().getAllByAlbumId(id);
                            break;

                    }

                    trackList.clear();
                    for (Track track : tracks)
                        trackList.add(new TrackViewModel(track));

                    if (artistList.isEmpty())
                        trackList.add(new TrackViewModel(new Track("EMPTY")));

                    viewController.hideArtistList();
                    viewController.hideAlbumList();

                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();

        compositeDisposable.add(d);
    }

    public Observable<Device> getMediaRenderers() {
        return upnpApi.getMediaRenderers();
    }

    public void selectMediaRenderer(String name) {

        upnpApi.getMediaRenderers()
                .filter(device -> Objects.equals(device.getDetails().getFriendlyName(), name))
                .subscribe(device ->  upnpApi.selectMediaRenderer(device));
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

    public void play(Track track) {

        if (upnpApi.getSelectedMediaRenderer() == null) {
            viewController.showDialog("Error", "please select a media renderer from the navigation drawer", true);
        }
        Disposable d =
                Observable.create(e -> {
                    Server server = appDatabase.serverDao().getByName(track.getServerName());
                    String uri = server.getAddress() + track.getMediaPath();
                    upnpApi.playTrack(uri);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe();

        compositeDisposable.add(d);


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
