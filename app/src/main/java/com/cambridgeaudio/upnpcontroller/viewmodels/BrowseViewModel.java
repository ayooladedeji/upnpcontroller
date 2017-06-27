package com.cambridgeaudio.upnpcontroller.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.widget.SearchView;

import com.cambridgeaudio.upnpcontroller.database.AppDatabase;
import com.cambridgeaudio.upnpcontroller.viewmodels.itemviews.TrackViewModel;
import com.cambridgeaudio.upnpcontroller.upnp.UpnpApi;
import com.jakewharton.rxbinding2.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.SearchViewQueryTextEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

import static java.lang.String.format;

/**
 * Created by Ayo on 27/06/2017.
 */

public class BrowseViewModel extends BaseObservable {

    @Bindable
    public ObservableArrayList<TrackViewModel> trackList = new ObservableArrayList<>();

    private Context context;
    private UpnpApi upnpApi;
    private AppDatabase appDatabase;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BrowseViewModel(Context context, UpnpApi upnpApi, AppDatabase appDatabase) {
        this.context = context;
        this.upnpApi = upnpApi;
        this.appDatabase = appDatabase;
    }


    public void registerSearchView(SearchView searchView) {
        compositeDisposable.add(RxSearchView.queryTextChangeEvents(searchView)
                .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
                .filter(changes -> !Objects.equals(changes.queryText().toString().trim(), ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchObserver()));

    }

    private DisposableObserver<SearchViewQueryTextEvent> searchObserver() {

        return new DisposableObserver<SearchViewQueryTextEvent>() {

            @Override
            public void onNext(SearchViewQueryTextEvent searchViewQueryTextEvent) {
                String query = searchViewQueryTextEvent.queryText().toString();
                //todo update list with search results
            }

            @Override
            public void onError(Throwable e) {
                //todo handle errors
            }

            @Override
            public void onComplete() {
            }
        };
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }

}
