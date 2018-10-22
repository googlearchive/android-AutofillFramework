/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.example.android.autofill.service.simple;

import static com.example.android.autofill.service.simple.BasicService.getLatestAssistStructure;
import static com.example.android.autofill.service.simple.BasicService.newDatasetPresentation;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.android.autofill.service.MyAutofillService;
import com.example.android.autofill.service.settings.MyPreferences;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A basic service used to demonstrate multi-steps workflows (such as
 * {@code MultipleStepsSignInActivity} and {@code MultipleStepsCreditCardActivity}) by saving the
 * save type from previous requests in the client state bundle that's passed along to next requests.
 *
 * <p>This class should <strong>not</strong> be used as a reference for real autofill service
 * implementations because it lacks fundamental security requirements such as data partitioning and
 * package verification &mdashthese requirements are fullfilled by {@link MyAutofillService}.
 */
public class MultiStepsService extends AutofillService {

    private static final String TAG = "MultiStepsService";
    private static final String SAVE_TYPE_KEY = "saveType";

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal,
            FillCallback callback) {
        int saveType = SaveInfo.SAVE_DATA_TYPE_GENERIC;
        Bundle clientState = request.getClientState();
        if (clientState != null) {
            saveType = clientState.getInt(SAVE_TYPE_KEY, saveType);
        }
        Log.d(TAG, "onFillRequest(): saveType=" + saveType);

        // Find autofillable fields
        AssistStructure structure = getLatestAssistStructure(request);
        ArrayMap<String, AutofillId> fields = getAutofillableFields(structure);
        Log.d(TAG, "autofillable fields:" + fields);

        if (fields.isEmpty()) {
            toast("No autofill hints found");
            callback.onSuccess(null);
            return;
        }

        Collection<AutofillId> ids = fields.values();
        AutofillId[] requiredIds = new AutofillId[ids.size()];
        ids.toArray(requiredIds);
        for (int i = 0; i < fields.size(); i++) {
            String hint = fields.keyAt(i);
            switch (hint) {
                case View.AUTOFILL_HINT_USERNAME:
                    saveType |= SaveInfo.SAVE_DATA_TYPE_USERNAME;
                    break;
                case View.AUTOFILL_HINT_EMAIL_ADDRESS:
                    saveType |= SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    saveType |= SaveInfo.SAVE_DATA_TYPE_PASSWORD;
                    break;
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    saveType |= SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD;
                    break;
                case View.AUTOFILL_HINT_POSTAL_ADDRESS:
                case View.AUTOFILL_HINT_POSTAL_CODE:
                    saveType |= SaveInfo.SAVE_DATA_TYPE_ADDRESS;
                    break;
                default:
                    Log.d(TAG, "Ignoring hint '" + hint + "'");
            }
        }

        Log.d(TAG, "new saveType=" + saveType);
        if (clientState == null) {
            // Initial request
            clientState = new Bundle();
        }
        // NOTE: to simplify, we're saving just the saveType, but a real service implementation
        // would have to save the previous values as well, so they can be used later (for example,
        // it would have to save the username in the first request so it's used to save the
        // username + password combo in the second request.
        clientState.putInt(SAVE_TYPE_KEY, saveType);

        // Create response...
        callback.onSuccess(new FillResponse.Builder()
                .setClientState(clientState)
                .setSaveInfo(new SaveInfo.Builder(saveType, requiredIds).build())
                .build());
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        Log.d(TAG, "onSaveRequest()");
        toast("Save not supported");
        callback.onSuccess();
    }

    @NonNull
    private ArrayMap<String, AutofillId> getAutofillableFields(@NonNull AssistStructure structure) {
        ArrayMap<String, AutofillId> fields = new ArrayMap<>();
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addAutofillableFields(fields, node);
        }
        return fields;
    }

    private void addAutofillableFields(@NonNull Map<String, AutofillId> fields,
            @NonNull ViewNode node) {
        String[] hints = node.getAutofillHints();
        if (hints != null) {
            // We're simple, we only care about the first hint
            String hint = hints[0];
            AutofillId id = node.getAutofillId();
            if (!fields.containsKey(hint)) {
                Log.v(TAG, "Setting hint '" + hint + "' on " + id);
                fields.put(hint, id);
            } else {
                Log.v(TAG, "Ignoring hint '" + hint + "' on " + id
                        + " because it was already set");
            }
        }
        int childrenSize = node.getChildCount();
        for (int i = 0; i < childrenSize; i++) {
            addAutofillableFields(fields, node.getChildAt(i));
        }
    }

    private void toast(@NonNull CharSequence message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
