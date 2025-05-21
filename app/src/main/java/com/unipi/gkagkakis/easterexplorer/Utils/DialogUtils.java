package com.unipi.gkagkakis.easterexplorer.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import com.unipi.gkagkakis.easterexplorer.R;

// class to show image preview dialog
public class DialogUtils {

    public static void showImagePreviewDialog(Context context, String imagePath, Bitmap bitmap) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_image_preview);
        ImageView imageView = dialog.findViewById(R.id.dialogImageView);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (imagePath != null) {
            Bitmap fileBitmap = BitmapFactory.decodeFile(imagePath);
            if (fileBitmap != null) {
                imageView.setImageBitmap(fileBitmap);
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No image to preview", Toast.LENGTH_SHORT).show();
        }

        dialog.show();
    }
}