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
package com.android.launcher3.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PackageStatusReceiver extends BroadcastReceiver {

    private Context mContext;

    public PackageStatusReceiver(Context context) {
        mContext = context;
    }

    public void register(String pkg) {
        mContext.registerReceiver(this, getFilter(pkg, "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_FULLY_REMOVED"));
    }

    public void unregister() {
        mContext.unregisterReceiver(this);
    }

    public void onReceive(Context context, Intent intent) {
        // Will be overriden
    }

    public IntentFilter getFilter(String pkg, String... action) {
        IntentFilter intentFilter = new IntentFilter();
        for (String actionList : action) {
            intentFilter.addAction(actionList);
        }
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart(pkg, 0);
        return intentFilter;
    }
}
