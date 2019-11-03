package com.android.launcher3.shadespace;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.launcher3.Utilities;
import com.android.launcher3.notification.NotificationListener;

import java.util.Collections;
import java.util.List;

/**
 * Paused mode is not supported on Marshmallow because the MediaSession is missing
 * notifications. Without this information, it is impossible to hide on stop.
 */
public class MediaListener extends MediaController.Callback
        implements MediaSessionManager.OnActiveSessionsChangedListener {
    private static final String TAG = "MediaListener";

    private final ComponentName mComponent;
    private final MediaSessionManager mManager;
    private final List<StatusBarNotification> mSbn;
    private final Runnable mOnChange;
    private List<MediaController> mControllers = Collections.emptyList();
    private MediaController mTracking;

    MediaListener(Context context, List<StatusBarNotification> sbn, Runnable onChange) {
        mComponent = new ComponentName(context, NotificationListener.class);
        mManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSbn = sbn;
        mOnChange = onChange;
    }

    void onResume() {
        try {
            mManager.addOnActiveSessionsChangedListener(this, mComponent);
        } catch (SecurityException ignored) {
        }
        onActiveSessionsChanged(null); // Bind all current controllers.
    }

    void onPause() {
        mManager.removeOnActiveSessionsChangedListener(this);
        onActiveSessionsChanged(Collections.emptyList()); // Unbind all previous controllers.
    }

    boolean isTracking() {
        return mTracking != null;
    }

    CharSequence getTitle() {
        return mTracking.getMetadata().getText(MediaMetadata.METADATA_KEY_TITLE);
    }

    CharSequence getArtist() {
        return mTracking.getMetadata().getText(MediaMetadata.METADATA_KEY_ARTIST);
    }

    CharSequence getAlbum() {
        return mTracking.getMetadata().getText(MediaMetadata.METADATA_KEY_ALBUM);
    }

    String getPackage() {
        return mTracking.getPackageName();
    }

    private void updateControllers(List<MediaController> controllers) {
        for (MediaController mc : mControllers) {
            mc.unregisterCallback(this);
        }
        for (MediaController mc : controllers) {
            mc.registerCallback(this);
        }
        mControllers = controllers;
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (controllers == null) {
            try {
                controllers = mManager.getActiveSessions(mComponent);
            } catch (SecurityException ignored) {
                controllers = Collections.emptyList();
            }
        }
        updateControllers(controllers);

        // If the current controller is not paused or playing, stop tracking it.
        if (mTracking != null
                && (!controllers.contains(mTracking) || !isPausedOrPlaying(mTracking))) {
            mTracking = null;
        }

        for (MediaController mc : controllers) {
            // Either we are not tracking a controller and this one is valid,
            // or this one is playing while the one we track is not.
            if ((mTracking == null && isPausedOrPlaying(mc))
                    || (isPlaying(mc) && !isPlaying(mTracking))) {
                mTracking = mc;
            }
        }

        mOnChange.run();
    }

    private void pressButton(int keyCode) {
        if (mTracking != null) {
            mTracking.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            mTracking.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        }
    }

    void toggle(boolean finalClick) {
        if (Utilities.ATLEAST_NOUGAT && !finalClick) {
            Log.d(TAG, "Toggle");
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }
    }

    void next(boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Next");
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    void previous(boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Previous");
            pressButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    private boolean isPlaying(MediaController mc) {
        return (!Utilities.ATLEAST_NOUGAT || hasNotification(mc))
                && hasTitle(mc)
                && mc.getPlaybackState() != null
                && mc.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
    }

    private boolean isPausedOrPlaying(MediaController mc) {
        if (Utilities.ATLEAST_NOUGAT) {
            if (!hasNotification(mc) || !hasTitle(mc) || mc.getPlaybackState() == null) {
                return false;
            }
            int state = mc.getPlaybackState().getState();
            return state == PlaybackState.STATE_PAUSED
                    || state == PlaybackState.STATE_PLAYING;
        }
        return isPlaying(mc);
    }

    private boolean hasTitle(MediaController mc) {
        return mc.getMetadata() != null
                && !TextUtils.isEmpty(mc.getMetadata().getText(MediaMetadata.METADATA_KEY_TITLE));
    }

    // If there is no notification, consider the state to be stopped.
    private boolean hasNotification(MediaController mc) {
        MediaSession.Token controllerToken = mc.getSessionToken();
        for (StatusBarNotification notif : mSbn) {
            Bundle extras = notif.getNotification().extras;
            MediaSession.Token notifToken = extras.getParcelable(Notification.EXTRA_MEDIA_SESSION);
            if (controllerToken.equals(notifToken)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Events that refresh the current handler.
     */
    public void onPlaybackStateChanged(PlaybackState state) {
        super.onPlaybackStateChanged(state);
        onActiveSessionsChanged(null);
    }

    public void onMetadataChanged(MediaMetadata metadata) {
        super.onMetadataChanged(metadata);
        onActiveSessionsChanged(null);
    }
}
