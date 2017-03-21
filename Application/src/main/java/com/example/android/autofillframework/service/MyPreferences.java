/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.autofillframework.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

final class MyPreferences {
    private static final String TAG = "MyPreferences";

    private static final String PREF_NUMBER_DATASET = "number_datasets";
    private static final String PREF_RESPONSE_AUTH = "response_auth";
    private static final String PREF_DATASET_AUTH = "dataset_auth";
    private static MyPreferences sInstance;
    private final SharedPreferences mPrefs;

    private MyPreferences(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences("my-settings",
                Context.MODE_PRIVATE);
    }

    static MyPreferences getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MyPreferences(context);
        }
        return sInstance;
    }

    /**
     * Gets the number of {@link Dataset}s that should be added to a {@link FillResponse}.
     */
    int getNumberDatasets() {
        return mPrefs.getInt(PREF_NUMBER_DATASET, 2);
    }

    /**
     * Gets whether {@link FillResponse}s should require authentication.
     */
    boolean isResponseAuth() {
        return mPrefs.getBoolean(PREF_RESPONSE_AUTH, false);
    }


    /**
     * Gets whether {@link Dataset}s should require authentication.
     */
    boolean isDatasetAuth() {
        return mPrefs.getBoolean(PREF_DATASET_AUTH, false);
    }

    void bulkEdit(int numberDatasets, boolean responseAuth, boolean datasetAuth) {
        Log.v(TAG, "bulk edit:" + numberDatasets + ":" + responseAuth + ":" + datasetAuth);
        mPrefs.edit()
                .putInt(PREF_NUMBER_DATASET, numberDatasets)
                .putBoolean(PREF_RESPONSE_AUTH, responseAuth)
                .putBoolean(PREF_DATASET_AUTH, datasetAuth)
                .apply();

    }
}