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
package com.example.android.autofillframework.service

import android.content.Context
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import android.util.Log
import android.widget.RemoteViews
import com.example.android.autofillframework.CommonUtil.TAG
import com.example.android.autofillframework.R
import com.example.android.autofillframework.service.model.AutofillFieldsCollection
import com.example.android.autofillframework.service.model.ClientFormData
import java.util.HashMap

/**
 * This is a class containing helper methods for building Autofill Datasets and Responses.
 */
object AutofillHelper {

    /**
     * Wraps autofill data in a [Dataset] object which can then be sent back to the
     * client View.
     */
    fun newDataset(context: Context, autofillFields: AutofillFieldsCollection,
            clientFormData: ClientFormData, datasetAuth: Boolean): Dataset? {
        clientFormData.datasetName?.let { datasetName ->
            val datasetBuilder = Dataset.Builder(newRemoteViews(context.packageName, datasetName))
            val setValueAtLeastOnce = clientFormData.applyToFields(autofillFields, datasetBuilder)
            if (datasetAuth) {
                val sender = AuthActivity.getAuthIntentSenderForDataset(context, datasetName)
                datasetBuilder.setAuthentication(sender)
            }
            if (setValueAtLeastOnce) {
                return datasetBuilder.build()
            }
        }
        return null
    }

    fun newRemoteViews(packageName: String, remoteViewsText: String): RemoteViews {
        val presentation = RemoteViews(packageName, R.layout.list_item)
        presentation.setTextViewText(R.id.text1, remoteViewsText)
        return presentation
    }

    /**
     * Wraps autofill data in a [FillResponse] object (essentially a series of Datasets) which can
     * then be sent back to the client View.
     */
    fun newResponse(context: Context,
            datasetAuth: Boolean, autofillFields: AutofillFieldsCollection,
            clientFormDataMap: HashMap<String, ClientFormData>?): FillResponse? {
        val responseBuilder = FillResponse.Builder()
        clientFormDataMap?.keys?.let { datasetNames ->
            for (datasetName in datasetNames) {
                clientFormDataMap[datasetName]?.let { clientFormData ->
                    val dataset = newDataset(context, autofillFields, clientFormData, datasetAuth)
                    dataset?.let(responseBuilder::addDataset)
                }
            }
        }
        if (autofillFields.saveType != 0) {
            val autofillIds = autofillFields.autofillIds
            responseBuilder.setSaveInfo(SaveInfo.Builder(autofillFields.saveType,
                    autofillIds.toTypedArray()).build())
            return responseBuilder.build()
        } else {
            Log.d(TAG, "These fields are not meant to be saved by autofill.")
            return null
        }
    }
}