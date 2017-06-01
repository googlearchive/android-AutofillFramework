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

import android.app.assist.AssistStructure
import android.view.autofill.AutofillValue

/**
 * JSON serializable data class containing the same data as an [AutofillValue].
 */
class SavableAutofillData(viewNode: AssistStructure.ViewNode) {
    var textValue: String? = null
    var dateValue: Long? = null
    var toggleValue: Boolean? = null

    init {
        viewNode.autofillValue?.let { autofillValue ->
            if (autofillValue.isList) {
                val index = autofillValue.listValue
                viewNode.autofillOptions?.let { autofillOptions ->
                    if (autofillOptions.size > index) {
                        textValue = autofillOptions[index]
                    }
                }
            } else if (autofillValue.isDate) {
                dateValue = autofillValue.dateValue
            } else if (autofillValue.isText) {
                // Using toString of AutofillValue.getTextValue in order to save it to
                // SharedPreferences.
                textValue = autofillValue.textValue.toString()
            } else {
            }
        }
    }

    fun isNull(): Boolean {
        return textValue == null && dateValue == null && toggleValue == null
    }
}
