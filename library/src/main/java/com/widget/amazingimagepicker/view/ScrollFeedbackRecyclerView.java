package com.widget.amazingimagepicker.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.widget.amazingimagepicker.PickerActivity;

import java.lang.ref.WeakReference;

public class ScrollFeedbackRecyclerView extends RecyclerView {

    private WeakReference<Callbacks> mCallbacks;
    private boolean drag = false;

    public ScrollFeedbackRecyclerView(Context context) {
        super(context);
        attachCallbacks(context);
    }

    public ScrollFeedbackRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        attachCallbacks(context);
    }

    /*If the first completely visible item in the RecyclerView is at
    index 0, then we're at the top of the list, so we want the AppBar to expand
    **if the AppBar is also collapsed** (otherwise the AppBar will constantly
    attempt to expand).
    */
    @Override
    public void onScrolled(int dx, int dy) {
        com.widget.amazingimagepicker.superslim.LayoutManager lm = (com.widget.amazingimagepicker.superslim.LayoutManager) getLayoutManager();
        if (lm.findFirstCompletelyVisibleItem() != null && lm.findFirstCompletelyVisibleNoHeaderItemPosition() == 1) {
            if (mCallbacks.get().isAppBarCollapsed() && dy < 0 && !drag) {
                mCallbacks.get().setExpanded(true);
            }
        }
        super.onScrolled(dx, dy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drag = false;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                drag = true;
                break;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && !mCallbacks.get().isAppBarCollapsed()) {
            mCallbacks.get().setExpanded(mCallbacks.get().getOffsetChangeListener().getLastNotNullOffsetChange() > 0);
        }
        return super.onTouchEvent(event);
    }

    /* the findFirstCompletelyVisibleItem() method is only available with
    LinearLayoutManager and its subclasses, so test for it when setting the
    LayoutManager
    */
    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof com.widget.amazingimagepicker.superslim.LayoutManager)) {
            throw new IllegalArgumentException(layout.toString() + " must be of type superslim LayoutManager");
        }
        super.setLayoutManager(layout);
    }

    private void attachCallbacks(Context context) {

        try {
            mCallbacks = new WeakReference<>((Callbacks) context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " +
                    "ScrollFeedbackRecyclerView.Callbacks");
        }

    }

    /**
     * Necessary to interact with the AppBarLayout in the hosting Activity
     */
    public interface Callbacks {
        boolean isAppBarCollapsed();

        void setExpanded(boolean expanded);

        PickerActivity.OffsetChangeListener getOffsetChangeListener();
    }
}