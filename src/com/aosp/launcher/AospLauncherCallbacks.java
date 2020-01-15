/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aosp.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.AppInfo;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.settings.SettingsActivity;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener;
import com.android.launcher3.util.Themes;
import com.android.launcher3.Utilities;

import com.google.android.libraries.gsa.launcherclient.ClientOptions;
import com.google.android.libraries.gsa.launcherclient.ClientService;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AospLauncherCallbacks implements LauncherCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener, OnChangeListener {
    public static final String SEARCH_PACKAGE = "com.google.android.googlequicksearchbox";

    private final AospLauncher mLauncher;

    private OverlayCallbackImpl mOverlayCallbacks;
    private LauncherClient mLauncherClient;
    private SharedPreferences mPrefs;

    private boolean mStarted;
    private boolean mResumed;
    private boolean mAlreadyOnHome;

    private final Bundle mUiInformation = new Bundle();

    public AospLauncherCallbacks(AospLauncher launcher) {
        mLauncher = launcher;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPrefs = Utilities.getPrefs(mLauncher);
        mOverlayCallbacks = new OverlayCallbackImpl(mLauncher);
        mLauncherClient = new LauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(mPrefs));
        mOverlayCallbacks.setClient(mLauncherClient);
        mUiInformation.putInt("system_ui_visibility", mLauncher.getWindow().getDecorView().getSystemUiVisibility());
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        mResumed = true;
        if (mStarted) {
            mAlreadyOnHome = true;
        }

        mLauncherClient.onResume();
    }

    @Override
    public void onStart() {
        mStarted = true;
        mLauncherClient.onStart();
    }

    @Override
    public void onStop() {
        mStarted = false;
        if (!mResumed) {
            mAlreadyOnHome = false;
        }

        mLauncherClient.onStop();
    }

    @Override
    public void onPause() {
        mResumed = false;
        mLauncherClient.onPause();
    }

    @Override
    public void onDestroy() {
        if (!mLauncherClient.isDestroyed()) {
            mLauncherClient.getActivity().unregisterReceiver(mLauncherClient.mInstallListener);
        }

        mLauncherClient.setDestroyed(true);
        mLauncherClient.getBaseService().disconnect();
        if (mLauncherClient.getOverlayCallback() != null) {
            mLauncherClient.getOverlayCallback().mClient = null;
            mLauncherClient.getOverlayCallback().mWindowManager = null;
            mLauncherClient.getOverlayCallback().mWindow = null;
            mLauncherClient.setOverlayCallback(null);
        }

        ClientService service = mLauncherClient.getClientService();
        LauncherClient client = service.getClient();
        if (client != null && client.equals(mLauncherClient)) {
            service.mWeakReference = null;
            if (!mLauncherClient.getActivity().isChangingConfigurations()) {
                service.disconnect();
                if (ClientService.sInstance == service) {
                    ClientService.sInstance = null;
                }
            }
        }

        Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
        WallpaperColorInfo.getInstance(mLauncher).removeOnChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onAttachedToWindow() {
        mLauncherClient.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        if (!mLauncherClient.isDestroyed()) {
            mLauncherClient.getEventInfo().parse(0, "detachedFromWindow", 0.0f);
            mLauncherClient.setParams(null);
        }
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {

    }

    @Override
    public void onHomeIntent(boolean internalStateHandled) {
        mLauncherClient.hideOverlay(mAlreadyOnHome);
    }

    @Override
    public boolean handleBackPressed() {
        return false;
    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onLauncherProviderChange() {

    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (SettingsActivity.MINUS_ONE_KEY.equals(key)) {
            ClientOptions clientOptions = getClientOptions(prefs);
            if (clientOptions.options != mLauncherClient.mFlags) {
                mLauncherClient.mFlags = clientOptions.options;
                if (mLauncherClient.getParams() != null) {
                    mLauncherClient.updateConfiguration();
                }
                mLauncherClient.getEventInfo().parse("setClientOptions ", mLauncherClient.mFlags);
            }
        }
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);
        mUiInformation.putInt("background_color_hint", primaryColor(wallpaperColorInfo, mLauncher, alpha));
        mUiInformation.putInt("background_secondary_color_hint", secondaryColor(wallpaperColorInfo, mLauncher, alpha));
        mUiInformation.putBoolean("is_background_dark", Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark));
        mLauncherClient.redraw(mUiInformation);
    }

    private ClientOptions getClientOptions(SharedPreferences prefs) {
        boolean hasPackage = AospUtils.hasPackageInstalled(mLauncher, SEARCH_PACKAGE);
        boolean isEnabled = prefs.getBoolean(SettingsActivity.MINUS_ONE_KEY, true);
        int canUse = hasPackage && isEnabled ? 1 : 0;
        return new ClientOptions(canUse | 2 | 4 | 8);
    }

    public static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getMainColor(), alpha), context);
    }

    public static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    public static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }
}