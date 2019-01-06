package com.facepp.demo.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facepp.demo.R;

import java.util.ArrayList;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    private static final String TAG = "PictureGridAdapter";
    ArrayList<Uri> pictures;
    Activity activity;
    RecyclerView recyclerView;

    public PictureGridAdapter( Activity activity, RecyclerView recyclerView, ArrayList<Uri> pictures) {
        this.pictures = pictures;
        this.activity = activity;
        this.recyclerView = recyclerView;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = activity.getLayoutInflater().inflate(R.layout.single_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called");
        Glide.with(activity).load(pictures.get(position)).into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        View parent;
        public ViewHolder(View v) {
            super(v);
            parent = v;
            photo = (ImageView) parent.findViewById(R.id.photo);
        }
    }
}
