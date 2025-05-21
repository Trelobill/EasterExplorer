package com.unipi.gkagkakis.easterexplorer.Utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;

// class to show custom toasts that I created
public class CustomToastUtil {
    public static void showCustomToast(Context context, View customToastView, MaterialTextView textView, String message) {
        textView.setText(message);
        Toast toast = new Toast(context);
        toast.setView(customToastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}