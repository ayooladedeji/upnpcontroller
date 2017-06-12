package com.cambridgeaudio.upnpcontroller;

import android.support.v7.widget.RecyclerView;

import com.cambridgeaudio.upnpcontroller.databinding.DidlDataBinding;

/**
 * Created by ayoola on 12/06/2017.
 */

public class DidlViewHolder extends RecyclerView.ViewHolder {

    private DidlDataBinding dataBinding;

    public DidlViewHolder(DidlDataBinding dataBinding) {
        super(dataBinding.getRoot());
        this.dataBinding = dataBinding;
    }

    public void bind(DidlViewModel model){
        this.dataBinding.setViewModel(model);
    }

    public DidlDataBinding getDataBinding(){
        return dataBinding;
    }
}
