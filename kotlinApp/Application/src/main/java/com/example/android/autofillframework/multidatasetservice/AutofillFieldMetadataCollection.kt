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
package com.example.android.autofillframework.multidatasetservice

import android.view.autofill.AutofillId

/**
 * Data structure that stores a collection of `AutofillFieldMetadata`s. Contains all of the client's `View`
 * hierarchy autofill-relevant metadata.
 */
data class AutofillFieldMetadataCollection(val autofillIds: java.util.ArrayList<AutofillId> = java.util.ArrayList<AutofillId>(),
        val allAutofillHints: java.util.ArrayList<String> = java.util.ArrayList<String>(),
        val focusedAutofillHints: java.util.ArrayList<String> = java.util.ArrayList<String>()) {

    private val autofillHintsToFieldsMap = java.util.HashMap<String, MutableList<AutofillFieldMetadata>>()
    var saveType = 0
        private set

    fun add(autofillFieldMetadata: AutofillFieldMetadata) {
        saveType = saveType or autofillFieldMetadata.saveType
        autofillIds.add(autofillFieldMetadata.autofillId)
        val hintsList = autofillFieldMetadata.autofillHints
        allAutofillHints.addAll(hintsList)
        if (autofillFieldMetadata.isFocused) {
            focusedAutofillHints.addAll(hintsList)
        }
        autofillFieldMetadata.autofillHints.forEach { autofillHint ->
            autofillHintsToFieldsMap[autofillHint] = autofillHintsToFieldsMap[autofillHint] ?: java.util.ArrayList<AutofillFieldMetadata>()
            autofillHintsToFieldsMap[autofillHint]?.add(autofillFieldMetadata)
        }
    }

    fun getFieldsForHint(hint: String): MutableList<AutofillFieldMetadata>? {
        return autofillHintsToFieldsMap[hint]
    }
}
