package com.appolica.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.appolica.sample.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewActivities);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ActivitiesAdapter();
        recyclerView.setAdapter(adapter);

        final ArrayList<Class<? extends Activity>> activityClasses = new ArrayList<>();
        activityClasses.add(MapFragmentActivity.class);
        activityClasses.add(MapViewActivity.class);
        activityClasses.add(MapViewClusteringActivity.class);

        adapter.addData(activityClasses);

    }
}
