package com.widget.amazingimagepicker.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.widget.amazingimagepicker.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    public TextView title;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
    }
}
