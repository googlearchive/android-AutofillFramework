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
package com.example.android.autofill.service.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.support.annotation.NonNull;

import com.example.android.autofill.service.Util;

public class MyPreferences {
    private static final String RESPONSE_AUTH_KEY = "response_auth";
    private static final String DATASET_AUTH_KEY = "dataset_auth";
    private static final String MASTER_PASSWORD_KEY = "master_password";
    private static final String LOGGING_LEVEL = "logging_level";
    private static MyPreferences sInstance;
    private final SharedPreferences mPrefs;

    private MyPreferences(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences("my-settings",
                Context.MODE_PRIVATE);
    }

    public static MyPreferences getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MyPreferences(context);
        }
        return sInstance;
    }

    /**
     * Gets whether {@link FillResponse}s should require authentication.
     */
    public boolean isResponseAuth() {
        return mPrefs.getBoolean(RESPONSE_AUTH_KEY, false);
    }

    /**
     * Enables/disables authentication for the entire autofill {@link FillResponse}.
     */
    public void setResponseAuth(boolean responseAuth) {
        mPrefs.edit().putBoolean(RESPONSE_AUTH_KEY, responseAuth).apply();
    }

    /**
     * Gets whether {@link Dataset}s should require authentication.
     */
    public boolean isDatasetAuth() {
        return mPrefs.getBoolean(DATASET_AUTH_KEY, false);
    }

    /**
     * Enables/disables authentication for individual autofill {@link Dataset}s.
     */
    public void setDatasetAuth(boolean datasetAuth) {
        mPrefs.edit().putBoolean(DATASET_AUTH_KEY, datasetAuth).apply();
    }

    /**
     * Gets autofill master username.
     */
    public String getMasterPassword() {
        return mPrefs.getString(MASTER_PASSWORD_KEY, null);
    }

    /**
     * Sets autofill master password.
     */
    public void setMasterPassword(@NonNull String masterPassword) {
        mPrefs.edit().putString(MASTER_PASSWORD_KEY, masterPassword).apply();
    }

    public void clearCredentials() {
        mPrefs.edit().remove(MASTER_PASSWORD_KEY).apply();
    }

    public Util.LogLevel getLoggingLevel() {
        return Util.LogLevel.values()[mPrefs.getInt(LOGGING_LEVEL, Util.LogLevel.OFF.ordinal())];
    }

    public void setLoggingLevel(Util.LogLevel level) {
        mPrefs.edit().putInt(LOGGING_LEVEL, level.ordinal()).apply();
        Util.setLoggingLevel(level);
    }
}
