package com.android.launcher3.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import com.android.launcher3.settings.SettingsActivity;
import com.android.launcher3.R;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

@SuppressWarnings("unused")
public class ReloadingListPreference extends ListPreference
        implements SettingsActivity.OnResumePreferenceCallback {
    public interface OnReloadListener {
        ThreadSwitchingRunnable listUpdater(ListPreference pref);
    }

    /**
     * Interface that runs a Supplier on a background thread, then continues with the result
     * as a Runnable on the main thread.
     */
    @SuppressWarnings("WeakerAccess")
    public interface ThreadSwitchingRunnable extends Supplier<Runnable> {
    }

    private OnReloadListener mOnReloadListener;

    public ReloadingListPreference(Context context) {
        super(context);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                   int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        // Run the entries updater on the main thread immediately.
        // Should be fast as the data was cached from the async load before.
        // If it wasn't, we need to block to ensure the data has been loaded.
        loadEntries(false);
        super.onClick();
    }

    public void setOnReloadListener(Function<Context, OnReloadListener> supplier) {
        mOnReloadListener = supplier.apply(getContext());
        loadEntries(true);
    }
    @Override
    public void onResume() {
        loadEntries(true);
    }

    private void loadEntries(boolean async) {
        if (mOnReloadListener != null) {
            if (async) {
                if (getEntryValues() == null) {
                    setSummary(R.string.loading);
                }
                MODEL_EXECUTOR.execute(() -> {
                        Runnable uiRunnable = mOnReloadListener.listUpdater(this).get();
                        MAIN_EXECUTOR.execute(() -> {
                            uiRunnable.run();
                            setSummary("%s");
                        });
                });
            } else {
                mOnReloadListener.listUpdater(this).get().run();
            }
        }
    }
}
