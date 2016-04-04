package com.widget.amazingimagepicker.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.widget.amazingimagepicker.PickerActivity;

public class HeaderTouchDelegate extends SquareRelativeLayout {

    private GestureDetectorCompat mDetector;
    private OnTouchListener onTouchListener;
    private PickerActivity.GestureListener listener;
    private ScrollFeedbackRecyclerView.Callbacks callbacks;
    private View view;

    private boolean dragImageFromCollapse = false;

    public HeaderTouchDelegate(Context context) {
        super(context);
    }

    public HeaderTouchDelegate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderTouchDelegate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderTouchDelegate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setGestureDetector(GestureDetectorCompat mDetector) {
        this.mDetector = mDetector;
    }

    public void setOnTouchListener(OnTouchListener onTouchListener, View view) {
        this.onTouchListener = onTouchListener;
        this.view = view;
    }

    public void setOnGestureDetectorListener(PickerActivity.GestureListener listener) {
        this.listener = listener;
    }

    public void setCallbacks(ScrollFeedbackRecyclerView.Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (listener != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL)) {
            listener.onUp(ev);
            dragImageFromCollapse = false;
        }
        if (onTouchListener != null && view.getVisibility() == View.VISIBLE && callbacks != null) {
            if (callbacks.isAppBarCollapsed() && (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE)) {
                dragImageFromCollapse = true;
            } else if (!dragImageFromCollapse) {
                return onTouchListener.onTouch(view, ev);
            }
        }
        if (mDetector != null) {
            return mDetector.onTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }
}
