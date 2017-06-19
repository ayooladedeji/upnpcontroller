package com.cambridgeaudio.upnpcontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fourthline.cling.support.model.DIDLObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Ayo on 19/06/2017.
 */

public class DidlListAdapter extends RecyclerView.Adapter<DidlListAdapter.ViewHolder> {

    private ArrayList<DIDLObject> list = new ArrayList<>();
    private Context context;
    private IonClickListener ionClickListener;

    public DidlListAdapter(Context context, IonClickListener ionClickListener) {
        this.context = context;
        this.ionClickListener = ionClickListener;
    }

    public DIDLObject getDidlObject(int pos){
        return list.get(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.didl_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ionClickListener.didlOnClick(v, this.getLayoutPosition());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_didl, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(list.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface IonClickListener{
        void didlOnClick(View v, int pos);
    }

    public void update(ArrayList<DIDLObject> didlObjects){
        list.clear();
        list.addAll(didlObjects);
        notifyDataSetChanged();
    }
}
