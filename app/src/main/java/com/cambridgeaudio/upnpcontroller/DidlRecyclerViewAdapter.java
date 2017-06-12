package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cambridgeaudio.upnpcontroller.databinding.DidlDataBinding;

import java.util.ArrayList;

/**
 * Created by ayoola on 12/06/2017.
 */

public class DidlRecyclerViewAdapter extends RecyclerView.Adapter<DidlViewHolder> {

    private Context context;
    private ArrayList<DidlViewModel> list;
    private LayoutInflater inflater;

    public DidlRecyclerViewAdapter(Context context, ArrayList<DidlViewModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public DidlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(inflater == null){
            inflater = LayoutInflater.from(parent.getContext());
        }

        DidlDataBinding dataBinding = DidlDataBinding.inflate(inflater, parent, false);
        return new DidlViewHolder(dataBinding);
    }

    @Override
    public void onBindViewHolder(DidlViewHolder holder, int position) {
        DidlViewModel model = list.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
