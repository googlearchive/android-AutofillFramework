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
package com.example.android.autofillframework.multidatasetservice.model;

import android.app.assist.AssistStructure;
import android.view.autofill.AutofillValue;

import com.example.android.autofillframework.multidatasetservice.AutofillHelper;
import com.google.gson.annotations.Expose;

/**
 * JSON serializable data class containing the same data as an {@link AutofillValue}.
 */
public class FilledAutofillField {
    @Expose
    private String mTextValue = null;
    @Expose
    private Long mDateValue = null;
    @Expose
    private Boolean mToggleValue = null;

    /**
     * Does not need to be serialized into persistent storage, so it's not exposed.
     */
    private String[] mAutofillHints = null;

    public FilledAutofillField(AssistStructure.ViewNode viewNode) {
        mAutofillHints = AutofillHelper.filterForSupportedHints(viewNode.getAutofillHints());
        AutofillValue autofillValue = viewNode.getAutofillValue();
        if (autofillValue != null) {
            if (autofillValue.isList()) {
                CharSequence[] autofillOptions = viewNode.getAutofillOptions();
                int index = autofillValue.getListValue();
                if (autofillOptions != null && autofillOptions.length > 0) {
                    mTextValue = autofillOptions[index].toString();
                }
            } else if (autofillValue.isDate()) {
                mDateValue = autofillValue.getDateValue();
            } else if (autofillValue.isText()) {
                // Using toString of AutofillValue.getTextValue in order to save it to
                // SharedPreferences.
                mTextValue = autofillValue.getTextValue().toString();
            }
        }
    }

    public String[] getAutofillHints() {
        return mAutofillHints;
    }

    public String getTextValue() {
        return mTextValue;
    }

    public Long getDateValue() {
        return mDateValue;
    }

    public Boolean getToggleValue() {
        return mToggleValue;
    }

    public boolean isNull() {
        return mTextValue == null && mDateValue == null && mToggleValue == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilledAutofillField that = (FilledAutofillField) o;

        if (mTextValue != null ? !mTextValue.equals(that.mTextValue) : that.mTextValue != null)
            return false;
        if (mDateValue != null ? !mDateValue.equals(that.mDateValue) : that.mDateValue != null)
            return false;
        return mToggleValue != null ? mToggleValue.equals(that.mToggleValue) : that.mToggleValue == null;
    }

    @Override
    public int hashCode() {
        int result = mTextValue != null ? mTextValue.hashCode() : 0;
        result = 31 * result + (mDateValue != null ? mDateValue.hashCode() : 0);
        result = 31 * result + (mToggleValue != null ? mToggleValue.hashCode() : 0);
        return result;
    }
}
