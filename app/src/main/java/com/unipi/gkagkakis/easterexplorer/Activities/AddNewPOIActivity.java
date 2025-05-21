package com.unipi.gkagkakis.easterexplorer.Activities;

import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_ADDED_FAILED;
import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_ADDED_SUCCESSFULLY;
import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_UPDATED_FAILED;
import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_UPDATED_SUCCESSFULLY;
import static com.unipi.gkagkakis.easterexplorer.Utils.CustomToastUtil.showCustomToast;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import com.unipi.gkagkakis.easterexplorer.Database.POIManager;
import com.unipi.gkagkakis.easterexplorer.Models.POI;
import com.unipi.gkagkakis.easterexplorer.R;
import com.unipi.gkagkakis.easterexplorer.Utils.DialogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddNewPOIActivity extends AppCompatActivity {

    int poiId;
    String formattedText;
    EditText etTitle, etInfo;
    RatingBar ratingBar;
    MaterialAutoCompleteTextView categoryDropdown;
    MaterialTextView locationDetails, toastWarningMessageView;
    ShapeableImageView myImageView;
    Button getLocationButton, choosePhotoButton, saveButton;
    LocationManager locationManager;
    LocationListener locationListener;
    ConstraintLayout layout;
    POIManager poiManager;
    boolean isRatingChanged = false, hasAddress = false, hasImage = false, isEdit = false;
    double latitude, longitude;
    String addressText;
    View customWarningToastView;
    String photoPath;
    Bitmap bitmap;
    // All categories used in the dropdown
    private final String[] categories = {
            "Restaurant", "Park", "Museum", "Shopping Mall",
            "Library", "Beach", "Hotel", "Cinema", "Theater", "Zoo", "Amusement Park", "Other"
    };

    // Launcher for the image selection activity
    ActivityResultLauncher<Intent> activityToAddImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                int resultCode = result.getResultCode();
                Intent data = result.getData();
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImageUri = data.getData();
                    myImageView.setImageURI(selectedImageUri);
                    myImageView.setVisibility(View.VISIBLE);
                    choosePhotoButton.setVisibility(View.GONE);
                    hasImage = true;
                    checkFields();
                } else {
                    showCustomToast(this, customWarningToastView, toastWarningMessageView, "Cancelled selecting an image");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_poiactivity);

        initializeViews();
        checkIfEdit();
        setupCategoryDropdown();
        setupListeners();
    }

    // This method checks if the activity is opened for editing an existing POI
    private void checkIfEdit() {
        Intent intent = getIntent();
        if (intent.hasExtra("POI_ID")) {
            isEdit = true;
            isRatingChanged = true;
            saveButton.setText("Update");
            poiId = intent.getIntExtra("POI_ID", -1);
            etTitle.setText(intent.getStringExtra("POI_TITLE"));
            categoryDropdown.setText(intent.getStringExtra("POI_CATEGORY"));
            etInfo.setText(intent.getStringExtra("POI_INFO"));
            ratingBar.setRating(intent.getFloatExtra("POI_RATING", 0));
            latitude = intent.getDoubleExtra("POI_LATITUDE", 0);
            longitude = intent.getDoubleExtra("POI_LONGITUDE", 0);

            addressText = intent.getStringExtra("POI_ADDRESS");
            setLocationTextAndConstraints();

            photoPath = intent.getStringExtra("POI_PHOTO_PATH");
            if (photoPath != null) {
                myImageView.setImageURI(Uri.parse(photoPath));
                choosePhotoButton.setVisibility(View.GONE);
                myImageView.setVisibility(View.VISIBLE);
                hasImage = true;
            }
        }
        checkFields();
    }

    // This method initializes the views and sets up the layout
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
        customWarningToastView = getLayoutInflater().inflate(R.layout.custom_toast_warning, null);
        toastWarningMessageView = customWarningToastView.findViewById(R.id.toastWarningMessage);

        poiManager = new POIManager(this);
    }

    // This method sets up the category dropdown with an ArrayAdapter
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

    // This method sets up the listeners for the buttons and text fields
    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        choosePhotoButton.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> handleSaveButtonClick());

        // If text is changed
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

        // Add text change listeners to the fields
        etTitle.addTextChangedListener(textWatcher);
        categoryDropdown.addTextChangedListener(textWatcher);
        etInfo.addTextChangedListener(textWatcher);

        // Set up the rating bar listener
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


            // Get the address from the latitude and longitude
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

            setLocationTextAndConstraints();

            // Stop receiving location updates
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        };

        // Show preview of the image when clicked
        myImageView.setOnClickListener(v -> {
            if (myImageView.getDrawable() != null) {
                bitmap = ((BitmapDrawable) myImageView.getDrawable()).getBitmap();
                DialogUtils.showImagePreviewDialog(AddNewPOIActivity.this, photoPath, bitmap);
            } else {
                showCustomToast(this, customWarningToastView, toastWarningMessageView, "No image to preview");
            }
        });
    }

    private void setLocationTextAndConstraints() {
        // Format the text with bold titles
        formattedText = String.format(
                Locale.getDefault(),
                "<b>Latitude:</b> %s<br><b>Longitude:</b> %s<br><b>Address:</b> %s",
                latitude, longitude, addressText
        );

        locationDetails.setText(android.text.Html.fromHtml(formattedText, android.text.Html.FROM_HTML_MODE_LEGACY));
        locationDetails.post(() -> {
            // Hide the "Get Location" button only after the text is set
            getLocationButton.setVisibility(View.GONE);
            locationDetails.setVisibility(View.VISIBLE);

            // Adjust constraints for choosePhotoButton
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(layout);

            // location details should be in the middle
            constraintSet.connect(R.id.choosePhotoButton, ConstraintSet.TOP, R.id.locationDetails, ConstraintSet.BOTTOM);
            constraintSet.applyTo(layout);
            hasAddress = true;
            checkFields();
        });
    }

    // Handle the click event for the save button
    private void handleSaveButtonClick() {
        String title = etTitle.getText().toString().trim();
        String selectedCategory = categoryDropdown.getText().toString().trim();
        String info = etInfo.getText().toString().trim();
        float rating = ratingBar.getRating();

        // check if category is one of the predefined categories
        if (!Arrays.asList(categories).contains(selectedCategory)) {
            categoryDropdown.setError("Invalid category selected");
            categoryDropdown.requestFocus();
            return;
        }

        String imagePath = null;
        // Save the image to file system and get path
        if (myImageView.getDrawable() != null) {
            bitmap = ((BitmapDrawable) myImageView.getDrawable()).getBitmap();
            imagePath = saveImageToFile(bitmap);
        }

        // setup the poi fields
        POI poi = new POI();
        poi.setTitle(title);
        poi.setCategory(selectedCategory);
        poi.setRating(rating);
        poi.setPhotoPath(imagePath);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        poi.setAddress(addressText);
        poi.setTimestamp(System.currentTimeMillis());
        poi.setInfo(info);

        long result;

        // if we editing then update the POI
        if (isEdit) {
            result = poiManager.updatePOI(poi, poiId);
            if (result != -1) {
                //setResult won't matter as I don't return to main activity, I didn't have time to implement it
                setResult(POI_UPDATED_SUCCESSFULLY);
                Toast.makeText(this, "POI updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                setResult(POI_UPDATED_FAILED);
                Toast.makeText(this, "Failed to update POI", Toast.LENGTH_SHORT).show();
            }
            // if we adding then add the POI
        } else {
            result = poiManager.insertPOI(poi);
            if (result != -1) {
                setResult(POI_ADDED_SUCCESSFULLY);
            } else {
                setResult(POI_ADDED_FAILED);
            }
        }

        // go back to the previous activity
        finish();
    }

    // implement the save image to file system
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
        activityToAddImage.launch(intent);
    }

    // check if save button should be enabled
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getLocationButton.setText("Getting location...");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void gps(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        getLocationButton.setText("Getting location...");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}