package com.appolica.sample.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appolica.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogomil Kolarov on 21.09.16.
 * Copyright © 2016 Appolica. All rights reserved.
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ItemViewHolder> {

    private List<Class<? extends Activity>> data = new ArrayList<>();

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        ItemViewHolder(View itemView) {
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
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        holder.textView.setText(data.get(position).getSimpleName());

        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();

                context.startActivity(new Intent(context, data.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addData(List<Class<? extends Activity>> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
