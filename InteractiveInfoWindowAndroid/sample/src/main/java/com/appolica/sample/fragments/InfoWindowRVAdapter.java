package com.appolica.sample.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appolica.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogomil Kolarov on 22.09.16.
 * Copyright © 2016 Appolica. All rights reserved.
 */
public class InfoWindowRVAdapter extends RecyclerView.Adapter<InfoWindowRVAdapter.ItemViewHolder> {

    private List<String> data = new ArrayList<>();

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.listItemTextView);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new ItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.textView.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addData(List<String> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(String item) {
        data.add(item);
        notifyDataSetChanged();
    }

}
