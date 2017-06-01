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
package com.example.android.autofillframework.service.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;

import com.example.android.autofillframework.service.model.ClientFormData;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Singleton autofill data repository that stores autofill fields to SharedPreferences.
 * Disclaimer: you should not store sensitive fields like user data unencrypted. This is done
 * here only for simplicity and learning purposes.
 */
public class SharedPrefsAutofillRepository implements AutofillRepository {
    private static final String SHARED_PREF_KEY = "com.example.android.autofillframework.service";
    private static final String CLIENT_FORM_DATA_KEY = "loginCredentialDatasets";
    private static final String DATASET_NUMBER_KEY = "datasetNumber";

    private static SharedPrefsAutofillRepository sInstance;

    private final SharedPreferences mPrefs;

    private SharedPrefsAutofillRepository(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static SharedPrefsAutofillRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SharedPrefsAutofillRepository(context);
        }
        return sInstance;
    }

    @Override
    public HashMap<String, ClientFormData> getClientFormData(List<String> focusedAutofillHints,
            List<String> allAutofillHints) {
        boolean hasDataForFocusedAutofillHints = false;
        HashMap<String, ClientFormData> clientFormDataMap = new HashMap<>();
        Set<String> clientFormDataStringSet = getAllAutofillDataStringSet();
        for (String clientFormDataString : clientFormDataStringSet) {
            ClientFormData clientFormData = new Gson().fromJson(clientFormDataString, ClientFormData.class);
            if (clientFormData != null) {
                if (clientFormData.helpsWithHints(focusedAutofillHints)) {
                    // Saved data has data relevant to at least 1 of the hints associated with the
                    // View in focus.
                    hasDataForFocusedAutofillHints = true;
                }
                if (clientFormData.helpsWithHints(allAutofillHints)) {
                    // Saved data has data relevant to at least 1 of these hints associated with any
                    // of the Views in the hierarchy.
                    clientFormDataMap.put(clientFormData.getDatasetName(), clientFormData);
                }
            }
        }
        if (hasDataForFocusedAutofillHints) {
            return clientFormDataMap;
        } else {
            return null;
        }
    }

    @Override
    public void saveClientFormData(ClientFormData clientFormData) {
        String datasetName = "dataset-" + getDatasetNumber();
        clientFormData.setDatasetName(datasetName);
        Set<String> allAutofillData = getAllAutofillDataStringSet();
        allAutofillData.add(new Gson().toJson(clientFormData));
        saveAllAutofillDataStringSet(allAutofillData);
        incrementDatasetNumber();
    }

    @Override
    public void clear() {
        mPrefs.edit().remove(CLIENT_FORM_DATA_KEY).apply();
    }

    private Set<String> getAllAutofillDataStringSet() {
        return mPrefs.getStringSet(CLIENT_FORM_DATA_KEY, new ArraySet<String>());
    }

    private void saveAllAutofillDataStringSet(Set<String> allAutofillDataStringSet) {
        mPrefs.edit().putStringSet(CLIENT_FORM_DATA_KEY, allAutofillDataStringSet).apply();
    }

    /**
     * For simplicity, datasets will be named in the form "dataset-X" where X means
     * this was the Xth dataset saved.
     */
    private int getDatasetNumber() {
        return mPrefs.getInt(DATASET_NUMBER_KEY, 0);
    }

    /**
     * Every time a dataset is saved, this should be called to increment the dataset number.
     * (only important for this service's dataset naming scheme).
     */
    private void incrementDatasetNumber() {
        mPrefs.edit().putInt(DATASET_NUMBER_KEY, getDatasetNumber() + 1).apply();
    }
}