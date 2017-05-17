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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Singleton autofill data repository, that stores autofill fields to SharedPreferences.
 * DISCLAIMER, you should not store sensitive fields like user data unencrypted. This is only done
 * here for simplicity and learning purposes.
 */
public class LocalAutofillRepository implements AutofillRepository {
    private static final String SHARED_PREF_KEY = "com.example.android.autofillframework.service";
    private static final String CLIENT_FORM_DATA_KEY = "loginCredentialDatasets";
    private static final String DATASET_NUMBER_KEY = "datasetNumber";

    private static LocalAutofillRepository sInstance;

    private final SharedPreferences mPrefs;

    // TODO prepend with autofill data set in Settings.
    private LocalAutofillRepository(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    public static LocalAutofillRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocalAutofillRepository(context);
        }
        return sInstance;
    }

    @Override
    public HashMap<String, ClientFormData> getClientFormData(List<String> focusedAutofillHints,
            List<String> allAutofillHints) {
        try {
            // TODO use sqlite instead.
            boolean hasDataForFocusedAutofillHints = false;
            HashMap<String, ClientFormData> clientFormDataMap = new HashMap<>();
            Set<String> clientFormDataStringSet = getAllAutofillDataStringSet();
            for (String clientFormDataString : clientFormDataStringSet) {
                ClientFormData clientFormData = ClientFormData
                        .fromJson(new JSONObject(clientFormDataString));
                if (clientFormData != null) {
                    if (clientFormData.helpsWithHints(focusedAutofillHints)) {
                        hasDataForFocusedAutofillHints = true;
                    }
                    if (clientFormData.helpsWithHints(allAutofillHints)) {
                        clientFormDataMap.put(clientFormData.getDatasetName(), clientFormData);
                    }
                }
            }
            if (hasDataForFocusedAutofillHints) {
                return clientFormDataMap;
            } else {
                return null;
            }
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public void saveClientFormData(ClientFormData clientFormData) {
        //TODO use sqlite instead.
        String datasetName = "dataset-" + getDatasetNumber();
        clientFormData.setDatasetName(datasetName);
        Set<String> allAutofillData = getAllAutofillDataStringSet();
        allAutofillData.add(clientFormData.toJson().toString());
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