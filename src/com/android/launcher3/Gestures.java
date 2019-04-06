/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.launcher3;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;

import static com.android.launcher3.Utilities.getDevicePrefs;

public class Gestures extends SettingsActivity
        implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    public static final String KEY_HOMESCREEN_DT_GESTURES = "pref_homescreen_dt_gestures";
    public static final String KEY_HOMESCREEN_SWIPE_DOWN_GESTURES =
        "pref_homescreen_swipe_down_gestures";

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null) {
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GesturesSettingsFragment()).commit();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
            Preference preference) {
        Fragment instantiate = Fragment.instantiate(this, preference.getFragment(),
            preference.getExtras());
        if (instantiate instanceof DialogFragment) {
            ((DialogFragment) instantiate).show(getFragmentManager(), preference.getKey());
        } else {
            getFragmentManager().beginTransaction().replace(
                android.R.id.content, instantiate).addToBackStack(preference.getKey()).commit();
        }
        return true;
    }

    public static class GesturesSettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private Context mContext;

        ActionBar actionBar;

        private ListPreference mDoubleTapGestures;
        private ListPreference mSwipeDownGestures;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.gestures_preferences);

            mContext = getActivity();

            actionBar = getActivity().getActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);

            mDoubleTapGestures = (ListPreference) findPreference(KEY_HOMESCREEN_DT_GESTURES);
            mDoubleTapGestures.setValue(getDevicePrefs(mContext).getString(
                KEY_HOMESCREEN_DT_GESTURES, "0"));
            mDoubleTapGestures.setSummary(mDoubleTapGestures.getEntry());
            mDoubleTapGestures.setOnPreferenceChangeListener(this);

            mSwipeDownGestures = (ListPreference) findPreference(KEY_HOMESCREEN_SWIPE_DOWN_GESTURES);
            mSwipeDownGestures.setValue(getDevicePrefs(mContext).getString(
                KEY_HOMESCREEN_SWIPE_DOWN_GESTURES, "7"));
            mSwipeDownGestures.setSummary(mSwipeDownGestures.getEntry());
            mSwipeDownGestures.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, final Object newValue) {
            switch (preference.getKey()) {
                case KEY_HOMESCREEN_DT_GESTURES:
                    String dtGestureValue = (String) newValue;
                    getDevicePrefs(mContext).edit().putString(KEY_HOMESCREEN_DT_GESTURES,
                        dtGestureValue).commit();
                    mDoubleTapGestures.setValue(dtGestureValue);
                    mDoubleTapGestures.setSummary(mDoubleTapGestures.getEntry());
                    LauncherAppState.getInstanceNoCreate().setNeedsRestart();
                    break;
                case KEY_HOMESCREEN_SWIPE_DOWN_GESTURES:
                    String swipeDownGestureValue = (String) newValue;
                    getDevicePrefs(mContext).edit().putString(KEY_HOMESCREEN_SWIPE_DOWN_GESTURES,
                        swipeDownGestureValue).commit();
                    mSwipeDownGestures.setValue(swipeDownGestureValue);
                    mSwipeDownGestures.setSummary(mSwipeDownGestures.getEntry());
                    LauncherAppState.getInstanceNoCreate().setNeedsRestart();
                    break;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
