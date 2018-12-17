/*
 * Copyright (C) 2018 CypherOS
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
package com.android.launcher3.quickspace;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.internal.util.custom.weather.WeatherClient;
import com.android.internal.util.custom.weather.WeatherClient.WeatherInfo;
import com.android.internal.util.custom.weather.WeatherClient.WeatherObserver;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import java.util.List;
import java.util.ArrayList;

public class QuickEventsController {

    // private static final int AMBIENT_INFO_MAX_DURATION = 120000; // 2 minutes
    private static final String SETTING_DEVICE_INTRO_COMPLETED = "device_introduction_completed";
    private Context mContext;

    private String mEventTitle;
    private String mEventTitleSub;
    private OnClickListener mEventTitleSubAction = null;
    private int mEventSubIcon;

    private boolean mIsQuickEvent = false;

    public QuickEventsController(Context context) {
        mContext = context;
        //context.registerReceiver(mAmbientReceiver, new IntentFilter(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction()));
        initQuickEvents();
    }

    public void initQuickEvents() {}

    public boolean isQuickEvent() {
        return mIsQuickEvent;
    }

    public String getTitle() {
        return mEventTitle;
    }

    public String getActionTitle() {
        return mEventTitleSub;
    }

    public OnClickListener getAction() {
        return mEventTitleSubAction;
    }

    public int getActionIcon() {
        return mEventSubIcon;
    }
}
