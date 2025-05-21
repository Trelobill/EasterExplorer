package com.unipi.gkagkakis.easterexplorer.Adapters;

import static com.unipi.gkagkakis.easterexplorer.Utils.CustomToastUtil.showCustomToast;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.unipi.gkagkakis.easterexplorer.Activities.AddNewPOIActivity;
import com.unipi.gkagkakis.easterexplorer.Database.POIManager;
import com.unipi.gkagkakis.easterexplorer.Models.POI;
import com.unipi.gkagkakis.easterexplorer.R;
import com.unipi.gkagkakis.easterexplorer.Utils.DialogUtils;

import java.util.List;

public class POIAdapter extends RecyclerView.Adapter<POIAdapter.POIViewHolder> {

    private final List<POI> poiList;

    View customFailToastView, customWarningToastView, customDoneToastView;

    MaterialTextView toastFailMessageView, toastWarningMessageView, toastDoneMessageView;
    Bitmap bitmap;

    public POIAdapter(List<POI> poiList) {
        this.poiList = poiList;
    }

    @NonNull
    @Override
    public POIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poi, parent, false);

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        customFailToastView = inflater.inflate(R.layout.custom_toast_fail, null);
        customWarningToastView = inflater.inflate(R.layout.custom_toast_warning, null);
        customDoneToastView = inflater.inflate(R.layout.custom_toast_done, null);
        toastFailMessageView = customFailToastView.findViewById(R.id.toastFailMessage);
        toastWarningMessageView = customWarningToastView.findViewById(R.id.toastWarningMessage);
        toastDoneMessageView = customDoneToastView.findViewById(R.id.toastDoneMessage);

        return new POIViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull POIViewHolder holder, int position) {
        POI poi = poiList.get(position);
        holder.tvTitle.setText("Title: " + poi.getTitle());
        holder.tvCategory.setText("Category: " + poi.getCategory());
        holder.tvComments.setText("Comments: " + poi.getInfo());
        holder.tvLatitude.setText("Latitude: " + poi.getLatitude());
        holder.tvLongitude.setText("Longitude: " + poi.getLongitude());
        holder.tvAddress.setText("Address: " + poi.getAddress());
        holder.ratingBar.setRating(poi.getRating());
        // Set the tint color for the filled stars (progress)
        holder.ratingBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.YELLOW));
        // Optionally, set the tint for the empty stars (background)
        holder.ratingBar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
        // Load the image from the file path
        if (poi.getPhotoPath() != null) {
            bitmap = BitmapFactory.decodeFile(poi.getPhotoPath());
            if (bitmap != null) {
                holder.image.setImageBitmap(bitmap);
                System.out.println("Image loaded successfully");
            } else {
                showCustomToast(holder.itemView.getContext(), customFailToastView, toastFailMessageView, "Failed to load image!");
            }
        } else {
            showCustomToast(holder.itemView.getContext(), customFailToastView, toastFailMessageView, "No image path provided");
        }

        // Add click listener to the image
        holder.image.setOnClickListener(v -> DialogUtils.showImagePreviewDialog(holder.itemView.getContext(), poi.getPhotoPath(), bitmap));

        // Edit button action
        holder.iconEdit.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, AddNewPOIActivity.class);
            intent.putExtra("POI_ID", poi.getId());
            intent.putExtra("POI_TITLE", poi.getTitle());
            intent.putExtra("POI_CATEGORY", poi.getCategory());
            intent.putExtra("POI_INFO", poi.getInfo());
            intent.putExtra("POI_RATING", poi.getRating());
            // photo, lat, long and address cant be changed in the edit POI
            intent.putExtra("POI_PHOTO_PATH", poi.getPhotoPath());
            intent.putExtra("POI_LATITUDE", poi.getLatitude());
            intent.putExtra("POI_LONGITUDE", poi.getLongitude());
            intent.putExtra("POI_ADDRESS", poi.getAddress());

            context.startActivity(intent);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        // Delete button action
        holder.iconDelete.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete POI")
                    .setMessage("Are you sure you want to delete this POI?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove the POI from the list
                        poiList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, poiList.size());

                        // Optionally, delete the POI from the database
                        POIManager poiManager = new POIManager(context);
                        poiManager.deletePOI(poi.getId());
                        showCustomToast(holder.itemView.getContext(), customDoneToastView, toastDoneMessageView, "POI deleted successfully!");
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        showCustomToast(holder.itemView.getContext(), customWarningToastView, toastWarningMessageView, "POI deletion canceled!");
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    static class POIViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvComments, tvLatitude, tvLongitude, tvAddress;
        RatingBar ratingBar;
        ImageView image, iconEdit, iconDelete;

        public POIViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvLatitude = itemView.findViewById(R.id.tvLatitude);
            tvLongitude = itemView.findViewById(R.id.tvLongitude);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            image = itemView.findViewById(R.id.imageView);
            iconEdit = itemView.findViewById(R.id.iconEdit);
            iconDelete = itemView.findViewById(R.id.iconDelete);
        }
    }
}