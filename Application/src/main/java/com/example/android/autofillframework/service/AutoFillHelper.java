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
import android.content.IntentSender;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.util.Log;
import android.view.autofill.AutoFillId;
import android.view.autofill.AutoFillValue;
import android.widget.RemoteViews;

import com.example.android.autofillframework.R;

import java.util.Map;
import java.util.Set;

import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
public final class AutoFillHelper {
    /**
     * Wraps autofill data in a Dataset object which can then be sent back to the client View.
     */
    public static Dataset newCredentialDataset(Context context,
            LoginCredential loginCredential, AutoFillId usernameId,
            AutoFillId passwordId) {
        String datasetName = loginCredential.getDatasetName();
        RemoteViews presentation = new RemoteViews(context.getPackageName(),
                R.layout.list_item);
        presentation.setTextViewText(R.id.text1, datasetName);
        Dataset.Builder datasetBuilder = new Dataset.Builder(presentation);
        datasetBuilder.setValue(usernameId, AutoFillValue.forText(loginCredential.getUsername()));
        datasetBuilder.setValue(passwordId, AutoFillValue.forText(loginCredential.getPassword()));
        return datasetBuilder.build();
    }

    /**
     * Wraps autofill data in a Response object (essentially a series of Datasets) which can then
     * be sent back to the client View.
     */
    public static FillResponse newCredentialsResponse(Context context,
            boolean datasetAuth, AutoFillId usernameId, AutoFillId passwordId,
            Map<String, LoginCredential> credentialsMap) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        if (usernameId == null || passwordId == null ||
                credentialsMap == null || credentialsMap.isEmpty()) {
            // Activity does not have usernameField and passwordField, can't do anything
            return null;
        } else {
            int numReplies = 0;
            Set<Map.Entry<String, LoginCredential>> credentialSet = credentialsMap.entrySet();
            for (Map.Entry<String, LoginCredential> credential : credentialSet) {
                if (datasetAuth) {
                    String datasetName = credential.getKey();
                    RemoteViews presentation = new RemoteViews(context.getPackageName(),
                            R.layout.list_item);
                    presentation.setTextViewText(R.id.text1, datasetName);
                    Dataset.Builder datasetBuilder = new Dataset.Builder(presentation);
                    IntentSender sender =
                            AuthActivity.getAuthIntentSenderForDataset(context, datasetName);
                    datasetBuilder.setAuthentication(sender);
                    responseBuilder.addDataset(datasetBuilder.build());
                } else {
                    Dataset dataset = newCredentialDataset(context,
                            credential.getValue(), usernameId, passwordId);
                    responseBuilder.addDataset(dataset);
                }
                numReplies++;
            }
            if (numReplies > 0) {
                return responseBuilder.build();
            } else {
                Log.d(TAG, "No Autofill data found.");
                return null;
            }
        }
    }
}
