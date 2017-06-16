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

import android.content.Context;
import android.content.IntentSender;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.android.autofillframework.R;
import com.example.android.autofillframework.multidatasetservice.model.FilledAutofillFieldCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
public final class AutofillHelper {

    /**
     * Wraps autofill data in a LoginCredential  Dataset object which can then be sent back to the
     * client View.
     */
    public static Dataset newDataset(Context context,
            AutofillFieldMetadataCollection autofillFields, FilledAutofillFieldCollection filledAutofillFieldCollection, boolean datasetAuth) {
        String datasetName = filledAutofillFieldCollection.getDatasetName();
        if (datasetName != null) {
            Dataset.Builder datasetBuilder;
            if (datasetAuth) {
                datasetBuilder = new Dataset.Builder
                        (newRemoteViews(context.getPackageName(), datasetName,
                                R.drawable.ic_lock_black_24dp));
                IntentSender sender = AuthActivity.getAuthIntentSenderForDataset(context, datasetName);
                datasetBuilder.setAuthentication(sender);
            } else {
                datasetBuilder = new Dataset.Builder
                        (newRemoteViews(context.getPackageName(), datasetName,
                                R.drawable.ic_person_black_24dp));
            }
            boolean setValueAtLeastOnce = filledAutofillFieldCollection.applyToFields(autofillFields, datasetBuilder);
            if (setValueAtLeastOnce) {
                return datasetBuilder.build();
            }
        }
        return null;
    }

    public static RemoteViews newRemoteViews(String packageName, String remoteViewsText,
            @DrawableRes int drawableId) {
        RemoteViews presentation = new RemoteViews(packageName, R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, remoteViewsText);
        presentation.setImageViewResource(R.id.icon, drawableId);
        return presentation;
    }

    /**
     * Wraps autofill data in a Response object (essentially a series of Datasets) which can then
     * be sent back to the client View.
     */
    public static FillResponse newResponse(Context context,
            boolean datasetAuth, AutofillFieldMetadataCollection autofillFields,
            HashMap<String, FilledAutofillFieldCollection> clientFormDataMap) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        if (clientFormDataMap != null) {
            Set<String> datasetNames = clientFormDataMap.keySet();
            for (String datasetName : datasetNames) {
                FilledAutofillFieldCollection filledAutofillFieldCollection = clientFormDataMap.get(datasetName);
                if (filledAutofillFieldCollection != null) {
                    Dataset dataset = newDataset(context, autofillFields, filledAutofillFieldCollection, datasetAuth);
                    if (dataset != null) {
                        responseBuilder.addDataset(dataset);
                    }
                }
            }
        }
        if (autofillFields.getSaveType() != 0) {
            AutofillId[] autofillIds = autofillFields.getAutofillIds();
            responseBuilder.setSaveInfo
                    (new SaveInfo.Builder(autofillFields.getSaveType(), autofillIds).build());
            return responseBuilder.build();
        } else {
            Log.d(TAG, "These fields are not meant to be saved by autofill.");
            return null;
        }
    }

    public static String[] filterForSupportedHints(String[] hints) {
        ArrayList<String> supportedHints = new ArrayList<>(hints.length);
        for (int i = 0; i < hints.length; i++) {
            if (AutofillHelper.isValidHint(hints[i])) {
                supportedHints.add(hints[i]);
            }
        }
        return supportedHints.toArray(new String[supportedHints.size()]);
    }

    public static boolean isValidHint(String hint) {
        switch (hint) {
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
            case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
            case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
            case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
            case View.AUTOFILL_HINT_EMAIL_ADDRESS:
            case View.AUTOFILL_HINT_PHONE:
            case View.AUTOFILL_HINT_NAME:
            case View.AUTOFILL_HINT_PASSWORD:
            case View.AUTOFILL_HINT_POSTAL_ADDRESS:
            case View.AUTOFILL_HINT_POSTAL_CODE:
            case View.AUTOFILL_HINT_USERNAME:
                return true;
            default:
                return false;
        }
    }
}
