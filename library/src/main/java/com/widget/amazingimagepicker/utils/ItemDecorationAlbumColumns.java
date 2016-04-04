package com.widget.amazingimagepicker.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.widget.amazingimagepicker.PickerAdapter;

public class ItemDecorationAlbumColumns<Adapter extends PickerAdapter> extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx;
    private int mGridSize;
    private Class<Adapter> adapterClass;

    public ItemDecorationAlbumColumns(int gridSpacingPx, int gridSize, Class<Adapter> adapterClass) {
        mSizeGridSpacingPx = gridSpacingPx;
        mGridSize = gridSize;
        this.adapterClass = adapterClass;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (!adapterClass.isInstance(parent.getAdapter()) ) {
            throw new RuntimeException("adapter must extend PickerAdapter");
        }
        Adapter adapter = adapterClass.cast(parent.getAdapter());
        int frameWidth, frameHeight;
        frameWidth = frameHeight = (int) ((parent.getWidth() / (float) mGridSize));
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        int itemCount = parent.getAdapter().getItemCount();

        if (adapter.isHeader(itemPosition)) {
            return;
        }

        itemPosition -= 1; //header always shown

        if (itemPosition < mGridSize) {
            outRect.top = 0;
            outRect.bottom = Math.max(1, mSizeGridSpacingPx / 2);
        } else if (itemPosition >= itemCount - mGridSize) {
            outRect.top = Math.max(1, mSizeGridSpacingPx / 2);
            outRect.bottom = 0;
        } else {
            outRect.top = Math.max(1, mSizeGridSpacingPx / 2);
            outRect.bottom = Math.max(1, mSizeGridSpacingPx / 2);
        }

        if (itemPosition % mGridSize == 0) {
            outRect.left = 0;
            outRect.right = Math.max(1, mSizeGridSpacingPx / 2);
        } else if (itemPosition % mGridSize == mGridSize - 1) {
            outRect.left = Math.max(1, mSizeGridSpacingPx / 2);
            outRect.right = 0;
        } else {
            outRect.left = Math.max(1, mSizeGridSpacingPx / 2);
            outRect.right = Math.max(1, mSizeGridSpacingPx / 2);
        }

        ViewGroup.MarginLayoutParams clp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        clp.height = frameHeight;
        clp.width = frameWidth;
    }
}
