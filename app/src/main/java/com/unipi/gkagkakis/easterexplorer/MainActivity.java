package com.unipi.gkagkakis.easterexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unipi.gkagkakis.easterexplorer.Database.POIManager;
import com.unipi.gkagkakis.easterexplorer.Models.POI;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void AddNewPOI(View view) {
        System.out.println("Add New POI clicked");
        Intent intent = new Intent(this, AddNewPOIActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void ViewAllPOIs(View view) {
        POIManager poiManager = new POIManager(this);
        List<POI> poiList = poiManager.getAllPOIs();

        if (poiList.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("All POIs")
                    .setMessage("No POIs available.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            StringBuilder poiDetails = new StringBuilder();
            for (POI poi : poiList) {
                poiDetails.append("Title: ").append(poi.getTitle()).append("\n")
                        .append("Category: ").append(poi.getCategory()).append("\n")
                        .append("Rating: ").append(poi.getRating()).append("\n\n");
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("All POIs")
                    .setMessage(poiDetails.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

}
