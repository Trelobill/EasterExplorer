package com.unipi.gkagkakis.easterexplorer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import com.unipi.gkagkakis.easterexplorer.Database.POIManager;
import com.unipi.gkagkakis.easterexplorer.Models.POI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddNewPOIActivity extends AppCompatActivity {

    String formattedText;
    EditText etTitle, etInfo;
    RatingBar ratingBar;
    MaterialAutoCompleteTextView categoryDropdown;
    MaterialTextView locationDetails, toastMessageView;
    ShapeableImageView myImageView;
    Button getLocationButton, choosePhotoButton, saveButton;
    LocationManager locationManager;
    LocationListener locationListener;
    ConstraintLayout layout;
    POIManager poiManager;
    boolean isRatingChanged = false, hasAddress = false, hasImage = false;
    double latitude, longitude;
    String addressText;
    View customToastView;
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
        customToastView = getLayoutInflater().inflate(R.layout.custom_toast, null);
        toastMessageView = customToastView.findViewById(R.id.toastMessage);

        poiManager = new POIManager(this);
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
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            System.out.println("Location: " + location.getLatitude() + ", " + location.getLongitude());


            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addressText = "No address found";
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

        if (!Arrays.asList(categories).contains(selectedCategory)) {
            categoryDropdown.setError("Invalid category selected");
            categoryDropdown.requestFocus();
            return;
        }

        String imagePath = null;
        // Save the image to file system and get path
        if (myImageView.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) myImageView.getDrawable()).getBitmap();
            imagePath = saveImageToFile(bitmap);
        }

        // Here you can save the POI to the database or perform any other action
        POI poi = new POI();
        poi.setTitle(title);
        poi.setCategory(selectedCategory);
        poi.setRating(rating);
        poi.setPhotoPath(imagePath);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        poi.setTimestamp(System.currentTimeMillis());
        poi.setInfo(info);

        long result = poiManager.insertPOI(poi);
        if (result != -1) {
            showCustomToast(customToastView, Toast.LENGTH_SHORT, "POI added successfully");
        } else {
            Toast.makeText(this, "Failed to add POI", Toast.LENGTH_SHORT).show();
        }


        System.out.println("Title: " + title);
        System.out.println("Category: " + selectedCategory);
        System.out.println("Info: " + info);
        System.out.println("Rating: " + rating);
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("Address: " + addressText);
        System.out.println("Image blob: " + myImageView.getDrawable());

        //go back to the previous activity
        finish();
    }

    private void showCustomToast(View customToastView, int duration, String message) {
        toastMessageView.setText(message);
        // Create and configure the Toast
        Toast toast = new Toast(this);
        toast.setView(customToastView);
        toast.setDuration(duration);
        toast.show();
    }

    private String saveImageToFile(Bitmap bitmap) {
        File directory = getExternalFilesDir("images");
        if (directory != null && !directory.exists()) {
            if (!directory.mkdirs()) {
                System.out.println("Failed to create directory");
                return null;
            }
        }
        File file = new File(directory, System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            System.out.println("Error saving image: " + e.getMessage());
        }
        return null;
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
        saveButton.setEnabled(!title.isEmpty() && !category.isEmpty() && !info.isEmpty() && isRatingChanged && !hasAddress && hasImage && !hasError);
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }
}