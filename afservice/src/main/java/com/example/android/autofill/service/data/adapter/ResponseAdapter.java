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

package com.example.android.autofill.service.data.adapter;

import android.content.Context;
import android.content.IntentSender;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.view.autofill.AutofillId;
import android.widget.RemoteViews;

import com.example.android.autofill.service.AuthActivity;
import com.example.android.autofill.service.RemoteViewsHelper;
import com.example.android.autofill.service.data.ClientViewMetadata;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;

import java.util.HashMap;
import java.util.List;

public class ResponseAdapter {
    private final Context mContext;
    private final DatasetAdapter mDatasetAdapter;
    private final String mPackageName;
    private final ClientViewMetadata mClientViewMetadata;

    public ResponseAdapter(Context context, ClientViewMetadata clientViewMetadata,
            String packageName, DatasetAdapter datasetAdapter) {
        mContext = context;
        mClientViewMetadata = clientViewMetadata;
        mDatasetAdapter = datasetAdapter;
        mPackageName = packageName;
    }

    public FillResponse buildResponseForFocusedNode(String datasetName, FilledAutofillField field,
            FieldType fieldType) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        RemoteViews remoteViews = RemoteViewsHelper.viewsWithNoAuth(
                mPackageName, datasetName);
        Dataset dataset = mDatasetAdapter.buildDatasetForFocusedNode(field, fieldType, remoteViews);
        if (dataset != null) {
            responseBuilder.addDataset(dataset);
            return responseBuilder.build();
        } else {
            return null;
        }
    }

    /**
     * Wraps autofill data in a Response object (essentially a series of Datasets) which can then
     * be sent back to the client View.
     */
    public FillResponse buildResponse(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint,
            List<DatasetWithFilledAutofillFields> datasets, boolean datasetAuth) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        if (datasets != null) {
            for (DatasetWithFilledAutofillFields datasetWithFilledAutofillFields : datasets) {
                if (datasetWithFilledAutofillFields != null) {
                    Dataset dataset;
                    String datasetName = datasetWithFilledAutofillFields.autofillDataset
                            .getDatasetName();
                    if (datasetAuth) {
                        IntentSender intentSender = AuthActivity.getAuthIntentSenderForDataset(
                                mContext, datasetName);
                        RemoteViews remoteViews = RemoteViewsHelper.viewsWithAuth(
                                mPackageName, datasetName);
                        dataset = mDatasetAdapter.buildDataset(fieldTypesByAutofillHint,
                                datasetWithFilledAutofillFields, remoteViews, intentSender);
                    } else {
                        RemoteViews remoteViews = RemoteViewsHelper.viewsWithNoAuth(
                                mPackageName, datasetName);
                        dataset = mDatasetAdapter.buildDataset(fieldTypesByAutofillHint,
                                datasetWithFilledAutofillFields, remoteViews);
                    }
                    if (dataset != null) {
                        responseBuilder.addDataset(dataset);
                    }
                }
            }
        }
        int saveType = mClientViewMetadata.getSaveType();
        AutofillId[] autofillIds = mClientViewMetadata.getAutofillIds();
        if (autofillIds != null && autofillIds.length > 0) {
            SaveInfo saveInfo = new SaveInfo.Builder(saveType, autofillIds).build();
            responseBuilder.setSaveInfo(saveInfo);
            return responseBuilder.build();
        } else {
            return null;
        }
    }

    public FillResponse buildResponse(IntentSender sender, RemoteViews remoteViews) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        int saveType = mClientViewMetadata.getSaveType();
        AutofillId[] autofillIds = mClientViewMetadata.getAutofillIds();
        if (autofillIds != null && autofillIds.length > 0) {
            SaveInfo saveInfo = new SaveInfo.Builder(saveType, autofillIds).build();
            responseBuilder.setSaveInfo(saveInfo);
            responseBuilder.setAuthentication(autofillIds, sender, remoteViews);
            return responseBuilder.build();
        } else {
            return null;
        }
    }

    public FillResponse buildManualResponse(IntentSender sender, RemoteViews remoteViews) {
        FillResponse.Builder responseBuilder = new FillResponse.Builder();
        int saveType = mClientViewMetadata.getSaveType();
        AutofillId[] focusedIds = mClientViewMetadata.getFocusedIds();
        if (focusedIds != null && focusedIds.length > 0) {
            SaveInfo saveInfo = new SaveInfo.Builder(saveType, focusedIds).build();
            return responseBuilder.setSaveInfo(saveInfo)
                    .setAuthentication(focusedIds, sender, remoteViews)
                    .build();
        } else {
            return null;
        }
    }
}
