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

import android.app.assist.AssistStructure;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutoFillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.util.Log;
import android.view.autofill.AutoFillId;
import android.widget.RemoteViews;

import com.example.android.autofillframework.R;

import java.util.Map;

import static com.example.android.autofillframework.CommonUtil.TAG;
import static com.example.android.autofillframework.CommonUtil.bundleToString;

public class MyAutoFillService extends AutoFillService {

    @Override
    public void onFillRequest(AssistStructure structure, Bundle data,
            CancellationSignal cancellationSignal, FillCallback callback) {
        Log.d(TAG, "onFillRequest(): data=" + bundleToString(data));

        // Temporary hack for disabling autofill for components in this autofill service.
        if (structure.getActivityComponent().toShortString()
                .contains("com.example.android.autofillframework.service")) {
            callback.onSuccess(null);
            return;
        }
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                Log.w(TAG, "Cancel autofill not implemented in this sample.");
            }
        });
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        // Check user's settings for authenticating Responses and Datasets
        boolean responseAuth = MyPreferences.getInstance(this).isResponseAuth();
        if (responseAuth) {
            // If the entire Autofill Response is authenticated, AuthActivity is used
            // to generate Response
            IntentSender sender = AuthActivity.getAuthIntentSenderForResponse(this);
            RemoteViews presentation = new RemoteViews(getPackageName(), R.layout.list_item);
            presentation.setTextViewText(R.id.text1, getString(R.string.autofill_sign_in_prompt));
            responseBuilder.setAuthentication(sender, presentation);
            callback.onSuccess(responseBuilder.build());
        } else {
            boolean datasetAuth = MyPreferences.getInstance(this).isDatasetAuth();
            // Parse AutoFill data in Activity
            StructureParser parser = new StructureParser(structure);
            parser.parse();
            AutoFillId usernameId = parser.getUsernameField().getId();
            AutoFillId passwordId = parser.getPasswordField().getId();
            Map<String, LoginCredential> credentialsMap =
                    AutoFillData.getInstance().getCredentialsMap(this);
            if (usernameId == null || passwordId == null ||
                    credentialsMap == null || credentialsMap.isEmpty()) {
                // Activity does not have usernameField and passwordField fields, or service does not
                // have any usernameField and passwordField autofill data.
                Log.d(TAG, "No Autofill data found for this Activity");
                callback.onSuccess(null);
                return;
            }

            FillResponse response = AutoFillHelper.newCredentialsResponse(
                    this, datasetAuth, usernameId, passwordId, credentialsMap);
            callback.onSuccess(response);
        }
    }

    @Override
    public void onSaveRequest(AssistStructure structure, Bundle data, SaveCallback callback) {
        Log.d(TAG, "onSaveFillRequest(): data=" + bundleToString(data));
        StructureParser parser = new StructureParser(structure);
        parser.parse();
        String packageName = structure.getActivityComponent().getPackageName();
        String username = parser.getUsernameField().getValue();
        String password = parser.getPasswordField().getValue();
        LoginCredential loginCredential = new LoginCredential(username, password);
        AutoFillData.getInstance().updateCredentials(packageName, loginCredential);
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
    }
}