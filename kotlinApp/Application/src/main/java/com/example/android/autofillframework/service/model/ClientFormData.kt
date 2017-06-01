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
package com.example.android.autofillframework.service.model

import android.service.autofill.Dataset
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import java.util.HashMap


/**
 * ClientFormData is the model that represents all of the form data on a client app's page, plus the
 * dataset name associated with it.
 */
class ClientFormData constructor(var datasetName: String? = null,
        private val hintMap: HashMap<String, SavableAutofillData> = HashMap<String, SavableAutofillData>()) {

    private val TAG = "ClientFormData"

    /**
     * Sets values for a list of autofillHints.
     */
    fun setAutofillValuesForHints(autofillHints: Array<String>, autofillData: SavableAutofillData) {
        autofillHints.forEach { hint ->
            hintMap[hint] = autofillData
        }
    }

    /**
     * Populates a [Dataset.Builder] with appropriate values for each [AutofillId]
     * in a `AutofillFieldsCollection`.
     */
    fun applyToFields(autofillFieldsCollection: AutofillFieldsCollection,
            datasetBuilder: Dataset.Builder): Boolean {
        var setValueAtLeastOnce = false
        for (hint in autofillFieldsCollection.allAutofillHints) {
            val autofillFields = autofillFieldsCollection.getFieldsForHint(hint) ?: continue
            for (autofillField in autofillFields) {
                val autofillId = autofillField.autofillId
                val autofillType = autofillField.autofillType
                val savedAutofillValue = hintMap[hint]
                when (autofillType) {
                    View.AUTOFILL_TYPE_LIST -> {
                        savedAutofillValue?.textValue?.let(autofillField::getAutofillOptionIndex)?.let { index ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forList(index))
                            setValueAtLeastOnce = true
                        }
                    }
                    View.AUTOFILL_TYPE_DATE -> {
                        savedAutofillValue?.dateValue?.let { date ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forDate(date))
                            setValueAtLeastOnce = true
                        }
                    }
                    View.AUTOFILL_TYPE_TEXT -> {
                        savedAutofillValue?.textValue?.let { text ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forText(text))
                            setValueAtLeastOnce = true
                        }
                    }
                    View.AUTOFILL_TYPE_TOGGLE -> {
                        savedAutofillValue?.toggleValue?.let { toggle ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forToggle(toggle))
                            setValueAtLeastOnce = true
                        }
                    }
                    else -> Log.w(TAG, "Invalid autofill type - " + autofillType)
                }
            }
        }
        return setValueAtLeastOnce
    }

    /**
     * Returns whether this model contains autofill data that is relevant to any of the
     * autofillHints that are passed in.
     */
    fun helpsWithHints(autofillHints: List<String>): Boolean {
        for (autofillHint in autofillHints) {
            hintMap[autofillHint]?.let { savedAutofillValue ->
                if (!savedAutofillValue.isNull()) {
                    return true
                }
            }
        }
        return false
    }
}
