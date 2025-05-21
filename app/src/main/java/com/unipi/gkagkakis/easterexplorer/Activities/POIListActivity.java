package com.unipi.gkagkakis.easterexplorer.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.gkagkakis.easterexplorer.Adapters.POIAdapter;
import com.unipi.gkagkakis.easterexplorer.Database.POIManager;
import com.unipi.gkagkakis.easterexplorer.Models.POI;
import com.unipi.gkagkakis.easterexplorer.R;

import java.util.List;

public class POIListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPOI);

        POIManager poiManager = new POIManager(this);
        List<POI> poiList = poiManager.getAllPOIs();

        POIAdapter adapter = new POIAdapter(poiList);
        recyclerView.setAdapter(adapter);
    }

    // when editing a POI, the result is returned here and we update the list
    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPOI);

        POIManager poiManager = new POIManager(this);
        List<POI> updatedPOIList = poiManager.getAllPOIs();

        POIAdapter adapter = new POIAdapter(updatedPOIList);
        recyclerView.setAdapter(adapter);
    }
}