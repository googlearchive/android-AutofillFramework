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
package com.example.android.autofillframework.multidatasetservice;

import android.app.assist.AssistStructure;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.android.autofillframework.R;
import com.example.android.autofillframework.multidatasetservice.datasource.SharedPrefsAutofillRepository;
import com.example.android.autofillframework.multidatasetservice.datasource.SharedPrefsPackageVerificationRepository;
import com.example.android.autofillframework.multidatasetservice.model.FilledAutofillFieldCollection;
import com.example.android.autofillframework.multidatasetservice.settings.MyPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.android.autofillframework.CommonUtil.TAG;
import static com.example.android.autofillframework.CommonUtil.VERBOSE;
import static com.example.android.autofillframework.CommonUtil.bundleToString;
import static com.example.android.autofillframework.CommonUtil.dumpStructure;

public class MyAutofillService extends AutofillService {

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal,
            FillCallback callback) {
        AssistStructure structure = request.getFillContexts()
                .get(request.getFillContexts().size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();
        if (!SharedPrefsPackageVerificationRepository.getInstance()
                .putPackageSignatures(getApplicationContext(), packageName)) {
            callback.onFailure(
                    getApplicationContext().getString(R.string.invalid_package_signature));
            return;
        }
        final Bundle data = request.getClientState();
        if (VERBOSE) {
            Log.v(TAG, "onFillRequest(): data=" + bundleToString(data));
            dumpStructure(structure);
        }

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                Log.w(TAG, "Cancel autofill not implemented in this sample.");
            }
        });
        // Parse AutoFill data in Activity
        StructureParser parser = new StructureParser(getApplicationContext(), structure);
        // TODO: try / catch on other places (onSave, auth activity, etc...)
        try {
            parser.parseForFill();
        } catch (SecurityException e) {
            // TODO: handle cases where DAL didn't pass by showing a custom UI asking the user
            // to confirm the mapping. Might require subclassing SecurityException.
            Log.w(TAG, "Security exception handling " + request, e);
            callback.onFailure(e.getMessage());
            return;
        }
        AutofillFieldMetadataCollection autofillFields = parser.getAutofillFields();
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        // Check user's settings for authenticating Responses and Datasets.
        boolean responseAuth = MyPreferences.getInstance(this).isResponseAuth();
        AutofillId[] autofillIds = autofillFields.getAutofillIds();
        if (responseAuth && !Arrays.asList(autofillIds).isEmpty()) {
            // If the entire Autofill Response is authenticated, AuthActivity is used
            // to generate Response.
            IntentSender sender = AuthActivity.getAuthIntentSenderForResponse(this);
            RemoteViews presentation = AutofillHelper
                    .newRemoteViews(getPackageName(), getString(R.string.autofill_sign_in_prompt),
                            R.drawable.ic_lock_black_24dp);
            responseBuilder
                    .setAuthentication(autofillIds, sender, presentation);
            callback.onSuccess(responseBuilder.build());
        } else {
            boolean datasetAuth = MyPreferences.getInstance(this).isDatasetAuth();
            HashMap<String, FilledAutofillFieldCollection> clientFormDataMap =
                    SharedPrefsAutofillRepository.getInstance().getFilledAutofillFieldCollection(
                            this, autofillFields.getFocusedHints(), autofillFields.getAllHints());
            FillResponse response = AutofillHelper.newResponse
                    (this, datasetAuth, autofillFields, clientFormDataMap);
            callback.onSuccess(response);
        }
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        List<FillContext> context = request.getFillContexts();
        final AssistStructure structure = context.get(context.size() - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();
        if (!SharedPrefsPackageVerificationRepository.getInstance()
                .putPackageSignatures(getApplicationContext(), packageName)) {
            callback.onFailure(
                    getApplicationContext().getString(R.string.invalid_package_signature));
            return;
        }
        final Bundle data = request.getClientState();
        if (VERBOSE) {
            Log.v(TAG, "onSaveRequest(): data=" + bundleToString(data));
            dumpStructure(structure);
        }
        StructureParser parser = new StructureParser(getApplicationContext(), structure);
        parser.parseForSave();
        FilledAutofillFieldCollection filledAutofillFieldCollection = parser.getClientFormData();
        SharedPrefsAutofillRepository.getInstance()
                .saveFilledAutofillFieldCollection(this, filledAutofillFieldCollection);
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
