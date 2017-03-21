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
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * Singleton holding Autofill data. In this simple Autofill service, it only holds LoginCredentials
 * for every client app that uses the service.
 */
final class AutoFillData {

    private static AutoFillData sAutoFillData;

    // Structure: {<packageName> : {<datasetName> : <loginCredential object>, ...}, ...}
    private final Map<String, Map<String, LoginCredential>> mLoginCredentials =
            new LinkedHashMap<>();

    private AutoFillData() {
    }

    public static AutoFillData getInstance() {
        if (sAutoFillData == null) {
            sAutoFillData = new AutoFillData();
        }
        return sAutoFillData;
    }

    /**
     * Get all login credentials associated with the caller's package.
     */
    public Map<String, LoginCredential> getCredentialsMap(Context context) {
        int numDatasets = MyPreferences.getInstance(context).getNumberDatasets();
        for (int i = 0; i < numDatasets; i++) {
            LoginCredential loginCredential =
                    new LoginCredential("user" + i, "user" + i);
            updateCredentials(context.getPackageName(), loginCredential);
        }
        return mLoginCredentials.get(context.getPackageName());
    }

    /**
     * Add a loginCredential mapped to the caller's package (maintaining idempotency).
     */
    public void updateCredentials(String packageName, LoginCredential loginCredential) {
        if (!mLoginCredentials.containsKey(packageName)) {
            mLoginCredentials.put(packageName, new LinkedHashMap<String, LoginCredential>());
        }
        mLoginCredentials.get(packageName).put(loginCredential.getDatasetName(), loginCredential);
        Log.d(TAG, "Creating credentials for " + packageName + ":" +
                loginCredential.getDatasetName());
    }
}