package com.widget.amazingimagepicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.widget.amazingimagepicker.view.PickerCoordinatorLayout;
import com.widget.amazingimagepicker.view.ScrollFeedbackRecyclerView;
import com.widget.amazingimagepicker.view.SquareRelativeLayout;
import com.widget.amazingimagepicker.view.TextureVideoView;
import com.widget.amazingimagepicker.view.appbarlayout_23_2_1.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PickerActivity extends AppCompatActivity implements ScrollFeedbackRecyclerView.Callbacks {

    private final static int NUM_COLUMNS = 4;
    private final static int STORAGE_PERMISSION_RC = 0;

    private AppBarLayout appBarLayout;
    private PickerCoordinatorLayout coordinatorLayout;
    private ImageView imageContent;
    private SquareRelativeLayout contentContainer;
    private RelativeLayout videoContentContainer;
    private TextureVideoView videoContent;
    private Toolbar invisibleToolbar;
    private HeaderTouchDelegate headerTouchDelegate;
    private RecyclerView mRecyclerView;
    private View flipper;
    private View flipperIcon;
    private View emptyLayout;

    private Uri selectedUri = null;
    private String mToolbarTitle;
    private String mNextTitle;
    private int mToolbarColor;
    private int mStatusBarColor;
    private int mToolbarTextColor;

    private PickerAdapter pickerAdapter;
    private OffsetChangeListener offsetChangeListener;
    private PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setContentView(R.layout.activity_picker);

        setupViews(getIntent());

        coordinatorLayout = (PickerCoordinatorLayout) findViewById(R.id.coordinator_layout);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        ViewCompat.setElevation(appBarLayout, 0);
        imageContent = (ImageView) findViewById(R.id.image_content);
        contentContainer = (SquareRelativeLayout) findViewById(R.id.content_container);
        videoContentContainer = (RelativeLayout) findViewById(R.id.video_content_container);
        videoContent = new TextureVideoView(this);
        invisibleToolbar = (Toolbar) findViewById(R.id.invisible_toolbar);
        headerTouchDelegate = (HeaderTouchDelegate) findViewById(R.id.header_touch_delegate);
        flipperIcon = findViewById(R.id.flipper_icon);
        flipper = findViewById(R.id.flipper);
        flipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpanded(isAppBarCollapsed());
            }
        });
        emptyLayout = findViewById(R.id.empty_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LayoutManager(this));
        mRecyclerView.addItemDecoration(new ItemDecorationAlbumColumns<>(getResources().getDimensionPixelOffset(R.dimen.spacing), NUM_COLUMNS, PickerAdapter.class));
        pickerAdapter = new PickerAdapter(new ArrayList<Content>(), NUM_COLUMNS, new PickerAdapter.OnContentClickListener() {
            @Override
            public void onClick(Content content) {
                loadContent(content);
            }
        });
        mRecyclerView.setAdapter(pickerAdapter);

        offsetChangeListener = new OffsetChangeListener();
        appBarLayout.addOnOffsetChangedListener(offsetChangeListener);
        mAttacher = new PhotoViewAttacher(imageContent);
        GestureListener gestureListener = new GestureListener();
        final GestureDetectorCompat gestures = new GestureDetectorCompat(this, gestureListener);
        headerTouchDelegate.setGestureDetector(gestures);
        headerTouchDelegate.setOnTouchListener(mAttacher, imageContent);
        headerTouchDelegate.setOnGestureDetectorListener(gestureListener);
        headerTouchDelegate.setCallbacks(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_RC);
        } else {
            loadContentFromSDCard();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_RC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContentFromSDCard();
            } else {
                finish();
            }
        }
    }

    private void loadContentFromSDCard() {
        List<Content> all = new ArrayList<>();
        if (getIntent().hasExtra(Picker.Options.EXTRA_VIDEOS)) {
            List<Content> videos = ContentStoreAccessor.getAllVideos(this);
            all.addAll(videos);
        }
        if (getIntent().hasExtra(Picker.Options.EXTRA_IMAGES)) {
            List<Content> images = ContentStoreAccessor.getAllImages(this);
            all.addAll(images);
        }
        coordinatorLayout.setEnabled(all.size() > 0);
        emptyLayout.setVisibility(all.size() == 0 ? View.VISIBLE : View.GONE);
        pickerAdapter.setData(all);
        pickerAdapter.notifyDataSetChanged();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (pickerAdapter.getItemCount() > 0) {
                    loadContent(pickerAdapter.getItem(0));
                }
            }
        });
    }

    private void setupViews(@NonNull Intent intent) {
        mStatusBarColor = intent.getIntExtra(Picker.Options.EXTRA_STATUS_BAR_COLOR, ContextCompat.getColor(this, android.R.color.black));
        mToolbarColor = intent.getIntExtra(Picker.Options.EXTRA_TOOL_BAR_COLOR, ContextCompat.getColor(this, android.R.color.black));
        mToolbarTextColor = intent.getIntExtra(Picker.Options.EXTRA_PICKER_TITLE_COLOR_TOOLBAR, ContextCompat.getColor(this, android.R.color.white));
        mToolbarTitle = intent.getStringExtra(Picker.Options.EXTRA_PICKER_TITLE_TEXT_TOOLBAR);
        mNextTitle = intent.getStringExtra(Picker.Options.EXTRA_PICKER_NEXT_TEXT_TOOLBAR);

        setupAppBar();
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {
        setStatusBarColor(mStatusBarColor);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setBackgroundColor(mToolbarColor);
        toolbar.setTitleTextColor(mToolbarTextColor);
        toolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View menuNext = findViewById(R.id.menu_next);
                if (menuNext != null) {
                    if (menuNext instanceof TextView) {
                        TextView tv = (TextView) menuNext;
                        tv.setAllCaps(false);
                        tv.setText(mNextTitle);
                        tv.setTextColor(mToolbarTextColor);
                        tv.setMinWidth(tv.getWidth());
                    }
                    if (Build.VERSION.SDK_INT < 16) {
                        toolbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });

        final TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(mToolbarTextColor);
        toolbarTitle.setText(mToolbarTitle);

        // Color buttons inside the Toolbar
        Drawable stateButtonDrawable = ContextCompat.getDrawable(this, R.drawable.picker_clear_white_24dp).mutate();
        stateButtonDrawable.setColorFilter(mToolbarTextColor, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(stateButtonDrawable);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getWindow() != null) {
                getWindow().setStatusBarColor(color);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.picker, menu);

        MenuItem next = menu.findItem(R.id.menu_next);
        next.setTitle(mNextTitle);

        Drawable defaultIcon = next.getIcon();
        if (defaultIcon != null && TextUtils.isEmpty(mNextTitle)) {
            defaultIcon.mutate();
            defaultIcon.setColorFilter(mToolbarTextColor, PorterDuff.Mode.SRC_ATOP);
            next.setIcon(defaultIcon);
        } else if (defaultIcon != null) {
            next.setIcon(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_next) {
            sendResult();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadContent(final Content content) {
        switch (content.getType()) {
            case IMAGE:
                loadImage(content.getContentUri());
                break;
            case VIDEO:
                videoContentContainer.removeAllViews();
                videoContent.release();
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
            headerTouchDelegate.setBottom(appBarLayout.getBottom() - invisibleToolbar.getHeight());
        }
        if (videoContent != null && videoContent.getVisibility() == View.VISIBLE) {
            headerTouchDelegate.setBottom(appBarLayout.getBottom() - invisibleToolbar.getHeight());
        }
    }

    private void sendResult() {
        Intent intent = new Intent();
        if (selectedUri != null) {
            intent.setData(selectedUri);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        public boolean onUp(MotionEvent event) {
            if (offsetChangeListener.getLastNotNullOffsetChange() != 0) {
                setExpanded(offsetChangeListener.getLastNotNullOffsetChange() > 0);
            }
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
            int degree = - 180 * offset / contentContainer.getHeight();
            flipperIcon.setRotationX(degree);
            updateTouchDelegate();
        }
    }
}
