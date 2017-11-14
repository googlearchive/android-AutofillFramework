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
package com.example.android.autofill.service;

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
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.android.autofill.service.datasource.SharedPrefsAutofillRepository;
import com.example.android.autofill.service.datasource.SharedPrefsPackageVerificationRepository;
import com.example.android.autofill.service.model.FilledAutofillFieldCollection;
import com.example.android.autofill.service.settings.MyPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.android.autofill.service.AutofillHelper.CLIENT_STATE_PARTIAL_ID_TEMPLATE;
import static com.example.android.autofill.service.Util.AUTOFILL_ID_FILTER;
import static com.example.android.autofill.service.Util.bundleToString;
import static com.example.android.autofill.service.Util.dumpStructure;
import static com.example.android.autofill.service.Util.findNodeByFilter;
import static com.example.android.autofill.service.Util.logVerboseEnabled;
import static com.example.android.autofill.service.Util.logd;
import static com.example.android.autofill.service.Util.logv;
import static com.example.android.autofill.service.Util.logw;

public class MyAutofillService extends AutofillService {

    @Override public void onCreate() {
        super.onCreate();
        Util.setLoggingLevel(MyPreferences.getInstance(this).getLoggingLevel());
    }

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
        final Bundle clientState = request.getClientState();
        if (logVerboseEnabled()) {
            logv("onFillRequest(): clientState=%s", bundleToString(clientState));
        }
        dumpStructure(structure);

        cancellationSignal.setOnCancelListener(() ->
                logw("Cancel autofill not implemented in this sample.")
        );
        // Parse AutoFill data in Activity
        StructureParser parser = new StructureParser(getApplicationContext(), structure);
        // TODO: try / catch on other places (onSave, auth activity, etc...)
        try {
            parser.parseForFill();
        } catch (SecurityException e) {
            // TODO: handle cases where DAL didn't pass by showing a custom UI asking the user
            // to confirm the mapping. Might require subclassing SecurityException.
            logw(e, "Security exception handling %s", request);
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
                    (this, clientState, datasetAuth, autofillFields, clientFormDataMap);
            callback.onSuccess(response);
        }
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        List<FillContext> fillContexts = request.getFillContexts();
        int size = fillContexts.size();
        AssistStructure structure = fillContexts.get(size - 1).getStructure();
        String packageName = structure.getActivityComponent().getPackageName();
        if (!SharedPrefsPackageVerificationRepository.getInstance()
                .putPackageSignatures(getApplicationContext(), packageName)) {
            callback.onFailure(getApplicationContext().getString(R.string.invalid_package_signature));
            return;
        }
        Bundle clientState = request.getClientState();
        if (logVerboseEnabled()) {
            logv("onSaveRequest(): clientState=%s", bundleToString(clientState));
        }
        dumpStructure(structure);

        // TODO: hardcode check for partial username
        if (clientState != null) {
            String usernameKey =
                    String.format(CLIENT_STATE_PARTIAL_ID_TEMPLATE, View.AUTOFILL_HINT_USERNAME);
            AutofillId usernameId = clientState.getParcelable(usernameKey);
            logd("client state for %s: %s", usernameKey, usernameId);
            if (usernameId != null) {
                String passwordKey =
                        String.format(CLIENT_STATE_PARTIAL_ID_TEMPLATE, View.AUTOFILL_HINT_PASSWORD);
                AutofillId passwordId = clientState.getParcelable(passwordKey);

                logd("Scanning %d contexts for username ID %s and password ID %s.", size,
                        usernameId, passwordId);
                AssistStructure.ViewNode usernameNode =
                        findNodeByFilter(fillContexts, usernameId, AUTOFILL_ID_FILTER);
                AssistStructure.ViewNode passwordNode =
                        findNodeByFilter(fillContexts, passwordId, AUTOFILL_ID_FILTER);
                String username = null, password = null;
                if (usernameNode != null) {
                    username = usernameNode.getAutofillValue().getTextValue().toString();
                }
                if (passwordNode != null) {
                    password = passwordNode.getAutofillValue().getTextValue().toString();
                }

                if (username != null && password != null) {
                    logd("user: %s, pass: %s", username, password);
                    // TODO: save it
                    callback.onFailure("TODO: save " + username + "/" + password);
                    return;
                } else {
                    logw(" missing user (%s) or pass (%s)", username, password);
                }
            }
        }

        StructureParser parser = new StructureParser(getApplicationContext(), structure);
        parser.parseForSave();
        FilledAutofillFieldCollection filledAutofillFieldCollection = parser.getClientFormData();
        SharedPrefsAutofillRepository.getInstance()
                .saveFilledAutofillFieldCollection(this, filledAutofillFieldCollection);
    }

    @Override
    public void onConnected() {
        logd("onConnected");
    }

    @Override
    public void onDisconnected() {
        logd("onDisconnected");
    }
}
