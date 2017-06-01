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

import java.util.HashMap;
import java.util.List;

import static com.example.android.autofillframework.CommonUtil.TAG;

/**
 * ClientFormData is the model that holds all of the data on a client app's page, plus the dataset
 * name associated with it.
 */
public final class ClientFormData {
    private final HashMap<String, SavableAutofillData> mHintMap;
    private String mDatasetName;

    public ClientFormData() {
        this(null, new HashMap<String, SavableAutofillData>());
    }

    public ClientFormData(String datasetName, HashMap<String, SavableAutofillData> hintMap) {
        mHintMap = hintMap;
        mDatasetName = datasetName;
    }

    /**
     * Returns the name of the {@link Dataset}.
     */
    public String getDatasetName() {
        return mDatasetName;
    }

    /**
     * Sets the {@link Dataset} name.
     */
    public void setDatasetName(String datasetName) {
        mDatasetName = datasetName;
    }

    /**
     * Sets values for a list of hints.
     */
    public void setAutofillValuesForHints(@NonNull String[] autofillHints, @NonNull SavableAutofillData autofillValue) {
        for (int i = 0; i < autofillHints.length; i++) {
            mHintMap.put(autofillHints[i], autofillValue);
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
                SavableAutofillData savableAutofillData = mHintMap.get(hint);
                if (savableAutofillData == null) {
                    continue;
                }
                AutofillField autofillField = autofillFields.get(autofillFieldIndex);
                AutofillId autofillId = autofillField.getId();
                int autofillType = autofillField.getAutofillType();
                switch (autofillType) {
                    case View.AUTOFILL_TYPE_LIST:
                        int listValue = autofillField.getAutofillOptionIndex(savableAutofillData.getTextValue());
                        if (listValue != -1) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forList(listValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_DATE:
                        Long dateValue = savableAutofillData.getDateValue();
                        if (dateValue != null) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forDate(dateValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_TEXT:
                        String textValue = savableAutofillData.getTextValue();
                        if (textValue != null) {
                            datasetBuilder.setValue(autofillId, AutofillValue.forText(textValue));
                            setValueAtLeastOnce = true;
                        }
                        break;
                    case View.AUTOFILL_TYPE_TOGGLE:
                        Boolean toggleValue = savableAutofillData.getToggleValue();
                        if (toggleValue != null) {
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

    public boolean helpsWithHints(List<String> autofillHints) {
        for (int i = 0; i < autofillHints.size(); i++) {
            String autofillHint = autofillHints.get(i);
            if (mHintMap.get(autofillHint) != null && !mHintMap.get(autofillHint).isNull()) {
                return true;
            }
        }
        return false;
    }
}
