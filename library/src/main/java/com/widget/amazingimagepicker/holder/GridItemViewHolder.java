package com.widget.amazingimagepicker.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.widget.amazingimagepicker.R;

public class GridItemViewHolder extends RecyclerView.ViewHolder {

    public ImageView image;
    public RelativeLayout selected;
    public TextView duration;

    public GridItemViewHolder(View itemView) {
        super(itemView);
        selected = (RelativeLayout) itemView.findViewById(R.id.selected);
        image = (ImageView) itemView.findViewById(R.id.image);
        duration = (TextView) itemView.findViewById(R.id.duration);
    }
}
