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
import android.service.autofill.SaveInfo;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;

/**
 * Class that represents a field that can be autofilled. It will contain a description
 * (what type data the field holds), an AutoFillId (an ID unique to the rest of the ViewStructure),
 * and a value (what data is currently in the field).
 */
public class AutofillField {
    private int mSaveType = 0;
    private String[] mHints;
    private AutofillId mId;
    private int mAutofillType;
    private String[] mAutofillOptions;
    private boolean mFocused;

    public AutofillField(AssistStructure.ViewNode view) {
        mId = view.getAutofillId();
        setHints(view.getAutofillHints());
        mAutofillType = view.getAutofillType();
        mAutofillOptions = view.getAutofillOptions();
        mFocused = view.isFocused();
    }

    public String[] getHints() {
        return mHints;
    }

    public void setHints(String[] hints) {
        mHints = hints;
        updateSaveTypeFromHints();
    }

    public int getSaveType() {
        return mSaveType;
    }

    public AutofillId getId() {
        return mId;
    }

    public void setId(AutofillId id) {
        mId = id;
    }

    public int getAutofillType() {
        return mAutofillType;
    }

    public int getAutofillOptionIndex(String value) {
        for (int i = 0; i < mAutofillOptions.length; i++) {
            if (mAutofillOptions[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFocused() {
        return mFocused;
    }

    private void updateSaveTypeFromHints() {
        mSaveType = 0;
        if (mHints == null) {
            return;
        }
        for (String hint : mHints) {
            switch (hint) {
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH:
                case View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR:
                case View.AUTOFILL_HINT_CREDIT_CARD_NUMBER:
                case View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD;
                    break;
                case View.AUTOFILL_HINT_EMAIL_ADDRESS:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS;
                    break;
                case View.AUTOFILL_HINT_PHONE:
                case View.AUTOFILL_HINT_NAME:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_GENERIC;
                    break;
                case View.AUTOFILL_HINT_PASSWORD:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_PASSWORD;
                    mSaveType &= ~SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS;
                    mSaveType &= ~SaveInfo.SAVE_DATA_TYPE_USERNAME;
                    break;
                case View.AUTOFILL_HINT_POSTAL_ADDRESS:
                case View.AUTOFILL_HINT_POSTAL_CODE:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_ADDRESS;
                    break;
                case View.AUTOFILL_HINT_USERNAME:
                    mSaveType |= SaveInfo.SAVE_DATA_TYPE_USERNAME;
                    break;
            }
        }
    }
}
