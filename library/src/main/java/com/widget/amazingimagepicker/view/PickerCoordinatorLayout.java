package com.widget.amazingimagepicker.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PickerCoordinatorLayout extends CoordinatorLayout {

    public PickerCoordinatorLayout(Context context) {
        super(context);
    }

    public PickerCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PickerCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isEnabled() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isEnabled();
    }
 }

