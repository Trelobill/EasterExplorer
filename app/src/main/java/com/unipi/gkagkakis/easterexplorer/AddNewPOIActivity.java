package com.unipi.gkagkakis.easterexplorer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddNewPOIActivity extends AppCompatActivity {

    String formattedText;
    EditText etTitle, etInfo;
    RatingBar ratingBar;
    MaterialAutoCompleteTextView categoryDropdown;
    MaterialTextView locationDetails;
    ShapeableImageView myImageView;
    Button getLocationButton, choosePhotoButton, saveButton;
    LocationManager locationManager;
    LocationListener locationListener;
    ConstraintLayout layout;
    boolean isRatingChanged = false, hasAddress = false, hasImage = false;
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
        locationDetails = findViewById(R.id.locationDetails);
        myImageView = findViewById(R.id.myImageView);
        getLocationButton = findViewById(R.id.getLocation);
        choosePhotoButton = findViewById(R.id.choosePhotoButton);
        saveButton = findViewById(R.id.saveButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        layout = findViewById(R.id.add_poi);
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
        choosePhotoButton.setOnClickListener(v -> openGallery());
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

        locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            System.out.println("Location: " + location.getLatitude() + ", " + location.getLongitude());


            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String addressText = "No address found";
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0);
                }
            } catch (IOException e) {
                Log.e("AddNewPOIActivity", "Error fetching address from location", e);
            }

            // Format the text with bold titles
            formattedText = String.format(
                    Locale.getDefault(),
                    "<b>Latitude:</b> %s<br><b>Longitude:</b> %s<br><b>Address:</b> %s",
                    latitude, longitude, addressText
            );


            locationDetails.setText(android.text.Html.fromHtml(formattedText));
            locationDetails.post(() -> {
                // Hide the "Get Location" button only after the text is set
                getLocationButton.setVisibility(View.GONE);
                locationDetails.setVisibility(View.VISIBLE);

                // Adjust constraints for choosePhotoButton
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(layout);

                // Set choosePhotoButton to be below locationDetails when visible
                constraintSet.connect(R.id.choosePhotoButton, ConstraintSet.TOP, R.id.locationDetails, ConstraintSet.BOTTOM);
                constraintSet.applyTo(layout);
                hasAddress = true;
                checkFields();
            });

            // Stop receiving location updates
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        };
    }

    private void handleSaveButtonClick() {
        String title = etTitle.getText().toString().trim();
        String selectedCategory = categoryDropdown.getText().toString().trim();
        String info = etInfo.getText().toString().trim();
        float rating = ratingBar.getRating();
        String lat = "", lng = "", address = "";
        if (formattedText != null) {
            String regex = "<b>Latitude:</b> (.*?)<br><b>Longitude:</b> (.*?)<br><b>Address:</b> (.*)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(formattedText);

            if (matcher.find()) {
                lat = matcher.group(1);
                lng = matcher.group(2);
                address = matcher.group(3);
            }
        }

        if (!Arrays.asList(categories).contains(selectedCategory)) {
            categoryDropdown.setError("Invalid category selected");
            categoryDropdown.requestFocus();
            return;
        }

        System.out.println("Title: " + title);
        System.out.println("Category: " + selectedCategory);
        System.out.println("Info: " + info);
        System.out.println("Rating: " + rating);
        System.out.println("Latitude: " + lat);
        System.out.println("Longitude: " + lng);
        System.out.println("Address: " + address);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 234);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 234 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Hide the button
            choosePhotoButton.setVisibility(View.GONE);
            myImageView.setVisibility(View.VISIBLE);
            myImageView.setImageURI(selectedImageUri);
            hasImage = true;
            checkFields();
        }
    }

    private void checkFields() {
        String title = etTitle.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();
        String info = etInfo.getText().toString().trim();
        boolean hasError = categoryDropdown.getError() != null;
        saveButton.setEnabled(!title.isEmpty() && !category.isEmpty() && !info.isEmpty() && isRatingChanged && hasAddress && hasImage && !hasError);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void gps(View v) {

        System.out.println("GPS button clicked");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            System.out.println("1");
            return;
        }
        System.out.println("2");
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