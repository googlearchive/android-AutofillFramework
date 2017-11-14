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

import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.android.autofill.service.model.FilledAutofillFieldCollection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import static com.example.android.autofill.service.Util.bundleToString;
import static com.example.android.autofill.service.Util.getSaveTypeAsString;
import static com.example.android.autofill.service.Util.logd;

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
public final class AutofillHelper {

    static final String CLIENT_STATE_PARTIAL_ID_TEMPLATE = "partial-%s";
    // TODO: move to settings activity and document it
    private static final boolean SUPPORT_MULTIPLE_STEPS = true;

    private AutofillHelper() {
        throw new UnsupportedOperationException("provide static methods only");
    }

    /**
     * Wraps autofill data in a LoginCredential  Dataset object which can then be sent back to the
     * client View.
     */
    public static Dataset newDataset(Context context,
            AutofillFieldMetadataCollection autofillFields,
            FilledAutofillFieldCollection filledAutofillFieldCollection, boolean datasetAuth) {
        String datasetName = filledAutofillFieldCollection.getDatasetName();
        if (datasetName != null) {
            Dataset.Builder datasetBuilder;
            if (datasetAuth) {
                datasetBuilder = new Dataset.Builder
                        (newRemoteViews(context.getPackageName(), datasetName,
                                R.drawable.ic_lock_black_24dp));
                IntentSender sender =
                        AuthActivity.getAuthIntentSenderForDataset(context, datasetName);
                datasetBuilder.setAuthentication(sender);
            } else {
                datasetBuilder = new Dataset.Builder
                        (newRemoteViews(context.getPackageName(), datasetName,
                                R.drawable.ic_person_black_24dp));
            }
            boolean setValueAtLeastOnce =
                    filledAutofillFieldCollection.applyToFields(autofillFields, datasetBuilder);
            if (setValueAtLeastOnce) {
                return datasetBuilder.build();
            }
        }
        return null;
    }

    public static RemoteViews newRemoteViews(String packageName, String remoteViewsText,
            @DrawableRes int drawableId) {
        RemoteViews presentation =
                new RemoteViews(packageName, R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, remoteViewsText);
        presentation.setImageViewResource(R.id.icon, drawableId);
        return presentation;
    }

    /**
     * Wraps autofill data in a Response object (essentially a series of Datasets) which can then
     * be sent back to the client View.
     */
    public static FillResponse newResponse(Context context, Bundle previousClientState,
            boolean datasetAuth, AutofillFieldMetadataCollection autofillFields,
            HashMap<String, FilledAutofillFieldCollection> clientFormDataMap) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        if (clientFormDataMap != null) {
            Set<String> datasetNames = clientFormDataMap.keySet();
            for (String datasetName : datasetNames) {
                FilledAutofillFieldCollection filledAutofillFieldCollection =
                        clientFormDataMap.get(datasetName);
                if (filledAutofillFieldCollection != null) {
                    Dataset dataset = newDataset(context, autofillFields,
                            filledAutofillFieldCollection, datasetAuth);
                    if (dataset != null) {
                        responseBuilder.addDataset(dataset);
                    }
                }
            }
        }
        int saveType = autofillFields.getSaveType();
        if (saveType != 0) {
            if (SUPPORT_MULTIPLE_STEPS) {
                setPartialSaveInfo(responseBuilder, saveType, autofillFields, previousClientState);
            } else {
                setFullSaveInfo(responseBuilder, saveType, autofillFields);
            }
            return responseBuilder.build();
        } else {
            logd("These fields are not meant to be saved by autofill.");
            return null;
        }
    }

    private static void setFullSaveInfo(FillResponse.Builder responseBuilder, int saveType,
            AutofillFieldMetadataCollection autofillFields) {
        AutofillId[] autofillIds = autofillFields.getAutofillIds();
        responseBuilder.setSaveInfo(new SaveInfo.Builder(saveType, autofillIds).build());
    }

    private static void setPartialSaveInfo(FillResponse.Builder responseBuilder, int saveType,
            AutofillFieldMetadataCollection autofillFields,
            Bundle previousClientState) {
        AutofillId[] autofillIds = autofillFields.getAutofillIds();
        List<String> allHints = autofillFields.getAllHints();
        if (Util.logDebugEnabled()) {
            logd("setPartialSaveInfo() for type %s: allHints=%s, ids=%s, clientState=%s",
                    getSaveTypeAsString(saveType), allHints, Arrays.toString(autofillIds),
                    bundleToString(previousClientState));
        }

        // TODO: this should be more generic, but for now it's hardcode to support just activities
        // that have an username and a password in separate steps (like MultipleStepsSigninActivity)
        if ((saveType != SaveInfo.SAVE_DATA_TYPE_USERNAME
                && saveType != SaveInfo.SAVE_DATA_TYPE_PASSWORD)
                || autofillIds.length != 1 || allHints.size() != 1) {
            logd("Unsupported activity for partial info; returning full");
            setFullSaveInfo(responseBuilder, saveType, autofillFields);
            return;
        }

        int previousSaveType;
        String previousHint;
        if (saveType == SaveInfo.SAVE_DATA_TYPE_PASSWORD) {
            previousHint = View.AUTOFILL_HINT_USERNAME;
            previousSaveType = SaveInfo.SAVE_DATA_TYPE_USERNAME;
        } else {
            previousHint = View.AUTOFILL_HINT_PASSWORD;
            previousSaveType = SaveInfo.SAVE_DATA_TYPE_PASSWORD;
        }
        String previousKey = String.format(CLIENT_STATE_PARTIAL_ID_TEMPLATE, previousHint);

        AutofillId previousValue = previousClientState == null
                ? null
                : previousClientState.getParcelable(previousKey);
        logd("previous: %s=%s", previousKey, previousValue);

        Bundle newClientState = new Bundle();
        String key = String.format(CLIENT_STATE_PARTIAL_ID_TEMPLATE, allHints.get(0));
        AutofillId value = autofillIds[0];
        logd("New client state: %s = %s", key, value);
        newClientState.putParcelable(key, value);

        if (previousValue != null) {
            AutofillId[] newIds = new AutofillId[]{previousValue, value};
            int newSaveType = saveType | previousSaveType;
            logd("new values: type=%s, ids=%s",
                    getSaveTypeAsString(newSaveType), Arrays.toString(newIds));
            newClientState.putAll(previousClientState);
            responseBuilder.setSaveInfo
                    (new SaveInfo.Builder(newSaveType, newIds)
                            .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
                            .build())
                    .setClientState(newClientState);

            return;
        }

        responseBuilder.setClientState(newClientState);

        // TODO: on MR1, creates a new SaveType without required ids
        setFullSaveInfo(responseBuilder, saveType, autofillFields);
    }
}
