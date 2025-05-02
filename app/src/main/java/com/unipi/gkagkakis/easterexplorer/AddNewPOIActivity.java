package com.unipi.gkagkakis.easterexplorer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.gkagkakis.easterexplorer.Database.POIDatabaseHelper;

public class AddNewPOIActivity extends AppCompatActivity {

    private EditText etTitle, etCategory, etRating;
    private POIDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_poiactivity);

        etTitle = findViewById(R.id.etTitle);
        etCategory = findViewById(R.id.etCategory);
        etRating = findViewById(R.id.etRating);
        Button btnSave = findViewById(R.id.btnSave);

        dbHelper = new POIDatabaseHelper(this);

        btnSave.setOnClickListener(v -> savePOI());
    }

    private void savePOI() {
        String title = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();
        String gpsCoordinates = getIntent().getStringExtra("gpsCoordinates");

        System.out.println("GPS Coordinates: " + gpsCoordinates);

        if (title.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(this, "Title and Rating are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double rating = Double.parseDouble(ratingStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POIDatabaseHelper.COLUMN_TITLE, title);
        values.put(POIDatabaseHelper.COLUMN_CATEGORY, category);
        values.put(POIDatabaseHelper.COLUMN_RATING, rating);

        long result = db.insert(POIDatabaseHelper.TABLE_POI, null, values);
        if (result != -1) {
            Toast.makeText(this, "POI added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add POI", Toast.LENGTH_SHORT).show();
        }
    }
}