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

import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ClientFormData is the model that holds all of the data on a client app's page, plus the dataset
 * name associated with it.
 */
public final class ClientFormData {
    private static final String TAG = "ClientFormData";
    private final HashMap<String, SavedAutofillValue> hintMap;
    private String datasetName;

    public ClientFormData() {
        this(null, new HashMap<String, SavedAutofillValue>());
    }

    public ClientFormData(String datasetName, HashMap<String, SavedAutofillValue> hintMap) {
        this.hintMap = hintMap;
        this.datasetName = datasetName;
    }

    public static ClientFormData fromJson(JSONObject jsonObject) {
        HashMap<String, SavedAutofillValue> hintMap = new HashMap<>();
        try {
            String datasetName = jsonObject.has("datasetName") ?
                    jsonObject.getString("datasetName") : null;
            JSONObject valuesJson = jsonObject.getJSONObject("values");
            Iterator<String> hints = valuesJson.keys();
            while (hints.hasNext()) {
                String hint = hints.next();
                JSONObject valueAsJson = valuesJson
                        .getJSONObject(hint);
                if (valueAsJson != null) {
                    SavedAutofillValue savedAutofillValue = SavedAutofillValue.fromJson(valueAsJson);
                    hintMap.put(hint, savedAutofillValue);
                }
            }
            return new ClientFormData(datasetName, hintMap);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the name of the {@link Dataset}.
     */
    public String getDatasetName() {
        return this.datasetName;
    }

    /**
     * Sets the {@link Dataset} name.
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Sets values for a list of hints.
     */
    public void set(@NonNull String[] autofillHints, @NonNull SavedAutofillValue autofillValue) {
        if (autofillHints.length < 1) {
            return;
        }
        for (int i = 0; i < autofillHints.length; i++) {
            hintMap.put(autofillHints[i], autofillValue);
        }
    }

    /**
     * Populates a {@link Dataset.Builder} with appropriate values for each {@link AutofillId}
     * in a {@code AutofillFieldsCollection}.
     */
    public boolean applyToFields(AutofillFieldsCollection autofillFieldsCollection,
            Dataset.Builder datasetBuilder) {
        boolean setValueAtLeastOnce = false;
        List<String> allHints = autofillFieldsCollection.getAllHints();
        for (int hintIndex = 0; hintIndex < allHints.size(); hintIndex++) {
            String hint = allHints.get(hintIndex);
            List<AutofillField> autofillFields = autofillFieldsCollection.getFieldsForHint(hint);
            if (autofillFields == null) {
                continue;
            }
            for (int autofillFieldIndex = 0; autofillFieldIndex < autofillFields.size(); autofillFieldIndex++) {
                AutofillField autofillField = autofillFields.get(autofillFieldIndex);
                AutofillId autofillId = autofillField.getId();
                int autofillType = autofillField.getAutofillType();
                SavedAutofillValue savedAutofillValue = hintMap.get(hint);
                switch (autofillType) {
                    case View.AUTOFILL_TYPE_LIST:
                        int listValue = autofillField.getAutofillOptionIndex(savedAutofillValue.getTextValue());
                        if (listValue != -1) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forList(listValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_DATE:
                        long dateValue = savedAutofillValue.getDateValue();
                        if (dateValue != -1) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forDate(dateValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_TEXT:
                        String textValue = savedAutofillValue.getTextValue();
                        if (textValue != null) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forText(textValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_TOGGLE:
                        if (savedAutofillValue.hasToggleValue()) {
                            boolean toggleValue = savedAutofillValue.getToggleValue();
                            datasetBuilder.setValue(autofillId, AutofillValue.forToggle(toggleValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_NONE:
                    default:
                        Log.w(TAG, "Invalid autofill type - " + autofillType);
                        break;
                }
            }
        }
        return setValueAtLeastOnce;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("datasetName", datasetName != null ? datasetName : JSONObject.NULL);
            JSONObject jsonValues = new JSONObject();
            Set<String> hints = hintMap.keySet();
            for (String hint : hints) {
                SavedAutofillValue value = hintMap.get(hint);
                jsonValues.put(hint, value != null ? value.toJson() : JSONObject.NULL);
            }
            jsonObject.put("values", jsonValues);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return jsonObject;
    }

    public boolean helpsWithHints(List<String> autofillHints) {
        for (int i = 0; i < autofillHints.size(); i++) {
            String autofillHint = autofillHints.get(i);
            if (hintMap.get(autofillHint) != null && !hintMap.get(autofillHint).isNull()) {
                return true;
            }
        }
        return false;
    }
}
