package com.unipi.gkagkakis.easterexplorer;

import android.annotation.SuppressLint;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Arrays;

public class AddNewPOIActivity extends AppCompatActivity {

    EditText etTitle, etInfo;
    RatingBar ratingBar;
    MaterialAutoCompleteTextView categoryDropdown;
    Button saveButton;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean isRatingChanged = false;
    private final String[] categories = {
            "Restaurant", "Park", "Museum", "Shopping Mall",
            "Library", "Beach", "Hotel", "Cinema", "Theater", "Zoo", "Amusement Park", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_poiactivity);

        initializeViews();
        setupCategoryDropdown();
        setupListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etInfo = findViewById(R.id.etInfo);
        ratingBar = findViewById(R.id.ratingBar);
        categoryDropdown = findViewById(R.id.etCategory);
        saveButton = findViewById(R.id.saveButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(adapter);
        categoryDropdown.setThreshold(1);
        categoryDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        categoryDropdown.setOnTouchListener((v, event) -> {
            categoryDropdown.setError(null);
            categoryDropdown.showDropDown();
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        saveButton.setOnClickListener(v -> handleSaveButtonClick());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etTitle.addTextChangedListener(textWatcher);
        categoryDropdown.addTextChangedListener(textWatcher);
        etInfo.addTextChangedListener(textWatcher);

        ratingBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isRatingChanged = true;
                checkFields();
            }
            return false;
        });
    }

    private void handleSaveButtonClick() {
        String title = etTitle.getText().toString().trim();
        String selectedCategory = categoryDropdown.getText().toString().trim();
        String info = etInfo.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (!Arrays.asList(categories).contains(selectedCategory)) {
            categoryDropdown.setError("Invalid category selected");
            categoryDropdown.requestFocus();
        }

        System.out.println("Title: " + title);
        System.out.println("Category: " + selectedCategory);
        System.out.println("Info: " + info);
        System.out.println("Rating: " + rating);
    }

    private void checkFields() {
        String title = etTitle.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String info = etInfo.getText().toString().trim();
        boolean hasError = categoryDropdown.getError() != null;
        saveButton.setEnabled(!title.isEmpty() && !category.isEmpty() && !info.isEmpty() && isRatingChanged && !hasError);
    }

    private void gps(View v)
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }



//    private void savePOI() {
//        String title = etTitle.getText().toString().trim();
//        String category = categoryDropdown.getText().toString().trim();
//        String ratingStr = etRating.getText().toString().trim();
//        String gpsCoordinates = getIntent().getStringExtra("gpsCoordinates");
//
//        System.out.println("GPS Coordinates: " + gpsCoordinates);
//
//        if (title.isEmpty() || ratingStr.isEmpty()) {
//            Toast.makeText(this, "Title and Rating are required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double rating = Double.parseDouble(ratingStr);
//
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(POIDatabaseHelper.COLUMN_TITLE, title);
//        values.put(POIDatabaseHelper.COLUMN_CATEGORY, category);
//        values.put(POIDatabaseHelper.COLUMN_RATING, rating);
//
//        long result = db.insert(POIDatabaseHelper.TABLE_POI, null, values);
//        if (result != -1) {
//            Toast.makeText(this, "POI added successfully", Toast.LENGTH_SHORT).show();
//            finish();
//        } else {
//            Toast.makeText(this, "Failed to add POI", Toast.LENGTH_SHORT).show();
//        }
//    }
}