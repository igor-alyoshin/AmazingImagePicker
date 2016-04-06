package com.widget.amazingimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Picker {

    public static int REQUEST_CODE = 1212;

    private static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;

    Picker() {
    }
    
    private Bundle mPickerOptionsBundle = new Bundle();

    public static Picker get() {
        return new Picker();
    }
    
    public void pickImages(Activity activity) {
        Intent intent = new Intent(activity, PickerActivity.class);
        intent.putExtra(Options.EXTRA_IMAGES, true);
        intent.putExtras(mPickerOptionsBundle);
        activity.startActivityForResult(intent, Picker.REQUEST_CODE);
    }

    public void pickVideos(Activity activity) {
        Intent intent = new Intent(activity, PickerActivity.class);
        intent.putExtra(Options.EXTRA_VIDEOS, true);
        intent.putExtras(mPickerOptionsBundle);
        activity.startActivityForResult(intent, Picker.REQUEST_CODE);
    }

    public void pickAll(Activity activity) {
        Intent intent = new Intent(activity, PickerActivity.class);
        intent.putExtra(Options.EXTRA_VIDEOS, true);
        intent.putExtra(Options.EXTRA_IMAGES, true);
        intent.putExtras(mPickerOptionsBundle);
        activity.startActivityForResult(intent, Picker.REQUEST_CODE);
    }
    
    public Picker withOptions(@NonNull Options options) {
        mPickerOptionsBundle.putAll(options.getOptionBundle());
        return this;
    }

    /**
     * Class that helps to setup advanced configs that are not commonly used.
     * Use it with method {@link #withOptions(Options)}
     */
    public static class Options {
        
        public final static String EXTRA_IMAGES= "EXTRA_IMAGES";
        public final static String EXTRA_VIDEOS= "EXTRA_VIDEOS";
        
        public static final String EXTRA_TOOL_BAR_COLOR = EXTRA_PREFIX + ".ToolbarColor";
        public static final String EXTRA_STATUS_BAR_COLOR = EXTRA_PREFIX + ".StatusBarColor";
        public static final String EXTRA_PICKER_TITLE_TEXT_TOOLBAR = EXTRA_PREFIX + ".PickerToolbarTitleText";
        public static final String EXTRA_PICKER_NEXT_TEXT_TOOLBAR = EXTRA_PREFIX + ".PickerToolbarNextText";
        public static final String EXTRA_PICKER_TITLE_COLOR_TOOLBAR = EXTRA_PREFIX + ".PickerToolbarTitleColor";

        private final Bundle mOptionBundle;

        public Options() {
            mOptionBundle = new Bundle();
        }

        @NonNull
        public Bundle getOptionBundle() {
            return mOptionBundle;
        }

        /**
         * @param color - desired resolved color of the toolbar
         */
        public void setToolbarColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_TOOL_BAR_COLOR, color);
        }

        /**
         * @param color - desired resolved color of the statusbar
         */
        public void setStatusBarColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_STATUS_BAR_COLOR, color);
        }

        /**
         * @param color - desired resolved color of Toolbar text and buttons (default is darker orange)
         */
        public void setToolbarTitleTextColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_PICKER_TITLE_COLOR_TOOLBAR, color);
        }

        /**
         * @param text - desired text for Toolbar title
         */
        public void setToolbarTitle(@Nullable String text) {
            mOptionBundle.putString(EXTRA_PICKER_TITLE_TEXT_TOOLBAR, text);
        }

        /**
         * @param text - desired text for Toolbar next action
         */
        public void setNextTitle(@Nullable String text) {
            mOptionBundle.putString(EXTRA_PICKER_NEXT_TEXT_TOOLBAR, text);
        }
    }
}
