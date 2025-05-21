package com.unipi.gkagkakis.easterexplorer.Activities;

import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_ADDED_FAILED;
import static com.unipi.gkagkakis.easterexplorer.Utils.Constants.POI_ADDED_SUCCESSFULLY;
import static com.unipi.gkagkakis.easterexplorer.Utils.CustomToastUtil.showCustomToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textview.MaterialTextView;
import com.unipi.gkagkakis.easterexplorer.R;

public class MainActivity extends AppCompatActivity {

    View customFailToastView, customWarningToastView, customDoneToastView;
    MaterialTextView toastFailMessageView, toastWarningMessageView, toastDoneMessageView;

    // Launcher for the AddNewPOIActivity with custom Toast based on the result
    ActivityResultLauncher<Intent> activityToAddPOI = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                int resultCode = result.getResultCode();
                switch (resultCode) {
                    case RESULT_CANCELED:
                        showCustomToast(this, customWarningToastView, toastWarningMessageView, "Cancelled adding new POI");
                        break;
                    case POI_ADDED_SUCCESSFULLY:
                        showCustomToast(this, customDoneToastView, toastDoneMessageView, "POI added successfully");
                        break;
                    case POI_ADDED_FAILED:
                        showCustomToast(this, customFailToastView, toastFailMessageView, "Failed to add POI");
                        break;
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void initializeViews() {
        customFailToastView = getLayoutInflater().inflate(R.layout.custom_toast_fail, null);
        customWarningToastView = getLayoutInflater().inflate(R.layout.custom_toast_warning, null);
        customDoneToastView = getLayoutInflater().inflate(R.layout.custom_toast_done, null);
        toastFailMessageView = customFailToastView.findViewById(R.id.toastFailMessage);
        toastWarningMessageView = customWarningToastView.findViewById(R.id.toastWarningMessage);
        toastDoneMessageView = customDoneToastView.findViewById(R.id.toastDoneMessage);
    }

    // method for add POI button click
    public void AddNewPOI(View view) {
        Intent intent = new Intent(this, AddNewPOIActivity.class);
        activityToAddPOI.launch(intent);
        // custom animation for the transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // method for view POI button click
    public void ViewAllPOIs(View view) {
        Intent intent = new Intent(this, POIListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
