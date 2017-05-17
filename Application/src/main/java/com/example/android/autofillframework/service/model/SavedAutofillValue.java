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
package com.example.android.autofillframework.service.model;

import android.app.assist.AssistStructure;
import android.util.Log;
import android.view.autofill.AutofillValue;

import org.json.JSONException;
import org.json.JSONObject;

public class SavedAutofillValue {
    private static final String TAG = "SavedAutofillValue";
    private String textValue = null;
    private Long dateValue = -1L;
    private Boolean toggleValue = false;
    private boolean hasToggleValue = false;

    public static SavedAutofillValue fromJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        try {
            SavedAutofillValue savedAutofillValue = new SavedAutofillValue();

            savedAutofillValue.textValue =
                    !jsonObject.isNull("textValue") ? jsonObject.getString("textValue") : null;
            savedAutofillValue.dateValue =
                    !jsonObject.isNull("dateValue") ? jsonObject.getLong("dateValue") : null;
            savedAutofillValue.setToggleValue
                    (!jsonObject.isNull("toggleValue") ? jsonObject.getBoolean("toggleValue") : null);
            return savedAutofillValue;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public static SavedAutofillValue fromViewNode(AssistStructure.ViewNode viewNode) {
        SavedAutofillValue savedAutofillValue = new SavedAutofillValue();
        AutofillValue autofillValue = viewNode.getAutofillValue();
        if (autofillValue != null) {
            if (autofillValue.isList()) {
                String[] autofillOptions = viewNode.getAutofillOptions();
                int index = autofillValue.getListValue();
                if (autofillOptions != null && autofillOptions.length > 0) {
                    savedAutofillValue.textValue = autofillOptions[index];
                }
            } else if (autofillValue.isDate()) {
                savedAutofillValue.dateValue = autofillValue.getDateValue();
            } else if (autofillValue.isText()) {
                // Using toString of AutofillValue.getTextValue in order to save it to
                // SharedPreferences.
                savedAutofillValue.textValue = autofillValue.getTextValue().toString();
            }
        }
        return savedAutofillValue;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("textValue", textValue != null ? textValue : JSONObject.NULL);
            jsonObject.put("dateValue", dateValue != null ? dateValue : JSONObject.NULL);
            jsonObject.put("toggleValue", toggleValue != null ? toggleValue : JSONObject.NULL);
            return jsonObject;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public String getTextValue() {
        return textValue;
    }

    public long getDateValue() {
        return dateValue;
    }


    public boolean getToggleValue() {
        return toggleValue;
    }

    public void setToggleValue(Boolean toggleValue) {
        this.toggleValue = toggleValue;
        hasToggleValue = toggleValue != null;
    }


    public boolean isNull() {
        return textValue == null && dateValue == -1L && !hasToggleValue;
    }

    public boolean hasToggleValue() {
        return hasToggleValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedAutofillValue that = (SavedAutofillValue) o;

        if (textValue != null ? !textValue.equals(that.textValue) : that.textValue != null)
            return false;
        if (dateValue != null ? !dateValue.equals(that.dateValue) : that.dateValue != null)
            return false;
        return toggleValue != null ? toggleValue.equals(that.toggleValue) : that.toggleValue == null;

    }

    @Override
    public int hashCode() {
        int result = textValue != null ? textValue.hashCode() : 0;
        result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
        result = 31 * result + (toggleValue != null ? toggleValue.hashCode() : 0);
        return result;
    }
}
