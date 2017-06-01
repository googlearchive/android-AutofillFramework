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

import android.view.autofill.AutofillId
import java.util.ArrayList
import java.util.HashMap

/**
 * Data structure that stores a collection of `AutofillField`s. Contains all of the client's `View`
 * hierarchy autofill-relevant metadata.
 */
data class AutofillFieldsCollection(val autofillIds: ArrayList<AutofillId> = ArrayList<AutofillId>(),
        val allAutofillHints: ArrayList<String> = ArrayList<String>(),
        val focusedAutofillHints: ArrayList<String> = ArrayList<String>()) {

    private val autofillHintsToFieldsMap = HashMap<String, MutableList<AutofillField>>()
    var saveType = 0
        private set

    fun add(autofillField: AutofillField) {
        saveType = saveType or autofillField.saveType
        autofillIds.add(autofillField.autofillId)
        val hintsList = autofillField.autofillHints
        allAutofillHints.addAll(hintsList)
        if (autofillField.isFocused) {
            focusedAutofillHints.addAll(hintsList)
        }
        autofillField.autofillHints.forEach { autofillHint ->
            autofillHintsToFieldsMap[autofillHint] = autofillHintsToFieldsMap[autofillHint] ?: ArrayList<AutofillField>()
            autofillHintsToFieldsMap[autofillHint]?.add(autofillField)
        }
    }

    fun getFieldsForHint(hint: String): MutableList<AutofillField>? {
        return autofillHintsToFieldsMap[hint]
    }
}
