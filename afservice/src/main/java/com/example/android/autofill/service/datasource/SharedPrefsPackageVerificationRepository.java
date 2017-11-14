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
package com.example.android.autofill.service.datasource;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.autofill.service.SecurityHelper;

import static com.example.android.autofill.service.Util.logd;
import static com.example.android.autofill.service.Util.logw;

public class SharedPrefsPackageVerificationRepository implements PackageVerificationDataSource {

    private static final String SHARED_PREF_KEY = "com.example.android.autofill.service"
            + ".datasource.PackageVerificationDataSource";
    private static PackageVerificationDataSource sInstance;

    private SharedPrefsPackageVerificationRepository() {
    }

    public static PackageVerificationDataSource getInstance() {
        if (sInstance == null) {
            sInstance = new SharedPrefsPackageVerificationRepository();
        }
        return sInstance;
    }

    @Override
    public void clear(Context context) {
        context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @Override
    public boolean putPackageSignatures(Context context, String packageName) {
        String hash;
        try {
            hash = SecurityHelper.getFingerprint(context, packageName);
            logd("Hash for %s: %s", packageName, hash);
        } catch (Exception e) {
            logw(e, "Error getting hash for %s.", packageName);
            return false;
        }

        if (!containsSignatureForPackage(context, packageName)) {
            // Storage does not yet contain signature for this package name.
            context.getApplicationContext()
                    .getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putString(packageName, hash)
                    .apply();
            return true;
        }
        return containsMatchingSignatureForPackage(context, packageName, hash);
    }

    private boolean containsSignatureForPackage(Context context, String packageName) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(
                SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return prefs.contains(packageName);
    }

    private boolean containsMatchingSignatureForPackage(Context context, String packageName,
            String hash) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(
                SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return hash.equals(prefs.getString(packageName, null));
    }
}
