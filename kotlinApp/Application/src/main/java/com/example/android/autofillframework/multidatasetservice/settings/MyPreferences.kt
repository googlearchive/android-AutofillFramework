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
package com.example.android.autofillframework.multidatasetservice.settings

import android.content.Context
import android.content.SharedPreferences
import android.service.autofill.Dataset
import android.service.autofill.FillResponse

object MyPreferences {
    private val TAG = "MyPreferences"

    private val SHARED_PREF_KEY = "com.example.android.autofillframework.service.settings.MyPreferences"
    private val RESPONSE_AUTH_KEY = "response_auth"
    private val DATASET_AUTH_KEY = "dataset_auth"
    private val MASTER_PASSWORD_KEY = "master_password"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
    }

    /**
     * Determines whether [FillResponse]s should require authentication.
     */
    fun isResponseAuth(context: Context): Boolean {
        return getPrefs(context).getBoolean(RESPONSE_AUTH_KEY, false)
    }

    fun setResponseAuth(context: Context, responseAuth: Boolean) {
        getPrefs(context).edit().putBoolean(RESPONSE_AUTH_KEY, responseAuth).apply()
    }

    /**
     * Determines whether [Dataset]s should require authentication.
     */
    fun isDatasetAuth(context: Context): Boolean {
        return getPrefs(context).getBoolean(DATASET_AUTH_KEY, false)
    }

    fun setDatasetAuth(context: Context, datasetAuth: Boolean) {
        getPrefs(context).edit().putBoolean(DATASET_AUTH_KEY, datasetAuth).apply()
    }

    /**
     * Gets autofill master password.
     */
    fun getMasterPassword(context: Context): String? {
        return getPrefs(context).getString(MASTER_PASSWORD_KEY, null)
    }

    /**
     * Sets autofill master password.
     */
    fun setMasterPassword(context: Context, masterPassword: String) {
        getPrefs(context).edit().putString(MASTER_PASSWORD_KEY, masterPassword).apply()
    }

    /**
     * Removes master password.
     */
    fun clearCredentials(context: Context) {
        getPrefs(context).edit().remove(MASTER_PASSWORD_KEY).apply()
    }
}
