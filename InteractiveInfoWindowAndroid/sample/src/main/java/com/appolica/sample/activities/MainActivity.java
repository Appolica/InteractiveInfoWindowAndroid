package com.appolica.sample.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appolica.sample.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewActivities);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ActivitiesAdapter();
        recyclerView.setAdapter(adapter);

        final ArrayList<Class<? extends Activity>> activityClasses = new ArrayList<>();
        activityClasses.add(MapFragmentActivity.class);
        activityClasses.add(MapViewActivity.class);

        adapter.addData(activityClasses);

    }
}
