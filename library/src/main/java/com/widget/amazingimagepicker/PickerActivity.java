package com.widget.amazingimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.widget.amazingimagepicker.model.Content;
import com.widget.amazingimagepicker.superslim.LayoutManager;
import com.widget.amazingimagepicker.utils.ContentStoreAccessor;
import com.widget.amazingimagepicker.utils.ItemDecorationAlbumColumns;
import com.widget.amazingimagepicker.view.HeaderTouchDelegate;
import com.widget.amazingimagepicker.view.ScrollFeedbackRecyclerView;
import com.widget.amazingimagepicker.view.TextureVideoView;
import com.widget.amazingimagepicker.view.appbarlayout_23_2_1.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PickerActivity extends AppCompatActivity implements ScrollFeedbackRecyclerView.Callbacks {

    public final static String EXTRA_RESULT = "EXTRA_RESULT";
    public final static String EXTRA_TOPBAR_ID = "EXTRA_TOPBAR_ID";
    private final static int NUM_COLUMNS = 4;

    private AppBarLayout appBarLayout;
    private ImageView imageContent;
    private RelativeLayout videoContentContainer;
    private TextureVideoView videoContent;
    private Toolbar invisibleToolbar;
    private HeaderTouchDelegate headerTouchDelegate;
    private RecyclerView mRecyclerView;
    private View btnClose;
    private View btnNext;
    private Uri selectedUri = null;

    private PickerAdapter pickerAdapter;
    private OffsetChangeListener offsetChangeListener;
    private PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_picker);

        int topbarId = getIntent().getIntExtra(EXTRA_TOPBAR_ID, 0);
        if (topbarId > 0) {
            LayoutInflater.from(this).inflate(topbarId, (ViewGroup) findViewById(R.id.topbar_container), true);
        }

        btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        btnNext = findViewById(R.id.btn_next);
        if (btnNext != null) {
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendResult();
                }
            });
        }

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        ViewCompat.setElevation(appBarLayout, 0);
        imageContent = (ImageView) findViewById(R.id.image_content);
        videoContentContainer = (RelativeLayout) findViewById(R.id.video_content_container);
        invisibleToolbar = (Toolbar) findViewById(R.id.invisible_toolbar);
        headerTouchDelegate = (HeaderTouchDelegate) findViewById(R.id.header_touch_delegate);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LayoutManager(this));
        mRecyclerView.addItemDecoration(new ItemDecorationAlbumColumns<>(getResources().getDimensionPixelOffset(R.dimen.spacing), NUM_COLUMNS, PickerAdapter.class));
        List<Content> videos = ContentStoreAccessor.getAllVideos(this);
        List<Content> images = ContentStoreAccessor.getAllImages(this);
        List<Content> all = new ArrayList<>();
        all.addAll(videos);
        all.addAll(images);
        pickerAdapter = new PickerAdapter(all, NUM_COLUMNS, new PickerAdapter.OnContentClickListener() {
            @Override
            public void onClick(Content content) {
                if (btnNext == null && content.getContentUri().equals(selectedUri)) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            sendResult();
                        }
                    });
                } else {
                    loadContent(content);
                }
            }
        });
        mRecyclerView.setAdapter(pickerAdapter);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (pickerAdapter.getItemCount() > 0) {
                    loadContent(pickerAdapter.getItem(0));
                }
            }
        });

        offsetChangeListener = new OffsetChangeListener();
        appBarLayout.addOnOffsetChangedListener(offsetChangeListener);
        mAttacher = new PhotoViewAttacher(imageContent);
        GestureListener gestureListener = new GestureListener();
        final GestureDetectorCompat gestures = new GestureDetectorCompat(this, gestureListener);
        headerTouchDelegate.setGestureDetector(gestures);
        headerTouchDelegate.setOnTouchListener(mAttacher, imageContent);
        headerTouchDelegate.setOnGestureDetectorListener(gestureListener);
        headerTouchDelegate.setCallbacks(this);
    }

    private void loadContent(final Content content) {
        switch (content.getType()) {
            case IMAGE:
                loadImage(content.getContentUri());
                break;
            case VIDEO:
                videoContentContainer.removeAllViews();
                if (videoContent != null) {
                    videoContent.release();
                }
                videoContent = new TextureVideoView(this);
                videoContent.setListener(new TextureVideoView.MediaPlayerListener() {
                    @Override
                    public void onVideoPrepared() {
                    }

                    @Override
                    public void onVideoEnd() {
                        videoContent.play();
                    }
                });
                videoContentContainer.addView(videoContent);
                loadVideo(content.getContentUri());
                break;
        }
    }

    private void loadVideo(Uri uri) {
        selectedUri = uri;
        imageContent.setVisibility(View.INVISIBLE);
        imageContent.setImageDrawable(new StateListDrawable());
        mAttacher.update();

        setExpanded(true);
        videoContent.setDataSource(this, uri);
        videoContent.play();
        updateTouchDelegate();

    }

    private void loadImage(Uri uri) {
        selectedUri = uri;
        videoContent.stop();
        videoContent.setVisibility(View.INVISIBLE);
        imageContent.setVisibility(View.VISIBLE);
        //final Drawable prevDrawable = imageContent.getDrawable() == null ? new StateListDrawable() : imageContent.getDrawable();
        Glide.with(PickerActivity.this)
                .load(uri)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{new StateListDrawable(), resource});
                        transitionDrawable.setCrossFadeEnabled(true);
                        transitionDrawable.startTransition(500);
                        imageContent.setImageDrawable(transitionDrawable);
                        mAttacher.update();
                        setExpanded(true);
                        return true;
                    }
                })
                .into(new ImageViewTarget<GlideDrawable>(imageContent) {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }

                    @Override
                    public void onLoadStarted(Drawable d) {
                    }

                    @Override
                    protected void setResource(GlideDrawable resource) {
                    }
                });

        updateTouchDelegate();
    }

    @Override
    public boolean isAppBarCollapsed() {
        final int appBarVisibleHeight = (int) (appBarLayout.getY() + appBarLayout.getHeight());
        final int toolbarHeight = invisibleToolbar.getHeight();
        return (appBarVisibleHeight == toolbarHeight);
    }

    @Override
    public void setExpanded(boolean expanded) {
        appBarLayout.setExpanded(expanded, true);
    }

    @Override
    public OffsetChangeListener getOffsetChangeListener() {
        return offsetChangeListener;
    }

    private void updateTouchDelegate() {
        if (imageContent.getVisibility() == View.VISIBLE) {
            headerTouchDelegate.setBottom(appBarLayout.getBottom());
        }
        if (videoContent != null && videoContent.getVisibility() == View.VISIBLE) {
            headerTouchDelegate.setBottom(appBarLayout.getBottom());
        }
    }

    private void sendResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, selectedUri.toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        public boolean onUp(MotionEvent event) {
            setExpanded(offsetChangeListener.getLastNotNullOffsetChange() > 0);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int distance = (int) distanceY;
            if (appBarLayout.getTop() - distance > 0) {
                distance = appBarLayout.getTop();
            }
            if (appBarLayout.getBottom() - distance < invisibleToolbar.getHeight()) {
                distance = -invisibleToolbar.getHeight() + appBarLayout.getBottom();
            }
            appBarLayout.offsetTopAndBottom(-distance);
            updateTouchDelegate();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            setExpanded(velocityY > 0);
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (videoContent.getVisibility() == View.VISIBLE) {
                if (videoContent.isPlaying()) {
                    videoContent.pause();
                } else {
                    videoContent.play();
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (videoContent.getVisibility() == View.VISIBLE) {
                if (videoContent.getScaleType() == TextureVideoView.ScaleType.CENTER_CROP) {
                    videoContent.setScaleType(TextureVideoView.ScaleType.FIT_CENTER);
                } else if (videoContent.getScaleType() == TextureVideoView.ScaleType.FIT_CENTER) {
                    videoContent.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);
                }
            }
            return false;
        }
    }

    public class OffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

        private int lastNotNullOffsetChange = 0;
        private int offset = 0;

        public int getLastNotNullOffsetChange() {
            return lastNotNullOffsetChange;
        }

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            if (verticalOffset - offset != 0) {
                lastNotNullOffsetChange = verticalOffset - offset;
            }
            offset = verticalOffset;
            updateTouchDelegate();
        }
    }
}
